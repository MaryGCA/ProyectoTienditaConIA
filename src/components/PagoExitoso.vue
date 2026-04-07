<template>
  <v-container class="mt-10" style="max-width:500px; text-align:center;">
    <v-card class="pa-6" color="#e8f5e9">
      <v-icon size="64" color="green">mdi-check-circle</v-icon>
      <h2 class="mt-4">¡Pago Procesado Correctamente!</h2>
      <p>Gracias por tu compra. Tu pedido ha sido registrado exitosamente.</p>
      <v-btn color="green" class="mt-4" @click="$router.push('/')">
        Volver al inicio
      </v-btn>
    </v-card>
  </v-container>
</template>

<script>
export default {
  name: 'PagoExitoso'
}
</script>

<style scoped>
.v-card {
  text-align: center;
}
</style>