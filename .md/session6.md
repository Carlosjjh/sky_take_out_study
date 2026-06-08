# Session 6：员工状态修改与详情查询

## 这次做了什么

这次我们继续完善员工管理模块，完成了两个接口：

```text
1. 启用 / 禁用员工账号
2. 根据 id 查询员工详情
```

这两个接口都是员工管理后台的基础能力。

前端常见流程：

```text
员工列表
  -> 点击禁用 / 启用
  -> 调用状态修改接口

员工列表
  -> 点击编辑
  -> 根据 id 查询员工详情
  -> 回显到编辑表单
```

## 一、启用 / 禁用员工账号

接口：

```text
POST /admin/employee/status/{status}?id=员工id
```

示例：

```text
POST /admin/employee/status/0?id=2
POST /admin/employee/status/1?id=2
```

含义：

```text
status = 0：禁用
status = 1：启用
id：要修改的员工 id
```

## 二、Controller 层

路径：

```text
src/main/java/com/sky/sky_server/controller/EmployeeController.java
```

新增接口：

```java
@PostMapping("/status/{status}")
public Result<String> startOrStop(@PathVariable Integer status, @RequestParam Long id) {
    employeeService.startOrStop(status, id);
    return Result.success("update employee status success");
}
```

这里用了两个注解：

```java
@PathVariable
```

从路径里取参数：

```text
/status/0
```

```java
@RequestParam
```

从 URL 问号后面取参数：

```text
?id=2
```

最终得到：

```text
status = 0
id = 2
```

## 三、Service 层

路径：

```text
src/main/java/com/sky/sky_server/service/EmployeeService.java
```

新增方法：

```java
void startOrStop(Integer status, Long id);
```

实现类路径：

```text
src/main/java/com/sky/sky_server/service/impl/EmployeeServiceImpl.java
```

实现逻辑：

```java
@Override
public void startOrStop(Integer status, Long id) {
    Employee employee = new Employee();

    employee.setId(id);
    employee.setStatus(status);
    employee.setUpdateTime(LocalDateTime.now());
    employee.setUpdateUser(BaseContext.getCurrentId());

    employeeMapper.update(employee);
}
```

这里的设计重点：

```text
只设置要更新的字段。
```

比如这次只更新：

```text
id
status
update_time
update_user
```

其他字段保持不变。

## 四、Mapper 动态更新

路径：

```text
src/main/java/com/sky/sky_server/mapper/EmployeeMapper.java
```

新增通用更新方法：

```java
void update(Employee employee);
```

它使用 MyBatis 动态 SQL：

```text
哪个字段不为 null，就更新哪个字段。
```

这次修改状态时，只有这些字段不为空：

```text
id
status
updateTime
updateUser
```

所以最终 SQL 类似：

```sql
update employee
set status = ?, update_time = ?, update_user = ?
where id = ?
```

这个 `update(Employee employee)` 后面还能复用给：

```text
编辑员工信息
修改密码
更新手机号
```

## 五、405 问题排查

测试时遇到：

```text
405 Method Not Allowed
```

原因：

Controller 一开始写的是：

```java
@GetMapping("/status/{status}")
```

但测试命令用的是：

```powershell
-Method Post
```

所以 Spring 的意思是：

```text
路径找到了，但是请求方法不匹配。
```

修复：

```java
@PostMapping("/status/{status}")
```

状态码可以这样记：

```text
401：token 没通过，没权限
404：路径找不到
405：路径找到了，但是 HTTP 方法不对
```

## 六、根据 id 查询员工详情

接口：

```text
GET /admin/employee/{id}
```

示例：

```text
GET /admin/employee/2
```

用途：

```text
编辑员工信息前，先查询员工详情，用于表单回显。
```

## 七、详情接口的 Controller

路径：

```text
src/main/java/com/sky/sky_server/controller/EmployeeController.java
```

新增接口：

```java
@GetMapping("/{id}")
public Result<EmployeePageVO> getById(@PathVariable Long id) {
    EmployeePageVO employeePageVO = employeeService.getById(id);
    return Result.success(employeePageVO);
}
```

这里继续返回 `EmployeePageVO`。

原因：

```text
详情接口也不能返回 password。
```

## 八、详情接口的 Service

接口：

```java
EmployeePageVO getById(Long id);
```

实现：

```java
@Override
public EmployeePageVO getById(Long id) {
    return employeeMapper.getById(id);
}
```

这里 Service 暂时没有复杂业务逻辑，只负责调用 Mapper。

## 九、详情接口的 Mapper

路径：

```text
src/main/java/com/sky/sky_server/mapper/EmployeeMapper.java
```

查询语句：

```java
@Select("select id, name, username, phone, sex, id_number, status, create_time, update_time, create_user, update_user from employee where id = #{id}")
EmployeePageVO getById(Long id);
```

注意：

```text
这里没有查询 password。
```

这是后端接口安全意识：

```text
密码即使加密了，也不要返回给前端。
```

## 十、测试命令

启动项目：

```powershell
mvn spring-boot:run
```

登录拿 token：

```powershell
$loginResult = Invoke-RestMethod -Uri "http://localhost:8088/admin/employee/login" -Method Post -ContentType "application/json" -Body '{"username":"admin","password":"123456"}'
$token = $loginResult.data.token
```

禁用 id=2：

```powershell
Invoke-RestMethod -Uri "http://localhost:8088/admin/employee/status/0?id=2" -Method Post -Headers @{token=$token} | ConvertTo-Json -Depth 5
```

启用 id=2：

```powershell
Invoke-RestMethod -Uri "http://localhost:8088/admin/employee/status/1?id=2" -Method Post -Headers @{token=$token} | ConvertTo-Json -Depth 5
```

查询 id=2 的员工详情：

```powershell
Invoke-RestMethod -Uri "http://localhost:8088/admin/employee/2" -Method Get -Headers @{token=$token} | ConvertTo-Json -Depth 5
```

分页查看状态是否变化：

```powershell
Invoke-RestMethod -Uri "http://localhost:8088/admin/employee/page?page=1&pageSize=10" -Method Get -Headers @{token=$token} | ConvertTo-Json -Depth 5
```

## 十一、当前进度

员工模块当前已经完成：

```text
登录
JWT 鉴权
新增员工
分页查询
分页结果隐藏 password
启用 / 禁用员工
根据 id 查询员工详情
```

下一步建议：

```text
编辑员工信息
```

它会复用这次写好的：

```java
employeeMapper.update(employee)
```

