package sxs.injector.goals.mixin;

import sxs.injector.goals.TypeAccessor;
import sxs.injector.goals.GoalFormatting;
import sxs.injector.goals.GoalArguments;

import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;

import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Predicate;

@Mixin(AvoidEntityGoal.class)
public abstract class AvoidEntityGoalMixin<T extends LivingEntity> implements GoalArguments, GoalFormatting, TypeAccessor<T> {
	@Shadow
	@Final
	private double walkSpeedModifier;
	@Shadow
	@Final
	private double sprintSpeedModifier;
	@Shadow
	@Final
	protected float maxDist;
	@Shadow
	@Final
	protected Class<T> avoidClass;
	@Shadow
	@Final
	protected Predicate<LivingEntity> avoidPredicate;
	@Shadow
	@Final
	protected Predicate<LivingEntity> predicateOnAvoidEntity;

	@Override
	public Class<T> getType() {
		return this.avoidClass;
	}

	@Override
	public float getFloatArgument(String name) {
		if (name == "maxDist")
			return this.maxDist;
		return 0;
	}

	@Override
	public double getDoubleArgument(String name) {
		if (name == "walkSpeedModifier")
			return this.walkSpeedModifier;
		if (name == "sprintSpeedModifier")
			return this.sprintSpeedModifier;
		return 0;
	}

	@Override
	public <T extends Object> T getArgument(String name) {
		if (name == "avoidClass")
			return (T) this.avoidClass;
		if (name == "avoidPredicate")
			return (T) this.avoidPredicate;
		if (name == "predicateOnAvoidEntity")
			return (T) predicateOnAvoidEntity;
		return null;
	}
}