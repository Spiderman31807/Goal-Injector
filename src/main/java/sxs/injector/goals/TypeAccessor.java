package sxs.injector.goals;

public interface TypeAccessor<T> {
	abstract Class<T> getType();
}