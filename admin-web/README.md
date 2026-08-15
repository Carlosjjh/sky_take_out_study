# 苍穹外卖管理台

这是当前 Spring Boot 后端的本地管理端界面，连接云端 `sky-server` API。

## 启动

```powershell
npm.cmd install
npm.cmd run dev
```

浏览器打开 `http://localhost:5173`。登录页的服务地址默认为：

```text
http://8.163.103.170:30088
```

也可以在登录页切换到其他 API 地址。当前界面覆盖管理员登录、运营总览、分类管理和员工管理。

本地开发时，页面通过 Vite `/api` 代理访问云端接口，避免浏览器跨域限制；部署后端新镜像时可通过 `WEB_ALLOWED_ORIGIN` 配置允许的前端地址。
