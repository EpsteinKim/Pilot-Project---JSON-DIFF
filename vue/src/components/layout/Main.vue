<template>
  <div id="main">
    <textarea placeholder="Enter JSON" v-model="areaVal1"></textarea>
    <div>
      <button @click="compare">Compare It</button>
      <button @click="test">Test</button>
      <button @click="arraySample">arraySample</button>
      <button @click="setSample3">sample 3</button>
      <button @click="jsonSample">jsonSample</button>
      <button @click="jsonInArraySample">jsonInArraySample</button>
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
    test() {
      const [leftJson, rightJson] = [JSON.parse(this.areaVal1), JSON.parse(this.areaVal2)];

      const diffJson = this.findDiffJson();

      const result = {};
      this.compareByDiffJsonAndJson(leftJson, diffJson, result);
      console.log(JSON.stringify(result, null, 2))
      const [leftJsonStr, rightJsonStr] = [JSON.stringify(leftJson, null, 1), JSON.stringify(rightJson, null, 1)]
      console.log(leftJsonStr)
      console.log(diffJson)
      console.log(JSON.stringify(result, null, 2))
      const id = this.hash(leftJsonStr + rightJsonStr);
      const jsonStr = leftJsonStr + ' (separator) ' + rightJsonStr;

      this.save(id, jsonStr, result)
    },
    async save(id, jsonStr, result) {
      const response = await this.$axios.get(`api/isExist?id=${id}`)
      if (!response.data) { // DB에 값이 없는 경우
        this.$axios.post(`api/save`, {
          id,
          json: jsonStr,
          result: JSON.stringify(result)
        })
      }
      this.$router.push({
        name: 'after',
        query: {id}
      })
    },
    setSample() {
      this.areaVal1 = '{ "name":"hello", "age":"20", "noKey":"I have no Key", "isGood" : 1}'
      this.areaVal2 = '{ "name":"world", "age":20, "isGood" : 1}'
    },
    arraySample() {
      this.areaVal1 = '{ "name":"hello", "age":"20", "noKey":"I have no Key", "isGood" : ["1"]}'
      this.areaVal2 = '{ "name":"world", "age":20, "isGood" : [1,2]}'
    },
    setSample3() {
      this.areaVal1 = '{"Aidan Gillen": {"array": ["Game of Thron\\"es","The Wire"],"string": "some string","int": 2,"aboolean": true, "boolean": true,"object": {"foo": "bar","object1": {"new prop1": "new prop value"},"object2": {"new prop1": "new prop value"},"object3": {"new prop1": "new prop value"},"object4": {"new prop1": "new prop value"}}},"Amy Ryan": {"one": "In Treatment","two": "The Wire"},"Annie Fitzgerald": ["Big Love","True Blood"],"Anwan Glover": ["Treme","The Wire"],"Alexander Skarsgard": ["Generation Kill","True Blood"], "Clarke Peters": null}'
      this.areaVal2 = '{"Aidan Gillen": {"array": ["Game of Thrones","The Wire"],"string": "some string","int": "2","otherint": 4, "aboolean": "true", "boolean": false,"object": {"foo": "bar"}},"Amy Ryan": ["In Treatment","The Wire"],"Annie Fitzgerald": ["True Blood","Big Love","The Sopranos","Oz"],"Anwan Glover": ["Treme","The Wire"],"Alexander Skarsg?rd": ["Generation Kill","True Blood"],"Alice Farmer": ["The Corner","Oz","The Wire"]}'
    },
    jsonSample() {
      this.areaVal1 = '{ "name" : {"hi":"1"}}'
      this.areaVal2 = '{ "name" : {"hi":1}}'
    },
    jsonInArraySample() {
      this.areaVal1 = '{ "name" : [{"hi":1}, 1]}'
      this.areaVal2 = '{ "name" : [{"hi":2}, 1]}'
    },

    hash(str) { //idMake에서 hash로 변경
      const cryptoJs = require('crypto-js')
      return cryptoJs.SHA3(str, {outputLength: 256}).toString();
    },
    // todo: Depth 상관없이 프로퍼티값이 JSON, Array, 등등 일 경우 차이점 함수 구현 완료
    findDiffJson(json1 = JSON.parse(this.areaVal1), json2 = JSON.parse(this.areaVal2)) {
      const diffJson = {}
      for (let key in json1) {
        if (!json2.hasOwnProperty(key)) {
          diffJson[key] = 'not_key'
        } else {
          // 프로퍼티를 가지고 있다는 가정
          if (json1[key] != json2[key]) {
            if (Array.isArray(json1[key]) && Array.isArray(json2[key])) {
              diffJson[key] = this.findDiffArray(json1[key], json2[key]);
            } else if (typeof json1[key] == 'object' && typeof json2[key] == 'object') {
              diffJson[key] = this.findDiffJson(json1[key], json2[key])
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
      return diffJson
    },

    // findDiffJson 에 종속적인 배열 비교 함수 구현
    findDiffArray(arr1, arr2) {
      const diffJson = {}
      for (let i in arr1.length > arr2.length ? arr1 : arr2) {
        if (Array.isArray(arr1[i]) && Array.isArray(arr2[i])) {
          this.findDiffArray()
        } else if (typeof arr1[i] == 'object' && typeof arr2[i] == 'object') {
          diffJson[i] = this.findDiffJson(arr1[i], arr2[i])
        } else { // 객체, 배열 걸렀음
          if (arr1[i] !== arr2[i]) {
            if (arr1[i] == arr2[i]) {
              diffJson[i] = 'diff_type';
            } else {
              diffJson[i] = 'diff_value'
            }
          }
        }
      }
      return diffJson;
    },

    // todo : 새로운 findDiffJson의 리턴데이터를 바탕으로 템플릿 엔진에서 돌릴 배열 생성
    compareByDiffJsonAndJson(json, diffJson, result = {}, countArr = []) {
      // countArr는 length로 index를 뽑아내기 위한 역할입니다. 아무 원소나 작성하시고 .length로 뽑아내면 됩니다.
      const jsonStr = JSON.stringify(json, null, 1);
      const strArr = jsonStr.split('\n')

      for (let str of strArr) {
        if (str.match('"[\\w\\s]+": {') !== null) { // JSON
          const key = str.match('"[\\w\\s]+"').toString().replaceAll('"', '')
          if (diffJson.hasOwnProperty(key)) { // 차이점을 발견했을 경우
            this.compareByDiffJsonAndJson(json[key], diffJson[key], result, countArr);
          }
        } else if (str.match('"[\\w\\s]+": \\[') !== null) { // Array
          const key = str.match('"[\\w\\s]+"').toString().replaceAll('"', '')
          if (diffJson.hasOwnProperty(key)) { // 차이점을 발견했을 경우
            // todo :  종속적인 함수 구현 필요 ( 배열 전용 )
            this.compareByDiffJsonAndArray(json[key], diffJson[key], result, countArr);
          }
        } else if (str.match('"[\\w\\s]+":') !== null) { // depth
          const key = str.match('"[\\w\\s]+"').toString().replaceAll('"', '')
          if (diffJson.hasOwnProperty(key)) {
            result[countArr.length] = diffJson[key];
          }
        }
        countArr.push(0)
      }
    },
    // compareByDiffJsonAndJson에 종속적인 함수
    compareByDiffJsonAndArray(arr, diffJson, result, countArr) {
      for (let i in arr) {
        countArr.push(0);
        if (diffJson.hasOwnProperty(i)) {
          if (Array.isArray(arr[i])) {
            this.compareByDiffJsonAndArray(arr[i], diffJson[i], result, countArr)
          } else if (typeof arr[i] == 'object') {
            this.compareByDiffJsonAndJson(arr[i], diffJson[i], result, countArr)
          } else {
            result[countArr.length] = diffJson[i]
          }
        }
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