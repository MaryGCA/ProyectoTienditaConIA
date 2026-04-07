```vue
<template>
  <v-app>
    <Navbar />

    <v-container fluid>
      <v-row>
        <v-col cols="12">

          <!-- HERO -->
          <v-card class="mx-auto rounded-xl" flat>
            <v-img src="/cover2.png" max-height="100%" max-width="100%" contain>

              <v-card-title class="top ml-10">
                <h2 class="title1">Order your</h2>
              </v-card-title>

              <v-card-title class="ml-10 mt-n8">
                <h2 class="title2">Daily Groceries</h2>
              </v-card-title>

              <v-card-text class="ml-10">
                <h3 class="green--text">#Free Delivery</h3>
              </v-card-text>

              <v-card-text class="ml-10">
                <v-row>
                  <v-col cols="4">
                    <v-text-field
                      v-model="busqueda"
                      label="Search your daily groceries"
                      prepend-inner-icon="mdi-magnify"
                      solo
                      rounded
                      outlined
                      clearable
                      hide-details
                    />
                  </v-col>
                </v-row>
              </v-card-text>

            </v-img>
          </v-card>

          <!-- CATEGORÍAS -->
          <v-toolbar flat color="transparent" class="mt-4">
            <v-toolbar-title class="text-h6">Category</v-toolbar-title>
            <v-spacer></v-spacer>

            <v-btn-toggle v-model="toggle_exclusive" group color="#49D9A0">
              <v-btn><v-icon>mdi-chevron-left</v-icon></v-btn>
              <v-btn><v-icon>mdi-chevron-right</v-icon></v-btn>
            </v-btn-toggle>
          </v-toolbar>

          <Category />

          <!-- RESULTADOS DE BUSQUEDA -->
          <div v-if="busqueda">

            <v-toolbar flat color="transparent" class="mt-6">
              <v-toolbar-title class="text-h6">
                Search Results
              </v-toolbar-title>
            </v-toolbar>

            <v-row>

              <v-col
                v-for="item in productosFiltrados"
                :key="item.id"
                cols="12"
                sm="6"
                md="3"
              >

                <v-card class="pa-4 product-card" outlined>

                  <v-img
                    :src="item.img"
                    height="120"
                    contain
                    class="mb-2"
                  />

                  <div class="text-subtitle-1 font-weight-medium">
                    {{ item.name }}
                  </div>

                  <div class="text-caption grey--text mb-2">
                    {{ item.weight }}
                  </div>

                  <div class="text-h6 font-weight-bold mb-3">
                    ${{ item.price }}
                  </div>

                  <div class="d-flex align-center justify-space-between">

                    <div class="d-flex align-center">

                      <v-btn icon small @click="decrease(item)">
                        <v-icon>mdi-minus</v-icon>
                      </v-btn>

                      <span class="mx-2">{{ item.qty }}</span>

                      <v-btn icon small @click="increase(item)">
                        <v-icon color="green">mdi-plus</v-icon>
                      </v-btn>

                    </div>

                    <div class="d-flex">

                      <v-btn fab small class="wishlist-btn" @click="addToWishlist(item)">
                        <v-icon color="#FF6D59">mdi-heart</v-icon>
                      </v-btn>

                      <v-btn color="green" dark fab small class="cart-btn" @click="addToCart(item)">
                        <v-icon>mdi-cart</v-icon>
                      </v-btn>

                    </div>

                  </div>

                </v-card>

              </v-col>

            </v-row>

          </div>

          <!-- PRODUCTOS POPULARES -->
          <v-card
            v-if="!busqueda"
            flat
            color="#E2F2E5"
            class="rounded-xl mt-4 pa-6"
          >

            <v-toolbar flat color="transparent" class="mb-4">
              <v-toolbar-title class="text-h6">
                Popular Products
              </v-toolbar-title>
            </v-toolbar>

            <v-row>

              <v-col
                v-for="item in productosRandom"
                :key="'random-'+item.id"
                cols="12"
                sm="6"
                md="3"
              >

                <v-card class="pa-4 product-card" outlined>

                  <v-img
                    :src="item.img"
                    height="140"
                    contain
                  />

                  <div class="text-subtitle-1 font-weight-medium">
                    {{ item.name }}
                  </div>

                  <div class="text-caption grey--text mb-2">
                    {{ item.weight }}
                  </div>

                  <div class="text-h6 font-weight-bold mb-3">
                    ${{ item.price }}
                  </div>

                  <div class="d-flex align-center justify-space-between">

                    <div class="d-flex align-center">

                      <v-btn icon small @click="decrease(item)">
                        <v-icon>mdi-minus</v-icon>
                      </v-btn>

                      <span class="mx-2">{{ item.qty }}</span>

                      <v-btn icon small @click="increase(item)">
                        <v-icon color="green">mdi-plus</v-icon>
                      </v-btn>

                    </div>

                    <div class="d-flex">

                      <v-btn fab small class="wishlist-btn" @click="addToWishlist(item)">
                        <v-icon color="#FF6D59">mdi-heart</v-icon>
                      </v-btn>

                      <v-btn color="green" dark fab small class="cart-btn" @click="addToCart(item)">
                        <v-icon>mdi-cart</v-icon>
                      </v-btn>

                    </div>

                  </div>

                </v-card>

              </v-col>

            </v-row>

          </v-card>

          <Client />
          <Partner />

        </v-col>
      </v-row>
    </v-container>

    <!-- MODAL -->
    <v-dialog v-model="dialog" max-width="400">
      <v-card class="pa-6 text-center" color="#e8f5e9" outlined>
        <v-icon size="64" color="green">mdi-check-circle</v-icon>
        <h2 class="mt-4">{{ dialogMessage }}</h2>
        <v-btn color="green" class="mt-4" @click="dialog = false">
          Cerrar
        </v-btn>
      </v-card>
    </v-dialog>

    <Footer/>
  </v-app>
</template>

<script>

import Navbar from "../components/Navbar"
import Footer from "../components/Footer"
import Category from "../components/Category.vue"
import Client from "../components/Client.vue"
import Partner from "../components/Partner.vue"

export default {

  name: "Home",

  components:{
    Navbar,
    Footer,
    Category,
    Client,
    Partner
  },

  data(){
    return{
      toggle_exclusive:1,
      busqueda:"",
      productos:[],
      dialog:false,
      dialogMessage:""
    }
  },

  mounted(){
    this.cargarProductos()
  },

  computed:{

    productosFiltrados(){
      const texto = this.busqueda.trim().toLowerCase()

      if(!texto) return []

      return this.productos.filter(p =>
        (p.name || "").toLowerCase().includes(texto) ||
        (p.weight || "").toLowerCase().includes(texto)
      )
    },

    productosRandom(){
      return [...this.productos]
        .sort(() => Math.random() - 0.5)
        .slice(0,12)
    }

  },

  methods:{

    async cargarProductos(){
      try{
        const response = await fetch("http://localhost:8081/api/productos")
        if(!response.ok) throw new Error("Error API")

        const data = await response.json()

        this.productos = data.map(product => ({
          id:product.id,
          name:product.nombre,
          weight:product.variedad || "Sin variedad",
          price:product.precio,
          stock:product.stock || 0,
          qty:1,
          img: product.imagen || "/default.webp"
        }))

      }catch(error){
        console.error("Error cargando productos:", error)
      }
    },

    increase(item){
      if(item.qty < item.stock){
        item.qty++
      }else{
        alert("No hay más stock disponible")
      }
    },

    decrease(item){
      if(item.qty > 1){
        item.qty--
      }
    },

    // ✅ CARRITO CORREGIDO
    async addToCart(item){

      const user = JSON.parse(localStorage.getItem("user"))

      if(!user){
        this.dialogMessage = "⚠️ Debes iniciar sesión"
        this.dialog = true
        return
      }

      try{
        const response = await fetch("http://localhost:8081/api/carrito/agregar",{
          method:"POST",
          headers:{
            "Content-Type":"application/x-www-form-urlencoded"
          },
          body:new URLSearchParams({
  nombre: item.name,
  variedad: item.weight,   // 🔥 IMPORTANTE
  cantidad: item.qty,
  usuario: user.id || user.email
})
        })

        if(!response.ok) throw new Error("Error agregando al carrito")

        this.dialogMessage = "🛒 Producto agregado al carrito"
        this.dialog = true
        item.qty = 1

      }catch(error){
        console.error("Error carrito", error)
      }
    },

   addToWishlist(item){

  const user = JSON.parse(localStorage.getItem("user"))

  if(!user){
    this.dialogMessage = "⚠️ Debes iniciar sesión"
    this.dialog = true
    return
  }

  const key = `wishlist_${user.id || user.email}`

  let wishlist = JSON.parse(localStorage.getItem(key)) || []

  const existe = wishlist.find(p => p.id === item.id)

  if(!existe){

    wishlist.push({
      id: item.id,
      name: item.name,
      price: item.price,
      weight: item.weight,
      img: item.img
    })

    localStorage.setItem(key, JSON.stringify(wishlist))

    this.dialogMessage = "❤️ Producto agregado a wishlist"
  }else{
    this.dialogMessage = "❤️ Este producto ya está en tu wishlist"
  }

  this.dialog = true
}
  }

}
</script>

<style scoped>

.product-card{
  transition:0.2s;
}

.product-card:hover{
  transform:translateY(-5px);
  box-shadow:0 8px 20px rgba(0,0,0,0.1);
}

.cart-btn{
  margin-left:8px;
  border:2px solid #e0e0e0;
}

.wishlist-btn{
  border:2px solid #e0e0e0;
  background:white;
}

</style>
```
