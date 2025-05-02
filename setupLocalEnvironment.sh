#!/bin/bash

# Ensure user is authenicated, and run login if not.
gcloud auth print-identity-token &> /dev/null
if [ $? -gt 0 ]; then
    gcloud auth login
fi
kubectl config use-context dev-fss
kubectl config set-context --current --namespace=okonomi

# Get AZURE system variables
envValue=$(kubectl exec -it $(kubectl get pods | grep sokos-oppdrag | cut -f1 -d' ') -c sokos-oppdrag -- env | egrep "^AZURE|^DATABASE|EREG_URL|^PDL|^SKJERMING|^GA_OKONOMI|^GA_SOKOS|^MQ_" | sort)

# Set AZURE as local environment variables
rm -f defaults.properties
echo "$envValue" > defaults.properties
echo "AZURE stores as defaults.properties"