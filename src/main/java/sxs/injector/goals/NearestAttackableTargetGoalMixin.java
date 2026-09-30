package sxs.injector.goals.mixin;

import sxs.injector.goals.TypeAccessor;
import sxs.injector.goals.GoalFormatting;

import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;

import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.LivingEntity;

@Mixin(NearestAttackableTargetGoal.class)
public abstract class NearestAttackableTargetGoalMixin<T extends LivingEntity> implements GoalFormatting, TypeAccessor<T> {
	@Shadow
	@Final
	protected Class<T> targetType;

	@Override
	public Class<T> getType() {
		return this.targetType;
	}

	@Override
	public String getDisplay() {
		return "[%s]".formatted(this.targetType.getSimpleName());
	}
}