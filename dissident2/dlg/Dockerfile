# Base image with Java Runtime
FROM eclipse-temurin:17.0.9_9-jre as runtime

# Install curl for the wait-for-agent.sh script
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy the pre-built JAR file and the wait-for-agent script into the Docker image
COPY target/dlg-0.0.1-SNAPSHOT.jar /app.jar
COPY wait-for-agent.sh /wait-for-agent.sh

# Make the script executable
RUN chmod +x /wait-for-agent.sh

# Expose the port the application runs on
EXPOSE 8888

# Set the entrypoint to run the wait-for-agent script, which then runs the application
ENTRYPOINT ["/wait-for-agent.sh"]
