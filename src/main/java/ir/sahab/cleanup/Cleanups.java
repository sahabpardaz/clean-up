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
 * <p>It accepts a list of {@link AutoCloseable}s and Lamda callbacks. Upon calling {@code #doAll()}
 * all {@link AutoCloseable}s and Lamda callbacks will be called.
 *
 * <p>If any exceptions occur during these operations, they will be logged but other operations will
 * not be interrupted. That is, we ensure that all operations, will be performed if it is possible.
 *
 * <p>An example usage may look like this:
 * <pre>
 *  cleanups.of(httpServer)     // Calls httpServer.close()
 *          .and(this::stopWorkingThreads())
 *          .and(this::cleanTempFolder())
 *          .and(dbConnection)  // Calls dbConnection.close()
 *          .doAllQuitely();    // If there is an exception on any operation just logs it.
 *                              // You can replace it with doAll() then it throws the first exception
 *                              // after trying for all operations.
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

    /**
     * Does all cleanup operations and if there is an exception on any operation just logs it.
     * @see {@link #doAll()}
     */
    public void doAllQuietly() {
        try {
            doAll();
        } catch (IOException e) {
            logger.warn("Failed to clean-up all of the given operations.", e);
        }
    }

    /**
     * Does all cleanup operations and if there is an exception on any operation, throws the first
     * exception after trying for all operations. Probably the first error was the cause of the
     * problem for the rest of the operations and therefore the most important error to investigate
     * the problem.
     * @throws IOException if there is an exception on any operation, wraps it in {@link IOException}
     *         and returns it. Most of the time, the {@code close()} function in which {@link IOException}
     *         occurs is used as clean-up operation. For this reason, we wrap the catched error with it.
     */
    public void doAll() throws IOException {
        boolean allSucceeded = true;
        Exception firstException = null;
        for (AutoCloseable closeStatement : closeStatements) {
            try {
                closeStatement.close();
            } catch (Exception e) {
                logger.error("Failed to run clean-up statement.", e);
                if (firstException == null) {
                    firstException = e;
                }
                allSucceeded = false;
            }
        }
        if (!allSucceeded) {
            throw new IOException("Failed to clean-up all resources.", firstException);
        }
    }
}
