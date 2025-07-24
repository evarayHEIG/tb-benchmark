# README

## Prerequisites

Before launching the application, make sure you have installed the following:

- [Docker](https://docs.docker.com/get-docker/)
- [Docker Compose](https://docs.docker.com/compose/install/)

Ports `7070` and `8080` must be free on your machine for the application to work correctly.

## Launch the application with Docker

Navigate to the directory that contains the `docker-compose.yml` file and execute the following command:

```bash
docker compose up --build
```

This command will build the Docker images and start the application.

Once the first build is complete, you can use the following command to update the application without rebuilding the images the next time you want to run it:

```bash
docker compose up -d
```

## Access the application

-  The backend is accessible at: [http://localhost:7070](http://localhost:7070)
-  The frontend is accessible at: [http://localhost:8080](http://localhost:8080)
  
Logs will be displayed in the terminal where you launched the `docker compose up` command. You can use them to monitor the progress of the benchmarks.

## Troubleshooting

- The backend connects to databases hosted on HEIG-VD's virtual machine infrastructure. To be able to access it, you need to be connected to the HEIG-VD VPN or to the `HEIG-VD` Wi-Fi network. If you are not connected, you will see errors in the logs indicating that the backend could not create the SSH tunnel to the virtual machine.
- If you encounter the error `Error during ssh tunnel creation: Session.connect: java.io.IOException: End of IO Stream Read` in the backend, just restart the backend service and it will work on the second try. This error can occur if the SSH tunnel to the virtual machine is not established correctly, and restarting the backend will attempt to create the tunnel again.
- If you don't see the benchmark options displayed in the frontend and you are sure that the backend is running, just reload the page in your browser. This can happen if the frontend is not able to fetch the benchmark options from the backend on the first load.

## For developers

If you want to update the version of the backend or frontend used, you have to provide the new builds:

- Backend: you need to change the file `backend.jar` in the `docker/backend` directory with the new version of the backend. You can build the backend by running `mvn clean install` in the `backend` directory.
- Frontend: you need to change the folder `frontend/dist` with the new version of the frontend. You can build the frontend by running `npm run build` in the `frontend` directory.

Once the build files are updated, you need to rebuild the Docker images by running:

```bash
docker compose build
```