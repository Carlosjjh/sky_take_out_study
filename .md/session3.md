# Session 3 Summary

## 目标

本次 session 的目标是完成 GitHub 项目文档基础、回到员工登录主线，并开始从 mock token 过渡到真实 JWT token。

## 1. GitHub 和 README

项目已经接入 Git 管理，并配置了远程仓库：

```text
origin https://github.com/Carlosjjh/sky_take_out_study.git
```

当前分支：

```text
main
```

本地和远程已经同步。

新增：

```text
README.md
```

README 内容包括：

- 项目当前进度
- 技术栈
- 目录结构
- MySQL 建库建表 SQL
- 本地配置说明
- 启动方式
- 登录接口测试方式
- simulator 契约测试说明
- 学习笔记位置
- 下一步计划

注意事项：

```text
application.properties 中包含数据库密码，不建议提交到公开仓库。
```

当前 `.gitignore` 已忽略：

```text
src\main\resources\application.properties
```

## 2. 回到主线：密码 MD5 校验

之前登录逻辑是明文密码比较：

```text
前端传 123456
数据库存 123456
直接 equals 比较
```

本次升级为 MD5 比较：

```text
前端传 123456
-> 后端将 123456 转 MD5
-> 和数据库中的 MD5 密码比较
```

`123456` 的 MD5：

```text
e10adc3949ba59abbe56e057f20f883e
```

数据库更新语句：

```sql
UPDATE employee
SET password = 'e10adc3949ba59abbe56e057f20f883e'
WHERE username = 'admin';
```

Service 中使用：

```java
String password = DigestUtils.md5DigestAsHex(employeeLoginDTO.getPassword().getBytes());
```

然后比较：

```java
if (!employee.getPassword().equals(password)) {
    throw new BusinessException(MessageConstant.PASSWORD_ERROR);
}
```

当前登录密码链路：

```text
EmployeeLoginDTO.password
-> DigestUtils.md5DigestAsHex(...)
-> MD5 字符串
-> employee.password
-> equals 比较
```

## 3. 引入 JWT 依赖

为了把登录返回中的：

```java
employeeLoginVO.setToken("mock-token");
```

替换成真实 JWT，我们引入了 `jjwt`。

新增 Maven 依赖：

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
```

三个依赖的作用：

```text
jjwt-api      编码时使用的 API
jjwt-impl     JWT 功能实现
jjwt-jackson  JWT 内部 JSON 序列化支持
```

## 4. 新增 JwtUtil

新增：

```text
src/main/java/com/sky/sky_server/utils/JwtUtil.java
```

目标：

```text
生成 JWT token
解析 JWT token
```

核心方法：

```java
createJWT(String secretKey, long ttlMillis, Map<String, Object> claims)
parseJWT(String secretKey, String token)
```

`createJWT` 输入：

```text
secretKey  签名密钥
ttlMillis  过期时间，单位毫秒
claims     token 中携带的数据，比如 employeeId
```

`parseJWT` 输入：

```text
secretKey  签名密钥
token      前端传回来的 JWT
```

返回：

```text
Claims，也就是 token 中保存的数据
```

## 5. Deprecated API 问题

IDE 中出现横线，表示部分 `jjwt` API 已经过时：

```java
Jwts.parser()
.setSigningKey(...)
.signWith(signatureAlgorithm, byte[])
```

横线含义：

```text
Deprecated，不推荐继续使用。
不是编译错误，也不是不能运行。
```

推荐改成新写法：

```java
Keys.hmacShaKeyFor(...)
Jwts.parserBuilder()
```

推荐版 `JwtUtil` 使用：

```java
SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
```

生成 token：

```java
Jwts.builder()
    .setClaims(claims)
    .setExpiration(exp)
    .signWith(key, SignatureAlgorithm.HS256)
    .compact();
```

解析 token：

```java
Jwts.parserBuilder()
    .setSigningKey(key)
    .build()
    .parseClaimsJws(token)
    .getBody();
```

## 6. 密钥长度注意事项

使用 `HS256` 时，密钥长度至少需要 256 bit。

简单理解：

```text
secretKey 至少 32 个英文字符左右
```

后续可使用类似：

```text
sky-take-out-secret-key-1234567890
```

正式项目中密钥不应硬编码在代码里，后面可以放到配置文件或环境变量中。

## 7. 当前登录链路状态

目前登录已经具备：

```text
DTO 接收 username/password
-> Mapper 查询数据库员工
-> MD5 校验密码
-> 校验账号状态
-> 返回 EmployeeLoginVO
```

仍然待完成：

```text
把 mock-token 换成真实 JWT token
配置 JWT 密钥和过期时间
登录拦截器解析 token
```

## 8. 下一步

下一步建议：

1. 在配置文件中添加 JWT 配置：

```properties
sky.jwt.secret-key=sky-take-out-secret-key-1234567890
sky.jwt.ttl=7200000
```

2. 在 `EmployeeServiceImpl` 中生成 JWT：

```text
employee.id -> claims -> JwtUtil.createJWT(...) -> token
```

3. 替换：

```java
employeeLoginVO.setToken("mock-token");
```

为：

```java
employeeLoginVO.setToken(token);
```

