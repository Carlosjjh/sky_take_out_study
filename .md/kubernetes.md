# Kubernetes（K8s）入门：以 sky-server 为例

## 1. K8s 是什么

Kubernetes，通常简称 K8s，是一个开源的**容器编排平台**。

Docker 负责把应用及其运行环境打包为镜像；K8s 负责在多台机器上部署、调度、扩缩容和维护这些容器。它解决的不是“应用如何启动”，而是“许多应用实例如何稳定运行”。

对于本项目，完整的交付链路可以是：

```text
Java 代码
-> Maven 构建 sky-server.jar
-> Docker 构建 sky-server 镜像
-> 推送镜像仓库
-> Kubernetes 部署和滚动升级
-> 用户访问 API
```

可类比为模型推理服务：Docker 将推理代码、Python 环境和模型依赖封装成一个可复现的运行包；K8s 决定每个推理实例运行在哪里、实例异常后如何恢复，以及流量增加时启动多少实例。

## 2. K8s 为什么出现

传统部署往往是把 JAR、Python 服务或 Docker 容器手动运行在一台服务器上。这在服务少、流量低时足够，但会遇到以下问题：

- 应用崩溃后需要人工发现和重启。
- 新版本发布容易中断服务，也不容易快速回滚。
- 流量增长时，需要人工新增机器和配置负载均衡。
- 同一个服务的多个实例地址会变化，调用方难以发现它们。
- 配置、密码和运行命令分散在各台机器上，难以复现。

K8s 让开发者只需声明期望状态，例如“`sky-server` 运行 3 个实例，镜像为某个版本，对外暴露 8088 端口”。控制器会持续将实际状态调整到这个期望状态。

## 3. 核心对象

### Pod

Pod 是 K8s 的最小调度单位，通常包含一个应用容器。

本项目中，一个 Pod 可以运行一个 `sky-server` 容器。Pod 是可替换的：故障重建或新版发布后，Pod 名称和 IP 可能改变，因此不应依赖其固定地址。

### Deployment

Deployment 用于管理无状态应用的多个 Pod。

它可以声明：

```text
sky-server 的副本数：2
容器镜像：registry.example.com/sky-server:1.0.0
容器端口：8088
```

Deployment 能自动重建异常 Pod，并在新镜像发布时采用滚动更新，逐步替换旧版本实例。

### Service

Service 为一组 Pod 提供固定的网络入口和 DNS 名称。

因为 Pod 的 IP 会变化，前端、网关或其他服务不能直接连接某个 Pod。它们应通过 Service 访问 `sky-server`。Service 再将请求分发到健康的 Pod。

### Ingress

Ingress 负责 HTTP/HTTPS 的入口路由，通常可以将域名或路径转发到 Service。

例如：

```text
https://api.example.com/admin/*
-> Ingress
-> sky-server Service
-> 一个健康的 sky-server Pod
```

### ConfigMap 和 Secret

`ConfigMap` 保存非敏感配置，如运行环境、日志级别、MySQL 地址。

`Secret` 保存敏感数据，如数据库用户名、密码、JWT 密钥。它们应以环境变量或挂载文件的方式注入容器，不能写进镜像或提交到 Git 仓库。

本项目当前的 `application.properties` 含有数据库密码。部署到 K8s 前应改为读取环境变量，例如：

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

### Namespace

Namespace 用于资源隔离。常见做法是为 `dev`、`test`、`prod` 分别创建命名空间，防止测试环境影响生产环境。

## 4. 一次请求在 K8s 中的流转

```text
客户端
-> Ingress / 负载均衡器
-> Service（稳定地址）
-> sky-server Pod（Spring Boot :8088）
-> MySQL
```

其中，`Controller -> Service -> Mapper -> MySQL` 仍然是项目内部的业务调用链。K8s 不替代 Spring Boot、MyBatis 或 MySQL；它管理的是应用在基础设施层面的运行方式。

## 5. K8s 的优点

- **自愈**：Pod 进程退出或健康检查失败时，自动重建。
- **弹性扩缩容**：可以手动扩容，也可按 CPU、内存等指标自动扩容。
- **滚动发布和回滚**：逐步替换旧 Pod，失败时可切回历史版本。
- **服务发现和负载均衡**：Service 提供稳定地址，并在多个 Pod 之间分流。
- **配置与代码分离**：镜像可在不同环境复用，配置和密钥在部署时注入。
- **声明式交付**：通过 YAML 描述目标状态，发布过程可审计、可复现。

## 6. K8s 的缺点和适用边界

- 集群、网络、存储、权限、监控等概念较多，学习成本高。
- 对单机、单体、低流量项目，K8s 可能比直接运行 Docker Compose 更复杂。
- MySQL 这类有状态服务需要持久化存储、备份、恢复和高可用设计，不应在入门阶段随意部署到 K8s。
- K8s 不能自动修复业务代码错误、慢 SQL 或数据库设计问题。

对当前学习项目，建议先在本机以 `kind` 或 `minikube` 学习 K8s；生产环境优先使用云托管 MySQL，而不是先在集群中自建 MySQL。

## 7. 本项目如何接入 K8s

当前项目已具备 Java 17、Spring Boot、Maven、MySQL 和 `/status` 接口，适合部署应用本身。还需补齐以下内容：

1. 新增 `Dockerfile`，把 Maven 构建产物打成镜像。
2. 将数据库连接信息改为环境变量，并用 `Secret` 管理密码。
3. 新增 K8s YAML：`Deployment`、`Service`、`ConfigMap`、`Secret`，后续再加 `Ingress`。
4. 为 Spring Boot 增加健康检查端点，供 K8s 判断应用是否存活、是否已就绪接流量。
5. 准备镜像仓库，例如 Docker Hub、阿里云 ACR 或 Azure Container Registry。
6. 用 GitHub Actions、GitLab CI 或 Jenkins 串联测试、构建镜像、推送、部署。

## 8. 一键打包发布的目标

最终流程可以收敛成如下命令或 CI 流水线：

```text
git push
-> mvn test && mvn package
-> docker build
-> docker push
-> kubectl apply 或 helm upgrade
-> K8s 滚动发布 sky-server
```

学习顺序建议：先完成 Docker 化，再本地部署到 K8s，最后接入云端集群和 CI/CD。这样每一层问题都容易定位。
