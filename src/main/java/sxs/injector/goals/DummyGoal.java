package sxs.injector.goals;

import net.minecraft.world.entity.ai.goal.Goal;

public class DummyGoal extends Goal implements GoalFormatting {
	private final String displayId;

	public DummyGoal(String displayId) {
		this.displayId = displayId;
	}
	
	public String getId() {
		return this.displayId;
	}

	@Override
	public String getDisplay(String name) {
		return "%s[%s]".formatted(name, this.displayId);
	}

	@Override
	public boolean canUse() {
		return false;
	}
}