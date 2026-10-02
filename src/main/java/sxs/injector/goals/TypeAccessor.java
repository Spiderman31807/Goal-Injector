package sxs.injector.goals;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.function.Predicate;

public interface TypeAccessor<T> {
	abstract Class<T> getType();
}