# Cloud deployment

Run this directory on the Ubuntu server after cloning the `codex/minimal-sky-server` branch.

## Prerequisites

- K3s is running.
- Docker is running.
- K3s can pull public images through the configured mirror.
- The cloud security group allows TCP 30088 only from the IPs that need access.

## Deploy

```bash
git clone --branch codex/minimal-sky-server https://github.com/Carlosjjh/sky_take_out_study.git sky-server
cd sky-server
chmod +x deploy/deploy.sh
./deploy/deploy.sh
```

The script prompts for the MySQL root password, application database password, and JWT secret. It does not store them in the repository.

After the script completes, verify the deployment on the server:

```bash
kubectl get pods -n sky-lab
kubectl get service -n sky-lab
curl http://127.0.0.1:30088/status
```

From another computer, use:

```text
http://SERVER_PUBLIC_IP:30088/status
```

The default administrator is `admin` and the initial password is `123456`. This is a learning-only account and must be changed before any real use.
