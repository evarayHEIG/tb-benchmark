# README

This file contains the description of the backend of the benchmark application.

## Prerequisites

Before launching the application, make sure you have installed the following:
- [Java 21 or up](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
- [Maven](https://maven.apache.org/install.html)
- [Python 3.10](https://www.python.org/downloads/release/python-3100/)

Python is required to execute the benchmark report generation script. The file `requirements.txt` in the `backend` directory contains the required Python packages. You can install them using the following command:

```bash
pip install -r requirements.txt
```

## Setting up the environment for SSH connection

To connect to the HEIG-VD virtual machine, where the databases are hosted, the application uses SSH tunneling. The credentials are stored in the `backend/src/main/resources/config.properties` file. You need to provide the following properties:

- `ssh.host`: The hostname or IP address of the HEIG-VD virtual machine.
- `ssh.port`: The SSH port of the HEIG-VD virtual machine (usually `22`).
- `ssh.user`: The username to connect to the HEIG-VD virtual machine.

## Launch the backend

Navigate to the `backend` directory, where the `pom.xml` file is located, and execute the following command to build the backend:

```bash
mvn clean install
```

This command will compile the backend code and create a JAR file in the `target` directory named `backend.jar`.

Once the build is complete, you can run the backend using the following command:

```bash
java -jar target/backend.jar
```

This will start the backend application, which will listen for incoming requests on port `7070`.

## Endpoints

## Endpoints

### Benchmark Endpoints

These endpoints accept POST requests with a JSON configuration file defining the benchmark scenario to execute:

- **`/benchmark/unique`**: Executes a predefined query.
- **`/benchmark/unique-custom`**: Executes a custom query provided by the user.
- **`/benchmark/workload`**: Executes a predefined set of queries (workload).
- **`/benchmark/workload-custom`**: Executes a custom workload defined in the configuration.

The format of the configuration files are explained in details in the `configuration` folder of the repository.

### Metadata Endpoints

To allow the frontend to dynamically display available configuration options, several GET endpoints provide metadata:

- **`/meta/databases`**: Lists the databases supported for benchmarking.
- **`/meta/queries`**: Lists the available predefined queries.
- **`/meta/workloads`**: Lists the predefined workloads.
- **`/meta/sizes`**: Lists the supported database sizes (small, medium, large).
- **`/meta/indexes`**: Lists the types of indexes that can be used in benchmarks.

These endpoints prevent hardcoding options in the frontend, ensuring that the displayed data is always synchronized with the actual backend state. This also makes it easier to add new features without modifying code in multiple system components.

### Report Generation

A final endpoint, **`/generate-report`**, accepts POST requests containing benchmark results (in JSON format) to generate an HTML report with charts and tables. This feature relies on a Python script invoked by the backend.

## Troubleshooting

- If you encounter the error `Error during ssh tunnel creation: Session.connect: java.io.IOException: End of IO Stream Read`, just restart the backend, it will work. This error can occur if the SSH tunnel to the virtual machine is not established correctly, and restarting the backend will attempt to create the tunnel again.