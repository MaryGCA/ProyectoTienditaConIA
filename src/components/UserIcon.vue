<template>
  <v-avatar size="40">
    <v-img :src="usuario.imagen || 'https://i.pravatar.cc/150'" />
  </v-avatar>
</template>

<script>
export default {
  props: {
    usuario: {
      type: Object,
      required: true
    }
  }
};
</script>