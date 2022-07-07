<template>
  <div id="main">
    <textarea placeholder="Enter JSON" v-model="leftArea"/>
    <div>
      <button @click="test1">Compare It</button>
      <button @click="test1">Test</button>
      <button @click="setSample3">sample</button>
      <button @click="troubleSample">troubleSample</button>
      <p><a @click="setSample">Sample Data</a></p>
    </div>

    <textarea placeholder="Enter JSON" v-model="rightArea"/>
  </div>

</template>

<script>
export default {
  name: "Main",
  data() {
    return {
      leftArea: '',
      rightArea: '',
    }
  },
  methods: {
    test1() {
      const [leftJson, rightJson] = [JSON.parse(this.leftArea), JSON.parse(this.rightArea)]
      const diffJson = this.findDiffJson(leftJson, JSON.parse(JSON.stringify(rightJson)))
      this.delNullInDiffJson(diffJson)
      const [leftJsonStr, rightJsonStr, diffJsonStr] = [JSON.stringify(leftJson), JSON.stringify(rightJson), JSON.stringify(diffJsonStr)]
      const id = this.hash(leftJsonStr + rightJsonStr);
      this.save(id, leftJsonStr, rightJsonStr, diffJsonStr)


    },

    async save(id, leftJsonStr, rightJsonStr, diffJsonStr) {
      const response = await this.$axios.get(`api/isExist?id=${id}`)
      if (!response.data) { // DB에 값이 없는 경우
        await this.$axios.post(`api/save`, {
          id:id,
          leftJson:leftJsonStr,
          rightJson:rightJsonStr,
          diffJson:diffJsonStr
        })
      }
      await this.$router.push({
        name: 'after',
        query: {id}
      })
    },
    setSample() {
      this.leftArea = '{ "name":"hello", "age":"20", "noKey":"I have no Key", "isGood" : 1}'
      this.rightArea = '{ "name":"world", "age":20, "isGood" : 1}'
    },

    setSample3() {
      this.leftArea = '{"Aidan Gillen": {"array": ["Game of Thron\\"es","The Wire"],"string": "some string","int": 2,"aboolean": true, "boolean": true,"object": {"foo": "bar","object1": {"new prop1": "new prop value"},"object2": {"new prop1": "new prop value"},"object3": {"new prop1": "new prop value"},"object4": {"new prop1": "new prop value"}}},"Amy Ryan": {"one": "In Treatment","two": "The Wire"},"Annie Fitzgerald": ["Big Love","True Blood"],"Anwan Glover": ["Treme","The Wire"],"Alexander Skarsgard": ["Generation Kill","True Blood"], "Clarke Peters": null}'
      this.rightArea = '{"Aidan Gillen": {"array": ["Game of Thrones","The Wire"],"string": "some string","int": "2","otherint": 4, "aboolean": "true", "boolean": false,"object": {"foo": "bar"}},"Amy Ryan": ["In Treatment","The Wire"],"Annie Fitzgerald": ["True Blood","Big Love","The Sopranos","Oz"],"Anwan Glover": ["Treme","The Wire"],"Alexander Skarsg?rd": ["Generation Kill","True Blood"],"Alice Farmer": ["The Corner","Oz","The Wire"]}'
    },

    troubleSample() {
      this.leftArea = '{"Anwan Glover": ["Treme","The Wire"]}'
      this.rightArea = '{"Anwan Glover": ["Treme","The Wire"]}'
    },

    hash(str) { //idMake에서 hash로 변경
      const cryptoJs = require('crypto-js')
      return cryptoJs.SHA3(str, {outputLength: 256}).toString()
    },
    getType(obj) {
      return Array.isArray(obj) && 'array' || typeof obj
    },
    getKey(str) {
      return str.match('".+"(?=:)')[0].replace(/"/g, '')
    },
    isEmptyObj(str){
      return str.match('{}|\\[\\]') !== null
    },

    // todo: Depth 상관없이 프로퍼티값이 JSON, Array, 등등 일 경우 차이점 함수 구현 완료
    findDiffJson(leftJson, rightJsonCopy) {
      const diffJson = {}
      for (let key in leftJson) {
        if (!rightJsonCopy.hasOwnProperty(key)) {
          if (this.getType(leftJson) == 'array') {
            diffJson[key] = 'not_idx'
          } else {
            diffJson[key] = 'not_key'
          }
        } else {
          if (leftJson[key] != rightJsonCopy[key]) {
            diffJson[key] = this.findDifference(leftJson[key], rightJsonCopy[key]);
          } else {
            if (leftJson[key] !== rightJsonCopy[key]) {
              diffJson[key] = 'diff_type'
            }
          }
          delete rightJsonCopy[key] // 메모리 주소 제거
        }
      }
      for (let key in rightJsonCopy) {
        diffJson[key] = 'not_key'
      }
      if (JSON.stringify(diffJson) == '{}') {
        return null
      }
      return diffJson
    },
    findDifference(leftJson, rightJson) {
      let result = ''
      if (this.getType(leftJson) == this.getType(rightJson) && typeof leftJson == 'object') {
        result = this.findDiffJson(leftJson, rightJson)
      } else if (this.getType(leftJson) != this.getType(rightJson)) {
        result = 'diff_type'
      } else {
        result = 'diff_val'
      }
      return result
    },
    delNullInDiffJson(obj) {
      for (let key in obj) {
        if (this.getType(obj[key]) == 'object') {
          if (obj[key] === null) {
            delete obj[key]
          }
          this.delNullInDiffJson(obj[key])
        }
      }
    },

// todo : 새로운 findDiffJson의 리턴데이터를 바탕으로 템플릿 엔진에서 돌릴 배열 생성 ( 완료 ) // countArr 타이밍 수정 해야됨 // replaceAll 지양 // 객체화 result, countArr
    compareObjAndDiffJson(json, diffJson, resultObject) {
      const rows = JSON.stringify(json, null, 1).split('\n')
      const result = resultObject['result']
      let objTabIdx = 0, arrTabIdx = 0 // 0 보다 크면 diff_type 진행중
      for (let row of rows) {
        if (objTabIdx > 0) {
          if (row.indexOf('}') == objTabIdx) {
            objTabIdx = 0
          }
          resultObject['count']++
          continue
        } else if (arrTabIdx > 0) {
          if (row.indexOf(']') == arrTabIdx) {
            arrTabIdx = 0
          }
          resultObject['count']++
          continue
        }

        if ((row.indexOf('{') == 0) && resultObject['first']) {
          resultObject['count']++
          resultObject['first'] = false
          continue
        } else if (row.match('".+": {') !== null) { // JSON
          const key = this.getKey(row)
          if (diffJson.hasOwnProperty(key)) {
            if (diffJson[key] == 'diff_type' || diffJson[key] == 'not_key') {
              result[resultObject['count']] = diffJson[key]
              if (!this.isEmptyObj(row)) {
                objTabIdx = row.search('[^ ]')
                resultObject['count']++
                continue
              }
            } else {
              this.compareObjAndDiffJson(json[key], diffJson[key], resultObject);
            }
          }
        } else if (row.match('".+": \\[') !== null) { // Array
          const key = this.getKey(row)
          if (diffJson.hasOwnProperty(key)) { // 차이점을 발견했을 경우
            if (diffJson[key] == 'diff_type' || diffJson[key] == 'not key') {
              result[resultObject['count']] = diffJson[key]
              if (!this.isEmptyObj(row)) {
                arrTabIdx = row.search('[^ ]')
                resultObject['count']++
                continue
              }
            } else {
              this.compareArrAndDiffJson(json[key], diffJson[key], resultObject)
            }
          }
        } else if (row.match('".+": ') !== null) { // depth ( JSON kv )
          const key = this.getKey(row)
          if (diffJson.hasOwnProperty(key)) {
            result[resultObject['count']] = diffJson[key]
          }
        }
        resultObject['count']++
      }
    },

    // compareByDiffJsonAndJson에 종속적인 함수
    compareArrAndDiffJson(arr, diffJson, resultObject) {
      const result = resultObject['result']
      const rows = JSON.stringify(arr, null, 1).split('\n')
      let objTabIdx = 0, arrTabIdx = 0 // 0보다 크면 diff_type 진행중
      let i = 0;
      for (let row of rows) {
        // diff_type 검증
        if (objTabIdx > 0) {
          if (row.indexOf('}') == objTabIdx) {
            objTabIdx = 0
            i++
          }
          resultObject['count']++
          continue
        } else if (arrTabIdx > 0) {
          if (row.indexOf(']') == arrTabIdx) {
            arrTabIdx = 0
            i++
          }
          resultObject['count']++
          continue
        } // diff_type 검증 끝

        if (row.indexOf('[') == 0 && resultObject['first']) {
          resultObject['count']++
          resultObject['first'] = false
          continue
        } else if (row.match('^\\s+{') !== null) { //JSON
          if (diffJson.hasOwnProperty(i)) {
            if (diffJson[i] == 'diff_type' || diffJson[i] == 'not_idx') {
              result[resultObject['count']] = diffJson[i]
              if(!this.isEmptyObj(row)){
                objTabIdx = row.indexOf('{')
                resultObject['count']++
                continue
              }
            } else {
              this.compareObjAndDiffJson(arr[i], diffJson[i], resultObject)
            }
          }
        } else if (row.match('^\\s+\\[') !== null) { //Array
          if (diffJson.hasOwnProperty(i)) {
            if (diffJson[i] == 'diff_type' || diffJson[i] == 'not_idx') {
              result[resultObject['count']] = diffJson[i]
              if(!this.isEmptyObj(row)){
                arrTabIdx = row.indexOf('[')
                resultObject['count']++
                continue
              }
            } else {
              this.compareArrAndDiffJson(arr[i], diffJson[i], resultObject)
            }
          }
        } else if(row.match('(true|false|\\d+|".+")') !== null){
          if (diffJson.hasOwnProperty(i)) {
            result[resultObject['count']] = diffJson[i]
          }
        } else if(row.match('^\\[')){
          continue
        }
        i++
        resultObject['count']++
      }
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