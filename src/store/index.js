import Vue from "vue";
import Vuex from "vuex";

Vue.use(Vuex);

export default new Vuex.Store({
  state: {
    usuario: null
  },
  mutations: {
    setUsuario(state, payload) {
      state.usuario = payload;
      localStorage.setItem("user", JSON.stringify(payload));
    },
    cargarUsuario(state) {
      const data = localStorage.getItem("user");
      if (data) state.usuario = JSON.parse(data);
    },
    logout(state) {
      state.usuario = null;
      localStorage.removeItem("user");
    }
  },
  actions: {
    login({ commit }, usuario) {
      commit("setUsuario", usuario);
    },
    cargarUsuario({ commit }) {
      commit("cargarUsuario");
    },
    logout({ commit }) {
      commit("logout");
    }
  }
});