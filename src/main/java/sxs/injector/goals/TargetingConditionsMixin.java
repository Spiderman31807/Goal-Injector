package sxs.injector.goals.mixin;

import sxs.injector.goals.TargetingSelectorAccessor;

import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.entity.ai.targeting.TargetingConditions;

@Mixin(TargetingConditions.class)
public class TargetingConditionsMixin implements TargetingSelectorAccessor {
	@Shadow
	private TargetingConditions.Selector selector;

	@Override
	public TargetingConditions.Selector getSelector() {
		return this.selector;
	}
}