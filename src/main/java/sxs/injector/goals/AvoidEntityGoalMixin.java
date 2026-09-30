package sxs.injector.goals.mixin;

import sxs.injector.goals.TypeAccessor;
import sxs.injector.goals.GoalFormatting;

import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;

import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.LivingEntity;

@Mixin(AvoidEntityGoal.class)
public abstract class AvoidEntityGoalMixin<T extends LivingEntity> implements GoalFormatting, TypeAccessor<T> {
	@Shadow
	@Final
	protected Class<T> avoidClass;

	@Override
	public Class<T> getType() {
		return this.avoidClass;
	}

	@Override
	public String getDisplay() {
		return "[%s]".formatted(this.avoidClass.getSimpleName());
	}
}