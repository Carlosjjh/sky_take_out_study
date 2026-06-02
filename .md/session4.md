# Session 4：JWT 登录拦截器

## 这次做了什么

这次我们完成了后台登录认证的基本闭环。

登录流程：

```text
前端 POST /admin/employee/login
  -> EmployeeController
  -> EmployeeServiceImpl
  -> EmployeeMapper
  -> 查询 MySQL employee 表
  -> JwtUtil 生成 token
  -> 返回给前端
```

访问受保护接口的流程：

```text
前端 GET /admin/employee/profile
  -> JwtTokenAdminInterceptor 先拦截
  -> 从请求头读取 token
  -> JwtUtil 解析 token
  -> token 正确：放行到 Controller
  -> token 缺失或错误：返回 401
```

## 涉及的文件

### 1. JwtUtil

路径：

```text
src/main/java/com/sky/sky_server/utils/JwtUtil.java
```

作用：

- 生成 JWT token。
- 解析 JWT token。
- 使用 `application.properties` 里的 `sky.jwt.secret-key`。
- 使用 `application.properties` 里的 `sky.jwt.ttl` 设置过期时间。

关键方法：

```java
createJWT(secretKey, ttlMillis, claims)
parseJWT(secretKey, token)
```

### 2. EmployeeServiceImpl

路径：

```text
src/main/java/com/sky/sky_server/service/impl/EmployeeServiceImpl.java
```

主要变化：

- 登录成功后不再返回 `mock-token`。
- 登录成功后返回真正的 JWT token。

我们往 token 里放了当前员工 id：

```java
claims.put("employeeId", employee.getId());
```

注意：

```text
生成 token 时的 key 和解析 token 时的 key 必须完全一致。
```

比如：

```text
employeeId  是正确的
employeeID  是错误的
```

Java 里的字符串区分大小写。

### 3. JwtTokenAdminInterceptor

路径：

```text
src/main/java/com/sky/sky_server/interceptor/JwtTokenAdminInterceptor.java
```

作用：

- 在 Controller 执行之前先检查请求。
- 从请求头里取出 token。
- 解析 token。
- token 正确就放行。
- token 错误或没有 token 就返回 `401`。

读取 token：

```java
String token = request.getHeader("token");
```

解析 token：

```java
Claims claims = JwtUtil.parseJWT(secretKey, token);
```

取出员工 id：

```java
Long employeeId = Long.valueOf(claims.get("employeeId").toString());
```

你可以把拦截器理解成后端的“门卫”：

```text
请求进 Controller 前，先检查有没有合法 token。
```

### 4. WebMvcConfiguration

路径：

```text
src/main/java/com/sky/sky_server/config/WebMvcConfiguration.java
```

作用：

- 把 JWT 拦截器注册到 Spring MVC。
- 指定哪些路径要拦截。
- 指定哪些路径要放行。

拦截后台接口：

```java
addPathPatterns("/admin/**")
```

放行登录接口：

```java
excludePathPatterns("/admin/employee/login")
```

为什么登录接口要放行：

```text
如果登录接口也需要 token，就会变成“没登录不能登录”。
```

### 5. EmployeeController

路径：

```text
src/main/java/com/sky/sky_server/controller/EmployeeController.java
```

我们给整个 Controller 加了统一路径前缀：

```java
@RequestMapping("/admin/employee")
```

所以方法上只需要写短路径：

```java
@PostMapping("/login")
@GetMapping("/profile")
```

最终接口路径是：

```text
POST /admin/employee/login
GET  /admin/employee/profile
```

## 排查过的问题

### 1. Maven 命令拼错

错误命令：

```powershell
mvn spring-bot:run
```

正确命令：

```powershell
mvn spring-boot:run
```

`spring-boot` 是 Spring Boot Maven 插件的名字。

### 2. 测试配置缺少 JWT 参数

`mvn test` 读取的是：

```text
src/test/resources/application.properties
```

所以测试配置里也要有：

```properties
sky.jwt.secret-key=sky-take-out-secret-key-1234567890
sky.jwt.ttl=7200000
```

否则 Spring 启动测试环境时找不到：

```text
${sky.jwt.secret-key}
${sky.jwt.ttl}
```

### 3. 带了 token 还是 401

原因是 token 里的字段名写错了。

生成 token 时写的是：

```java
claims.put("employeeId", employee.getId());
```

解析 token 时之前写成了：

```java
claims.get("employeeID")
```

这里大小写不一致，所以取不到值。

修复后：

```java
claims.get("employeeId")
```

### 4. 401 变成 404

这个其实是进步。

区别是：

```text
401：请求被拦截器挡住了，说明 token 没通过。
404：请求通过拦截器了，但 Controller 里没有匹配到路径。
```

当时的原因：

```java
@GetMapping("/profile")
```

实际路径是：

```text
/profile
```

但我们访问的是：

```text
/admin/employee/profile
```

解决方式：

在类上加统一前缀：

```java
@RequestMapping("/admin/employee")
```

然后保留：

```java
@GetMapping("/profile")
```

## 测试命令

启动项目：

```powershell
mvn spring-boot:run
```

登录并获取 token：

```powershell
$loginResult = Invoke-RestMethod -Uri "http://localhost:8088/admin/employee/login" -Method Post -ContentType "application/json" -Body '{"username":"admin","password":"123456"}'
$token = $loginResult.data.token
$token
```

不带 token 访问受保护接口：

```powershell
Invoke-RestMethod -Uri "http://localhost:8088/admin/employee/profile" -Method Get
```

预期结果：

```text
401 Unauthorized
```

带 token 访问受保护接口：

```powershell
Invoke-RestMethod -Uri "http://localhost:8088/admin/employee/profile" -Method Get -Headers @{token=$token}
```

预期结果：

```text
访问成功
```

## 当前进度

已经完成：

- 登录成功后生成真实 JWT token。
- 后台接口注册了 JWT 拦截器。
- `/admin/employee/login` 被放行。
- `/admin/**` 其他接口需要 token。
- 不带 token 会返回 `401`。
- 带正确 token 可以访问受保护接口。

## 下一步

建议下一步做：

```text
用 ThreadLocal 保存当前登录员工 id。
```

原因：

后面做新增员工、修改员工、公共字段填充时，需要知道：

```text
当前是谁在操作？
```

比如数据库字段：

```text
create_user
update_user
```

这些字段就可以用当前登录员工 id 来填。
