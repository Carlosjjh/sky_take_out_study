# 苍穹外卖管理台

这是当前 Spring Boot 后端的双端界面源码，包含用户点餐页与商家管理台。

## 启动

```powershell
npm.cmd install
npm.cmd run dev
```

本地开发时，用户端是 `http://localhost:5173`，管理台是 `http://localhost:5173/manage.html`。登录页的服务地址默认为：

```text
http://8.163.103.170:30088
```

部署到云端后，用户端是 `http://SERVER_PUBLIC_IP:30088`，管理台是 `http://SERVER_PUBLIC_IP:30088/manage.html`。当前界面覆盖用户菜单、购物车、订单提交，以及管理员登录、分类管理、菜品管理和员工管理。

本地开发时，页面通过 Vite `/api` 代理访问云端接口，避免浏览器跨域限制；部署后端新镜像时可通过 `WEB_ALLOWED_ORIGIN` 配置允许的前端地址。
