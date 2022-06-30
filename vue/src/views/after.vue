<template>
  <div id="after">
    <div>
      <div v-bind:class="item.err" class="result" v-for="item in this.$route.params.leftJson">
        {{ item.str.startsWith('\"') ? "&nbsp &nbsp" + item.str : item.str }}
      </div>
    </div>
    <div>
      <button @click="goBack">before 페이지로 이동</button>
    </div>
    <div>
      <div v-bind:class="item.err" class="result" v-for="item in this.$route.params.rightJson">
        {{ item.str.startsWith('\"') ? "&nbsp &nbsp" + item.str : item.str }}
      </div>
    </div>

  </div>
</template>

<script>

export default {
  name: "after",
  methods: {
    goBack() {
      this.$router.push('/');
    }
  }
}
</script>

<style scoped>
#after {
  display: flex;
  justify-content: space-around;
}

.result {
  font-size: 14px;
  font-weight: bold;
}
/* 일치 하는 키가 없을 경우 */
.not_key{ background-color : rgba(128, 128, 128, 0.2); }
/* type이 일치하지 않을 경우*/
.diff_type{ background-color : rgba(255, 0, 0, 0.2); }
/* value가 일치하지 않을 경우 */
.diff_val{ background-color : rgba(255, 255, 0, 0.3); }

</style>