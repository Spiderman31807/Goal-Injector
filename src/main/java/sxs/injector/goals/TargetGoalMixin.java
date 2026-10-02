package sxs.injector.goals.mixin;

import sxs.injector.goals.GoalArguments;

import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;

import net.minecraft.world.entity.ai.goal.target.TargetGoal;

@Mixin(TargetGoal.class)
public abstract class TargetGoalMixin implements GoalArguments {
	@Shadow
	@Final
	protected boolean mustSee;
	@Shadow
	@Final
	private boolean mustReach;

	@Override
	public boolean getBoolArgument(String name) {
		if (name == "mustSee")
			return this.mustSee;
		if (name == "mustReach")
			return this.mustReach;
		return false;
	}
}