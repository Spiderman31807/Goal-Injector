package sxs.injector.goals;

import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.function.Predicate;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;
import java.util.function.Function;

public class GoalBuilder {
	private final Set<GoalSet> goals = new HashSet();

	private GoalBuilder() {
	}

	public static GoalBuilder create(GoalSelector selector) {
		if(selector == null)
			return null;

		GoalBuilder builder = new GoalBuilder();
		for (WrappedGoal wrapped : selector.getAvailableGoals()) {
			builder.getOrCreate(wrapped.getPriority()).add(wrapped.getGoal());
		}

		return builder;
	}

	public Set<WrappedGoal> build() {
		Set<WrappedGoal> wrapped = new HashSet();
		this.goals.forEach((set) -> wrapped.addAll(set.build()));
		return wrapped;
	}

	public void apply(GoalSelector selector) {
		if(selector == null) return;
		selector.removeAllGoals((goal) -> true);
		this.build().forEach((wrapped) -> selector.addGoal(wrapped.getPriority(), wrapped.getGoal()));
	}

	public void print(String holder) {
		for (GoalSet set : this.goals.stream().sorted(Comparator.comparingInt(GoalSet::getPriority)).toList()) {
			StringBuilder builder = new StringBuilder();
			for(Goal goal : set.getAll()) {
				if(builder.length() > 0)
					builder.append(", ");
				builder.append(GoalFormatting.format(goal));
			}

			if(builder.length() > 0)
				DefineGoalsEvent.logger.debug("[{}] {} | {}", holder, set.getPriority(), builder.toString());
		}
	}

	public GoalSet getOrCreate(int priority) {
		GoalSet set = this.get(priority);
		if (set != null)
			return set;
			
		set = new GoalSet(priority);
		this.goals.add(set);
		return set;
	}

	public GoalSet get(int priority) {
		for (GoalSet set : this.goals) {
			if (set.getPriority() == priority)
				return set;
		}
		
		return null;
	}

	public Info find(Predicate<Goal> filter) {
		for(GoalSet set : this.goals) {
			Info info = set.find(filter);
			if(info != null)
				return info;
		}
			
		return null;
	}

	public Set<Info> findAll(Predicate<Goal> filter) {
		Set<Info> info = new HashSet();
		for(GoalSet set : this.goals) {
			info.addAll(set.findAll(filter));
		}
		
		return info;
	}

	public void lowerPriority(int target, boolean shouldMerge, boolean shouldPush) {
		GoalSet targetSet = this.get(target);
		GoalSet lowerSet = this.get(target++);
		if(targetSet == null)
			return;

		if(lowerSet == null) {
			targetSet.decreasePriority();
		} else if(shouldMerge) {
			lowerSet.addAll(targetSet.getAll());
			this.goals.remove(targetSet);
		} else if(shouldPush) {
			this.lowerPriority(target, false, true);
			targetSet.decreasePriority();
		}
	}

	public boolean add(int priority, Goal goal) {
		return this.getOrCreate(priority).add(goal);
	}

	public boolean remove(int priority, Predicate<Goal> filter) {
		GoalSet set = this.get(priority);
		return set == null ? false : set.remove(filter);
	}

	public int remove(Predicate<Goal> filter) {
		int count = 0;
		for(GoalSet set : this.goals) {
			if(set.remove(filter))
				count++;
		}

		return count;
	}

	public boolean remove(Info info) {
		GoalSet set = this.get(info.priority());
		return set == null ? false : set.remove(info);
	}

	public boolean replace(Goal goal, Info info) {
		GoalSet set = this.get(info.priority());
		return set == null || !set.remove(info) ? false : set.add(goal);
	}

	public static class GoalSet {
		private final ArrayList<Goal> contents = new ArrayList();
		private int priorityLevel;

		public GoalSet(int priorityLevel) {
			this.priorityLevel = priorityLevel;
		}

		public int getPriority() {
			return this.priorityLevel;
		}

		public void setPriority(int priorityLevel) {
			this.priorityLevel = priorityLevel;
		}

		public void increasePriority() {
			this.priorityLevel--;
		}

		public void decreasePriority() {
			this.priorityLevel++;
		}

		public List<Goal> getAll() {
			return List.copyOf(this.contents);
		}

		public boolean addAll(List<Goal> goals) {
			return this.contents.addAll(goals);
		}

		public boolean addAll(Goal... goals) {
			return this.addAll(List.of(goals));
		}

		public boolean add(Goal goal) {
			return this.contents.add(goal);
		}

		public boolean remove(Info info) {
			if(info.goal().equals(this.contents.get(info.index())))
				return this.remove(info.index());
			return false;
		}

		public boolean remove(Predicate<Goal> filter) {
			return this.contents.removeIf((goal) -> filter.test(goal));
		}

		public boolean remove(int index) {
			return this.contents.remove(index) != null;
		}

		public Info find(Predicate<Goal> filter) {
			for (int index = 0; index < this.contents.size(); index++) {
				Goal goal = this.contents.get(index);
				if (filter.test(goal))
					return new Info(this.priorityLevel, index, goal);
			}
			
			return null;
		}

		public Set<Info> findAll(Predicate<Goal> filter) {
			Set<Info> info = new HashSet();
			for (int index = 0; index < this.contents.size(); index++) {
				Goal goal = this.contents.get(index);
				if (filter.test(goal))
					info.add(new Info(this.priorityLevel, index, goal));
			}
			
			return info;
		}

		public Set<WrappedGoal> build() {
			Set<WrappedGoal> wrapped = new HashSet();
			for(Goal goal : this.contents) {
				wrapped.add(new WrappedGoal(this.priorityLevel, goal));
			}
			
			return wrapped;
		}
	}

	public static record Info(int priority, int index, Goal goal) {
	}
}