<template>
  <v-container>

    <h1 class="mb-6">📦 Mis Pedidos</h1>

    <v-alert v-if="!pedidos.length" type="info">
      No tienes pedidos
    </v-alert>

    <v-card
      v-for="pedido in pedidos"
      :key="pedido.id"
      class="mb-4 pa-4"
      outlined
    >

      <div class="d-flex justify-space-between">
        <div>
          <strong>Pedido #{{ pedido.id }}</strong>
          <div class="grey--text text-caption">
            {{ new Date(pedido.fecha).toLocaleDateString() }}
          </div>
        </div>

        <v-chip color="orange" dark>
          En proceso
        </v-chip>
      </div>

      <div class="mt-2 grey--text text-caption">
        📍 {{ pedido.direccion }}
      </div>

      <v-divider class="my-3"></v-divider>

      <!-- PRODUCTOS -->
      <div
        v-for="prod in pedido.productos"
        :key="prod.nombre + prod.variedad"
        class="d-flex justify-space-between"
      >
        <span>{{ prod.nombre }} ({{ prod.variedad }}) x{{ prod.cantidad }}</span>
        <span>${{ (prod.precio * prod.cantidad).toFixed(2) }}</span>
      </div>

      <v-divider class="my-2"></v-divider>

      <div class="d-flex justify-space-between font-weight-bold">
        <span>Total</span>
        <span>${{ calcularTotal(pedido) }}</span>
      </div>

    </v-card>

  </v-container>
</template>

<script>
export default{

  data(){
    return{
      pedidos:[]
    }
  },

  mounted(){
    this.cargarPedidos()
  },

  methods:{

    async cargarPedidos(){

      const user = JSON.parse(localStorage.getItem("user"))
      if(!user) return

      const usuario = user.id || user.email

      try{

        const res = await fetch(`http://localhost:8081/api/pedidos?usuario=${usuario}`)

        if(!res.ok) throw new Error()

        this.pedidos = await res.json()

      }catch(e){
        console.error(e)
      }
    },

    calcularTotal(pedido){
      return pedido.productos
        .reduce((acc,p)=> acc + (p.precio * p.cantidad),0)
        .toFixed(2)
    }

  }
}
</script>