<template>
  <v-container class="login-container">
    <v-card class="pa-6 login-card">

      <div class="logo-container">
        <img src="/frutero.png" class="logo"/>
      </div>

      <h2 class="mb-4 text-center">Inicio de sesión</h2>

      <!-- Email -->
      <v-text-field
        v-model="email"
        label="Correo"
        outlined
        dense
      />

      <!-- Contraseña -->
      <v-text-field
        v-model="password"
        :type="showPassword ? 'text' : 'password'"
        label="Contraseña"
        :append-icon="showPassword ? 'mdi-eye-off' : 'mdi-eye'"
        @click:append="showPassword = !showPassword"
        outlined
        dense
      />

      <!-- Botón Login -->
      <v-btn
        color="green"
        block
        class="mt-4"
        :loading="loading"
        :disabled="loading"
        @click="login"
      >
        Entrar
      </v-btn>

      <!-- Botón Registro -->
      <v-btn text color="green" @click="mostrarRegistro = true">
        Crear cuenta
      </v-btn>

      <!-- Mensajes -->
      <v-alert
        v-if="errorMessage"
        type="error"
        class="mt-4"
        dense
        outlined
      >
        {{ errorMessage }}
      </v-alert>

      <v-alert
        v-if="successMessage"
        type="success"
        class="mt-4"
        dense
        outlined
      >
        {{ successMessage }}
      </v-alert>

    </v-card>

    <!-- Dialogo Registro -->
    <v-dialog v-model="mostrarRegistro" max-width="400">
      <v-card>
        <v-card-title>Crear Nueva Cuenta</v-card-title>
        <v-card-text>
          <v-text-field v-model="nuevoUsuario.nombre" label="Nombre" />
          <v-text-field v-model="nuevoUsuario.email" label="Correo" />
          <v-text-field v-model="nuevoUsuario.password" label="Contraseña" type="password" />
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn text color="grey" @click="mostrarRegistro = false">Cancelar</v-btn>
          <v-btn color="green" @click="registrarUsuario">Registrarse</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- ✅ MODAL IGUAL QUE VEGETABLES -->
    <v-dialog v-model="dialog" max-width="400">
      <v-card class="pa-6 text-center" color="#e8f5e9" outlined>
        <v-icon size="64" color="green">mdi-check-circle</v-icon>
        <h2 class="mt-4">{{ dialogMessage }}</h2>
        <v-btn color="green" class="mt-4" @click="dialog = false">
          Cerrar
        </v-btn>
      </v-card>
    </v-dialog>

  </v-container>
</template>

<script>
import store from "@/store"; // Vuex store

export default {
  data() {
    return {
      email: "",
      password: "",
      showPassword: false,
      loading: false,
      errorMessage: "",
      successMessage: "",
      mostrarRegistro: false,
      nuevoUsuario: { nombre: "", email: "", password: "" },

      // ✅ NUEVO (modal tipo vegetables)
      dialog: false,
      dialogMessage: ""
    };
  },
  methods: {
    // Redirige según rol
    redirectUser(rol) {
      if (rol === "admin") this.$router.push("/admin");
      else this.$router.push("/");
    },

    // Login
    async login() {
  this.errorMessage = "";
  this.successMessage = "";
  this.loading = true;

  if (!this.email || !this.password) {
    this.errorMessage = "Ingresa correo y contraseña";
    this.loading = false;
    return;
  }

  try {
    const res = await fetch("http://localhost:8081/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email: this.email, password: this.password })
    });

    const data = await res.json();

    if (!res.ok) {
      this.errorMessage = data.message || "Correo o contraseña incorrectos";
      return;
    }

    // ✅ AQUÍ ES DONDE VA (después del login correcto)
    store.dispatch("login", data.user);
    localStorage.setItem("user", JSON.stringify(data.user));

    this.successMessage = "Login correcto!";
    setTimeout(() => this.redirectUser(data.user.rol), 500);

  } catch (err) {
    console.error(err);
    this.errorMessage = "Error de conexión con el servidor";
  } finally {
    this.loading = false;
  }
},
    // ✅ REGISTRO CON MODAL
    async registrarUsuario() {
      if (!this.nuevoUsuario.nombre || !this.nuevoUsuario.email || !this.nuevoUsuario.password) {
        this.dialogMessage = "⚠️ Completa todos los campos";
        this.dialog = true;
        return;
      }

      try {
        const res = await fetch("http://localhost:8081/auth/register", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(this.nuevoUsuario)
        });

        let data;
        try {
          data = await res.json();
        } catch {
          data = {};
        }

        if (!res.ok) {
          const msg = data.message || `Error creando usuario: ${res.status}`;
          throw new Error(msg);
        }

        // ✅ MENSAJE BONITO
        this.dialogMessage = "👤 Usuario creado con éxito";
        this.dialog = true;

        this.mostrarRegistro = false;
        this.nuevoUsuario = { nombre: "", email: "", password: "" };

      } catch (err) {
        console.error(err);
        this.dialogMessage = "❌ " + (err.message || "Error creando usuario");
        this.dialog = true;
      }
    }
  }
};
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #e8f5e9;
}

.login-card {
  width: 100%;
  max-width: 400px;
  text-align: center;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.logo-container {
  display: flex;
  justify-content: center;
  margin-bottom: 25px;
}

.logo {
  width: 140px;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0% { transform: translateY(0px); }
  50% { transform: translateY(-12px); }
  100% { transform: translateY(0px); }
}
</style>