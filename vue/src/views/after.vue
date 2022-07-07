<template>
  <div id="after">
    <div>
      <div  class="result" v-for="item in this.leftJson">
        {{ item }}
      </div>
    </div>
    <div>
      <button @click="goBack">before 페이지로 이동</button>
    </div>
    <div>
      <div class="result" v-for="item in this.rightJson">
        {{ item }}
      </div>

    </div>

  </div>
</template>

<script>

export default {
  name: "after",
  data(){
    return{
      leftJson:JSON.stringify(this.$route.params.leftJson).replace('  ', '&nbsp '),
      rightJson:JSON.stringify(this.$route.params.rightJson)
    }
  },
  methods: {
    goBack() {
      this.$router.push('/');
    },
  },
  beforeCreate(){
    console.log(this.leftJson)
    console.log(this.rightJson)
    this.$axios.get(`api/getData?id=${this.$route.query.id}`)
        .then(function(response){
          return response.data
        })
        .then(function(data){
          const json = data.json;
          const leftJson = json.split(' (separator) ')[0]
          const rightJson = json.split(' (separator) ')[1]
          console.log(leftJson)
          console.log(rightJson)
          console.log(JSON.parse(leftJson))
          console.log(JSON.parse(data.result))
        })
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
/* proto가 일치하지 않을 경우*/
.diff_proto{ background-color : rgba(255, 0, 0, 0.2); }
/* value가 일치하지 않을 경우 */
.diff_val{ background-color : rgba(255, 255, 0, 0.3); }

</style>