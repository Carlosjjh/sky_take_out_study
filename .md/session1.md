# Session 1 Summary

## 目标

本次 session 的目标是把 Sky Take Out 后端项目从“能启动”推进到“具备基础接口分层”，并初步建立上下游 simulator/契约测试的样板。

## 1. 修复项目构建

一开始项目引入了 MyBatis/MySQL 依赖，但没有配置数据库连接，导致 Spring Boot 启动时创建 `DataSource` 失败。

处理方式：

- 将 Spring Boot 依赖调整到更稳定的 `3.3.5` 生态。
- 使用 `spring-boot-starter-web` 和 `spring-boot-starter-test`。
- 测试环境增加 H2 内存数据库，让 `mvn test` 不依赖本机 MySQL。
- 主运行环境临时关闭 `DataSourceAutoConfiguration`，方便先练 Web 接口。

当前主配置：

```properties
spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
server.port=8088
```

## 2. 跑通第一个 Web 接口

新增了 `HelloController`。

接口：

```text
GET /hello
```

返回：

```text
hello sky take out
```

核心理解：

```text
浏览器请求 /hello
-> Spring MVC 根据 @GetMapping 找到 Controller 方法
-> 执行方法
-> 返回字符串
```

## 3. 建立统一响应格式

新增了 `Result<T>`。

统一响应结构：

```json
{
  "code": 1,
  "msg": "success",
  "data": {}
}
```

意义：

- `code` 表示成功或失败。
- `msg` 表示提示信息。
- `data` 表示真正的业务数据。

后续所有接口都尽量返回：

```java
Result.success(data)
Result.error("error message")
```

## 4. 建立 simulator 样板

我们做了一个最小 simulator/契约测试样板，用于验证上下游 JSON 数据是否一致。

当前结构：

```text
simulator/
  src/test/java/com/sky/sky_server/simulator/
    SimulatorContractTest.java
    client/
    config/
    controller/
  src/test/resources/mock/downstream/
    employee-detail.json
```

核心数据流：

```text
UT 请求 /simulator/employees/1001
-> SimulatorController
-> DownstreamClient 调用 http://localhost:9000/employees/1001
-> MockRestServiceServer 拦截下游请求
-> 返回 mock JSON
-> 上游原样返回
-> UT 断言上游响应 JSON == 下游 mock JSON
```

运行方式：

```powershell
mvn test
```

只运行 simulator 测试：

```powershell
mvn -Dtest=SimulatorContractTest test
```

结论：simulator 现在作为样板保留，后续每做一个真实业务模块，再逐步补对应契约测试。

## 5. 回到主线：员工登录骨架

创建了基础包结构：

```text
dto
vo
service
service/impl
mapper
```

### DTO

新增：

```text
EmployeeLoginDTO
```

作用：接收前端登录 JSON。

前端请求：

```json
{
  "username": "admin",
  "password": "123456"
}
```

会被 Spring 转换成：

```text
EmployeeLoginDTO
```

### VO

新增：

```text
EmployeeLoginVO
```

作用：返回给前端的登录结果。

示例响应数据：

```json
{
  "id": 1,
  "userName": "admin",
  "name": "Admin",
  "token": "mock-token"
}
```

### Controller

新增：

```text
EmployeeController
```

接口：

```text
POST /admin/employee/login
```

### Service

新增：

```text
EmployeeService
EmployeeServiceImpl
```

目前登录逻辑还是 mock：

```text
收到 username/password
-> 构造 EmployeeLoginVO
-> 返回 mock-token
```

## 6. 当前员工登录数据流

当前链路已经从 Controller 直接处理，升级成了标准分层：

```text
PowerShell/Postman 发送 JSON
-> EmployeeController.login()
-> @RequestBody 转成 EmployeeLoginDTO
-> EmployeeService.login()
-> EmployeeServiceImpl 生成 EmployeeLoginVO
-> Result.success(employeeLoginVO)
-> 返回 JSON 给前端
```
前端 JSON
  -> DTO
  -> Controller
  -> Service
  -> Mapper
  -> 数据库
  -> Service
  -> VO
  -> Result<VO>
  -> 前端 JSON


测试命令：

```powershell
Invoke-RestMethod -Uri "http://localhost:8088/admin/employee/login" -Method Post -ContentType "application/json" -Body '{"username":"admin","password":"123456"}' | ConvertTo-Json -Depth 5
```

预期返回：

```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "id": 1,
    "userName": "admin",
    "name": "Admin",
    "token": "mock-token"
  }
}
```

## 7. 本次关键概念

- `@RestController`：把类注册成 Web 接口控制器。
- `@GetMapping` / `@PostMapping`：声明接口路径和 HTTP 方法。
- `@RequestBody`：把请求体 JSON 转成 Java 对象。
- DTO：前端传给后端的数据对象。负责接收前端数据
- VO：后端返回给前端的数据对象。负责返回前端数据
- Controller 负责接请求
  - 接请求
  - 拿参数
  - 调 Service
  - 返回结果
- Service：业务逻辑层。
  - 账号是否存在？
  - 密码是否正确？
  - 员工是否禁用？
  - 是否生成 token？
- Mapper 负责查数据库
  - 根据 username 查 employee 表。Mapper 不关心登录规则，只负责取数据。
- Entity：数据库表对应的 Java 对象
- `@Service`：把业务实现类交给 Spring 管理。
- 构造器注入：Controller 通过构造器拿到 Service。
- `Result<T>`：统一响应格式。
- simulator：用 mock JSON 模拟下游，验证上下游契约。

## 8. 下一步

下一步建议继续员工登录模块：

1. 新建 `Employee` 实体类。
2. 新建 `EmployeeMapper`。
3. 配置真实 MySQL 数据源。
4. 创建员工表。
5. 把 mock 登录改成从数据库根据 username 查询员工。
6. 校验密码。
7. 后续再接 JWT token。

