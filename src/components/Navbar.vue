<template>
  <v-app-bar app color="white" flat class="app-bar-spaced">

    <!-- Logo y nombre solo si NO es admin -->
    <template v-if="!isAdmin">
      <v-btn icon to="/">
        <v-badge color="#D5F0DB" dot offset-x="5" offset-y="5">
          <v-img src="1.png" contain width="40"></v-img>
        </v-badge>
      </v-btn>

      <v-toolbar-title class="ml-2">
        <v-btn text to="/">
          <span class="green--text">e</span><strong>Grocery</strong>
        </v-btn>
      </v-toolbar-title>
    </template>

    <v-spacer></v-spacer>

    <!-- Wishlist, Cart y Pedidos solo para usuario normal -->
    <template v-if="!isAdmin">
      <v-badge color="#FF6D59" overlap content="2" class="mr-3">
        <v-avatar color="#FFF0EE" size="40">
          <v-btn icon to="/wishlist">
            <v-icon size="28" color="#FF6D59">far fa-heart</v-icon>
          </v-btn>
        </v-avatar>
      </v-badge>

      <v-badge color="#41AB55" overlap content="3" class="mr-3">
        <v-avatar color="#ECF7EE" size="40">
          <v-btn icon to="/cart">
            <v-icon size="28" color="#41AB55">fas fa-shopping-cart</v-icon>
          </v-btn>
        </v-avatar>
      </v-badge>

      <v-avatar color="#FFF8E1" size="40" class="mr-3">
        <v-btn icon to="/pedidos">
          <v-icon size="28" color="#FFB300">fas fa-box-open</v-icon>
        </v-btn>
      </v-avatar>
    </template>

    <!-- PERFIL siempre visible (solo cerrar sesión) -->
    <v-menu
      v-model="menu"
      offset-y
      bottom
      left
      transition="scale-transition"
      min-width="200"
    >
      <template v-slot:activator="{ on, attrs }">
        <v-avatar size="40" v-bind="attrs" v-on="on">
          <v-img src="/user.png" />
        </v-avatar>
      </template>

      <v-card class="pa-4 text-center">
        <div class="font-weight-bold text-subtitle-1 mb-3">{{ usuario.nombre || 'Invitado' }}</div>
        <div class="grey--text text-caption mb-3">{{ usuario.email || '' }}</div>
        <v-divider class="mb-3"></v-divider>
        <v-btn block color="red" dark @click="logout">🚪 Cerrar sesión</v-btn>
      </v-card>
    </v-menu>

  </v-app-bar>
</template>

<script>
export default {
  props: { isAdmin: { type: Boolean, default: false } },
  data() {
    return {
      menu: false,
      usuario: { nombre: "", email: "" }
    }
  },
  mounted() {
    this.cargarUsuario();
  },
  methods: {
    cargarUsuario() {
      const user = this.$store.state.usuario || JSON.parse(localStorage.getItem("user"));
      this.usuario = user || { nombre: "Invitado", email: "" };
    },
   logout() {
  this.$store.commit("logout")

  // 🔥 Limpia usuario actual
  localStorage.removeItem("user")

  this.menu = false
  this.$router.push("/login")
}
  }
};
</script>