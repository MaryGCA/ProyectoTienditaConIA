<template>
  <v-app>

    <!-- Navbar -->
    <Navbar v-if="!$route.meta.hideNavbar" />

    <v-main>
      <router-view/>
    </v-main>

    <!-- 🔥 Chat ocultable por ruta -->
    <AIAssistant v-if="!$route.meta.hideChat" />

  </v-app>
</template>

<script>
import Navbar from "@/components/Navbar.vue";
import AIAssistant from "@/components/AIAssistant.vue";

export default {
  components: { Navbar, AIAssistant }
};
</script>