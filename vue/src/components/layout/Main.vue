<template>
  <div id="main">
    <textarea placeholder="Enter JSON" v-model="areaVal1"></textarea>
    <div>
      <button @click="compare">Compare It</button>
      <button @click="hash">console encrypt Test</button>
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
      areaVal2: '',
    }
  },
  methods: {
    compare() {
      const leftJson = JSON.parse(this.areaVal1)
      const rightJson = JSON.parse(this.areaVal2)
      const diffJson = this.makeDiffJson()

      // todo(해쉬값 아이디 생성)
      const leftJsonHash = this.hash(this.areaVal1)
      const rightJsonHash = this.hash(this.areaVal2)
      const id = leftJsonHash + rightJsonHash

      // 데이터 베이스 전달값 간소화 : 결과값 해쉬값
      const leftJsonStr = JSON.stringify(leftJson)
      const rightJsonStr = JSON.stringify(rightJson)
      const diffJsonStr = JSON.stringify(diffJson)

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
          'leftJson': this.makeJsonSplitByEnterArr(leftJson),
          'rightJson': this.makeJsonSplitByEnterArr(rightJson)
        },
        props: {
          'idx': id
        }
      })
    },
    setSample() {
      this.areaVal1 = '{ "name":"hello", "age":"20", "noKey":"I have no Key", "isGood" : 1}'
      this.areaVal2 = '{ "name":"world", "age":20, "isGood" : 1}'
    },
    hash(data) { //idMake에서 hash로 변경
      const cryptoJs = require('crypto-js')
      if (typeof (data) == 'object') {
        const str = JSON.stringify(data, null, 1);
        const hash = cryptoJs.SHA3(str, {outputLength: 256}).toString();
        return hash
      } else if (typeof (data) == 'string') {
        // 코드의 낭비지만 JSON으로 파싱할 수 있는 지 확인합니다.
        const str = JSON.stringify(JSON.parse(data), null, 1);
        const hash = cryptoJs.SHA3(str, {outputLength: 256}).toString();
        return hash
      }
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
    },
    makeJsonSplitByEnterArr(json) {
      const jsonStr = JSON.stringify(json, null, 2);
      const diffJson = this.makeDiffJson();
      const resultList = [];

      const tmpArr = jsonStr.split('\n');
      for (let str of tmpArr) {
        str = str.trim();
        if (str.startsWith('\"')) {
          const key = str.match('"\\w*"')[0].replaceAll('"', '')
          // 문제가 있는 경우
          if (diffJson.hasOwnProperty(key)) {
            const reason = diffJson[key]
            resultList.push({
              "str": str,
              "err": reason
            })
            continue;
          }
          resultList.push({
            "str": str,
            "err": null
          });
        } else {
          resultList.push({
            'str': str,
            "err": null
          })
        }
      }
      return resultList;
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