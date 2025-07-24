package backend.service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * {@code ReportService} is responsible for generating an HTML benchmark report by invoking
 * an external Python script.
 *
 * <p>This service assumes a Python environment is available and that the
 * {@code generate_report.py} script exists in the {@code scripts/} directory.
 *
 * <p>The generated HTML is returned as a string and is intended for use in
 * frontend display or export.
 *
 * <p>Temporary files are automatically cleaned up after execution.
 *
 * <p><strong>Note:</strong> If the script execution exceeds the timeout duration,
 * the process will be forcibly terminated.
 *
 * @author Eva Ray
 */
public class ReportService {

    private static final String PYTHON_EXECUTABLE = determinePythonExecutable();
    private static final String REPORT_SCRIPT_PATH = "scripts/generate_report.py";
    private static final int PROCESS_TIMEOUT_SECONDS = 60;

    /**
     * Constructs a new {@code ReportService} instance and checks if the report generation
     * script exists. If not, it logs an error message to the console.
     */
    public ReportService() {
        // Vérifier que le script existe
        File scriptFile = new File(REPORT_SCRIPT_PATH);
        if (!scriptFile.exists()) {
            System.err.println("Report script not found: " + REPORT_SCRIPT_PATH);
        }
    }

    /**
     * Generates an HTML report from the provided benchmark data in JSON format.
     * It writes the input to a temporary file, invokes the external Python script,
     * waits for completion, and returns the generated HTML content as a string.
     *
     * @param benchmarkJson the benchmark data in JSON format
     * @return the generated HTML report as a string
     * @throws IOException if the report generation fails, times out, or is interrupted
     */
    public String generateHtmlReport(String benchmarkJson) throws IOException {
        // Créer des fichiers temporaires pour l'entrée et la sortie
        Path tempInputFile = Files.createTempFile("benchmark_data_", ".json");
        Path tempOutputFile = Files.createTempFile("benchmark_report_", ".html");

        try {
            // Write the benchmark JSON data to the temporary input file
            Files.write(tempInputFile, benchmarkJson.getBytes());

            // Construct the ProcessBuilder to run the Python script
            ProcessBuilder pb = new ProcessBuilder(
                    PYTHON_EXECUTABLE,
                    REPORT_SCRIPT_PATH,
                    tempInputFile.toString(),
                    tempOutputFile.toString()
            );

            // Redirect error stream to the output stream for easier debugging
            pb.redirectErrorStream(true);

            System.out.println("Executing python script to generate report");
            Process process = pb.start();

            // Read the output of the Python script
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("Python script output: " + line);
                }
            }

            // Wait for the process to complete with a timeout
            boolean completed = process.waitFor(PROCESS_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (!completed) {
                process.destroyForcibly();
                throw new IOException("Report generation timed out after " + PROCESS_TIMEOUT_SECONDS + " seconds");
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                throw new IOException("Report generation failed with exit code: " + exitCode);
            }

            // Read the generated HTML report from the temporary output file
            return new String(Files.readAllBytes(tempOutputFile), "UTF-8");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Report generation was interrupted", e);
        } finally {
            // Clean up temporary files
            try {
                Files.deleteIfExists(tempInputFile);
                Files.deleteIfExists(tempOutputFile);
            } catch (IOException e) {
                System.err.println("Failed to delete temporary files: " + e.getMessage());
            }
        }
    }

    /**
     * Determines which Python executable to use.
     * Tries {@code python3} first, then falls back to {@code python}.
     *
     * @return the name of the Python executable
     */
    private static String determinePythonExecutable() {
        // Try to use python3 first
        try {
            ProcessBuilder pb = new ProcessBuilder("python3", "--version");
            Process process = pb.start();
            if (process.waitFor() == 0) {
                return "python3";
            }
        } catch (IOException | InterruptedException e) {
            // Ignore and try python
        }

        return "python"; // Fallback to just "python"
    }
}