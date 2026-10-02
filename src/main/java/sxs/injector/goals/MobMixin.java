package sxs.injector.goals.mixin;

import sxs.injector.goals.DefineGoalsEvent;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Mixin;

import net.neoforged.neoforge.common.NeoForge;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntityType;

@Mixin(Mob.class)
public class MobMixin {
	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;registerGoals()V", shift = At.Shift.AFTER))
	private void init(EntityType<? extends Mob> type, Level level, CallbackInfo ci) {
		DefineGoalsEvent event = new DefineGoalsEvent((Mob) (Object) this);
		NeoForge.EVENT_BUS.post(event);
		event.build();
	}
}