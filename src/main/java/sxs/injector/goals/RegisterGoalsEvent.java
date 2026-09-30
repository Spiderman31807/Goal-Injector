package sxs.injector.goals;

import org.slf4j.Logger;

import net.neoforged.bus.api.Event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySelector;

import java.util.function.Predicate;

import com.mojang.logging.LogUtils;
import java.util.function.Consumer;

public class RegisterGoalsEvent extends Event {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final Holder holder;
	private final Mob mob;
	private boolean submitted;
	private boolean changed;

	public RegisterGoalsEvent(Mob mob) {
		this.holder = new Holder(mob);
		this.mob = mob;
	}

	public EntityType getType() {
		return this.mob.getType();
	}

	public Mob getMob() {
		return this.mob;
	}

	public boolean ifPathfinding(Consumer<PathfinderMob> function) {
		if(this.mob instanceof PathfinderMob pathingMob) {
			function.accept(pathingMob);
			return true;
		}

		return false;
	}

	public boolean isAvoiding(Class<? extends LivingEntity> avoidClass) {
		return this.holder.locateAvoidGoal(avoidClass) != -1;
	}

	public boolean doesTarget(Class<? extends LivingEntity> targetClass) {
		return this.holder.locateAttackGoal(NearestAttackableTargetGoal.class, targetClass) != -1;
	}

	public <G extends Goal> boolean hasGoal(Class<G> goalClass, Predicate<G> filter) {
		return this.holder.locateGoal(goalClass, filter) != -1;
	}

	public boolean hasGoal(Class<? extends Goal> goalClass) {
		return this.hasGoal(goalClass, (goal) -> true);
	}

	public void submit() {
		if (this.submitted)
			new Exception("Goals Event can not be registered twice!");
		this.submitted = true;
		if (this.changed)
			this.holder.submit(LOGGER);
	}

	public boolean removeGoals(Class<? extends Goal> goalClass) {
		return this.holder.removeGoals(goalClass, (goal) -> true);
	}

	public <G extends Goal> boolean removeGoals(Class<G> goalClass, Predicate<G> filter) {
		return this.holder.removeGoals(goalClass, filter);
	}

	public boolean removeTargetingGoals(Class<? extends LivingEntity> targetClass) {
		return this.holder.removeTargeting(targetClass);
	}

	public boolean removeAvoidingGoals(Class<? extends LivingEntity> avoidClass) {
		return this.holder.stopAvoiding(avoidClass);
	}

	public boolean replaceGoals(Class<? extends Goal> targetClass, Goal goal) {
		return this.holder.replaceGoals(targetClass, goal);
	}

	public boolean replaceGoals(Class<? extends Goal> targetClass, Predicate<Goal> filter, Goal goal) {
		return this.holder.replaceGoals(targetClass, goal, filter);
	}

	public boolean replaceTargetingGoals(Class<? extends LivingEntity> targetClass, TargetGoal goal) {
		return this.holder.replaceTargeting(targetClass, goal);
	}

	public boolean replaceAvoidingGoals(Class<? extends LivingEntity> avoidClass, Goal goal) {
		return this.holder.replaceAvoiding(avoidClass, goal);
	}

	public boolean registerAvoidingGoal(Class<? extends LivingEntity> avoidClass, float maxDist, double walkSpeedModifier, double sprintSpeedModifier, Predicate<? super LivingEntity> predicateOnAvoidEntity) {
		if (this.mob instanceof PathfinderMob pathfindingMob) {
			AvoidEntityGoal goal = new AvoidEntityGoal(pathfindingMob, avoidClass, maxDist, walkSpeedModifier, sprintSpeedModifier, predicateOnAvoidEntity);
			if (this.registerAtAvoiding(goal, LivingEntity.class))
				return true;
			return this.registerAfter(goal, FloatGoal.class);
		}
		return false;
	}

	public boolean registerTargetingGoal(Class<? extends LivingEntity> targetClass, int randomInterval, boolean mustSee, boolean mustReach, TargetingConditions.Selector selector) {
		NearestAttackableTargetGoal goal = new NearestAttackableTargetGoal(this.mob, targetClass, randomInterval, mustSee, mustReach, selector);
		if (targetClass == Player.class) {
			if (this.registerBeforeTargeting(goal, LivingEntity.class))
				return true;
		} else {
			if (this.registerAfterTargeting(goal, Player.class))
				return true;
		}
		
		if (this.registerAfter(goal, HurtByTargetGoal.class))
			return true;
		return this.registerGoal(goal);
	}

	public boolean registerGoal(Goal goal, int priority) {
		if (!this.holder.insert(goal, priority))
			return false;
		this.changed = true;
		return true;
	}

	public boolean registerGoal(Goal goal) {
		if (!this.holder.insert(goal))
			return false;
		this.changed = true;
		return true;
	}

	// Register Helpers
	public boolean registerAfter(Goal goal, Class<? extends Goal> targetClass) {
		return this.registerAfter(goal, targetClass, (goalF) -> true);
	}

	public boolean registerAt(Goal goal, Class<? extends Goal> targetClass) {
		return this.registerAt(goal, targetClass, (goalF) -> true);
	}

	public boolean registerBefore(Goal goal, Class<? extends Goal> targetClass) {
		return this.registerBefore(goal, targetClass, (goalF) -> true);
	}

	public <G extends Goal> boolean registerAfter(Goal goal, Class<G> targetClass, Predicate<G> filter) {
		if (!this.holder.insertAfter(targetClass, filter, goal))
			return false;
		this.changed = true;
		return true;
	}

	public <G extends Goal> boolean registerAt(Goal goal, Class<G> targetClass, Predicate<G> filter) {
		if (!this.holder.insertWith(targetClass, filter, goal))
			return false;
		this.changed = true;
		return true;
	}

	public <G extends Goal> boolean registerBefore(Goal goal, Class<G> targetClass, Predicate<G> filter) {
		if (!this.holder.insertBefore(targetClass, filter, goal))
			return false;
		this.changed = true;
		return true;
	}

	// Avoiding Goal Helpers
	public boolean registerAfterAvoiding(Goal goal, Class<? extends LivingEntity> avoidClass) {
		if (!this.holder.insertAfterAvoiding(goal, avoidClass))
			return false;
		this.changed = true;
		return true;
	}

	public boolean registerAtAvoiding(Goal goal, Class<? extends LivingEntity> avoidClass) {
		if (!this.holder.insertAtAvoiding(goal, avoidClass))
			return false;
		this.changed = true;
		return true;
	}

	public boolean registerBeforeAvoiding(Goal goal, Class<? extends LivingEntity> avoidClass) {
		if (!this.holder.insertBeforeAvoiding(goal, avoidClass))
			return false;
		this.changed = true;
		return true;
	}

	public boolean registerAvoidingGoal(Class<? extends LivingEntity> avoidClass, double walkSpeedModifier) {
		return this.registerAvoidingGoal(avoidClass, 10, walkSpeedModifier, walkSpeedModifier, EntitySelector.NO_CREATIVE_OR_SPECTATOR);
	}

	public boolean registerAvoidingGoal(Class<? extends LivingEntity> avoidClass, double walkSpeedModifier, Predicate<? super LivingEntity> predicateOnAvoidEntity) {
		return this.registerAvoidingGoal(avoidClass, 10, walkSpeedModifier, walkSpeedModifier, predicateOnAvoidEntity);
	}

	public boolean registerAvoidingGoal(Class<? extends LivingEntity> avoidClass, double walkSpeedModifier, double sprintSpeedModifier, Predicate<? super LivingEntity> predicateOnAvoidEntity) {
		return this.registerAvoidingGoal(avoidClass, 10, walkSpeedModifier, sprintSpeedModifier, predicateOnAvoidEntity);
	}

	public boolean registerAvoidingGoal(Class<? extends LivingEntity> avoidClass, float maxDist, double walkSpeedModifier) {
		return this.registerAvoidingGoal(avoidClass, maxDist, walkSpeedModifier, walkSpeedModifier, EntitySelector.NO_CREATIVE_OR_SPECTATOR);
	}

	public boolean registerAvoidingGoal(Class<? extends LivingEntity> avoidClass, float maxDist, double walkSpeedModifier, Predicate<? super LivingEntity> predicateOnAvoidEntity) {
		return this.registerAvoidingGoal(avoidClass, maxDist, walkSpeedModifier, walkSpeedModifier, predicateOnAvoidEntity);
	}

	public boolean registerAvoidingGoal(Class<? extends LivingEntity> avoidClass, float maxDist, double walkSpeedModifier, double sprintSpeedModifier) {
		return this.registerAvoidingGoal(avoidClass, maxDist, walkSpeedModifier, sprintSpeedModifier, EntitySelector.NO_CREATIVE_OR_SPECTATOR);
	}

	public boolean replaceAvoidingType(Class<? extends LivingEntity> oldClass, Class<? extends LivingEntity> newClass, float maxDist, double walkSpeedModifier, double sprintSpeedModifier, Predicate<? super LivingEntity> predicateOnAvoidEntity) {
		if(this.mob instanceof PathfinderMob pathfindingMob) {
			AvoidEntityGoal goal = new AvoidEntityGoal(pathfindingMob, newClass, maxDist, walkSpeedModifier, sprintSpeedModifier, predicateOnAvoidEntity);
			return this.replaceAvoidingGoals(oldClass, goal);
		}

		return false;
	}

	public boolean replaceAvoidingType(Class<? extends LivingEntity> oldClass, Class<? extends LivingEntity> newClass, double walkSpeedModifier) {
		return this.replaceAvoidingType(oldClass, newClass, 10, walkSpeedModifier, walkSpeedModifier, EntitySelector.NO_CREATIVE_OR_SPECTATOR);
	}

	public boolean replaceAvoidingType(Class<? extends LivingEntity> oldClass, Class<? extends LivingEntity> newClass, double walkSpeedModifier, Predicate<? super LivingEntity> predicateOnAvoidEntity) {
		return this.replaceAvoidingType(oldClass, newClass, 10, walkSpeedModifier, walkSpeedModifier, predicateOnAvoidEntity);
	}

	public boolean replaceAvoidingType(Class<? extends LivingEntity> oldClass, Class<? extends LivingEntity> newClass, double walkSpeedModifier, double sprintSpeedModifier, Predicate<? super LivingEntity> predicateOnAvoidEntity) {
		return this.replaceAvoidingType(oldClass, newClass, 10, walkSpeedModifier, sprintSpeedModifier, predicateOnAvoidEntity);
	}

	public boolean replaceAvoidingType(Class<? extends LivingEntity> oldClass, Class<? extends LivingEntity> newClass, float maxDist, double walkSpeedModifier) {
		return this.replaceAvoidingType(oldClass, newClass, maxDist, walkSpeedModifier, walkSpeedModifier, EntitySelector.NO_CREATIVE_OR_SPECTATOR);
	}

	public boolean replaceAvoidingType(Class<? extends LivingEntity> oldClass, Class<? extends LivingEntity> newClass, float maxDist, double walkSpeedModifier, Predicate<? super LivingEntity> predicateOnAvoidEntity) {
		return this.replaceAvoidingType(oldClass, newClass, maxDist, walkSpeedModifier, walkSpeedModifier, predicateOnAvoidEntity);
	}

	public boolean replaceAvoidingType(Class<? extends LivingEntity> oldClass, Class<? extends LivingEntity> newClass, float maxDist, double walkSpeedModifier, double sprintSpeedModifier) {
		return this.replaceAvoidingType(oldClass, newClass, maxDist, walkSpeedModifier, sprintSpeedModifier, EntitySelector.NO_CREATIVE_OR_SPECTATOR);
	}

	// Targeting Goal Helpers
	public boolean registerAfterTargeting(TargetGoal goal, Class<? extends NearestAttackableTargetGoal> goalClass, Class<? extends LivingEntity> targetClass) {
		if (!this.holder.insertAfterTargeting(goal, goalClass, targetClass))
			return false;
		this.changed = true;
		return true;
	}

	public boolean registerAtTargeting(TargetGoal goal, Class<? extends NearestAttackableTargetGoal> goalClass, Class<? extends LivingEntity> targetClass) {
		if (!this.holder.insertAtTargeting(goal, goalClass, targetClass))
			return false;
		this.changed = true;
		return true;
	}

	public boolean registerBeforeTargeting(TargetGoal goal, Class<? extends NearestAttackableTargetGoal> goalClass, Class<? extends LivingEntity> targetClass) {
		if (!this.holder.insertBeforeTargeting(goal, goalClass, targetClass))
			return false;
		this.changed = true;
		return true;
	}

	public boolean registerAfterTargeting(TargetGoal goal, Class<? extends LivingEntity> targetClass) {
		return this.registerAfterTargeting(goal, NearestAttackableTargetGoal.class, targetClass);
	}

	public boolean registerAtTargeting(TargetGoal goal, Class<? extends LivingEntity> targetClass) {
		return this.registerAtTargeting(goal, NearestAttackableTargetGoal.class, targetClass);
	}

	public boolean registerBeforeTargeting(TargetGoal goal, Class<? extends LivingEntity> targetClass) {
		return this.registerBeforeTargeting(goal, NearestAttackableTargetGoal.class, targetClass);
	}

	public boolean registerTargetingGoal(Class<? extends LivingEntity> targetClass) {
		return this.registerTargetingGoal(targetClass, 10, true, false, null);
	}

	public boolean registerTargetingGoal(Class<? extends LivingEntity> targetClass, boolean mustSee) {
		return this.registerTargetingGoal(targetClass, 10, mustSee, false, null);
	}

	public boolean registerTargetingGoal(Class<? extends LivingEntity> targetClass, boolean mustSee, boolean mustReach) {
		return this.registerTargetingGoal(targetClass, 10, mustSee, mustReach, null);
	}

	public boolean registerTargetingGoal(Class<? extends LivingEntity> targetClass, boolean mustSee, TargetingConditions.Selector selector) {
		return this.registerTargetingGoal(targetClass, 10, mustSee, false, selector);
	}

	public boolean registerTargetingGoal(Class<? extends LivingEntity> targetClass, boolean mustSee, boolean mustReach, TargetingConditions.Selector selector) {
		return this.registerTargetingGoal(targetClass, 10, mustSee, mustReach, selector);
	}

	public boolean registerTargetingGoal(Class<? extends LivingEntity> targetClass, int randomInterval, boolean mustSee, boolean mustReach) {
		return this.registerTargetingGoal(targetClass, randomInterval, mustSee, mustReach, null);
	}

	public boolean replaceTargetingType(Class<? extends LivingEntity> oldClass, Class<? extends LivingEntity> newClass, int randomInterval, boolean mustSee, boolean mustReach, TargetingConditions.Selector selector) {
		NearestAttackableTargetGoal goal = new NearestAttackableTargetGoal(this.mob, newClass, randomInterval, mustSee, mustReach, selector);
		return this.replaceTargetingGoals(oldClass, goal);
	}

	public boolean replaceTargetingType(Class<? extends LivingEntity> oldClass, Class<? extends LivingEntity> newClass) {
		return this.replaceTargetingType(oldClass, newClass, 10, true, false, null);
	}

	public boolean replaceTargetingType(Class<? extends LivingEntity> oldClass, Class<? extends LivingEntity> newClass, boolean mustSee) {
		return this.replaceTargetingType(oldClass, newClass, 10, mustSee, false, null);
	}

	public boolean replaceTargetingType(Class<? extends LivingEntity> oldClass, Class<? extends LivingEntity> newClass, boolean mustSee, boolean mustReach) {
		return this.replaceTargetingType(oldClass, newClass, 10, mustSee, mustReach, null);
	}

	public boolean replaceTargetingType(Class<? extends LivingEntity> oldClass, Class<? extends LivingEntity> newClass, boolean mustSee, TargetingConditions.Selector selector) {
		return this.replaceTargetingType(oldClass, newClass, 10, mustSee, false, selector);
	}

	public boolean replaceTargetingType(Class<? extends LivingEntity> oldClass, Class<? extends LivingEntity> newClass, boolean mustSee, boolean mustReach, TargetingConditions.Selector selector) {
		return this.replaceTargetingType(oldClass, newClass, 10, mustSee, mustReach, selector);
	}

	public boolean replaceTargetingType(Class<? extends LivingEntity> oldClass, Class<? extends LivingEntity> newClass, int randomInterval, boolean mustSee, boolean mustReach) {
		return this.replaceTargetingType(oldClass, newClass, randomInterval, mustSee, mustReach, null);
	}
}