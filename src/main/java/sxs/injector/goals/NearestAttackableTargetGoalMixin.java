package sxs.injector.goals.mixin;

import sxs.injector.goals.TypeAccessor;
import sxs.injector.goals.TargetingSelectorAccessor;
import sxs.injector.goals.GoalFormatting;
import sxs.injector.goals.GoalArguments;

import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;

import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.LivingEntity;

@Mixin(NearestAttackableTargetGoal.class)
public abstract class NearestAttackableTargetGoalMixin<T extends LivingEntity> implements GoalArguments, GoalFormatting, TypeAccessor<T> {
	@Shadow
	@Final
	protected Class<T> targetType;
	@Shadow
	@Final
	protected int randomInterval;
	@Shadow
	protected TargetingConditions targetConditions;

	@Override
	public Class<T> getType() {
		return this.targetType;
	}

	@Override
	public int getIntArgument(String name) {
		if (name == "randomInterval")
			return this.randomInterval;
		return 0;
	}

	@Override
	public <T extends Object> T getArgument(String name) {
		if (name == "targetType")
			return (T) this.targetType;
		if (name == "targetConditions")
			return (T) this.targetConditions;
		if (name == "targetConditions.selector" && this.targetConditions instanceof TargetingSelectorAccessor accessor)
			return (T) accessor.getSelector();
		return null;
	}
}