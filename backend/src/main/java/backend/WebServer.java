package backend;

import backend.model.request.CustomRequest;
import backend.model.request.CustomWorkloadRequest;
import backend.model.request.UniqueRequest;
import backend.model.request.WorkloadRequest;
import backend.service.BenchmarkService;
import backend.service.MetadataService;
import backend.service.ReportService;
import backend.ssh.SSHTunnel;
import com.jcraft.jsch.JSchException;
import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import io.javalin.plugin.bundled.CorsPluginConfig;

/**
 * The {@code WebServer} class initializes and starts a Javalin-based HTTP server
 * that exposes various REST API endpoints for benchmarking, metadata retrieval,
 * and report generation.
 *
 * It handles SSH tunneling and manages the lifecycle of backend services.
 *
 * @author Eva Ray
 */
public class WebServer {

    private final BenchmarkService benchmarkService;
    private final MetadataService metadataService;
    private final ReportService reportService;

    // The port on which the server will listen for incoming requests
    private static final int SERVER_PORT = 7070;

    // API endpoints for benchmarking and metadata
    private static final String BENCHMARK_ENDPOINT = "/benchmark";
    private static final String METADATA_ENDPOINT = "/meta";

    /**
     * Constructs a new WebServer instance, initializing the necessary services.
     */
    public WebServer() {
        this.benchmarkService = new BenchmarkService();
        this.metadataService = new MetadataService();
        this.reportService = new ReportService();
    }

    /**
     * Starts the HTTP server, sets up SSH tunneling, defines the API routes,
     * and registers a shutdown hook to clean up resources.
     */
    public void start() {
        try {
            SSHTunnel.getInstance().openTunnel();

            Javalin app = Javalin.create(config -> {
                config.bundledPlugins.enableCors(cors -> {
                    cors.addRule(CorsPluginConfig.CorsRule::anyHost);
                });
            }).start(SERVER_PORT);

            app.get("/", ctx -> ctx.result("test"));

            // Metadata endpoints
            app.get(METADATA_ENDPOINT + "/queries", ctx -> ctx.json(metadataService.getQueryOptions()));
            app.get(METADATA_ENDPOINT + "/databases", ctx -> ctx.json(metadataService.getDatabaseOptions()));
            app.get(METADATA_ENDPOINT + "/sizes", ctx -> ctx.json(metadataService.getSizeOptions()));
            app.get(METADATA_ENDPOINT + "/indexes", ctx -> ctx.json(metadataService.getIndexOptions()));
            app.get(METADATA_ENDPOINT + "/workloads", ctx -> ctx.json(metadataService.getWorkloadOptions()));

            // Benchmark endpoints
            app.post(BENCHMARK_ENDPOINT + "/unique", ctx -> {
                UniqueRequest request = ctx.bodyAsClass(UniqueRequest.class);
                ctx.json(benchmarkService.runSingleQueryBenchmark(request));
            });

            app.post(BENCHMARK_ENDPOINT + "/workload", ctx -> {
                WorkloadRequest request = ctx.bodyAsClass(WorkloadRequest.class);
                ctx.json(benchmarkService.runWorkloadBenchmark(request));
            });

            app.post(BENCHMARK_ENDPOINT + "/unique-custom", ctx -> {
                try {
                    CustomRequest request = ctx.bodyAsClass(CustomRequest.class);
                    ctx.json(benchmarkService.runCustomSingleQueryBenchmark(request));
                } catch (Exception e) {
                    ctx.status(400).result("Invalid request: " + e.getMessage());
                }
            });

            app.post(BENCHMARK_ENDPOINT + "/workload-custom", ctx -> {
                try {
                    CustomWorkloadRequest request = ctx.bodyAsClass(CustomWorkloadRequest.class);
                    ctx.json(benchmarkService.runCustomWorkloadBenchmark(request));
                } catch (Exception e) {
                    ctx.status(400).result("Invalid request: " + e.getMessage());
                }
            });

            app.post("/generate-report", ctx -> {
                        try {
                            String benchmarkData = ctx.body();
                            String htmlReport = reportService.generateHtmlReport(benchmarkData);

                            ctx.contentType("text/html")
                                    .header("Content-Disposition", "attachment; filename=benchmark-report.html")
                                    .result(htmlReport);
                        } catch (Exception e) {
                            System.err.println("Error generating report: " + e.getMessage());
                            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                    .result("Failed to generate report: " + e.getMessage());
                        }
                    }
            );

            // Register shutdown hooks
            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
        } catch (JSchException e) {
            System.err.println("Error during ssh tunnel creation: " + e.getMessage());
        }
    }

    /**
     * Shuts down the server by releasing benchmark resources and closing the SSH tunnel.
     */
    private void shutdown() {
        benchmarkService.shutdown();
        SSHTunnel.getInstance().closeTunnel();
    }

    /**
     * Entry point of the application. Instantiates and starts the web server.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        new WebServer().start();
        System.out.println("Web server started on port " + SERVER_PORT);
    }
}
