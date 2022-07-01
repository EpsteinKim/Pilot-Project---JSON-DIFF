<template>
  <div id="main">
    <textarea placeholder="Enter JSON" v-model="areaVal1"></textarea>
    <div>
      <button @click="compare">Compare It</button>
      <button @click="test">Test</button>
      <button @click="setSample2">sample 2</button>
      <button @click="setSample3">sample 3</button>
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
    // todo DB전송할 결과값 생성중
    test() {

    },
    compare() {
      const [leftJson, rightJson] = [JSON.parse(this.areaVal1), JSON.parse(this.areaVal2)]
      const diffJson = this.findDiffJson(leftJson, rightJson)

      const [leftJsonStr, rightJsonStr] = [JSON.stringify(leftJson, null, 1), JSON.stringify(rightJson, null, 1)];

      // 해시 아이디 생성
      const id = this.hash(leftJsonStr + rightJsonStr)

      // 데이터 베이스 전달값 간소화 필요 : 결과값 해쉬값
      const diffJsonStr = JSON.stringify(diffJson)

      // todo 결과값 생성중 ( 미완성 )
      console.log(leftJsonStr.replaceAll('\n', '\\n').split('\\n'))

      // todo ( 간소화 작업 ) 미완성 : 결과값 // 완성 : 해쉬값 원본값
      const jsonStrData = leftJsonStr + ' (separator) ' + rightJsonStr;
      const [left, right] = jsonStrData.split(' (separator) ');

      // 백단 REST API 변경 필요
      fetch('/api/save', {
        method: 'post',
        headers: {
          'content-type': 'application/json'
        },
        body: JSON.stringify({
          idx, leftJsonStr, rightJsonStr, diffJsonStr
        })
      })

      // routing 과정
      this.$router.push({
        name: 'after',
        params: {
          // 함수 변경 필요
          'leftJson': this.makeJsonSplitByEnterArr(leftJson),
          'rightJson': this.makeJsonSplitByEnterArr(rightJson)
        },
      })
    },
    setSample() {
      this.areaVal1 = '{ "name":"hello", "age":"20", "noKey":"I have no Key", "isGood" : 1}'
      this.areaVal2 = '{ "name":"world", "age":20, "isGood" : 1}'
    },
    setSample2() {
      this.areaVal1 = '{ "name":"hello", "age":"20", "noKey":"I have no Key", "isGood" : 1}'
      this.areaVal2 = '{ "name":"world", "age":20, "isGood" : [1,2]}'
    },
    setSample3() {
      this.areaVal1 = '{"Aidan Gillen": {"array": ["Game of Thron\\"es","The Wire"],"string": "some string","int": 2,"aboolean": true, "boolean": true,"object": {"foo": "bar","object1": {"new prop1": "new prop value"},"object2": {"new prop1": "new prop value"},"object3": {"new prop1": "new prop value"},"object4": {"new prop1": "new prop value"}}},"Amy Ryan": {"one": "In Treatment","two": "The Wire"},"Annie Fitzgerald": ["Big Love","True Blood"],"Anwan Glover": ["Treme","The Wire"],"Alexander Skarsgard": ["Generation Kill","True Blood"], "Clarke Peters": null}'
      this.areaVal2 = '{"Aidan Gillen": {"array": ["Game of Thrones","The Wire"],"string": "some string","int": "2","otherint": 4, "aboolean": "true", "boolean": false,"object": {"foo": "bar"}},"Amy Ryan": ["In Treatment","The Wire"],"Annie Fitzgerald": ["True Blood","Big Love","The Sopranos","Oz"],"Anwan Glover": ["Treme","The Wire"],"Alexander Skarsg?rd": ["Generation Kill","True Blood"],"Alice Farmer": ["The Corner","Oz","The Wire"]}'
    },
    hash(data) { //idMake에서 hash로 변경
      const cryptoJs = require('crypto-js')
      let str = '';
      if (typeof (data) == 'object') {
        str = JSON.stringify(data, null, 1);
      } else if (typeof (data) == 'string') {
        // 코드의 낭비지만 JSON으로 파싱할 수 있는 지 확인합니다.
        str = JSON.stringify(JSON.parse(data), null, 1);
      }
      const hash = cryptoJs.SHA3(str, {outputLength: 256}).toString();
      return hash
    },
    makeDiffJson() {
      const diffJson = {};
      const leftJson = JSON.parse(this.areaVal1);
      const rightJson = JSON.parse(this.areaVal2);

      // LeftJSON 기준으로 비교
      for (let [key, value] of Object.entries(leftJson)) {
        if (!rightJson.hasOwnProperty(key)) {
          diffJson[key] = 'not_key' //주석
        } else {
          if (value == rightJson[key]) {
            if (value !== rightJson[key]) {
              diffJson[key] = 'diff_type'
            }
          } else {
            diffJson[key] = 'diff_val'
          }
          delete rightJson[key];
        }
      }
      // RightJSON 에 남은 키들 소진 시작
      for (let key of Object.keys(rightJson)) {
        diffJson[key] = 'not_key'
      }
      return diffJson;
    },

    // todo: 새로운 차이점 발견하는 함수 생성중
    findDiffJson(json1 = JSON.parse(this.areaVal1), json2 = JSON.parse(this.areaVal2)) {
      const diffJson = {}
      for (let key in json1) {
        if (!json2.hasOwnProperty(key)) {
          diffJson[key] = 'not_key'
        } else {
          // 프로퍼티를 가지고 있다는 가정
          if (json1[key] != json2[key]) {
            if (Array.isArray(json1[key]) && Array.isArray(json2[key])) {
              // 인덱스
            } else if (typeof json1[key] == 'object' && typeof json2[key] == 'object') {

            } else {
              diffJson[key] = 'diff_val'
            }
          } else if (json1[key] !== json2[key]) {
            diffJson[key] = 'diff_type'
          }
          delete json2[key]
        }
      }

      for (let key in json2) {
        diffJson[key] = 'not_key'
      }
      console.log(diffJson)
      return diffJson
    },
    findDiffArray(arr1, arr2) {
      const diffJson = {}
      for (let index in arr1) {

      }
    },
    // 갈아 엎을 예정
    makeJsonSplitByEnterArr(json) {
      const jsonStr = JSON.stringify(json, null, 2);
      const diffJson = this.makeDiffJson();
      const resultList = [];

      const tmpArr = jsonStr.split('\n');
      console.log(tmpArr)

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