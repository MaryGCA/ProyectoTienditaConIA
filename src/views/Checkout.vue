<template>
  <v-container class="mt-10" style="max-width:500px">
    <h2 class="mb-6">Pago y Dirección de Envío</h2>

    <v-form ref="form" v-model="valido">

      <!-- 📍 DIRECCIÓN -->
      <v-text-field
        label="Dirección"
        placeholder="Calle, número, colonia"
        v-model="direccion"
        :rules="[direccionValida]"
        required
      />

      <v-row>
        <v-col cols="6">
          <v-text-field
            label="Ciudad"
            v-model="ciudad"
            :rules="[ciudadValida]"
            required
          />
        </v-col>

        <v-col cols="6">
          <v-text-field
            label="Código Postal"
            v-model="cp"
            :rules="[cpValido]"
            required
          />
        </v-col>
      </v-row>

      <v-text-field
        label="Teléfono"
        v-model="telefono"
        :rules="[telefonoValido]"
        required
      />

      <v-divider class="my-6"></v-divider>

      <!-- 💳 TARJETA -->
      <v-text-field
        label="Número de Tarjeta"
        placeholder="1234 5678 9012 3456"
        v-model="tarjeta"
        :rules="[tarjetaValida]"
        required
      />

      <v-row>
        <v-col cols="6">
          <v-text-field
            label="Mes Expiración"
            placeholder="MM"
            v-model="mes"
            :rules="[mesValido]"
            required
          />
        </v-col>

        <v-col cols="6">
          <v-text-field
            label="Año Expiración"
            placeholder="YY"
            v-model="anio"
            :rules="[anioValido]"
            required
          />
        </v-col>
      </v-row>

      <v-text-field
        label="Titular de la Tarjeta"
        v-model="titular"
        :rules="[titularValido]"
        required
      />

      <v-text-field
        label="Código de Seguridad"
        type="password"
        v-model="cvv"
        :rules="[cvvValido]"
        required
      />

      <v-checkbox
        label="Guardar datos para futuras compras"
        v-model="guardar"
      />

      <!-- BOTONES -->
      <v-row class="mt-6">
        <v-col cols="6">
          <v-btn color="green" block @click="pagar">
            PAGAR AHORA
          </v-btn>
        </v-col>

        <v-col cols="6">
          <v-btn outlined block @click="$router.push('/cart')">
            ATRÁS
          </v-btn>
        </v-col>
      </v-row>

    </v-form>
  </v-container>
</template>

<script>
export default {
  data() {
    const currentYear = new Date().getFullYear() % 100;

    return {
      direccion: "",
      ciudad: "",
      cp: "",
      telefono: "",
      tarjeta: "",
      mes: "",
      anio: "",
      titular: "",
      cvv: "",
      guardar: false,
      valido: false,
      currentYear
    };
  },

  methods: {

    direccionValida(v) {
      return !!v || "La dirección es obligatoria";
    },

    ciudadValida(v) {
      return !!v || "La ciudad es obligatoria";
    },

    cpValido(v) {
      return /^\d{5}$/.test(v) || "CP inválido";
    },

    telefonoValido(v) {
      return /^\d{10}$/.test(v) || "Teléfono inválido";
    },

    tarjetaValida(v) {
      return /^\d{16}$/.test(v.replace(/\s/g, "")) || "Número inválido";
    },

    mesValido(v) {
      const m = parseInt(v);
      return (m >= 1 && m <= 12) || "Mes inválido";
    },

    anioValido(v) {
      const a = parseInt(v);
      return (a >= this.currentYear) || "Año inválido";
    },

    titularValido(v) {
      return !!v || "El titular es obligatorio";
    },

    cvvValido(v) {
      return /^\d{3,4}$/.test(v) || "CVV inválido";
    },

    async pagar() {

      if (!this.$refs.form || !this.$refs.form.validate()) return;

      const user = JSON.parse(localStorage.getItem("user"))

      if(!user){
        alert("Debes iniciar sesión")
        return
      }

      const usuario = user.id || user.email

      try {

        const carritoRes = await fetch(`http://localhost:8081/api/carrito?usuario=${usuario}`)
        const productosCarrito = await carritoRes.json();

        if (!productosCarrito.length) {
          alert("Carrito vacío");
          return;
        }

        // 🔥 FIX IMPORTANTE (agregar variedad)
        const productos = productosCarrito.map(p => ({
          nombre: p.nombre,
          variedad: p.variedad,
          precio: p.precio,
          cantidad: p.cantidad
        }));

        const pedidoRes = await fetch("http://localhost:8081/api/pedidos", {
          method: "POST",
          headers: {
            "Content-Type": "application/json"
          },
          body: JSON.stringify({

            usuario,

            direccion: this.direccion,
            ciudad: this.ciudad,
            codigoPostal: this.cp,
            telefono: this.telefono,

            tarjeta: this.tarjeta,
            titular: this.titular,
            mes: this.mes,
            anio: this.anio,
            cvv: this.cvv,

            estado: "EN_PROCESO",
            fecha: new Date(),
            productos
          })
        });

        if (!pedidoRes.ok) throw new Error();

        await fetch(`http://localhost:8081/api/carrito?usuario=${usuario}`, {
          method: "DELETE"
        });

        this.$router.push("/pago-exitoso");

      } catch (error) {
        console.error(error);
        alert("Error al procesar el pago");
      }
    }
  }
};
</script>