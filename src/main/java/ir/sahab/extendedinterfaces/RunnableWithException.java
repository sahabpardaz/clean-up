package ir.sahab.extendedinterfaces;

/**
 * Java interface for {@link Runnable} cannot throw checked exceptions. This is a runnable type which can throw a
 * type of checked exception.
 */
@FunctionalInterface
public interface RunnableWithException<T extends Throwable> {
    void run() throws T;
}
