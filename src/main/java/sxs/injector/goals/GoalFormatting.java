package sxs.injector.goals;

import net.minecraft.world.entity.ai.goal.Goal;

public interface GoalFormatting {
	static String format(Goal goal) {
		if(goal == null)
			return "Void";
	
		String name = goal.getClass().getSimpleName();
		return goal instanceof GoalFormatting formatting ? formatting.getDisplay(name) : name;
	}

	default String getDisplay(String name) {
		if(this instanceof TypeAccessor accessor)
			return "%s[%s]".formatted(name, accessor.getType().getSimpleName());
		return name;
	}
}