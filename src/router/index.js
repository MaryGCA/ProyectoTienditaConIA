import Vue from "vue";
import VueRouter from "vue-router";
import store from "@/store";

import Home from "@/views/Home.vue";
import Cart from "@/views/Cart.vue";
import Wishlist from "@/views/Wishlist.vue";
import Fruts from "@/views/Fruts.vue";
import Vegetables from "@/views/Vegetables.vue";
import Checkout from "@/views/Checkout.vue";
import PagoExitoso from "@/components/PagoExitoso.vue";
import Pedidos from "@/views/Pedidos.vue";
import AdminPanel from "@/views/AdminPanel.vue";
import Login from "@/views/Login.vue";

Vue.use(VueRouter);

const routes = [
  { path: "/", component: Home },
  { path: "/cart", component: Cart },
  { path: "/wishlist", component: Wishlist },
  { path: "/category/fruts", component: Fruts },
  { path: "/category/vegetables", component: Vegetables },
  { path: "/checkout", component: Checkout },
  { path: "/pago-exitoso", component: PagoExitoso },
  { path: "/pedidos", component: Pedidos },
  {
    path: "/login",
    component: Login,
    meta: { hideNavbar: true, hideChat: true }
  },
  {
    path: "/admin",
    component: AdminPanel,
    meta: { requiresAdmin: true, hideNavbar: true, hideChat: true }
  }
];

const router = new VueRouter({
  mode: "history",
  routes
});

// 🔒 Guard global: protege TODAS las rutas excepto /login
router.beforeEach((to, from, next) => {
  const usuario = store.state.usuario || JSON.parse(localStorage.getItem("user"));

  // Si NO hay usuario logueado y no es login → redirigir a login
  if (!usuario && to.path !== "/login") {
    return next("/login");
  }

  // Bloquear login si ya está logueado
  if (to.path === "/login" && usuario) {
    return next(usuario.rol === "admin" ? "/admin" : "/");
  }

  // Proteger rutas admin
  if (to.matched.some(record => record.meta.requiresAdmin)) {
    if (usuario.rol !== "admin") {
      return next("/"); // no es admin → redirigir a home (usuario normal)
    }
  }

  next(); // todo ok
});

export default router;