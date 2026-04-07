<template>
  <v-container>

    <h1>Carrito de Compras</h1>

    <v-alert v-if="!carrito.length" type="info" class="mt-4">
      El carrito está vacío
    </v-alert>

    <v-row v-else dense>
      <v-col 
        v-for="item in carrito" 
        :key="item.id + '-' + item.variedad"
        cols="12"
      >

        <v-card class="pa-2 product-card d-flex justify-space-between align-center" outlined>

          <div>
            <div class="text-subtitle-1 font-weight-medium">
              {{ item.nombre }} ({{ item.variedad }})
            </div>

            <div class="grey--text text-caption">
              Precio: ${{ item.precio }} |
              Subtotal: ${{ (item.precio * item.cantidad).toFixed(2) }}
            </div>
          </div>

          <div class="d-flex align-center">

            <v-btn icon small @click="modificarCantidad(item, -1)">
              <v-icon small>mdi-minus</v-icon>
            </v-btn>

            <span class="mx-2">{{ item.cantidad }}</span>

            <v-btn icon small @click="modificarCantidad(item, 1)">
              <v-icon small color="green">mdi-plus</v-icon>
            </v-btn>

            <v-btn icon color="red" @click="eliminarProducto(item)">
              <v-icon small>mdi-delete</v-icon>
            </v-btn>

          </div>

        </v-card>

      </v-col>
    </v-row>

    <h2 v-if="carrito.length" class="mt-4">
      Total: ${{ total.toFixed(2) }}
    </h2>

    <div v-if="carrito.length" class="mt-4">
      <v-btn color="red" @click="vaciarCarrito">Vaciar carrito</v-btn>
      <v-btn color="green" class="ml-4" @click="$router.push('/checkout')">
        Pagar
      </v-btn>
    </div>

  </v-container>
</template>

<script>
export default {

  name:"Cart",

  data() {
    return {
      carrito: [],
      total: 0
    };
  },

  mounted() {
    this.cargarCarrito();
  },

  methods: {

    getUser(){
      return JSON.parse(localStorage.getItem("user"))
    },

    async cargarCarrito() {

      const user = this.getUser()
      if(!user){
        this.carrito = []
        this.total = 0
        return
      }

      const usuario = user.id || user.email

      try{
        const res = await fetch(`http://localhost:8081/api/carrito?usuario=${usuario}`)
        this.carrito = await res.json()

        const totalRes = await fetch(`http://localhost:8081/api/carrito/total?usuario=${usuario}`)
        this.total = await totalRes.json()
      }catch(e){
        console.error(e)
      }
    },

    // ✅ FIX IMPORTANTE (AGREGA VARIEDAD)
    async modificarCantidad(item, delta) {

      const user = this.getUser()
      if(!user) return

      const usuario = user.id || user.email

      const nuevaCantidad = item.cantidad + delta
      if (nuevaCantidad < 1) return

      await fetch("http://localhost:8081/api/carrito/agregar", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({
          nombre: item.nombre,
          variedad: item.variedad, // 🔥 CLAVE
          cantidad: delta,
          usuario: usuario
        })
      })

      this.cargarCarrito()
    },

    async eliminarProducto(item) {

      const user = this.getUser()
      if(!user) return

      const usuario = user.id || user.email

      await fetch(`http://localhost:8081/api/carrito/${item.id}?usuario=${usuario}&variedad=${item.variedad}`, {
        method: "DELETE"
      })

      this.cargarCarrito()
    },

    async vaciarCarrito() {

      const user = this.getUser()
      if(!user) return

      const usuario = user.id || user.email

      await fetch(`http://localhost:8081/api/carrito?usuario=${usuario}`, {
        method: "DELETE"
      })

      this.carrito = []
      this.total = 0
    }

  }

};
</script>

<style scoped>
.product-card {
  transition: 0.2s;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px;
  min-height: 60px;
}

.product-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(0,0,0,0.08);
}

.mx-2 {
  min-width: 28px;
  text-align: center;
  font-weight: bold;
}
</style>