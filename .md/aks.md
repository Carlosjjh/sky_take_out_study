# AKS 入门：Azure Kubernetes Service

## 1. AKS 是什么

AKS（Azure Kubernetes Service）是 Microsoft Azure 提供的**托管 Kubernetes 服务**。

它仍然是 Kubernetes，不是另一套容器编排技术。区别在于：自行搭建 K8s 时，你需要安装和维护控制平面；使用 AKS 时，Azure 负责控制平面的部署、可用性和升级，你主要管理自己的工作负载。

```text
自建 Kubernetes：你维护控制平面 + 工作节点 + 应用
AKS：           Azure 维护控制平面，你维护工作节点和应用
```

## 2. AKS 的组成

### 控制平面

控制平面包含 API Server、调度器和控制器等 Kubernetes 核心组件。AKS 将其托管给 Azure，开发者通过 `kubectl` 或 Azure Portal 与其交互。

### 节点池（Node Pool）

节点池是一组 Azure 虚拟机，用于实际运行 Pod。

你可以为不同负载创建不同节点池，例如：

- 普通 CPU 节点池：运行 `sky-server` 等 Web 服务。
- 高内存节点池：运行缓存、数据处理类服务。
- GPU 节点池：运行深度学习推理服务。

### 容器镜像仓库

AKS 通常和 Azure Container Registry（ACR）配合使用：

```text
本地或 CI 构建 sky-server 镜像
-> 推送到 ACR
-> AKS 从 ACR 拉取镜像
-> 创建或更新 Pod
```

### 负载均衡与 Ingress

AKS 可通过 Azure Load Balancer 将公网流量转发给 Kubernetes Service，也可部署 Ingress Controller，以域名和路径规则分发 HTTP 请求。

## 3. AKS 对本项目的意义

如果未来把 `sky-server` 发布到 Azure，AKS 可以运行后端应用的多个副本：

```text
用户
-> Azure Load Balancer / Ingress
-> sky-server Service
-> sky-server Pod x N
-> Azure Database for MySQL
```

建议使用 Azure Database for MySQL 作为数据库，而不是在 AKS 中自行运行生产 MySQL。这样数据库备份、高可用、存储和升级由托管服务处理，应用团队可以专注于 Spring Boot 服务。

## 4. AKS 的优点

- **减少集群运维**：Azure 维护控制平面。
- **Azure 集成**：可对接 ACR、Key Vault、Monitor、Entra ID、托管数据库等服务。
- **弹性能力**：支持节点自动伸缩和 Pod 水平自动伸缩。
- **安全能力**：可使用 Azure 身份与权限体系，减少长期静态凭据的使用。
- **适合 CI/CD**：Azure DevOps 或 GitHub Actions 可将镜像发布后自动部署到 AKS。

## 5. AKS 的限制和成本

- 仍需理解 Kubernetes 的 Deployment、Service、Ingress、Secret 等核心概念。
- 工作节点、负载均衡、公网 IP、磁盘、日志和托管数据库都会产生云资源费用。
- 网络、权限和镜像拉取配置需要学习 Azure 的相关概念。
- 对于只有一个实例的学习项目，AKS 的成本和复杂度通常高于本地 `kind` 或 `minikube`。

## 6. 推荐学习与发布路径

1. 在本地使用 Docker 运行 `sky-server`。
2. 用 `kind` 或 `minikube` 部署 Deployment 和 Service，理解原生 K8s。
3. 注册 Azure 账号，创建 ACR 和 AKS 集群。
4. 将镜像推送至 ACR。
5. 在 AKS 中部署 `sky-server`，通过 Service 或 Ingress 暴露接口。
6. 使用 GitHub Actions 或 Azure DevOps 实现自动测试、构建、推送和部署。

这条路径中，K8s 知识可以迁移到任何云平台；AKS 知识则是将其落地在 Azure 的平台能力。
