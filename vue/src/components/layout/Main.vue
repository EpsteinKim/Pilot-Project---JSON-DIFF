<template>
  <div id="main">
    <textarea placeholder="Enter JSON" v-model="areaVal1"></textarea>
    <div>
      <button @click="goAfter">Compare It</button>
      <p><a @click="getSample">Sample Data</a></p>
    </div>

    <textarea placeholder="Enter JSON" v-model="areaVal2"></textarea>
  </div>

</template>

<script>

export default {
  name: "Main",
  data() {
    return {
      areaVal1: '',
      areaVal2: ''
    }
  },
  methods: {
    goAfter() {
      this.$router.push({
        name: 'after',
        params: {
          leftJson: JSON.parse(this.areaVal1),
          rightJson: JSON.parse(this.areaVal2),
          diffJson: this.diffJson()}
      })
    },
    getSample() {
      this.areaVal1 = '{ "name":"hello", "age":"20", "noKey":"I have no Key" }'
      this.areaVal2 = '{ "name":"world", "age":20 }'
    },
    diffJson() {
      const diffJson = {};
      const leftJson = JSON.parse(this.areaVal1);
      const rightJson = JSON.parse(this.areaVal2);

      // LeftJSON 기준으로 비교
      for (let [key, value] of Object.entries(leftJson)) {
        if (!rightJson.hasOwnProperty(key))
          diffJson[key] = {'description': 'nk'}
        else {
          if (value == rightJson[key]) {
            if (value !== rightJson[key]){
              diffJson[key] = {'description': 'dt'}
            }
          } else diffJson[key] = {'description': 'dv'}

          delete rightJson[key];
        }
      }

      // RightJSON 에 남은 키들 소진 시작
      for (let key of Object.keys(rightJson)) {
        diffJson[key] = {'description': 'nk'}
      }

      return diffJson;
    }
  }
}
</script>

<style scoped>
#main {
  display: flex;
}
a {
  text-decoration-line: underline;
  text-decoration-color: cornflowerblue;
  cursor: pointer;
}
#main > div {
  margin: auto auto;
  width: 300px;
}

textArea {
  margin: 2%;
  width: 100%;
  height: 400px;
  resize: vertical;
  background-color: rgba(128, 128, 128, 0.3);
}

button {
  font-size: 15px;
  border-radius: 10px;
  height: 40px;
}
</style>