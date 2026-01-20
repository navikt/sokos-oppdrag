#!/bin/bash

# Ensure user is authenicated, and run login if not.
gcloud auth print-identity-token &> /dev/null
if [ $? -gt 0 ]; then
    gcloud auth login
fi

# Suppress kubectl config output
kubectl config use-context dev-fss
kubectl config set-context --current --namespace=okonomi

# Get pod name
POD_NAME=$(kubectl get pods --no-headers | grep sokos-oppdrag | head -n1 | awk '{print $1}')

if [ -z "$POD_NAME" ]; then
    echo "Error: No sokos-oppdrag pod found" >&2
    exit 1
fi

echo "Fetching environment variables from pod: $POD_NAME"

# Get system variables
envValue=$(kubectl exec "$POD_NAME" -c sokos-oppdrag -- env | egrep "^AZURE|^DATABASE|EREG_URL|^PDL|^SKJERMING|^GA_OKONOMI|^GA_SOKOS|^MQ_" | sort)

# Set local environment variables
rm -f defaults.properties
echo "$envValue" > defaults.properties
echo "Environment variables saved to defaults.properties"