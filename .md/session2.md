# Session 2 Summary

## 目标

本次 session 的目标是把员工登录从 mock 返回，推进到真实数据库查询，并补上业务异常和统一错误响应。

## 1. 理解后端分层关系

本次重点梳理了几个核心层：

```text
前端 JSON
-> DTO
-> Controller
-> Service
-> Mapper
-> Database
-> Entity
-> Service
-> VO
-> Result<VO>
-> 前端 JSON
```

各层职责：

```text
DTO        接收前端传入数据
Controller 接收 HTTP 请求，调用 Service
Service    处理业务逻辑
Mapper     访问数据库
Entity     数据库表对应的 Java 对象
VO         返回给前端的数据对象
Result     统一响应包装
```

## 2. 新增 Employee 实体类

新增：

```text
src/main/java/com/sky/sky_server/entity/Employee.java
```

作用：对应数据库 `employee` 表。

主要字段：

```text
id
name
username
password
phone
sex
idNumber
status
createTime
updateTime
createUser
updateUser
```

使用 Lombok：

```java
@Data
```

让 Lombok 自动生成 getter/setter。

## 3. 新增 EmployeeMapper

新增：

```text
src/main/java/com/sky/sky_server/mapper/EmployeeMapper.java
```

核心方法：

```java
Employee getByUsername(String username);
```

SQL：

```sql
select * from employee where username = #{username}
```

意义：

```text
Service 传入 username
-> Mapper 查询 employee 表
-> 返回 Employee 对象
```

## 4. 配置 MySQL 和 MyBatis

修改：

```text
src/main/resources/application.properties
```

核心配置：

```properties
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/sky_take_out?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8
spring.datasource.username=root
spring.datasource.password=你的密码

mybatis.configuration.map-underscore-to-camel-case=true
```

其中：

```properties
mybatis.configuration.map-underscore-to-camel-case=true
```

用于支持数据库下划线字段到 Java 驼峰字段的自动映射：

```text
id_number   -> idNumber
create_time -> createTime
update_user -> updateUser
```

## 5. 建库建表

创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS sky_take_out
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

创建 `employee` 表：

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

## 6. 登录逻辑接入数据库

修改：

```text
EmployeeServiceImpl
```

从原来的 mock：

```text
直接构造 EmployeeLoginVO
```

改成：

```text
根据 username 查询数据库
-> 判断账号是否存在
-> 判断密码是否正确
-> 判断账号是否启用
-> 组装 EmployeeLoginVO
```

当前登录核心流程：

```text
EmployeeLoginDTO.username
-> EmployeeMapper.getByUsername(username)
-> Employee
-> 校验 password/status
-> EmployeeLoginVO
```

## 7. 新增 BusinessException

新增：

```text
src/main/java/com/sky/sky_server/exception/BusinessException.java
```

作用：表示业务异常。

例如：

```text
account not found
password error
account disabled
```

为什么不直接用 `RuntimeException`：

```text
RuntimeException 太泛，系统 bug、空指针、数据库异常都属于运行时异常。
BusinessException 专门表示业务规则不通过。
```

## 8. 新增 GlobalExceptionHandler

新增：

```text
src/main/java/com/sky/sky_server/handler/GlobalExceptionHandler.java
```

作用：统一处理业务异常。

效果：

```java
throw new BusinessException("password error");
```

会返回：

```json
{
  "code": 0,
  "msg": "password error",
  "data": null
}
```

而不是默认的 500 错误页。

## 9. 处理编码问题

PowerShell 中出现过中文乱码：

```text
å¯ç éè¯¯
```

原因：Windows PowerShell 显示编码和后端 UTF-8 响应不一致。

处理方式：

- `application.properties` 增加 UTF-8 配置。
- 业务错误信息暂时改成英文，避免学习阶段被编码干扰。

配置：

```properties
server.servlet.encoding.charset=UTF-8
server.servlet.encoding.enabled=true
server.servlet.encoding.force=true
```

## 10. 新增 MessageConstant

新增：

```text
src/main/java/com/sky/sky_server/constant/MessageConstant.java
```

作用：统一管理业务提示信息。

当前常量：

```java
public static final String ACCOUNT_NOT_FOUND = "account not found";
public static final String PASSWORD_ERROR = "password error";
public static final String ACCOUNT_DISABLED = "account disabled";
```

Service 中使用：

```java
throw new BusinessException(MessageConstant.PASSWORD_ERROR);
```

好处：

```text
避免魔法字符串
减少拼写错误
方便后续统一修改文案
```

## 11. 当前登录接口测试

接口：

```text
POST /admin/employee/login
```

正确密码：

```powershell
Invoke-RestMethod -Uri "http://localhost:8088/admin/employee/login" -Method Post -ContentType "application/json" -Body '{"username":"admin","password":"123456"}' | ConvertTo-Json -Depth 5
```

错误密码：

```powershell
Invoke-RestMethod -Uri "http://localhost:8088/admin/employee/login" -Method Post -ContentType "application/json" -Body '{"username":"admin","password":"wrong"}' | ConvertTo-Json -Depth 5
```

预期错误返回：

```json
{
  "code": 0,
  "msg": "password error",
  "data": null
}
```

## 12. 当前状态

员工登录已经完成第一版真实链路：

```text
前端 JSON
-> EmployeeLoginDTO
-> EmployeeController
-> EmployeeService
-> EmployeeMapper
-> MySQL employee 表
-> Employee
-> EmployeeLoginVO
-> Result
-> 前端 JSON
```

还没有做：

```text
密码加密
JWT token
登录拦截器
统一错误码枚举
前后端联调
```

## 13. 下一步建议

下一步可以做：

1. 把明文密码改成 MD5 校验。
2. 生成真正的 JWT token。
3. 增加登录拦截器。
4. 引入 Swagger/OpenAPI 文档。
5. 开始员工管理 CRUD。

