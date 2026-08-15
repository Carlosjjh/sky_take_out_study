<script setup>
import { computed, onMounted, reactive, ref } from 'vue'

const apiPrefix = window.location.port === '30088' ? '' : '/api'
const categories = ref([])
const dishes = ref([])
const activeCategory = ref(null)
const loading = ref(true)
const error = ref('')
const cart = ref(JSON.parse(localStorage.getItem('sky-customer-cart') || '[]'))
const showOrder = ref(false)
const orderMessage = ref('')
const submitting = ref(false)
const orderForm = reactive({ customerName: '', phone: '', address: '' })

const fallbackImages = [
  'https://images.unsplash.com/photo-1547592180-85f173990554?auto=format&fit=crop&w=900&q=80',
  'https://images.unsplash.com/photo-1551218808-94e220e084d2?auto=format&fit=crop&w=900&q=80',
  'https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?auto=format&fit=crop&w=900&q=80',
  'https://images.unsplash.com/photo-1512058564366-18510be2db19?auto=format&fit=crop&w=900&q=80'
]

const cartCount = computed(() => cart.value.reduce((sum, item) => sum + item.number, 0))
const cartTotal = computed(() => cart.value.reduce((sum, item) => sum + Number(item.price) * item.number, 0))

function saveCart() {
  localStorage.setItem('sky-customer-cart', JSON.stringify(cart.value))
}

async function request(path, options = {}) {
  const headers = new Headers(options.headers || {})
  headers.set('Accept', 'application/json')
  if (options.body) headers.set('Content-Type', 'application/json')
  const response = await fetch(`${apiPrefix}${path}`, { ...options, headers })
  const body = await response.json().catch(() => ({}))
  if (!response.ok || body.code !== 1) throw new Error(body.msg || '服务暂时不可用')
  return body.data
}

async function loadMenu() {
  loading.value = true
  error.value = ''
  try {
    categories.value = await request('/user/category/list?type=1')
    if (categories.value.length) await selectCategory(categories.value[0])
    else dishes.value = []
  } catch (loadError) {
    error.value = loadError.message
  } finally {
    loading.value = false
  }
}

async function selectCategory(category) {
  activeCategory.value = category.id
  try {
    dishes.value = await request(`/user/dish/list?categoryId=${category.id}`)
  } catch (loadError) {
    error.value = loadError.message
  }
}

function addDish(dish) {
  const item = cart.value.find(cartItem => cartItem.dishId === dish.id)
  if (item) item.number += 1
  else cart.value.push({ dishId: dish.id, name: dish.name, price: Number(dish.price), image: dish.image, number: 1 })
  saveCart()
}

function changeQuantity(item, delta) {
  item.number += delta
  if (item.number <= 0) cart.value = cart.value.filter(cartItem => cartItem.dishId !== item.dishId)
  saveCart()
}

function imageFor(dish, index) {
  return dish.image || fallbackImages[index % fallbackImages.length]
}

async function submitOrder() {
  if (!cart.value.length) return
  submitting.value = true
  orderMessage.value = ''
  try {
    const orderId = await request('/user/order', {
      method: 'POST',
      body: JSON.stringify({ ...orderForm, items: cart.value.map(item => ({ dishId: item.dishId, number: item.number })) })
    })
    cart.value = []
    saveCart()
    showOrder.value = false
    orderMessage.value = `订单 #${orderId} 已提交，商家会尽快为你准备。`
    Object.assign(orderForm, { customerName: '', phone: '', address: '' })
  } catch (submitError) {
    orderMessage.value = submitError.message
  } finally {
    submitting.value = false
  }
}

onMounted(loadMenu)
</script>

<template>
  <main class="customer-app">
    <header class="customer-header"><a class="customer-brand" href="/"><span>☼</span><strong>苍穹外卖</strong></a><nav><a href="#menu">菜单</a><a href="#cart">购物车 <b>{{ cartCount }}</b></a><a class="manage-link" href="/manage.html">商家管理</a></nav></header>
    <section class="customer-hero"><div><span>ONLINE TAKEOUT</span><h1>今天想吃点<br /><em>热乎的。</em></h1><p>现做菜品，在线下单。选择喜欢的菜品，填写地址即可提交订单。</p><a href="#menu" class="hero-action">开始点餐 <i>↓</i></a></div><div class="hero-image"><img :src="fallbackImages[0]" alt="热腾腾的餐食" /></div></section>
    <section id="menu" class="menu-section"><div class="menu-heading"><div><span>MENU</span><h2>今日菜单</h2></div><p>只展示当前上架的菜品</p></div><div v-if="error" class="customer-error">{{ error }} <button @click="loadMenu">重试</button></div><div v-else-if="loading" class="menu-loading">正在加载菜单...</div><template v-else><div class="category-tabs"><button v-for="category in categories" :key="category.id" :class="{ active: activeCategory === category.id }" @click="selectCategory(category)">{{ category.name }}</button></div><div v-if="dishes.length" class="dish-grid"><article v-for="(dish, index) in dishes" :key="dish.id" class="customer-dish"><img :src="imageFor(dish, index)" :alt="dish.name" /><div class="dish-copy"><span>{{ activeCategory ? categories.find(category => category.id === activeCategory)?.name : '今日推荐' }}</span><h3>{{ dish.name }}</h3><p>{{ dish.description || '新鲜现做，欢迎品尝。' }}</p><div><b>￥{{ Number(dish.price).toFixed(2) }}</b><button @click="addDish(dish)" :aria-label="`加入购物车：${dish.name}`">＋</button></div></div></article></div><div v-else-if="categories.length" class="menu-empty"><strong>这个分类暂时没有上架菜品</strong><p>请稍后再来，或选择其他分类。</p></div><div v-else class="menu-empty"><strong>商家还没有创建菜单</strong><p>请先进入商家管理，创建菜品分类并新增菜品。</p><a href="/manage.html">进入商家管理</a></div></template></section>
    <section id="cart" class="cart-section"><div class="cart-summary"><span>YOUR CART</span><h2>购物车</h2><p>{{ cartCount ? `已选择 ${cartCount} 件菜品` : '还没有选择菜品' }}</p></div><div class="cart-panel"><div v-if="cart.length" class="cart-items"><div v-for="item in cart" :key="item.dishId" class="cart-item"><img :src="item.image || fallbackImages[1]" :alt="item.name" /><div><strong>{{ item.name }}</strong><small>￥{{ item.price.toFixed(2) }}</small></div><div class="quantity"><button @click="changeQuantity(item, -1)">−</button><b>{{ item.number }}</b><button @click="changeQuantity(item, 1)">＋</button></div></div></div><div v-else class="cart-empty">从菜单中选择菜品，它们会出现在这里。</div><div class="cart-footer"><div><span>合计</span><strong>￥{{ cartTotal.toFixed(2) }}</strong></div><button :disabled="!cart.length" @click="showOrder = true">去结算 <span>→</span></button></div></div></section>
    <footer>苍穹外卖 · 学习演示项目 <a href="/manage.html">进入商家管理台</a></footer>
    <div v-if="showOrder" class="order-backdrop" @click.self="showOrder = false"><form class="order-modal" @submit.prevent="submitOrder"><div><span>CHECKOUT</span><h2>填写配送信息</h2></div><button type="button" class="modal-close" @click="showOrder = false">×</button><label>姓名<input v-model="orderForm.customerName" required maxlength="32" placeholder="收餐人姓名" /></label><label>手机号<input v-model="orderForm.phone" required pattern="1\d{10}" placeholder="11 位手机号" /></label><label>配送地址<input v-model="orderForm.address" required maxlength="128" placeholder="详细地址" /></label><div class="checkout-total"><span>共 {{ cartCount }} 件</span><strong>￥{{ cartTotal.toFixed(2) }}</strong></div><button class="submit-order" :disabled="submitting">{{ submitting ? '正在提交...' : '提交订单' }}</button></form></div>
    <div v-if="orderMessage" class="order-toast">{{ orderMessage }}<button @click="orderMessage = ''">×</button></div>
  </main>
</template>
