package sxs.injector.goals;

import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Predicate;
import java.util.function.Function;
import java.util.Optional;

public class Helpers {
	public static Predicate<Goal> getAvoidTarget(Class clazz) {
		return (goal) -> {
			if (goal instanceof AvoidEntityGoal && goal instanceof TypeAccessor accessor)
				return accessor.getType().isAssignableFrom(clazz);
			return false;
		};
	}

	public static Predicate<Goal> getNearestTarget(Class clazz) {
		return (goal) -> {
			if (goal instanceof NearestAttackableTargetGoal && goal instanceof TypeAccessor accessor)
				return accessor.getType().isAssignableFrom(clazz);
			return false;
		};
	}

	public static class AvoidTargetBuilder {
		private Optional<Class<?>> avoidClass = Optional.empty();
		private Optional<Predicate<LivingEntity>> avoidPredicate = Optional.empty();
		private Optional<Float> maxDist = Optional.empty();
		private Optional<Double> walkSpeedModifier = Optional.empty();
		private Optional<Double> sprintSpeedModifier = Optional.empty();
		private Optional<Predicate<LivingEntity>> predicateOnAvoidEntity = Optional.empty();

		public AvoidTargetBuilder() {
		}

		public AvoidTargetBuilder avoid(Class<?> type) {
			this.avoidClass = Optional.ofNullable(type);
			return this;
		}

		public AvoidTargetBuilder filter(Predicate<LivingEntity> filter) {
			this.avoidPredicate = Optional.of(filter);
			return this;
		}

		public AvoidTargetBuilder distance(float dist) {
			this.maxDist = Optional.of(dist);
			return this;
		}

		public AvoidTargetBuilder walkingSpeed(double speed) {
			this.walkSpeedModifier = Optional.of(speed);
			return this;
		}

		public AvoidTargetBuilder sprintingSpeed(double speed) {
			this.sprintSpeedModifier = Optional.of(speed);
			return this;
		}

		public AvoidTargetBuilder secondaryFilter(Predicate<LivingEntity> filter) {
			this.predicateOnAvoidEntity = Optional.of(filter);
			return this;
		}

		public Function<Goal, Goal> build(Mob mob) {
			return (goal) -> {
				if (mob instanceof PathfinderMob pathfindingMob && goal instanceof GoalArguments args)
					return new AvoidEntityGoal(pathfindingMob, args.<Class<?>>getArgument("avoidClass", this.avoidClass), args.<Predicate<LivingEntity>>getArgument("avoidPredicate", this.avoidPredicate), args.getFloatArgument("maxDist", this.maxDist), args.getDoubleArgument("walkSpeedModifier", this.walkSpeedModifier), args.getDoubleArgument("sprintSpeedModifier", this.sprintSpeedModifier), args.<Predicate<LivingEntity>>getArgument("predicateOnAvoidEntity", this.predicateOnAvoidEntity));
				return null;
			};
		}
	}

	public static class NearestTargetBuilder {
		private Optional<Class<?>> targetType = Optional.empty();
		private Optional<Integer> randomInterval = Optional.empty();
		private Optional<Boolean> mustSee = Optional.empty();
		private Optional<Boolean> mustReach = Optional.empty();
		private Optional<TargetingConditions.Selector> targetConditions = Optional.empty();

		public NearestTargetBuilder() {
		}

		public NearestTargetBuilder target(Class<?> type) {
			this.targetType = Optional.ofNullable(type);
			return this;
		}

		public NearestTargetBuilder interval(int interval) {
			this.randomInterval = Optional.of(interval);
			return this;
		}

		public NearestTargetBuilder mustSee(boolean enabled) {
			this.mustSee = Optional.of(enabled);
			return this;
		}

		public NearestTargetBuilder mustReach(boolean enabled) {
			this.mustReach = Optional.of(enabled);
			return this;
		}

		public NearestTargetBuilder condition(TargetingConditions.Selector condition) {
			this.targetConditions = Optional.ofNullable(condition);
			return this;
		}

		public Function<Goal, TargetGoal> build(Mob mob) {
			return (goal) -> {
				if (goal instanceof GoalArguments args)
					return new NearestAttackableTargetGoal(mob, args.<Class<?>>getArgument("targetType", this.targetType), args.getIntArgument("randomInterval", this.randomInterval), args.getBoolArgument("mustSee", this.mustSee), args.getBoolArgument("mustReach", this.mustReach), args.<TargetingConditions.Selector>getArgument("targetConditions.selector", this.targetConditions));
				return null;
			};
		}
	}
}