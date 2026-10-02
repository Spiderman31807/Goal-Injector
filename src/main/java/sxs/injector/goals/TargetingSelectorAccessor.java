package sxs.injector.goals;

import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public interface TargetingSelectorAccessor {
	abstract TargetingConditions.Selector getSelector();
}