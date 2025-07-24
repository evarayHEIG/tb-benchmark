package backend.service.logging;

/**
 * The {@code ConsoleLogger} class implements the {@code BenchmarkLogger} interface
 * to provide logging functionality to the console. It formats log messages with
 * headers and sub-headers for better readability.
 *
 * @author Eva Ray
 */
public class ConsoleLogger implements BenchmarkLogger {
    private static final String SEPARATOR = "----------------------------------------------------";

    /**
     * {inheritDoc}
     */
    @Override
    public void logHeader(String title) {
        System.out.println(SEPARATOR);
        System.out.println(title.toUpperCase());
    }

    /**
     * {inheritDoc}
     */
    @Override
    public void logSubHeader(String title) {
        System.out.println(SEPARATOR);
        System.out.println(title.toUpperCase());
        System.out.println(SEPARATOR);
    }

    /**
     * {inheritDoc}
     */
    @Override
    public void log(String message) {
        System.out.println(message);
    }

    /**
     * {inheritDoc}
     */
    @Override
    public void logError(String message, Exception e) {
        System.err.println("ERROR: " + message);
        if (e != null) {
            e.printStackTrace(System.err);
        }
    }

    /**
     * {inheritDoc}
     */
    @Override
    public void logEnd() {
        System.out.println(SEPARATOR);
        System.out.println("BENCHMARK COMPLETED");
        System.out.println(SEPARATOR);
    }
}