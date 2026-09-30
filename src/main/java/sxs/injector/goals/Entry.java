package sxs.injector.goals;

import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.function.Predicate;
import java.util.HashSet;
import java.util.Comparator;
import java.util.Collections;
import java.util.ArrayList;

public record Entry(HashSet<WrappedGoal> goals) {
	public <G extends Goal> int locate(Class<G> targetClass) {
		return this.locate(targetClass, (goal) -> true);
	}

	public <G extends Goal> int locate(Class<G> targetClass, Predicate<G> filter) {
		for (WrappedGoal wrapped : this.goals) {
			Goal goal = wrapped.getGoal();
			if (targetClass.isInstance(goal) && filter.test((G) goal))
				return wrapped.getPriority();
		}
		return -1;
	}

	public int findPrioritySlot(int startingPriority) {
		int lowestPriority = -1;
		HashSet<Integer> foundPriorities = new HashSet();
		for (WrappedGoal wrapped : this.goals) {
			lowestPriority = Math.max(lowestPriority, wrapped.getPriority());
			foundPriorities.add(wrapped.getPriority());
		}

		for (int priority = 0; priority < lowestPriority; priority++) {
			if (startingPriority != -1 && startingPriority > priority)
				continue;
			if (!foundPriorities.contains(priority))
				return priority;
		}

		return lowestPriority + 1;
	}

	public <G extends Goal> boolean removeGoals(Class<G> goalClass, Predicate<G> filter) {
		return this.goals.removeIf((wrapped) -> goalClass.isInstance(wrapped.getGoal()) && filter.test((G) wrapped.getGoal()));
	}

	public void insertGoal(Goal goal, int priority) {
		this.insertGoal(goal, priority, ShiftMode.ChainReaction);
	}

	public void insertGoal(Goal goal, int priority, ShiftMode shift) {
		if (shift == ShiftMode.All) {
			this.lowerGoalsPriorities(priority, -1);
		} else if (shift == ShiftMode.Overlapping) {
			this.lowerGoalsPriorities(priority, priority);
		} else if (shift == ShiftMode.ChainReaction) {
			this.lowerGoalsPriorities(priority, this.findPrioritySlot(priority));
		}
		
		this.goals.add(new WrappedGoal(priority, goal));
	}

	public void replaceGoals(Goal goal, int priority, Predicate<Goal> filter) {
		HashSet<WrappedGoal> goals = this.getGoals(priority, priority);
		for (WrappedGoal wrapped : goals) {
			if(filter.test(wrapped.getGoal()))
				this.goals.remove(wrapped);
		}
		
		this.goals.add(new WrappedGoal(priority, goal));
	}

	public void lowerGoalsPriorities(int highestPriority, int lowestPriority) {
		HashSet<WrappedGoal> goals = this.getGoals(highestPriority, lowestPriority);
		for (WrappedGoal goal : goals) {
			this.goals.remove(goal);
			this.goals.add(new WrappedGoal(goal.getPriority() + 1, goal.getGoal()));
		}
	}

	public HashSet<WrappedGoal> getGoals(int highestPriority, int lowestPriority) {
		HashSet<WrappedGoal> goals = new HashSet();
		for (WrappedGoal wrapped : this.goals) {
			int priority = wrapped.getPriority();
			if (highestPriority != -1 && priority < highestPriority)
				continue;
			if (lowestPriority != -1 && priority > lowestPriority)
				continue;
			goals.add(wrapped);
		}
		
		return goals;
	}

	public ArrayList<WrappedGoal> get() {
		ArrayList<WrappedGoal> goals = new ArrayList();
		for (WrappedGoal wrapped : this.goals) {
			goals.add(wrapped);
		}
		
		Collections.sort(goals, Comparator.comparingInt(WrappedGoal::getPriority));
		return goals;
	}

	public void override(GoalSelector selector) {
		selector.removeAllGoals((goal) -> true);
		for (WrappedGoal wrapped : this.goals) {
			selector.addGoal(wrapped.getPriority(), wrapped.getGoal());
		}
	}

	public enum ShiftMode {
		Ignore, Overlapping, ChainReaction, All
	}
}