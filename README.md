# Sky Take Out Backend Study

这是一个用于学习 Java 企业级后端开发的 Spring Boot 项目，当前围绕 Sky Take Out 外卖系统后端逐步搭建。

项目重点不是一次性完成完整业务，而是按真实后端分层一步步理解：

```text
Controller -> Service -> Mapper -> Database
DTO -> Entity -> VO -> Result
```

## 当前进度

已完成：

- Spring Boot 项目基础构建
- `/hello` 和 `/status` 基础接口
- 统一响应对象 `Result<T>`
- 员工登录 DTO/VO
- 员工登录 Controller/Service/Mapper 分层
- MySQL `employee` 表查询
- 业务异常 `BusinessException`
- 全局异常处理 `GlobalExceptionHandler`
- 业务提示常量 `MessageConstant`
- simulator 契约测试样板

当前员工登录链路：

```text
前端 JSON
-> EmployeeLoginDTO
-> EmployeeController
-> EmployeeService
-> EmployeeMapper
-> MySQL employee 表
-> Employee
-> EmployeeLoginVO
-> Result<EmployeeLoginVO>
-> 前端 JSON
```

## 技术栈

- Java 17
- Spring Boot 3.3.5
- Spring MVC
- MyBatis
- MySQL
- Maven
- JUnit 5
- H2 test database
- Lombok

## 项目结构

```text
src/main/java/com/sky/sky_server
  constant/     常量
  controller/   HTTP 接口
  dto/          前端入参对象
  entity/       数据库实体对象
  exception/    业务异常
  handler/      全局异常处理
  mapper/       MyBatis 数据访问层
  result/       统一响应对象
  service/      业务逻辑接口
  service/impl/ 业务逻辑实现
  vo/           返回给前端的数据对象

simulator/
  src/test/java       上下游契约测试代码
  src/test/resources  mock 下游 JSON

.md/
  session1.md
  session2.md
```

## 本地环境准备

需要安装：

- JDK 17 或更高版本
- Maven
- MySQL

确认 Java：

```powershell
java -version
```

确认 Maven：

```powershell
mvn -version
```

## 数据库初始化

进入 MySQL：

```powershell
mysql -u root -p
```

创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS sky_take_out
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

选择数据库：

```sql
USE sky_take_out;
```

创建员工表：

```sql
CREATE TABLE IF NOT EXISTS employee (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    name VARCHAR(32) NOT NULL COMMENT '姓名',
    username VARCHAR(32) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(64) NOT NULL COMMENT '密码',
    phone VARCHAR(11) DEFAULT NULL COMMENT '手机号',
    sex VARCHAR(2) DEFAULT NULL COMMENT '性别',
    id_number VARCHAR(18) DEFAULT NULL COMMENT '身份证号',
    status INT NOT NULL DEFAULT 1 COMMENT '状态 0禁用 1启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user BIGINT DEFAULT NULL COMMENT '创建人',
    update_user BIGINT DEFAULT NULL COMMENT '修改人'
) COMMENT='员工表';
```

插入测试员工：

```sql
INSERT INTO employee (
    name, username, password, phone, sex, id_number, status, create_user, update_user
) VALUES (
    'Admin', 'admin', '123456', '13800000000', '1', '110101199001010000', 1, 1, 1
);
```

## 配置说明

主配置文件：

```text
src/main/resources/application.properties
```

示例配置：

```properties
spring.application.name=sky-server
server.port=8088

spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/sky_take_out?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8
spring.datasource.username=root
spring.datasource.password=your_password

mybatis.configuration.map-underscore-to-camel-case=true

server.servlet.encoding.charset=UTF-8
server.servlet.encoding.enabled=true
server.servlet.encoding.force=true
```

注意：不要把真实数据库密码提交到公开仓库。

## 启动项目

```powershell
mvn spring-boot:run
```

默认端口：

```text
8088
```

访问基础接口：

```text
GET http://localhost:8088/hello
GET http://localhost:8088/status
```

## 测试登录接口

正确密码：

```powershell
Invoke-RestMethod -Uri "http://localhost:8088/admin/employee/login" -Method Post -ContentType "application/json" -Body '{"username":"admin","password":"123456"}' | ConvertTo-Json -Depth 5
```

错误密码：

```powershell
Invoke-RestMethod -Uri "http://localhost:8088/admin/employee/login" -Method Post -ContentType "application/json" -Body '{"username":"admin","password":"wrong"}' | ConvertTo-Json -Depth 5
```

错误响应示例：

```json
{
  "code": 0,
  "msg": "password error",
  "data": null
}
```

## 运行测试

运行全部测试：

```powershell
mvn test
```

只运行 simulator 契约测试：

```powershell
mvn -Dtest=SimulatorContractTest test
```

## Simulator 说明

`simulator` 目录用于模拟下游接口返回，并用单元测试验证上下游数据契约是否一致。

当前样板流程：

```text
UT 请求 /simulator/employees/1001
-> SimulatorController
-> DownstreamClient 调用 http://localhost:9000/employees/1001
-> MockRestServiceServer 拦截下游请求
-> 返回 mock JSON
-> 上游原样返回
-> UT 断言上游响应 JSON == 下游 mock JSON
```

mock 数据：

```text
simulator/src/test/resources/mock/downstream/employee-detail.json
```

## 学习笔记

学习记录放在：

```text
.md/session1.md
.md/session2.md
```

## 下一步计划

- 密码加密校验
- JWT token 生成
- 登录拦截器
- Swagger/OpenAPI 接口文档
- 员工管理 CRUD
- 分类和菜品模块

