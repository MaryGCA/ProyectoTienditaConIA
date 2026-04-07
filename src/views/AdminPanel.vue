<template>
  <v-app>
    <!-- Navbar en modo admin: solo icono de usuario -->
    <Navbar 
      :isAdmin="true" 
      :usuario="usuario" 
      :logout="logout" 
    />

    <v-container>
      <!-- Encabezado con botón Agregar -->
      <div class="mb-6 d-flex justify-space-between align-center">
        <h1>Panel Admin - Productos</h1>
        <v-btn color="green" dark @click="abrirDialogo()" class="ml-4">
          <v-icon left>mdi-plus</v-icon> Agregar Producto
        </v-btn>
      </div>

      <!-- Productos -->
      <v-row>
        <v-col v-for="item in productos" :key="item.id" cols="12" sm="6" md="4" lg="3">
          <v-card class="pa-4 product-card" outlined>
            <div class="text-center">
              <v-img :src="getImage(item.imagenProducto)" contain height="180" />
            </div>
            <div class="mt-4 text-h6 font-weight-medium">{{ item.nombre }}</div>
            <div class="grey--text text-caption mb-1"><b>Variedad:</b> {{ item.variedad || 'Sin variedad' }}</div>
            <div class="text-h5 font-weight-bold mb-2">${{ item.precio }}</div>
            <div class="grey--text text-caption mb-2"><b>Stock:</b> {{ item.stock }}</div>

            <div class="d-flex justify-end">
              <v-btn icon large color="blue" @click="abrirDialogo(item)">
                <v-icon size="28">mdi-pencil</v-icon>
              </v-btn>
              <v-btn icon large color="red" @click="eliminarProducto(item.id)">
                <v-icon size="28">mdi-delete</v-icon>
              </v-btn>
            </div>
          </v-card>
        </v-col>
      </v-row>

      <!-- Dialogo agregar/editar producto -->
      <v-dialog v-model="dialog" max-width="500">
        <v-card>
          <v-card-title>{{ editando ? "Editar Producto" : "Nuevo Producto" }}</v-card-title>
          <v-card-text>
            <v-text-field v-model="producto.nombre" label="Nombre" />
            <v-text-field v-model="producto.variedad" label="Variedad" />
            <v-text-field v-model.number="producto.precio" label="Precio" type="number" />
            <v-text-field v-model.number="producto.stock" label="Stock" type="number" />

            <v-radio-group v-model.number="producto.categoriaId" label="Categoría">
              <v-radio label="Fruta" :value="1" />
              <v-radio label="Verdura" :value="2" />
            </v-radio-group>

            <v-text-field v-model="producto.imagenProducto" label="URL o nombre de imagen" />

            <div v-if="producto.imagenProducto" class="mt-2 text-center">
              <v-img :src="getImage(producto.imagenProducto)" contain height="120" width="120" />
            </div>
          </v-card-text>

          <v-card-actions>
            <v-spacer />
            <v-btn color="grey" @click="dialog=false">Cancelar</v-btn>
            <v-btn color="green" @click="guardarProducto">Guardar</v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>

      <!-- Notificación -->
      <v-dialog v-model="dialogNotif" max-width="400">
        <v-card class="pa-6 text-center" :color="notifColor" outlined>
          <v-icon size="64" :color="notifIconColor">{{ notifIcon }}</v-icon>
          <h2 class="mt-4">{{ notifMessage }}</h2>
          <v-btn :color="notifIconColor" class="mt-4" @click="dialogNotif=false">Cerrar</v-btn>
        </v-card>
      </v-dialog>
    </v-container>
  </v-app>
</template>

<script>
import Navbar from "@/components/Navbar.vue";

export default {
  components: { Navbar },

  data() {
    return {
      productos: [],
      dialog: false,
      editando: false,
      producto: this.nuevoProducto(),
      dialogNotif: false,
      notifMessage: "",
      notifColor: "#e8f5e9",
      notifIcon: "mdi-check-circle",
      notifIconColor: "green",
      usuario: { nombre: "Admin", email: "admin@email.com", imagen:"/user.png", rol:"admin" }
    };
  },

  mounted() {
    this.cargarUsuario();
    this.cargarProductos();
  },

  methods: {
    // Nuevo producto
    nuevoProducto() {
      return { id:null, nombre:"", variedad:"", precio:0, stock:0, categoriaId:1, imagenProducto:"" };
    },

    abrirDialogo(item=null) {
      this.editando = !!item;
      this.producto = item ? { ...item } : this.nuevoProducto();
      this.dialog = true;
    },

    async guardarProducto() {
      try {
        const body = {
          nombre: this.producto.nombre,
          variedad: this.producto.variedad,
          precio: this.producto.precio,
          stock: this.producto.stock,
          categoriaId: this.producto.categoriaId,
          imagen: this.producto.imagenProducto
        };
        const url = this.editando
          ? `http://localhost:8081/api/productos/${this.producto.id}`
          : `http://localhost:8081/api/productos`;
        const res = await fetch(url, {
          method: this.editando?"PUT":"POST",
          headers: {"Content-Type":"application/json"},
          body: JSON.stringify(body)
        });
        if(!res.ok) throw new Error();
        await this.cargarProductos();
        this.dialog = false;
        this.mostrarNotif(this.editando?"✏️ Producto actualizado":"✅ Producto agregado","success");
      } catch(err){ console.error(err); this.mostrarNotif("❌ Error al guardar","error"); }
    },

    async eliminarProducto(id) {
      if(!confirm("¿Eliminar producto?")) return;
      try{
        await fetch(`http://localhost:8081/api/productos/${id}`, { method:"DELETE" });
        this.productos = this.productos.filter(p=>p.id!==id);
        this.mostrarNotif("🗑 Producto eliminado","warning");
      } catch(err){ console.error(err); this.mostrarNotif("❌ Error al eliminar","error"); }
    },

    cargarUsuario() {
      const user = JSON.parse(localStorage.getItem("user"));
      if (!user || user.rol !== "admin") return;
      this.usuario = { ...user, imagen: "/user.png" };
    },

    logout() {
      this.$store.commit("logout");
      this.$nextTick(() => this.$router.push("/login"));
    },

    getImage(img){
      if(!img) return "/default.webp";
      if(img.startsWith("http")) return img;
      return `http://localhost:8081/api/productos/images/${img}`;
    },

    mostrarNotif(texto, tipo="success"){
      const tipos = {
        success:{ color:"#e8f5e9", icon:"mdi-check-circle", iconColor:"green" },
        error:{ color:"#ffebee", icon:"mdi-alert-circle", iconColor:"red" },
        warning:{ color:"#fff3e0", icon:"mdi-delete", iconColor:"orange" },
        info:{ color:"#e3f2fd", icon:"mdi-information", iconColor:"blue" }
      };
      const t = tipos[tipo];
      this.notifMessage = texto;
      this.notifColor = t.color;
      this.notifIcon = t.icon;
      this.notifIconColor = t.iconColor;
      this.dialogNotif = true;
      setTimeout(()=>this.dialogNotif=false, 2500);
    },

    async cargarProductos() {
      try {
        const res = await fetch("http://localhost:8081/api/productos");
        const data = await res.json();
        this.productos = data
          .sort((a,b)=>b.id - a.id)
          .map(p=>({
            id:p.id,
            nombre:p.nombre,
            variedad:p.variedad||"Sin variedad",
            precio:p.precio,
            stock:p.stock||0,
            categoriaId:p.categoriaId,
            imagenProducto:p.imagen||""
          }));
      } catch(err){ console.error(err); this.mostrarNotif("Error cargando productos","error"); }
    },
  }
};
</script>

<style scoped>
.product-card{transition:0.2s;margin-bottom:12px;}
.product-card:hover{transform:translateY(-5px);box-shadow:0 8px 20px rgba(0,0,0,0.1);}
.v-col{padding-left:6px!important;padding-right:6px!important;}
</style>