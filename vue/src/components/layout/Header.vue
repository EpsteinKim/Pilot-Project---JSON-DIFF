<template>
  <div>
    <h2>JSON DIFF</h2>
    <p>yellow : diff_value<br> pink : diff_type<br> gray : no key</p>
  </div>
</template>

<script>
export default {
  name: "headerDiv"
}
</script>

<style scoped>
div {
  display: flex;
  justify-content: space-between;
  padding: 20px;
  box-sizing: border-box;
}
</style>