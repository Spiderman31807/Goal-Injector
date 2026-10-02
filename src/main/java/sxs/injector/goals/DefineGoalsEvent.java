package sxs.injector.goals;

import net.neoforged.bus.api.Event;

import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.Mob;

import java.util.function.Predicate;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;

public class DefineGoalsEvent extends Event {
	public static final Logger logger = LoggerFactory.getLogger(DefineGoalsEvent.class); 
	private final GoalBuilder goalBuilder;
	private final GoalBuilder targetBuilder;
	private final String holder;
	private final Mob mob;

	public DefineGoalsEvent(Mob mob) {
		this.mob = mob;
		this.holder = mob.getClass().getSimpleName() + "::" + (mob.hasCustomName() ? mob.getCustomName().getString() : mob.getId());
		this.goalBuilder = GoalBuilder.create(mob.goalSelector);
		this.targetBuilder = GoalBuilder.create(mob.targetSelector);
	}

	public Mob getMob() {
		return this.mob;
	}

	public void build() {
		//logger.debug("[{}] building goals..", this.holder);
		if (this.goalBuilder != null) {
			//this.goalBuilder.print(this.holder);
			this.goalBuilder.apply(this.mob.goalSelector);
		}
		
		//logger.debug("[{}] building targeting..", this.holder);
		if (this.targetBuilder != null) {
			//this.targetBuilder.print(this.holder);
			this.targetBuilder.apply(this.mob.targetSelector);
		}
	}

	private GoalBuilder getBuilder(Goal goal) {
		return this.targetBuilder != null && goal instanceof TargetGoal ? this.targetBuilder : this.goalBuilder;
	}

	private boolean orBuilder(Function<GoalBuilder, Boolean> func) {
		boolean target = this.targetBuilder == null ? false : func.apply(this.targetBuilder);
		boolean goals = this.goalBuilder == null ? false : func.apply(this.goalBuilder);
		return target || goals;
	}

	public boolean removeGoals(Predicate<Goal> filter) {
		return this.orBuilder((builder) -> builder.remove(filter) != 0);
	}

	public boolean removeGoals(int priority, Predicate<Goal> filter) {
		return this.orBuilder((builder) -> builder.remove(priority, filter));
	}

	public boolean replaceGoal(Goal goal, Predicate<Goal> filter) {
		GoalBuilder builder = this.getBuilder(goal);
		if (builder == null) return false;
		GoalBuilder.Info info = builder.find(filter);
		if (info == null) return false;
		logger.debug("[{}] replacing {} with {}", this.holder, GoalFormatting.format(info.goal()), GoalFormatting.format(goal));
		return builder.replace(goal, info);
	}

	public  Optional<Goal> copyGoal(Function<Goal, Goal> func, Predicate<Goal> filter) {
		if (this.goalBuilder == null) return Optional.empty();
		GoalBuilder.Info info = this.goalBuilder.find(filter);
		if (info == null) return Optional.empty();
		return Optional.ofNullable(func.apply(info.goal()));
	}

	public Optional<TargetGoal> copyTargeting(Function<Goal, TargetGoal> func, Predicate<Goal> filter) {
		if (this.targetBuilder == null) return Optional.empty();
		GoalBuilder.Info info = this.targetBuilder.find(filter);
		if (info == null) return Optional.empty();
		return Optional.ofNullable(func.apply(info.goal()));
	}

	public boolean duplicateGoal(Function<Goal, Goal> func, Predicate<Goal> filter) {
		if (this.goalBuilder == null) return false;
		GoalBuilder.Info info = this.goalBuilder.find(filter);
		if (info == null) return false;
		Goal goal = func.apply(info.goal());
		logger.debug("[{}] duplicated {} for {}", this.holder, GoalFormatting.format(info.goal()), GoalFormatting.format(goal));
		return this.goalBuilder.add(info.priority(), goal);
	}

	public boolean duplicateTargeting(Function<Goal, TargetGoal> func, Predicate<Goal> filter) {
		if (this.targetBuilder == null) return false;
		GoalBuilder.Info info = this.targetBuilder.find(filter);
		if (info == null) return false;
		TargetGoal goal = func.apply(info.goal());
		logger.debug("[{}] duplicated {} for {}", this.holder, GoalFormatting.format(info.goal()), GoalFormatting.format(goal));
		return this.targetBuilder.add(info.priority(), goal);
	}

	public boolean transformGoal(Function<Goal, Goal> func, Predicate<Goal> filter) {
		if (this.goalBuilder == null) return false;
		GoalBuilder.Info info = this.goalBuilder.find(filter);
		if (info == null) return false;
		Goal goal = func.apply(info.goal());
		logger.debug("[{}] transformed {} to {}", this.holder, GoalFormatting.format(info.goal()), GoalFormatting.format(goal));
		return this.goalBuilder.replace(goal, info);
	}

	public boolean transformTargeting(Function<Goal, TargetGoal> func, Predicate<Goal> filter) {
		if (this.targetBuilder == null) return false;
		GoalBuilder.Info info = this.targetBuilder.find(filter);
		if (info == null) return false;
		TargetGoal goal = func.apply(info.goal());
		logger.debug("[{}] transformed {} to {}", this.holder, GoalFormatting.format(info.goal()), GoalFormatting.format(goal));
		return this.targetBuilder.replace(goal, info);
	}

	public boolean addGoal(int priority, Goal goal) {
		GoalBuilder builder = this.getBuilder(goal);
		if (builder == null) return false;
		logger.debug("[{}] adding {} at {}", this.holder, GoalFormatting.format(goal), priority);
		return builder.add(priority, goal);
	}

	public boolean addGoal(Goal goal, Predicate<Goal> filter) {
		GoalBuilder builder = this.getBuilder(goal);
		if (builder == null) return false;
		GoalBuilder.Info info = builder.find(filter);
		if (info == null) return false;
		logger.debug("[{}] adding {} at {}", this.holder, GoalFormatting.format(goal), info.priority());
		return builder.add(info.priority(), goal);
	}

	public boolean insertGoal(int priority, Goal goal) {
		GoalBuilder builder = this.getBuilder(goal);
		if (builder == null) return false;
		builder.lowerPriority(priority, false, true);
		logger.debug("[{}] inserting {} at {}", this.holder, GoalFormatting.format(goal), priority);
		return builder.add(priority, goal);
	}

	public boolean insertBeforeGoal(Goal goal, Predicate<Goal> filter) {
		GoalBuilder builder = this.getBuilder(goal);
		if (builder == null) return false;
		GoalBuilder.Info info = builder.find(filter);
		if (info == null) return false;
		logger.debug("[{}] inserting {} before {}", this.holder, GoalFormatting.format(goal), GoalFormatting.format(info.goal()));
		builder.lowerPriority(info.priority(), false, true);
		return builder.add(info.priority(), goal);
	}

	public boolean insertAfterGoal(Goal goal, Predicate<Goal> filter) {
		GoalBuilder builder = this.getBuilder(goal);
		if (builder == null) return false;
		GoalBuilder.Info info = builder.find(filter);
		if (info == null) return false;
		logger.debug("[{}] inserting {} after {}", this.holder, GoalFormatting.format(goal), GoalFormatting.format(info.goal()));
		builder.lowerPriority(info.priority() + 1, false, true);
		return builder.add(info.priority() + 1, goal);
	}

}