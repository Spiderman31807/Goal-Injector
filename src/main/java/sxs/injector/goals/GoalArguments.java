package sxs.injector.goals;

import java.util.Optional;

public interface GoalArguments {
	default int getIntArgument(String name) {
		return 0;
	}

	default double getDoubleArgument(String name) {
		return 0;
	}

	default float getFloatArgument(String name) {
		return 0;
	}

	default boolean getBoolArgument(String name) {
		return false;
	}

	default <T extends Object> T getArgument(String name) {
		return null;
	}

	default int getIntArgument(String name, Optional<Integer> override) {
		return override.isPresent() ? override.get() : this.getIntArgument(name);
	}

	default double getDoubleArgument(String name, Optional<Double> override) {
		return override.isPresent() ? override.get() : this.getDoubleArgument(name);
	}

	default float getFloatArgument(String name, Optional<Float> override) {
		return override.isPresent() ? override.get() : this.getFloatArgument(name);
	}

	default boolean getBoolArgument(String name, Optional<Boolean> override) {
		return override.isPresent() ? override.get() : this.getBoolArgument(name);
	}

	default <T extends Object> T getArgument(String name, Optional<T> override) {
		return override.isPresent() ? override.get() : this.getArgument(name);
	}
}