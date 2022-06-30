<template>
  <div id="main">
    <textarea placeholder="Enter JSON" v-model="areaVal1"></textarea>
    <div>
      <button @click="compare">Compare It</button>
      <p><a @click="setSample">Sample Data</a></p>
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
    compare() {
      const leftJson = JSON.parse(this.areaVal1)
      const rightJson = JSON.parse(this.areaVal2)
      const diffJson = this.makeDiffJson() //make
      /*
        데이터 베이스에 create할 것 : leftJson.stringify, rightJson.stringify, diffJson.stringify
      */
      const leftJsonStr = JSON.stringify(leftJson)
      const rightJsonStr = JSON.stringify(rightJson)
      const diffJsonStr = JSON.stringify(diffJson)
      const id = this.idMake();
      fetch('/api/save', {
        method: 'post',
        headers: {
          'content-type': 'application/json'
        },
        body: JSON.stringify({
          id, leftJsonStr, rightJsonStr, diffJsonStr
        })
      })
      // routing 과정
      this.$router.push({
        name: 'after',
        params: {
          leftJson,
          rightJson,
          diffJson,
        },
        props:{
          'idx': id
        }
      })
    },
    setSample() {
      this.areaVal1 = '{ "name":"hello", "age":"20", "noKey":"I have no Key", "isGood" : 1}'
      this.areaVal2 = '{ "name":"world", "age":20, "isGood" : 1}'
    },
    idMake() {
      const today = new Date();
      const year = today.getFullYear().toString().substring(2)
      const month = this.addZero(today.getMonth()+1)
      const date = this.addZero(today.getDate())
      const hour = this.addZero(today.getHours())
      const minute = this.addZero(today.getMinutes())
      const second = this.addZero(today.getSeconds())
      const random = Math.random().toString().substring(2, 5)
      return `${year}${month}${date}${hour}${minute}${second}${random}`;
    },
    addZero(dateComponent){
      return dateComponent < 10 ? '0' + dateComponent : dateComponent;
    },
    makeDiffJson() {
      const diffJson = {};
      const leftJson = JSON.parse(this.areaVal1);
      const rightJson = JSON.parse(this.areaVal2);

      // LeftJSON 기준으로 비교
      for (let [key, value] of Object.entries(leftJson)) {
        if (!rightJson.hasOwnProperty(key))
          diffJson[key] = 'not_key' //주석
        else {
          if (value == rightJson[key]) {
            if (value !== rightJson[key]) {
              diffJson[key] = 'diff_type'
            }
          } else diffJson[key] = 'diff_val'

          delete rightJson[key];
        }
      }

      // RightJSON 에 남은 키들 소진 시작
      for (let key of Object.keys(rightJson)) {
        diffJson[key] = 'not_key'
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