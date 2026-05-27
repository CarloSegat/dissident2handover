#!/bin/bash

start_time=$(date +%s)

# Function to build and push Docker images in parallel
build_and_push() {
    service_name=$1
    dockerfile_path=$2
    image_name="carlosegat/$service_name"

    # Building and pushing in the background
    (docker build -t $image_name -f $dockerfile_path . && docker push $image_name) &
}

# Build and push images for each service in parallel
build_and_push "d2-customer" "customer/Dockerfile"
build_and_push "d2-accesspoint" "accesspoint/Dockerfile"
build_and_push "d2-dlg" "dlg/Dockerfile"
# resolver moved to top-level sibling ../custom-did-resolution-submodule (no Dockerfile);
# the snet_resolver plugin is now installed into the aca-py agent via docker-compose
# (bind-mount + pip install), so the old d2-agent image build is superseded:
# build_and_push "d2-agent" "../custom-did-resolution-submodule/Dockerfile"
build_and_push "d2-issuer" "issuer/Dockerfile"

docker build -t carlosegat/d2-http-logger -f http-logger/Dockerfile ./http-logger/ && docker push carlosegat/d2-http-logger

# Wait for all background processes to finish
wait

end_time=$(date +%s)
echo "Total time taken: $((end_time - start_time)) seconds"