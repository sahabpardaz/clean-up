package ir.sahab.cleanup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class that helps to free up all desired resources and clean-up operations by a single call.
 *
 * <p>It accepts a list of {@link AutoCloseable}. Upon calling {@code #doAll()}
 * all {@link AutoCloseable} will be called.
 *
 * <p>If any exceptions occur during these operations, they will be logged but other operations will
 * not be interrupted. That is, we ensure that all operations, will be performed if it is possible.
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

    private List<AutoCloseable> closeStatements = new ArrayList<>();

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
        closeables.stream().filter(Objects::nonNull).forEach(closeable -> closeStatements.add(closeable));
        return this;
    }

    public void doAllQuietly() {
        try {
            doAll();
        } catch (IOException e) {
            logger.warn("Failed to clean-up all of the given operations.", e);
        }
    }

    public void doAll() throws IOException {
        boolean allSucceeded = true;
        // Probably the first error was the cause of the problem for the rest of the operations and therefore
        // the most important error to investigate the problem. For this reason, only the first error is
        // returned after the entire clean-up operations.
        IOException firstException = null;
        for (AutoCloseable closeStatement : closeStatements) {
            try {
                closeStatement.close();
            } catch (Exception e) {
                logger.error("Failed to run clean-up statement.", e);
                if (firstException == null) {
                    // Most of te time, the {@code close()} function in which {@link IOException} occurs is used.
                    // For this reason, we put the catched error in it.
                    firstException = new IOException("Failed to clean-up all resources.", e);
                }
                allSucceeded = false;
            }
        }
        if (!allSucceeded) {
            throw firstException;
        }
    }
}
