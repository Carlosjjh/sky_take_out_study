<script setup>
import { computed, onMounted, reactive, ref } from 'vue'

const DEFAULT_API = 'http://8.163.103.170:30088'
const LOCAL_PROXY = '/api'
const savedApi = localStorage.getItem('sky-api-base')
const apiBase = ref(savedApi || import.meta.env.VITE_API_BASE || DEFAULT_API)
const token = ref(localStorage.getItem('sky-admin-token') || '')
const currentUser = ref(localStorage.getItem('sky-admin-user') || 'admin')
const activeView = ref('overview')
const loginError = ref('')
const loading = ref(false)
const toast = ref({ visible: false, message: '', type: 'success' })
const categories = ref([])
const categoryTotal = ref(0)
const employees = ref([])
const employeeTotal = ref(0)
const categoryQuery = reactive({ name: '', type: '', page: 1, pageSize: 8 })
const employeeQuery = reactive({ name: '', page: 1, pageSize: 8 })
const categoryForm = reactive({ name: '', type: 1, sort: 0 })
const showCategoryModal = ref(false)
const profileId = ref('')

const navItems = [
  { key: 'overview', label: '运营总览', icon: '◈' },
  { key: 'categories', label: '分类管理', icon: '▦' },
  { key: 'employees', label: '员工管理', icon: '◎' }
]

const categoryPages = computed(() => Math.max(1, Math.ceil(categoryTotal.value / categoryQuery.pageSize)))
const employeePages = computed(() => Math.max(1, Math.ceil(employeeTotal.value / employeeQuery.pageSize)))

function showToast(message, type = 'success') {
  toast.value = { visible: true, message, type }
  window.clearTimeout(showToast.timer)
  showToast.timer = window.setTimeout(() => (toast.value.visible = false), 2800)
}

function normalizeBase(value) {
  return value.trim().replace(/\/$/, '')
}

async function request(path, options = {}) {
  const headers = new Headers(options.headers || {})
  headers.set('Accept', 'application/json')
  if (options.body) headers.set('Content-Type', 'application/json')
  if (token.value) headers.set('token', token.value)
  const requestBase = normalizeBase(apiBase.value) === DEFAULT_API ? LOCAL_PROXY : normalizeBase(apiBase.value)
  const response = await fetch(`${requestBase}${path}`, { ...options, headers })
  if (response.status === 401) {
    logout(false)
    throw new Error('登录已过期，请重新登录')
  }
  const body = await response.json().catch(() => ({}))
  if (!response.ok || body.code !== 1) throw new Error(body.msg || `请求失败（${response.status}）`)
  return body.data
}

const loginForm = reactive({ username: 'admin', password: '123456' })

async function login() {
  loginError.value = ''
  loading.value = true
  try {
    const data = await request('/admin/employee/login', {
      method: 'POST',
      body: JSON.stringify(loginForm)
    })
    token.value = data.token
    currentUser.value = data.userName || data.name || loginForm.username
    localStorage.setItem('sky-admin-token', token.value)
    localStorage.setItem('sky-admin-user', currentUser.value)
    localStorage.setItem('sky-api-base', normalizeBase(apiBase.value))
    await loadOverview()
  } catch (error) {
    loginError.value = error.message
  } finally {
    loading.value = false
  }
}

function logout(showMessage = true) {
  token.value = ''
  localStorage.removeItem('sky-admin-token')
  localStorage.removeItem('sky-admin-user')
  if (showMessage) showToast('已退出管理台')
}

async function loadOverview() {
  await Promise.all([loadCategories(), loadEmployees(), loadProfile()])
}

async function loadProfile() {
  try {
    profileId.value = await request('/admin/employee/profile')
  } catch (error) {
    showToast(error.message, 'error')
  }
}

async function loadCategories() {
  try {
    const params = new URLSearchParams({ page: categoryQuery.page, pageSize: categoryQuery.pageSize })
    if (categoryQuery.name) params.set('name', categoryQuery.name)
    if (categoryQuery.type) params.set('type', categoryQuery.type)
    const data = await request(`/admin/category/page?${params}`)
    categories.value = data.records || []
    categoryTotal.value = data.total || 0
  } catch (error) {
    showToast(error.message, 'error')
  }
}

async function loadEmployees() {
  try {
    const params = new URLSearchParams({ page: employeeQuery.page, pageSize: employeeQuery.pageSize })
    if (employeeQuery.name) params.set('name', employeeQuery.name)
    const data = await request(`/admin/employee/page?${params}`)
    employees.value = data.records || []
    employeeTotal.value = data.total || 0
  } catch (error) {
    showToast(error.message, 'error')
  }
}

async function createCategory() {
  if (!categoryForm.name.trim()) return showToast('请输入分类名称', 'error')
  try {
    await request('/admin/category', {
      method: 'POST',
      body: JSON.stringify({ ...categoryForm, name: categoryForm.name.trim() })
    })
    showCategoryModal.value = false
    Object.assign(categoryForm, { name: '', type: 1, sort: 0 })
    categoryQuery.page = 1
    await loadCategories()
    showToast('分类已创建')
  } catch (error) {
    showToast(error.message, 'error')
  }
}

async function toggleCategory(category) {
  try {
    await request(`/admin/category/status/${category.status ? 0 : 1}?id=${category.id}`, { method: 'POST' })
    await loadCategories()
    showToast(category.status ? '分类已停用' : '分类已启用')
  } catch (error) {
    showToast(error.message, 'error')
  }
}

async function toggleEmployee(employee) {
  try {
    await request(`/admin/employee/status/${employee.status ? 0 : 1}?id=${employee.id}`, { method: 'POST' })
    await loadEmployees()
    showToast(employee.status ? '员工已停用' : '员工已启用')
  } catch (error) {
    showToast(error.message, 'error')
  }
}

function switchView(view) {
  activeView.value = view
  if (view === 'categories') loadCategories()
  if (view === 'employees') loadEmployees()
}

function resetCategoryQuery() {
  Object.assign(categoryQuery, { name: '', type: '', page: 1 })
  loadCategories()
}

function resetEmployeeQuery() {
  Object.assign(employeeQuery, { name: '', page: 1 })
  loadEmployees()
}

onMounted(() => {
  if (token.value) loadOverview()
})
</script>

<template>
  <main class="app-shell" :class="{ 'is-authenticated': token }">
    <section v-if="!token" class="login-page">
      <div class="login-visual">
        <div class="visual-kicker">SKY TAKE OUT / ADMIN</div>
        <h1>把每一次经营决策，<br /><span>落到今天的订单上。</span></h1>
        <p>苍穹外卖管理台 · 云端服务 {{ normalizeBase(apiBase) }}</p>
        <div class="visual-rule"></div>
        <div class="visual-meta"><span>SPRING BOOT API</span><span>K3S READY</span><span>V1.0</span></div>
      </div>
      <form class="login-panel" @submit.prevent="login">
        <div class="brand-mark"><span>☼</span><div><strong>苍穹外卖</strong><small>运营管理台</small></div></div>
        <div class="login-heading"><span class="eyebrow">WELCOME BACK</span><h2>登录管理台</h2><p>使用管理员账号进入云端运营空间</p></div>
        <label>服务地址<input v-model="apiBase" autocomplete="url" /></label>
        <label>账号<input v-model="loginForm.username" autocomplete="username" placeholder="admin" /></label>
        <label>密码<input v-model="loginForm.password" autocomplete="current-password" type="password" placeholder="••••••" /></label>
        <div v-if="loginError" class="form-error">{{ loginError }}</div>
        <button class="primary-button login-button" :disabled="loading">{{ loading ? '正在连接...' : '进入管理台' }} <span>→</span></button>
        <p class="login-note">当前为学习环境，默认账号为 admin / 123456</p>
      </form>
    </section>

    <template v-else>
      <aside class="sidebar">
        <div class="brand-mark sidebar-brand"><span>☼</span><div><strong>苍穹外卖</strong><small>运营管理台</small></div></div>
        <div class="sidebar-label">WORKSPACE</div>
        <nav>
          <button v-for="item in navItems" :key="item.key" :class="{ active: activeView === item.key }" @click="switchView(item.key)"><span class="nav-icon">{{ item.icon }}</span>{{ item.label }}<span v-if="activeView === item.key" class="nav-line"></span></button>
        </nav>
        <div class="sidebar-bottom"><div class="connection-dot"><i></i><span>云端服务在线</span></div><div class="server-caption">{{ normalizeBase(apiBase) }}</div></div>
      </aside>

      <section class="workspace">
        <header class="topbar"><div><span class="topbar-context">ADMIN CONSOLE</span><span class="topbar-slash">/</span><span>{{ navItems.find(item => item.key === activeView)?.label }}</span></div><div class="topbar-actions"><button class="icon-button" title="刷新数据" @click="loadOverview">↻</button><div class="user-chip"><span class="avatar">{{ currentUser.slice(0, 1).toUpperCase() }}</span><span>{{ currentUser }}</span></div><button class="logout-button" @click="logout">退出</button></div></header>
        <div class="content">
          <section v-if="activeView === 'overview'" class="view-section">
            <div class="page-intro"><div><span class="eyebrow">THURSDAY, 13 AUGUST 2026</span><h1>早上好，{{ currentUser }}</h1><p>这是你的运营工作台，关键数据正在从云端同步。</p></div><button class="secondary-button" @click="loadOverview">↻ 刷新数据</button></div>
            <div class="stats-grid"><article class="stat-card stat-primary"><div class="stat-label">分类总数</div><div class="stat-value">{{ categoryTotal }}</div><div class="stat-foot">当前管理中的分类</div><span class="stat-symbol">▦</span></article><article class="stat-card"><div class="stat-label">员工总数</div><div class="stat-value">{{ employeeTotal }}</div><div class="stat-foot">后台账号与权限</div><span class="stat-symbol">◎</span></article><article class="stat-card"><div class="stat-label">服务状态</div><div class="stat-value status-value"><i></i>在线</div><div class="stat-foot">K3s / Spring Boot</div><span class="stat-symbol">⌁</span></article></div>
            <div class="overview-grid"><article class="panel recent-panel"><div class="panel-heading"><div><span class="eyebrow">QUICK ACCESS</span><h2>常用操作</h2></div></div><div class="quick-actions"><button @click="switchView('categories')"><span>▦</span><strong>分类管理</strong><small>新增、查询与启停分类</small><b>→</b></button><button @click="switchView('employees')"><span>◎</span><strong>员工管理</strong><small>查看员工状态与信息</small><b>→</b></button></div></article><article class="panel status-panel"><div class="panel-heading"><div><span class="eyebrow">SYSTEM</span><h2>服务连接</h2></div><span class="online-badge"><i></i>正常</span></div><div class="service-row"><span>API Gateway</span><b>{{ normalizeBase(apiBase) }}</b></div><div class="service-row"><span>当前会话</span><b>{{ profileId || '已验证' }}</b></div><div class="service-row"><span>数据同步</span><b class="green-text">实时</b></div></article></div>
          </section>

          <section v-else-if="activeView === 'categories'" class="view-section"><div class="page-intro compact"><div><span class="eyebrow">CATALOG / CATEGORIES</span><h1>分类管理</h1><p>维护菜品与套餐分类，让菜单结构清晰可控。</p></div><button class="primary-button" @click="showCategoryModal = true">＋ 新建分类</button></div><article class="panel data-panel"><div class="filter-bar"><div class="filter-field"><span>搜索</span><input v-model="categoryQuery.name" placeholder="输入分类名称" @keyup.enter="categoryQuery.page = 1; loadCategories()" /></div><select v-model="categoryQuery.type" @change="categoryQuery.page = 1; loadCategories()"><option value="">全部类型</option><option value="1">菜品分类</option><option value="2">套餐分类</option></select><button class="secondary-button" @click="categoryQuery.page = 1; loadCategories()">查询</button><button class="text-button" @click="resetCategoryQuery">重置</button></div><div class="table-wrap"><table><thead><tr><th>分类名称</th><th>类型</th><th>排序</th><th>状态</th><th>更新时间</th><th class="align-right">操作</th></tr></thead><tbody><tr v-for="category in categories" :key="category.id"><td><div class="primary-cell"><span class="table-avatar orange">{{ category.name.slice(0, 1) }}</span><strong>{{ category.name }}</strong></div></td><td>{{ category.type === 1 ? '菜品分类' : '套餐分类' }}</td><td>{{ category.sort }}</td><td><span class="status-pill" :class="category.status ? 'enabled' : 'disabled'"><i></i>{{ category.status ? '启用' : '停用' }}</span></td><td>{{ category.updateTime || category.createTime || '—' }}</td><td class="align-right"><button class="table-action" @click="toggleCategory(category)">{{ category.status ? '停用' : '启用' }}</button></td></tr><tr v-if="!categories.length"><td colspan="6" class="empty-row"><span>⌁</span><strong>还没有分类</strong><small>创建第一个分类，开始整理你的菜单。</small></td></tr></tbody></table></div><div class="table-footer"><span>共 {{ categoryTotal }} 条记录</span><div class="pagination"><button :disabled="categoryQuery.page <= 1" @click="categoryQuery.page--; loadCategories()">‹</button><b>{{ categoryQuery.page }} / {{ categoryPages }}</b><button :disabled="categoryQuery.page >= categoryPages" @click="categoryQuery.page++; loadCategories()">›</button></div></div></article></section>

          <section v-else class="view-section"><div class="page-intro compact"><div><span class="eyebrow">TEAM / EMPLOYEES</span><h1>员工管理</h1><p>查看后台员工状态与基础信息。</p></div></div><article class="panel data-panel"><div class="filter-bar"><div class="filter-field"><span>搜索</span><input v-model="employeeQuery.name" placeholder="输入姓名" @keyup.enter="employeeQuery.page = 1; loadEmployees()" /></div><button class="secondary-button" @click="employeeQuery.page = 1; loadEmployees()">查询</button><button class="text-button" @click="resetEmployeeQuery">重置</button></div><div class="table-wrap"><table><thead><tr><th>员工</th><th>账号</th><th>手机号</th><th>状态</th><th>创建时间</th><th class="align-right">操作</th></tr></thead><tbody><tr v-for="employee in employees" :key="employee.id"><td><div class="primary-cell"><span class="table-avatar blue">{{ employee.name?.slice(0, 1) }}</span><strong>{{ employee.name }}</strong></div></td><td>{{ employee.username }}</td><td>{{ employee.phone || '—' }}</td><td><span class="status-pill" :class="employee.status ? 'enabled' : 'disabled'"><i></i>{{ employee.status ? '启用' : '停用' }}</span></td><td>{{ employee.createTime || '—' }}</td><td class="align-right"><button class="table-action" @click="toggleEmployee(employee)">{{ employee.status ? '停用' : '启用' }}</button></td></tr><tr v-if="!employees.length"><td colspan="6" class="empty-row"><span>◎</span><strong>没有员工记录</strong><small>请先在管理端创建员工账号。</small></td></tr></tbody></table></div><div class="table-footer"><span>共 {{ employeeTotal }} 条记录</span><div class="pagination"><button :disabled="employeeQuery.page <= 1" @click="employeeQuery.page--; loadEmployees()">‹</button><b>{{ employeeQuery.page }} / {{ employeePages }}</b><button :disabled="employeeQuery.page >= employeePages" @click="employeeQuery.page++; loadEmployees()">›</button></div></div></article></section>
        </div>
      </section>
    </template>

    <div v-if="showCategoryModal" class="modal-backdrop" @click.self="showCategoryModal = false"><form class="modal" @submit.prevent="createCategory"><div class="modal-heading"><div><span class="eyebrow">NEW CATEGORY</span><h2>新建分类</h2></div><button type="button" class="close-button" @click="showCategoryModal = false">×</button></div><label>分类名称<input v-model="categoryForm.name" maxlength="32" placeholder="例如：热菜" autofocus /></label><div class="form-grid"><label>分类类型<select v-model.number="categoryForm.type"><option :value="1">菜品分类</option><option :value="2">套餐分类</option></select></label><label>排序<input v-model.number="categoryForm.sort" type="number" min="0" /></label></div><div class="modal-actions"><button type="button" class="secondary-button" @click="showCategoryModal = false">取消</button><button class="primary-button">保存分类</button></div></form></div>
    <div v-if="toast.visible" class="toast" :class="toast.type"><span>{{ toast.type === 'error' ? '!' : '✓' }}</span>{{ toast.message }}</div>
  </main>
</template>
