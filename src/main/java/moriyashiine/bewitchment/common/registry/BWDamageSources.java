/*
 * All Rights Reserved (c) 2022 MoriyaShiine
 */

package moriyashiine.bewitchment.common.registry;

import moriyashiine.bewitchment.api.BewitchmentAPI;
import moriyashiine.bewitchment.api.component.BloodComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class BWDamageSources {
	public static final DamageSource MAGIC_COPY = new ArmorPiercingDamageSource("magic");
	public static final DamageSource WEDNESDAY = new UnblockableDamageSource("wednesday");
	public static final DamageSource DEATH = new UnblockableDamageSource("death");
	public static final DamageSource VAMPIRE = new EmptyDamageSource("vampire");
	public static final DamageSource SUN = new SunDamageSource("sun");

	public static float handleDamage(LivingEntity entity, DamageSource source, float amount) {
		if (BewitchmentAPI.isWeakToSilver(entity) && BewitchmentAPI.isSourceFromSilver(source)) {
			amount += 4;
		} else if (BewitchmentAPI.isWerewolf(entity, false) && !isEffective(source, false)) {
			amount *= 2 / 3f;
		} else if (BewitchmentAPI.isVampire(entity, true)) {
			amount = handleVampireDamage(entity, source, amount);
		}
		return amount;
	}

	public static boolean isEffective(DamageSource source, boolean vampire) {
		if (source.isOutOfWorld() || (vampire && (source == MAGIC_COPY || source == SUN))) {
			return true;
		}
		Entity attacker = source.getSource();
		if (attacker != null) {
			if (attacker.getType().isIn(BWTags.TAGLOCK_BLACKLIST) || BewitchmentAPI.isVampire(attacker, true) || BewitchmentAPI.isWerewolf(attacker, true)) {
				return true;
			} else if (attacker instanceof LivingEntity && EnchantmentHelper.getEquipmentLevel(Enchantments.SMITE, (LivingEntity) attacker) > 0) {
				return true;
			}
		}
		return BewitchmentAPI.isSourceFromSilver(source);
	}

	private static float handleVampireDamage(LivingEntity entity, DamageSource source, float amount) {
		if (!isEffective(source, true)) {
			if (entity.getHealth() - amount < 1) {
				BloodComponent bloodComponent = BWComponents.BLOOD_COMPONENT.get(entity);
				while (entity.getHealth() - amount <= 0 && bloodComponent.getBlood() > 0) {
					amount--;
					bloodComponent.drainBlood(1, false);
				}
			}
		}
		return amount;
	}

	private static class EmptyDamageSource extends DamageSource {
		public EmptyDamageSource(String name) {
			super(name);
		}
	}

	private static class UnblockableDamageSource extends DamageSource {
		protected UnblockableDamageSource(String name) {
			super(name);
			setBypassesArmor();
			setUnblockable();
			setOutOfWorld();
		}
	}

	private static class ArmorPiercingDamageSource extends DamageSource {
		protected ArmorPiercingDamageSource(String name) {
			super(name);
			setBypassesArmor();
			setUnblockable();
		}
	}

	public static class MagicMob extends EntityDamageSource {
		public MagicMob(@Nullable Entity source) {
			super("mob", source);
			setUsesMagic();
			setBypassesArmor();
		}
	}

	public static class MagicPlayer extends EntityDamageSource {
		public MagicPlayer(@Nullable Entity source) {
			super("player", source);
			setUsesMagic();
			setBypassesArmor();
		}
	}

	public static class SunDamageSource extends ArmorPiercingDamageSource {
		public SunDamageSource(String name) {
			super(name);
		}

		@Override
		public Text getDeathMessage(LivingEntity entity) {
			return DamageSource.ON_FIRE.getDeathMessage(entity);
		}
	}
}
