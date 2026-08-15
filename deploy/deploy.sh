#!/usr/bin/env bash
set -euo pipefail

IMAGE_TAG="${IMAGE_TAG:-0.1.0}"
IMAGE="sky-server:${IMAGE_TAG}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

require_secret() {
  local variable_name="$1"
  local prompt="$2"
  if [[ -z "${!variable_name:-}" ]]; then
    read -r -s -p "${prompt}: " "$variable_name"
    echo
    export "$variable_name"
  fi
}

require_secret MYSQL_ROOT_PASSWORD "MySQL root password"
require_secret MYSQL_PASSWORD "MySQL application password"
require_secret JWT_SECRET "JWT secret (at least 32 characters)"

if [[ ${#JWT_SECRET} -lt 32 ]]; then
  echo "JWT_SECRET must be at least 32 characters." >&2
  exit 1
fi

cd "$PROJECT_DIR"
echo "Building ${IMAGE}..."
docker build --network=host -t "$IMAGE" .

echo "Importing application image into K3s containerd..."
docker save "$IMAGE" | k3s ctr images import -

kubectl apply -f k8s/namespace.yaml
kubectl -n sky-lab create secret generic sky-secrets \
  --from-literal=mysql-root-password="$MYSQL_ROOT_PASSWORD" \
  --from-literal=mysql-password="$MYSQL_PASSWORD" \
  --from-literal=jwt-secret="$JWT_SECRET" \
  --dry-run=client -o yaml | kubectl apply -f -

kubectl apply -f k8s/database-init-configmap.yaml
kubectl apply -f k8s/mysql.yaml
kubectl -n sky-lab rollout status statefulset/mysql --timeout=5m

kubectl apply -f k8s/sky-server.yaml
kubectl -n sky-lab rollout restart deployment/sky-server
kubectl -n sky-lab rollout status deployment/sky-server --timeout=5m

echo
echo "Deployment succeeded. Open TCP 30088 in the cloud security group."
echo "Admin web: http://$(curl -fsS --max-time 5 ifconfig.me 2>/dev/null || echo '<SERVER_PUBLIC_IP>'):30088"
echo "http://$(curl -fsS --max-time 5 ifconfig.me 2>/dev/null || echo '<SERVER_PUBLIC_IP>'):30088/status"
