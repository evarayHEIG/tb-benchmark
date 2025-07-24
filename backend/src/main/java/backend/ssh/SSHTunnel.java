package backend.ssh;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * {@code SSHTunnel} is a singleton class responsible for creating and managing
 * an SSH tunnel to a remote host. It forwards specific ports from the local
 * machine to remote services such as Couchbase and PostgreSQL.
 *
 * It reads the SSH password from a `config.properties` file and configures
 * local port forwarding using the JSch library.
 *
 * Ports forwarded include:
 * - Couchbase: 8091, 8092, 8093, 11210, 9102
 * - PostgreSQL: 5433, 5432
 *
 * Attention: This class is designed to run on the specific remote host 10.190.133.80
 *
 * @author Eva Ray
 */
public class SSHTunnel {

    private static final String CONFIG_FILE = "config.properties";
    private static final String PASSWORD_PROPERTY = "ssh.password";
    private static final String HOST_PROPERTY = "ssh.host";
    private static final String USER_PROPERTY = "ssh.user";
    private static final int SSH_PORT = 22;

    // Distant ports for Couchbase and PostgreSQL
    private static final int COUCHBASE_REMOTE_PORT = 8091;
    private static final int COUCHBASE_REMOTE_CAPI_PORT = 8092;
    private static final int COUCHBASE_REMOTE_QUERY_PORT = 8093;
    private static final int COUCHBASE_REMOTE_MEMCACHED_PORT = 11210;
    private static final int COUCHBASE_REMOTE_INDEXER_PORT = 9102;
    private static final int POSTGRES_REMOTE_PORT = 5433;
    private static final int POSTGRES_JSONB_REMOTE_PORT = 5432;

    // SSH session for managing the tunnel
    private Session session;

    /**
     * Private constructor to enforce the singleton pattern.
     */
    private SSHTunnel() {
    }

    /**
     * Loads the properties from the configuration file.
     *
     * @return Properties object containing configuration values
     */
    private Properties loadProperties() {
        Properties props = new Properties();

        try {
            File configFile = new File(CONFIG_FILE);
            if (configFile.exists()) {
                try (FileInputStream fis = new FileInputStream(configFile)) {
                    props.load(fis);
                }
            } else {
                try (InputStream is = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
                    if (is != null) {
                        props.load(is);
                    } else {
                        System.err.println("Configuration file not found: " + CONFIG_FILE);
                        return props;
                    }
                }
            }
            return props;
        } catch (IOException e) {
            System.err.println("Error loading configuration: " + e.getMessage());
            return props;
        }
    }

    /**
     * Gets a property from the configuration with a default fallback value.
     *
     * @param propertyName the name of the property to retrieve
     * @param defaultValue the default value to return if property is not found
     * @return the property value or default if not found
     */
    private String getProperty(String propertyName, String defaultValue) {
        Properties props = loadProperties();
        String value = props.getProperty(propertyName);

        if (value == null || value.isEmpty()) {
            System.err.println(propertyName + " not found in configuration file, using default.");
            return defaultValue;
        }
        return value;
    }

    /**
     * Loads the SSH password from configuration.
     *
     * @return the SSH password, or "password" as fallback if not found.
     */
    private String loadPassword() {
        return getProperty(PASSWORD_PROPERTY, "password");
    }

    /**
     * Loads the SSH host from configuration.
     *
     * @return the SSH host address, or "10.190.133.80" as fallback if not found.
     */
    private String loadHost() {
        return getProperty(HOST_PROPERTY, "10.190.133.80");
    }

    /**
     * Loads the SSH username from configuration.
     *
     * @return the SSH username, or "student" as fallback if not found.
     */
    private String loadUsername() {
        return getProperty(USER_PROPERTY, "student");
    }

    /**
     * Opens an SSH tunnel and sets up local port forwarding for Couchbase and PostgreSQL.
     *
     * @throws JSchException if the SSH connection or port forwarding setup fails.
     */
    public void openTunnel() throws JSchException {
        JSch jsch = new JSch();
        // Create a new SSH session
        String sshUsername = loadUsername();
        String sshHost = loadHost();

        session = jsch.getSession(sshUsername, sshHost, SSH_PORT);

        session.setPassword(loadPassword());

        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);

        System.out.println("Creating SSH connection...");
        session.connect();
        System.out.println("SSH connection established");

        session.setPortForwardingL(COUCHBASE_REMOTE_PORT, "localhost", COUCHBASE_REMOTE_PORT);
        session.setPortForwardingL(COUCHBASE_REMOTE_QUERY_PORT, "localhost", COUCHBASE_REMOTE_QUERY_PORT);
        session.setPortForwardingL(COUCHBASE_REMOTE_MEMCACHED_PORT, "localhost", COUCHBASE_REMOTE_MEMCACHED_PORT);
        session.setPortForwardingL(COUCHBASE_REMOTE_CAPI_PORT, "localhost", COUCHBASE_REMOTE_CAPI_PORT);
        session.setPortForwardingL(COUCHBASE_REMOTE_INDEXER_PORT, "localhost", COUCHBASE_REMOTE_INDEXER_PORT);
        session.setPortForwardingL(POSTGRES_REMOTE_PORT, "localhost", POSTGRES_REMOTE_PORT);
        session.setPortForwardingL(POSTGRES_JSONB_REMOTE_PORT, "localhost", POSTGRES_JSONB_REMOTE_PORT);

        System.out.println("Tunnels configured on the following ports:");
        System.out.println("Couchbase: localhost:" + COUCHBASE_REMOTE_PORT +
                           " (Query: " + COUCHBASE_REMOTE_QUERY_PORT +
                           ", Memcached: " + COUCHBASE_REMOTE_MEMCACHED_PORT +
                           ", CAPI: " + COUCHBASE_REMOTE_CAPI_PORT +
                           ", Indexer: " + COUCHBASE_REMOTE_INDEXER_PORT + ")");

        System.out.println("PostgreSQL: localhost:" + POSTGRES_REMOTE_PORT);
        System.out.println("PostgreSQL JSONB: localhost:" + POSTGRES_JSONB_REMOTE_PORT);
    }

    /**
     * Closes the SSH tunnel and releases the connection.
     */
    public void closeTunnel() {
        if (session != null && session.isConnected()) {
            System.out.println("Closing SSH tunnel...");
            session.disconnect();
            System.out.println("SSH tunnel closed");
        }
    }

    // Singleton instance
    private static SSHTunnel instance;

    /**
     * Returns the singleton instance of {@code SSHTunnel}. Creates a new instance if it does not exist.
     *
     * @return the singleton instance
     */
    public static synchronized SSHTunnel getInstance() {
        if (instance == null) {
            instance = new SSHTunnel();
        }
        return instance;
    }
}