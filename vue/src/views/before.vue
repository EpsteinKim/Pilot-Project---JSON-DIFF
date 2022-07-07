<template>
  <div id="before">
    <mainSection/>
    <router-link to="/after">after 페이지로 이동</router-link>
  </div>

</template>

<script>
import mainSection from '../components/layout/Main'

export default {
  name: "before",
  components: {
    mainSection
  }
}
</script>

<style>
/*body{
  background-color: darkslategrey;
}*/
</style>