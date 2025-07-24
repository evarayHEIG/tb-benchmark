FROM eclipse-temurin:latest

RUN apt-get update && apt-get install -y \
    python3 \
    python3-pip \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY backend.jar .

COPY generate_report.py ./scripts/

COPY requirements.txt .
RUN pip3 install -r requirements.txt --break-system-packages

EXPOSE 7070

CMD ["java", "-jar", "backend.jar"]
