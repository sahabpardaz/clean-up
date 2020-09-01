package ir.sahab.cleanup;

import ir.sahab.extendedinterfaces.RunnableWithException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class that helps to free up all desired resources or do all clean-up operations by a single call.
 *
 * <p>It accepts a list of {@link AutoCloseable}(s) and {@link RunnableWithException} statements.
 * Upon calling {@link #doAll()} all {@link AutoCloseable}(s) will be called and
 * all {@link RunnableWithException} statements will be executed.
 *
 * <p>If any exceptions occurs during these operations, other operations will not be interrupted.
 * That is, we ensure that all operations, will be performed if it is possible.
 *
 * <p>An example usage may look like this:
 * <pre>
 *     Cleanups.of(...)
 *             .and(...)
 *             .and(...)
 *             .and(...)
 *             .doAll();
 * </pre>
 * </p>
 */
public class Cleanups {

    private static final Logger logger = LoggerFactory.getLogger(Cleanups.class);

    private List<RunnableWithException<Exception>> closeStatements = new ArrayList<>();

    public static Cleanups empty() {
        return new Cleanups();
    }

    public static Cleanups of(AutoCloseable... closeables) {
        return new Cleanups().and(closeables);
    }

    public static Cleanups of(Collection<? extends AutoCloseable> closeables) {
        return new Cleanups().and(closeables);
    }

    public Cleanups and(AutoCloseable... closeables) {
        return and(Arrays.asList(closeables));
    }

    public Cleanups and(Collection<? extends AutoCloseable> closeables) {
        closeables.stream().filter(Objects::nonNull).forEach(
                (AutoCloseable closeable) -> closeStatements.add(closeable::close));
        return this;
    }

    public void doAllQuietly() {
        try {
            doAll();
        } catch (IOException e) {
            logger.warn("Failed to clean up all of the given operations.", e);
        }
    }

    public void doAll() throws IOException {
        boolean allSucceeded = true;
        for (RunnableWithException<Exception> closeStatement : closeStatements) {
            try {
                closeStatement.run();
            } catch (Exception e) {
                logger.error("Failed to run clean up statement.", e);
                allSucceeded = false;
            }
        }
        if (!allSucceeded) {
            throw new IOException("Failed to clean up all resources.");
        }
    }
}
