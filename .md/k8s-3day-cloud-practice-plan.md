# 三天完成 K8s 全流程实践：云端部署 sky-server

## 目标

三天后，将 `sky-server` 打包为 Docker 镜像，并在一台云服务器的 K3s 集群中运行。当前电脑可以通过云服务器公网 IP 或域名访问后端 API。

最终链路：

```text
当前电脑：Java 源码 -> Maven JAR -> Docker 镜像 -> 推送或传输镜像
                                             -> 云服务器 K3s
当前电脑浏览器 / Postman -> 公网 IP 或域名 -> Ingress / NodePort
                                             -> Service
                                             -> sky-server Deployment
                                             -> MySQL StatefulSet + PVC
```

本计划覆盖日常开发和运维中最重要的 K8s 对象：

```text
Namespace, Pod, Deployment, ReplicaSet, Service, Ingress,
ConfigMap, Secret, PV, PVC, StatefulSet, Job, CronJob,
HPA, DaemonSet, NetworkPolicy, ServiceAccount, Role, RoleBinding,
ResourceQuota, LimitRange, PodDisruptionBudget
```

Kubernetes 内置 API 资源有数百种，三天不追求逐个使用。本计划覆盖应用发布全流程中需要理解和动手过的核心对象。

## 云服务器方案

推荐配置：

| 项目 | 选择 |
| --- | --- |
| 操作系统 | Ubuntu Server 24.04 LTS x86_64 |
| 配置 | 最低 2 vCPU / 4 GB 内存 / 40 GB 云盘；推荐 4 vCPU / 8 GB |
| 集群发行版 | K3s 单节点 |
| 运行时 | K3s 内置 containerd，不单独安装 Docker 到云端 |
| 公网入口 | Day 2 用 NodePort；Day 3 用 K3s 自带 Traefik Ingress |
| 数据库 | 学习环境使用 K8s 内单副本 MySQL StatefulSet；生产环境应改为托管 MySQL |

安全组和 Ubuntu `ufw` 只开放：

- TCP `22`：仅允许当前电脑公网出口 IP，用于 SSH。
- TCP `80`、`443`：用于 Ingress；学习阶段可先临时向公网开放。
- TCP `30088`：用于 Day 2 NodePort 验收；完成 Day 3 后关闭。

不要开放 `3306`、`6443`（Kubernetes API）或 MySQL Pod 端口到公网。

## Day 1：镜像、Pod 和无状态服务

预计投入：5 至 6 小时。

### 1. 创建并加固云服务器

**讲解**：云服务器是 K8s 的 Node。K3s 同时在这台机器上运行控制平面和工作负载，适合学习；生产集群通常至少有 3 个控制平面节点。

**你动手做**：

1. 创建 Ubuntu 24.04 云服务器，绑定公网 IP。
2. 配置 SSH 密钥登录，不使用 root 密码直接登录。
3. 在安全组中仅开放 22、80、443、30088。
4. 从当前电脑连接：

```powershell
ssh ubuntu@<SERVER_PUBLIC_IP>
```

**验收**：能通过 SSH 登录，执行 `uname -a` 和 `free -h` 能看到 Ubuntu 与内存信息。

### 2. 安装 K3s 和 kubectl 访问权限

**讲解**：`kubectl` 向 API Server 提交期望状态。K3s 内置 API Server、调度器、kubelet、containerd、CoreDNS、local-path 存储和 Traefik。

**你动手做**：在云服务器执行 K3s 官方安装命令；安装完成后执行：

```bash
sudo kubectl get nodes -o wide
sudo kubectl get pods -A
```

将 `/etc/rancher/k3s/k3s.yaml` 复制到当前电脑，并将其中 `127.0.0.1` 改为云服务器公网 IP。当前电脑执行：

```powershell
kubectl get nodes
```

**验收**：当前电脑能查看云端节点。完成后不要把 kubeconfig 提交到 Git，它含有管理员证书。

### 3. Namespace 和 Pod

**讲解**：`Namespace` 隔离不同环境；`Pod` 是最小调度单位。直接创建的 Pod 不具备自动恢复能力。

**你动手做**：

1. 创建 `sky-lab` Namespace。
2. 手写一个 `nginx` Pod YAML，应用到 `sky-lab`。
3. 查看 Pod 状态、日志、事件；进入容器执行 `hostname`。
4. 删除这个 Pod。

**验收**：删除后 Pod 消失且不会自动恢复。你要能解释“Pod 不等于长期运行的服务”。

### 4. Deployment 和 ReplicaSet

**讲解**：`Deployment` 描述镜像版本与副本数；它自动创建 `ReplicaSet`，ReplicaSet 持续把 Pod 数量修正为目标数。

**你动手做**：

1. 将 nginx 改为 2 副本 Deployment。
2. 执行 `kubectl get deployment,rs,pods -n sky-lab`。
3. 删除其中一个 Pod。

**验收**：Deployment 始终保持 2 个 Running Pod，并且能看到 ReplicaSet 自动创建新的 Pod。

### 5. 为 sky-server 制作 Docker 镜像

**讲解**：镜像是跨机器可移植的交付物。镜像内不能写入数据库地址、密码、JWT 密钥等环境配置。

**你动手做**：在当前电脑完成以下任务：

1. 将 `application.properties` 中的数据库 URL、用户名、密码、JWT 密钥改为环境变量读取。
2. 创建多阶段 Dockerfile：Maven 构建 JAR，Java 17 运行 JAR。
3. 构建版本化镜像：

```powershell
mvn test
docker build -t sky-server:0.1.0 .
docker run --rm -p 8088:8088 sky-server:0.1.0
```

**验收**：本机访问 `http://localhost:8088/status` 成功。若应用依赖 MySQL，此阶段可先用本机 MySQL 或 Docker MySQL 验证。

### 6. 将镜像迁移到云端

**讲解**：K3s 使用 containerd，不能直接看见开发机 Docker 的镜像。学习阶段可以离线导入；后续将换成镜像仓库。

**你动手做**：

```powershell
docker save sky-server:0.1.0 -o sky-server-0.1.0.tar
scp .\sky-server-0.1.0.tar ubuntu@<SERVER_PUBLIC_IP>:/tmp/
```

云服务器执行：

```bash
sudo k3s ctr images import /tmp/sky-server-0.1.0.tar
sudo k3s ctr images list | grep sky-server
```

**验收**：云端 containerd 能列出 `sky-server:0.1.0`。

## Day 2：配置、数据库、服务发现和公网访问

预计投入：5 至 6 小时。

### 1. ConfigMap 和 Secret

**讲解**：`ConfigMap` 保存非敏感配置，`Secret` 保存密码和密钥。它们由 Pod 在运行时注入，镜像无需因环境不同而重建。

**你动手做**：

1. 创建 MySQL 地址、数据库名等 ConfigMap。
2. 创建 MySQL 用户、密码、JWT 密钥等 Secret。
3. 使用环境变量将它们注入应用容器。
4. 用 `kubectl describe pod` 检查环境变量名称。

**验收**：YAML 和镜像中没有明文密码；应用能通过服务名 `mysql` 连接数据库。

### 2. PV、PVC、StatefulSet 和 Headless Service

**讲解**：Pod 可被重建，数据不能随 Pod 消失。`PVC` 是应用申请存储的声明，K3s local-path provisioner 会生成对应 `PV`。`StatefulSet` 为有状态 Pod 提供稳定名称与稳定存储。

**你动手做**：

1. 创建 MySQL PVC。
2. 创建 MySQL Headless Service 与单副本 StatefulSet。
3. 在数据库初始化后写入一条测试数据。
4. 删除 MySQL Pod，等待 `mysql-0` 自动恢复。

**验收**：新 Pod 仍名为 `mysql-0`，测试数据没有丢失，`kubectl get pv,pvc -n sky-lab` 显示已绑定。

### 3. Job：数据库初始化

**讲解**：`Job` 用于一次性、可重试并有完成状态的任务。建表和初始化管理员数据是 Job 的典型用途，不应由每个应用 Pod 同时执行。

**你动手做**：

1. 将 `employee.sql` 和 `category.sql` 挂载到初始化 Job。
2. Job 等待 MySQL 就绪后执行 SQL。
3. 用 `kubectl logs job/<job-name>` 查看日志。

**验收**：`kubectl get jobs -n sky-lab` 显示 `Complete`，管理员数据可登录。

### 4. sky-server Deployment、Service 和探针

**讲解**：Spring Boot 后端是无状态工作负载，使用 Deployment。`Service` 提供固定 DNS 并在多个 Pod 之间负载均衡。存活探针决定是否重启，就绪探针决定是否接收流量。

**你动手做**：

1. 创建 2 副本 `sky-server` Deployment，镜像指定 `sky-server:0.1.0`。
2. 配置 `/status` 为 liveness 和 readiness Probe。
3. 创建 ClusterIP Service，名称为 `sky-server`。
4. 在临时调试 Pod 中访问 `http://sky-server:8088/status`。

**验收**：两个 Pod 均为 Ready，删除其中一个后自动恢复；集群内部通过 Service 可访问应用。

### 5. NodePort：从当前电脑访问云端应用

**讲解**：`NodePort` 将 Service 公开在节点端口。它适合验证网络和服务链路；对真实 HTTP 服务，最终入口应是 Ingress。

**你动手做**：

1. 为 `sky-server` 创建 NodePort Service，固定端口 `30088`。
2. 检查云安全组与 UFW 已放行 TCP 30088。
3. 在当前电脑执行：

```powershell
Invoke-RestMethod http://<SERVER_PUBLIC_IP>:30088/status
```

**验收**：返回 `sky-server is running`。再调用登录接口，带 token 新增并分页查询分类，验证完整业务链路。

## Day 3：入口、弹性、安全和一键部署

预计投入：5 至 6 小时。

### 1. Ingress 和域名

**讲解**：Ingress 根据域名和路径将 HTTP 请求路由到 Service。K3s 默认部署 Traefik，公网访问应优先使用 80/443，而不是长期暴露 NodePort。

**你动手做**：

1. 准备一个域名，将 A 记录指向云服务器公网 IP。
2. 创建 Ingress：`api.<your-domain>` 路由到 `sky-server` Service。
3. 先用 HTTP 验证，再使用 cert-manager 和 Let's Encrypt 配置 HTTPS。

**验收**：当前电脑访问 `http://api.<your-domain>/status` 成功。HTTPS 配置完成后，HTTP 自动跳转到 HTTPS。

### 2. Resource requests、limits、LimitRange、ResourceQuota

**讲解**：资源请求影响调度，资源上限防止异常容器耗尽节点。LimitRange 和 ResourceQuota 在 Namespace 层面设定默认值与总上限。

**你动手做**：

1. 为 Java 和 MySQL 设置合理的 CPU/内存 requests、limits。
2. 创建 `sky-lab` 的 LimitRange 与 ResourceQuota。
3. 用 `kubectl describe namespace sky-lab` 查看使用量。

**验收**：新 Pod 未声明资源时获得默认值；超过配额的工作负载无法创建。

### 3. HPA 和 PodDisruptionBudget

**讲解**：`HPA` 根据指标扩缩 Deployment；`PDB` 限制自愿中断时允许不可用的副本数。HPA 依赖 metrics-server，K3s 默认包含它。

**你动手做**：

1. 执行 `kubectl top pods -n sky-lab` 确认指标可用。
2. 为 `sky-server` 创建 HPA，最小 2、副本最大 5。
3. 使用集群内压测 Pod 持续请求 `/status`。
4. 为 `sky-server` 创建 PDB，设置 `minAvailable: 1`。

**验收**：负载上升后 HPA 增加副本；Deployment 滚动升级时至少保持一个可用实例。

### 4. CronJob 和 DaemonSet

**讲解**：CronJob 用于定时工作，如订单超时检查；DaemonSet 保证每个节点运行一个 Pod，常见于日志、监控、网络组件。

**你动手做**：

1. 创建每 5 分钟请求 `/status` 的 CronJob，并查看生成的 Job 和日志。
2. 部署一个仅用于观察的 DaemonSet，查看它在单节点上创建一个 Pod。

**验收**：CronJob 有成功执行记录；DaemonSet 的 Desired、Current、Ready 均为 1。

### 5. ServiceAccount、Role、RoleBinding 和 NetworkPolicy

**讲解**：Pod 与 CI 不应使用 cluster-admin。ServiceAccount 是身份，Role 是 Namespace 内权限集合，RoleBinding 将权限绑定给身份。NetworkPolicy 限制 Pod 间网络访问。

**你动手做**：

1. 创建部署专用 ServiceAccount，只允许在 `sky-lab` 读取和更新 Deployment、Service、ConfigMap。
2. 创建 Role 与 RoleBinding，并用 `kubectl auth can-i` 验证。
3. 创建 NetworkPolicy，只允许 `sky-server` Pod 访问 MySQL 的 3306 端口。

**验收**：发布身份不能操作其他 Namespace；未带 `app=sky-server` 标签的调试 Pod 无法连接 MySQL。

### 6. 整理发布包、升级和回滚

**讲解**：可交付物不能依赖开发机 IDE 或手工命令。Kustomize 通过一组 YAML 生成环境化资源；Deployment 支持滚动升级和回滚。

**你动手做**：整理以下目录并写 `deploy.sh`：

```text
release/
  images/sky-server-0.1.0.tar
  k8s/
    namespace.yaml
    configmap.yaml
    secret.example.yaml
    mysql-statefulset.yaml
    database-init-job.yaml
    sky-server-deployment.yaml
    service.yaml
    ingress.yaml
    hpa.yaml
    rbac.yaml
    network-policy.yaml
  deploy.sh
  README.md
```

`deploy.sh` 的职责是：导入镜像、应用 YAML、等待 MySQL/Job/Deployment 就绪，并打印公网访问地址。

然后构建 `sky-server:0.1.1`，更新 Deployment 镜像并观察滚动升级；故意回滚到 `0.1.0`。

**验收**：

```bash
kubectl rollout status deployment/sky-server -n sky-lab
kubectl rollout history deployment/sky-server -n sky-lab
kubectl rollout undo deployment/sky-server -n sky-lab
```

三条命令均能正确反映发布、版本记录和回滚。

## 最终验收清单

- [ ] 云端 K3s 节点正常，`sky-lab` Namespace 中所有关键 Pod Ready。
- [ ] MySQL StatefulSet 重建后，PVC 中的数据仍存在。
- [ ] database-init Job 成功执行且管理员能登录。
- [ ] `sky-server` Deployment 运行 2 个副本，探针成功。
- [ ] 当前电脑可通过 `http://<SERVER_PUBLIC_IP>:30088/status` 访问。
- [ ] 当前电脑可通过域名和 HTTPS Ingress 访问。
- [ ] HPA、CronJob、DaemonSet、RBAC、NetworkPolicy、PDB 已创建并完成对应验证。
- [ ] 将 `release/` 复制到另一台同规格云服务器后，执行 `deploy.sh` 可以重新部署。

## 重要边界

本计划中的 MySQL StatefulSet 是为了练习 PVC、StatefulSet 和 Job。真实生产环境应优先使用云厂商的托管 MySQL，并做好备份、监控、故障恢复和密钥托管。

Day 1 至 Day 2 结束前，不要将 MySQL、Kubernetes API 或含有明文 Secret 的 YAML 暴露到公网，也不要将真实 Secret 提交到 Git。
