```
++============================================================================++
||                                                                            ||
||  ██████  ██ ███████ ███████ ██ ██████  ███████ ███    ██ ████████ ██████   || 
||  ██   ██ ██ ██      ██      ██ ██   ██ ██      ████   ██    ██         ██  || 
||  ██   ██ ██ ███████ ███████ ██ ██   ██ █████   ██ ██  ██    ██     █████   ||
||  ██   ██ ██      ██      ██ ██ ██   ██ ██      ██  ██ ██    ██    ██       ||
||  ██████  ██ ███████ ███████ ██ ██████  ███████ ██   ████    ██    ███████  ||
||                                                                            ||
++============================================================================++
```

# Demonstrator overview

This project demonstrates a Service-aware network scenario within a Kubernetes cluster, encompassing multiple interconnected components. Those components are:

1. Client: Acts as an entity that can browse services listed on the Service Repository.

2. Provider: Serves as an entity capable of offering services to the Service Repository.

3. AccessPoint: Functions as the Service Repository itself, facilitating connections for all other components within the system.

4. Distributed Ledger Gateway (DLG): Provides secure DID (Decentralized Identifier) resolution for other entities in the system.

5. Issuer: Responsible for issuing credentials to other entities within the network.

Each component is deployed as a pod within the Kubernetes cluster. In each component there are two containers:
- Controller: Contains the main logic of the component. This also provide a UI to interact with.
- ACA-Py Agent: Accompanying each controller is an Aries Cloud Agent-Python (ACA-Py) agent, which handles specific functionalities related to the Hyperledger Indy.

## DID Allocation
The following DIDs were anchored on BCOVRIN ledger (the test ledger of British Columbia).
DID resolution relies on the aries cloud agent (acapy) endpoint for resolution.

| Entity Name | Seed                             | DID                    | Verkey                                       |
|-------------|----------------------------------|------------------------|----------------------------------------------|
| Client      | Dissident2_Client_---_0000000000 | V9fvKQjtmbsoJb7gLm2Fka | GLrx5YKrUQyWDp8f7FqbXpMtkaY9KvL97Cti5MH2yXiU |
| Provider    | Dissident2_Provider_---_00000000 | KssDMmREv3migEZNLMThjc | BHjs9GVtKs14xqX3BcJGVXamBGAPh7zeKjUn79XxjB5t |
| AccessPoint | Dissident2_Ap_---_00000000000000 | SUqWD8ZL3r6KeYTKKQ6zRw | EtUKSFhpxcZqYa3iRzCU9J34zuHvhDNzgBzMJCNHVLKa |
| Dlg         | Dissident2_Dlg_---_0000000000000 | 8eQhKkZMjKXbLwaBNEKRu3 | 5Ag23enN3zFtvapPKHiEcE9ysi36V4z6GBkKw4w3BKGW |
| Issuer      | Dissident2_Issuer_---_0000000000 | WQtxQy4ERo6vgxkM1o5BPh | H2mjGFtNikTXQSVysrcjYSt5iDJoRp4jFmNSY91UJMTy |


## Port Allocation

| Entity Name  | ACAPy Admin Port | ACAPy Transport Port | Controller Port |
|--------------|------------------|----------------------|-----------------|
| Client       | 5000             | 5100                 | 5555            |
| Provider     | 6000             | 6100                 | 6666            |
| AccessPoint  | 7000             | 7100                 | 7777            |
| Dlg          | 8000             | 8100                 | 8888            |
| Issuer       | 9000             | 9100                 | 9999            |
| Issuer 2     | 4000             | 4100                 | 4444            |

**ACA-Py Admin Port:** This is the port used to access the Swagger UI, providing a user-friendly interface to interact with the ACA-Py's API.

**ACA-Py Transport Port:** This port is used by ACA-Py for inbound communication, listening for messages from other agents.

**Controller Port:** This port is used to access the specific UI for the entity provided by the Controller, offering a dedicated interface for user interactions. This port is also used by the ACA-Py to send webhook events back to Controller.

# Requirements to run

## Login to Docker Hub

To ease the use of Kubernetes and prepare for the CI pipeline, use the credential below to login into Docker Hub:

Run 

```
docker login -u haidinhtuan
```
or
```
docker login -u carlosegat
```

At the password prompt, enter the personal access token.

Hai password:
```
<REDACTED-DOCKERHUB-TOKEN>
```

Carlo password:
```
<REDACTED-DOCKERHUB-TOKEN>
```

# Build and deploy the cluster

Located in the  project folder, `build.sh` is a convenient script to build and push all necessary Docker images to Docker Hub. Simply run the script after making code changes to update your Docker images.

```
./build.sh
```

After updating your Docker images, you can deploy the new code to your Kubernetes cluster with the following command sequence:

```
microk8s kubectl delete -f ./k8s/components/.
microk8s kubectl apply -f ./k8s/components/.
```

Check deployment with
```
microk8s kubectl get pods
```

The UIs of the Client and Provider can be accessed at: 

```
http://130.149.223.197/client_ui
http://130.149.223.197/provider_ui
```

# Demonstrator procedures

## Startup order in Kubernetes cluster

In this demonstrator, the Issuer will be deploy first. 

Other components (accesspoint, client, provider) waits for this Issuer by a init container, which periodically asks Issuer healthcheck endpoint. 

## Startup oder in Kubernetes pod

In each pod, the agent will be started first, the controller starts after the agent is up and running. 

## Issuer check for Schema and Credential Definition IDs

Issuer will check if the schemaID given in the application.yml exist or not. If there is no such schemaID stored in the wallet, it creates one with the ledger, then create a credential definition.

Otherwise it continues using the schema and credential Id in the wallet.

## 

# Set up on a fresh Ubuntu jammy 22.04 

I recorded the setup steps I went through when running this on a fresh VM.

## Install Docker
```
for pkg in docker.io docker-doc docker-compose docker-compose-v2 podman-docker containerd runc; do sudo apt-get remove $pkg; done
```

```
sudo apt-get update
sudo apt-get install ca-certificates curl gnupg
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg


echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt-get update
```

```
sudo apt-get install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
```

## Install microk8s
```
sudo snap install microk8s --classic --channel=1.27
```

Do this so you don't need to use sudo
```
sudo usermod -a -G microk8s $USER
sudo mkdir ~/.kube
sudo chown -R $USER ~/.kube
newgrp microk8s
```

## make sure microk8s is running
`microk8s start`

## Enable stuff

```
sudo microk8s enable community
sudo microk8s enable multus
sudo microk8s enable ingress 
microk8s enable storage
```

If you run `sudo microk8s kubectl get pods -A`    
you should see the following pods related to the above
enabled things:

```
kube-system   hostpath-provisioner-58694c9f4b-wjllg      1/1     Running   1 (2d18h ago)   2d18h
kube-system   coredns-7745f9f87f-kqnjw                   1/1     Running   1 (2d18h ago)   2d19h
ingress       nginx-ingress-microk8s-controller-pqcfm    1/1     Running   9 (2d18h ago)   2d18h
kube-system   calico-kube-controllers-6c99c8747f-6qvn5   1/1     Running   8 (2d18h ago)   2d19h
kube-system   calico-node-kx6f9                          1/1     Running   1 (2d18h ago)   2d19h
kube-system   kube-multus-ds-c74br                       1/1     Running   1 (2d18h ago)   2d18h
```

The calico pods are instanciated automatically by microk8s.

## Firewall

We unproudly turned off firewall to ensure we don't run in connectivity problems.
```
sudo ufw disable
```

Enable BuildKit for faster build by creating Docker daemon configuration in `/etc/docker/daemon.json` as below:

```
{
  "features": {
    "buildkit": true
}
```

## Troubleshoot pods in the EVICTING state

If your pods are being evicted and after using `describe` the issue seems to
be `disk pressure` then increasing the logical volume used by microk8s may help.

1: find the name of your logical volume with `df -T`
2: `sudo lvextend -L +10G <path to logical volume>`
3: `sudo resize2fs <path tp logical volume>`

Furtheremore, also consider `docker system prune`, as Docker images can take up quite some space.

# Mac M2 Setup

brew install ubuntu/microk8s/microk8s
microk8s install