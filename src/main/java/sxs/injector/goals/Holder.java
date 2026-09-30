package sxs.injector.goals;

import org.slf4j.Logger;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Predicate;
import java.util.HashSet;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;

public class Holder {
	private final Mob mob;
	private final Entry primaryGoals;
	private final Entry targetGoals;

	public Holder(Mob mob) {
		this.mob = mob;
		this.primaryGoals = mob.goalSelector instanceof GoalSelector goals ? new Entry(new HashSet(goals.getAvailableGoals())) : null;
		this.targetGoals = mob.targetSelector instanceof GoalSelector goals ? new Entry(new HashSet(goals.getAvailableGoals())) : null;
	}

	public <G extends Goal> int locateGoal(Class<G> targetClass) {
		return this.locateGoal(targetClass, (goal) -> true);
	}

	public <G extends Goal> int locateGoal(Class<G> targetClass, Predicate<G> filter) {
		Entry searchTarget = TargetGoal.class.isAssignableFrom(targetClass) ? this.targetGoals : this.primaryGoals;
		return searchTarget.locate(targetClass, filter);
	}

	public <T extends LivingEntity> int locateAvoidGoal(Class<T> avoidClass) {
		return this.locateGoal(AvoidEntityGoal.class, (goal) -> goal instanceof TypeAccessor accessor && avoidClass.isAssignableFrom(accessor.getType()));
	}

	public <T extends LivingEntity> int locateAttackGoal(Class<? extends NearestAttackableTargetGoal> targetGoalClass, Class<T> targetClass) {
		return this.locateGoal(targetGoalClass, (goal) -> goal instanceof TypeAccessor accessor && targetClass.isAssignableFrom(accessor.getType()));
	}

	public <G extends Goal> boolean removeGoals(Class<G> goalClass, Predicate<G> filter) {
		Entry removeTarget = TargetGoal.class.isAssignableFrom(goalClass) ? this.targetGoals : this.primaryGoals;
		return removeTarget.removeGoals(goalClass, filter);
	}

	public <T extends LivingEntity> boolean stopAvoiding(Class<T> avoidClass) {
		return this.removeGoals(AvoidEntityGoal.class, (goal) -> goal instanceof TypeAccessor accessor && avoidClass.isAssignableFrom(accessor.getType()));
	}

	public <T extends LivingEntity> boolean removeTargeting(Class<T> targetClass) {
		return this.removeGoals(NearestAttackableTargetGoal.class, (goal) -> goal instanceof TypeAccessor accessor && targetClass.isAssignableFrom(accessor.getType()));
	}

	public boolean insert(Goal goal) {
		return this.insert(goal, goal instanceof TargetGoal ? this.targetGoals.findPrioritySlot(-1) : this.primaryGoals.findPrioritySlot(-1));
	}

	public boolean insert(Goal goal, int priority) {
		return this.insert(goal, priority, Entry.ShiftMode.Overlapping);
	}

	public boolean insert(Goal goal, int priority, Entry.ShiftMode shift) {
		if (priority <= 0)
			return false;
			
		if (goal instanceof TargetGoal)
			this.targetGoals.insertGoal(goal, priority, shift);
		else
			this.primaryGoals.insertGoal(goal, priority, shift);
		return true;
	}

	public boolean replace(Goal goal, int priority, Predicate<Goal> filter) {
		if (priority <= 0)
			return false;
			
		if (goal instanceof TargetGoal)
			this.targetGoals.replaceGoals(goal, priority, filter);
		else
			this.primaryGoals.replaceGoals(goal, priority, filter);
		return true;
	}

	public boolean replaceGoals(Class<? extends Goal> targetClass, Goal goal) {
		return this.replaceGoals(targetClass, goal, (goalF) -> targetClass.isAssignableFrom(goalF.getClass()));
	}

	public boolean replaceGoals(Class<? extends Goal> targetClass, Goal goal, Predicate<Goal> filter) {
		return this.replace(goal, this.locateGoal(targetClass), (goalF) -> targetClass.isAssignableFrom(goalF.getClass()) && filter.test(goalF));
	}

	public boolean replaceAvoiding(Class<? extends LivingEntity> avoidClass, Goal goal) {
		return this.replace(goal, this.locateAvoidGoal(avoidClass), (goalF) -> goalF instanceof AvoidEntityGoal && goalF instanceof TypeAccessor accessor && avoidClass.isAssignableFrom(accessor.getType()));
	}

	public boolean replaceTargeting(Class<? extends LivingEntity> targetClass, TargetGoal goal) {
		return this.replace(goal, this.locateAttackGoal(NearestAttackableTargetGoal.class, targetClass), (goalF) -> goalF instanceof NearestAttackableTargetGoal && goalF instanceof TypeAccessor accessor && targetClass.isAssignableFrom(accessor.getType()));
	}

	public boolean insertBefore(Class<? extends Goal> targetClass, Goal goal) {
		return this.insert(goal, this.locateGoal(targetClass));
	}

	public boolean insertAt(Class<? extends Goal> targetClass, Goal goal) {
		return this.insert(goal, this.locateGoal(targetClass), Entry.ShiftMode.Ignore);
	}

	public boolean insertAfter(Class<? extends Goal> targetClass, Goal goal) {
		return this.insert(goal, this.locateGoal(targetClass) + 1);
	}

	public <G extends Goal> boolean insertBefore(Class<G> targetClass, Predicate<G> filter, Goal goal) {
		return this.insert(goal, this.locateGoal(targetClass, filter));
	}

	public <G extends Goal> boolean insertAfter(Class<G> targetClass, Predicate<G> filter, Goal goal) {
		return this.insert(goal, this.locateGoal(targetClass, filter) + 1);
	}

	public <G extends Goal> boolean insertWith(Class<G> targetClass, Predicate<G> filter, Goal goal) {
		return this.insert(goal, this.locateGoal(targetClass, filter), Entry.ShiftMode.Ignore);
	}

	public boolean insertBeforeAvoiding(Goal goal, Class<? extends LivingEntity> avoidClass) {
		return this.insert(goal, this.locateAvoidGoal(avoidClass));
	}

	public boolean insertAtAvoiding(Goal goal, Class<? extends LivingEntity> avoidClass) {
		return this.insert(goal, this.locateAvoidGoal(avoidClass), Entry.ShiftMode.Ignore);
	}

	public boolean insertAfterAvoiding(Goal goal, Class<? extends LivingEntity> avoidClass) {
		return this.insert(goal, this.locateAvoidGoal(avoidClass) + 1);
	}

	public boolean insertBeforeTargeting(TargetGoal goal, Class<? extends NearestAttackableTargetGoal> goalClass, Class<? extends LivingEntity> targetClass) {
		return this.insert(goal, this.locateAttackGoal(goalClass, targetClass));
	}

	public boolean insertAtTargeting(TargetGoal goal, Class<? extends NearestAttackableTargetGoal> goalClass, Class<? extends LivingEntity> targetClass) {
		return this.insert(goal, this.locateAttackGoal(goalClass, targetClass), Entry.ShiftMode.Ignore);
	}

	public boolean insertAfterTargeting(TargetGoal goal, Class<? extends NearestAttackableTargetGoal> goalClass, Class<? extends LivingEntity> targetClass) {
		return this.insert(goal, this.locateAttackGoal(goalClass, targetClass) + 1);
	}

	public void submit(Logger logger) {
		logger.info("Injected goals for mob '{}' new goals are as listed;", this.mob.getDisplayName().getString());
		if (this.mob.goalSelector instanceof GoalSelector selector) {
			this.primaryGoals.override(selector);
			for (WrappedGoal goal : this.primaryGoals.get()) {
				logger.debug("Goal: {} | {}", goal.getPriority(), getDisplay(goal.getGoal()));
			}
		}

		if (this.mob.targetSelector instanceof GoalSelector selector) {
			this.targetGoals.override(selector);
			for (WrappedGoal goal : this.targetGoals.get()) {
				logger.debug("Target Goal: {} | {}", goal.getPriority(), getDisplay(goal.getGoal()));
			}
		}

		logger.info("Submitted Goals for mob '{}'", this.mob.getDisplayName().getString());
	}

	private static String getDisplay(Goal goal) {
		String name = goal.getClass().getSimpleName();
		if(goal instanceof GoalFormatting formatting)
			return name + formatting.getDisplay();
		return name;
	}
}