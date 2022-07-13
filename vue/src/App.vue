<template>
  <v-app id="app">
    <v-container>
      <header-div/>
      <router-view/>
    </v-container>

  </v-app>
</template>

<script>
import headerDiv from './components/layout/Header'
import Main from './components/layout/Main'

export default {
  name: 'App',
  components:{
    headerDiv,
    Main
  }
}
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
}
</style>

