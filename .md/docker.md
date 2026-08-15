# Docker 入门：将 sky-server 打包为可运行镜像

## 1. Docker 是什么

Docker 是一个用于构建、分发和运行容器的工具。

容器将应用代码、运行时和依赖打包在一起，使应用在开发机、测试机和服务器上以相近方式运行。它不是虚拟机：容器共享宿主机操作系统内核，因此启动通常更快、资源开销更小。

对本项目而言，Docker 可以将以下内容打包为一个镜像：

```text
Java 17 运行环境
+ sky-server.jar
+ 启动命令
= sky-server Docker 镜像
```

## 2. 为什么需要 Docker

未容器化时，服务器需要人工安装 JDK、配置启动命令、处理端口和环境差异。Docker 将这些运行要求写入 `Dockerfile`，使构建过程可复现。

```text
本地开发：mvn package -> 生成 JAR
Docker：   Dockerfile -> 生成镜像
服务器：   docker run -> 运行相同镜像
K8s：      运行相同镜像的多个容器实例
```

Docker 是 K8s 的前置基础之一：K8s 负责编排容器，但首先需要一个可部署的镜像。

## 3. 四个核心概念

### 镜像（Image）

镜像是只读的应用模板，例如 `sky-server:1.0.0`。它包含运行应用所需的文件和环境。

镜像通常带有标签（tag）来标识版本。生产发布应使用明确版本或 Git 提交号，不应只使用 `latest`。

### 容器（Container）

容器是镜像运行后的实例。

同一个 `sky-server:1.0.0` 镜像可以运行多个容器。每个容器相互隔离，但共享宿主机内核。

### Dockerfile

Dockerfile 是镜像构建说明书。它通常包含：

- 使用哪个基础镜像，例如 Java 17 运行时。
- 复制哪个 JAR 文件。
- 暴露哪个端口。
- 容器启动时执行什么命令。

### 镜像仓库（Registry）

镜像仓库用于保存和分发镜像。常见选择包括 Docker Hub、阿里云 ACR、Azure Container Registry（ACR）和私有 Harbor。

发布链路为：

```text
docker build
-> 本地镜像
-> docker push
-> 镜像仓库
-> 服务器或 K8s 拉取镜像运行
```

## 4. Docker 与虚拟机的区别

| 对比项 | Docker 容器 | 虚拟机 |
| --- | --- | --- |
| 隔离层级 | 进程级隔离，共享宿主机内核 | 硬件级虚拟化，运行完整客户机系统 |
| 启动速度 | 通常秒级 | 通常分钟级 |
| 镜像体积 | 通常较小 | 通常较大 |
| 资源占用 | 较低 | 较高 |
| 适合场景 | 微服务、CI/CD、可移植部署 | 多操作系统、强隔离需求 |

## 5. 本项目容器化时的关键点

### 构建产物

`mvn clean package` 会生成可执行 JAR。Dockerfile 可以将这个 JAR 复制到 Java 17 基础镜像中。

### 端口

项目当前配置为 `server.port=8088`，容器需要暴露 8088 端口。运行时可用端口映射将宿主机端口转发到容器端口。

### 数据库连接

容器中的 `localhost` 指的是容器自身，并不是宿主机或另一个 MySQL 容器。因此 JDBC 地址不能在部署时固定写为 `localhost:3306`。

应改用环境变量：

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

运行容器或部署到 K8s 时，再注入不同环境的连接信息和密码。

### 密钥管理

数据库密码和 JWT 密钥不应写入 Dockerfile、镜像或 Git 仓库。开发环境可使用本机环境变量；K8s 环境应使用 Secret。

## 6. Docker 的优点

- 环境一致：开发、测试和生产使用同一镜像。
- 部署简单：服务器不必手工安装项目依赖。
- 启动和扩容快：同一镜像可以快速创建多个容器。
- 版本清晰：镜像标签可对应具体发布版本。
- 易于自动化：适合接入 CI/CD 和 Kubernetes。

## 7. Docker 的局限

- 容器不是完整隔离的虚拟机，仍要关注镜像和宿主机安全。
- 容器数据默认不持久化；数据库、上传文件等需要卷或托管存储。
- 多容器网络、日志和排障仍需要学习。
- Docker 只管理单机容器；多机器调度、高可用和自动扩缩容由 K8s 解决。

## 8. 本项目建议的下一步

1. 新增 `Dockerfile`，构建 `sky-server` 镜像。
2. 用 Docker Compose 在本机同时运行应用和 MySQL，练习网络、环境变量和数据卷。
3. 确认容器化版本能访问 `/hello`、`/status` 和员工登录接口。
4. 再把同一个应用镜像部署到本地 K8s。

这样能够清楚区分：Docker 负责“封装和运行一个应用”，K8s 负责“管理很多应用容器”。
