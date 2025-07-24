package backend.service.logging;

/**
 * The {@code BenchmarkLogger} interface defines methods for logging messages
 * during benchmarking operations. It provides methods for logging headers,
 * sub-headers, general messages, and error messages.
 *
 * @author Eva Ray
 */
public interface BenchmarkLogger {

    /**
     * Logs a header message with the specified title.
     *
     * @param title the title of the header to log
     */
    void logHeader(String title);

    /**
     * Logs a sub-header message with the specified title.
     *
     * @param title the title of the sub-header to log
     */
    void logSubHeader(String title);

    /**
     * Logs a general message.
     *
     * @param message the message to log
     */
    void log(String message);

    /**
     * Logs an error message with the specified message and exception.
     *
     * @param message the error message to log
     * @param e       the exception associated with the error
     */
    void logError(String message, Exception e);

    /**
     * Logs the end of a benchmarking operation with the specified message.
     */
    void logEnd();
}