#!/bin/bash

# Define the root directory of the Maven project
PROJECT_ROOT="/home/carlodev/dissident2"

# Start time (with nanoseconds)
START_TIME=$(date +%s.%N)

# Step 1: Build the entire Maven project at once
echo "Building Maven project at the root..."
cd "$PROJECT_ROOT"
sudo mvn clean package -DskipTests

# Check if Maven build was successful
if [ $? -eq 0 ]; then
    echo "Maven build was successful. Proceeding to Docker builds."

    # Step 2: Build Docker images for each module

    MODULES=("accesspoint" "customer" "dlg" "issuer" "provider" "common") 

    for MODULE in "${MODULES[@]}"; do
        echo "Building Docker image for $MODULE..."

        # Navigate to the module's directory
        cd "$MODULE"

        # Check if the JAR file was built
        JAR_FILE="target/$MODULE-0.0.1-SNAPSHOT.jar"
        if [[ -f "$JAR_FILE" ]] || [[ "$MODULE" == "http-logger" ]]; then
            IMAGE_NAME="carlosegat/d2-$MODULE"

            # Build the Docker image in the module's directory
            docker build -t "$IMAGE_NAME" .

            # Push the image to Docker Hub
            echo "Pushing $IMAGE_NAME to Docker Hub..."
            docker push "$IMAGE_NAME"
        else
            echo "JAR file for $MODULE not found, skipping Docker build..."
        fi

        # Navigate back to the project root
        cd "$PROJECT_ROOT"
    done
else
    echo "Maven build failed. Skipping Docker builds."
fi

cd $PROJECT_ROOT
cd http-logger
docker build -t carlosegat/d2-http-logger .
docker push carlosegat/d2-http-logger

# End time (with nanoseconds)
END_TIME=$(date +%s.%N)

# Calculate duration in seconds with two decimal places
DURATION=$(echo "$END_TIME - $START_TIME" | bc | awk '{printf "%.2f", $0}')

printf "Total time taken: $DURATION seconds\n"

echo "Build process completed."
