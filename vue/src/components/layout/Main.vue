<template>
  <div id="main">
    <textarea placeholder="Enter JSON" v-model="leftArea"/>
    <div>

      <span v-if="isErrExist" class="msg">{{ msg }}</span>
      <button :disabled="isErrExist" @click="compare" >Compare</button>
      <button @click="setSample3">sample</button>
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
      msg: 'compare'
    }
  },
  computed: {
    isErrExist(){
      if(this.leftArea.length == 0 || this.rightArea.length == 0){
        this.msg = 'input value'
        return true
      }else{
        try{
          JSON.parse(this.leftArea)
          JSON.parse(this.rightArea)
        }catch(e){
          this.msg =  `can't parse JSON`
          return true
        }
      }
      return false
    },
  },
  methods: {

    compare() {
      let leftJson = '', rightJson = ''
      try {
        [leftJson, rightJson] = [JSON.parse(this.leftArea), JSON.parse(this.rightArea)]
      } catch (e) {
        alert('올바른 JSON 형식을 입력하세요')
        return
      }
      if (this.getType(leftJson) != this.getType(rightJson)) {
        alert('다른 type과 비교할 수 없습니다.')
        return
      }
      const diffJson = this.findDiffJson(leftJson, JSON.parse(JSON.stringify(rightJson)))
      this.delNullInDiffJson(diffJson)
      const [leftJsonStr, rightJsonStr, diffJsonStr] = [JSON.stringify(leftJson), JSON.stringify(rightJson), JSON.stringify(diffJson)]
      const id = this.hash(leftJsonStr + rightJsonStr);
      if (leftJsonStr == rightJsonStr) {
        this.save(id, leftJsonStr, rightJsonStr, '{}')
      } else {
        this.save(id, leftJsonStr, rightJsonStr, diffJsonStr)
      }
    },

    async save(id, leftJsonStr, rightJsonStr, diffJsonStr) {
      try {
        const response = await this.$axios.get(`api/isExist?id=${id}`)
        if (!response.data) { // DB에 값이 없는 경우
          await this.$axios.post(`api/save`, {
            id: id,
            leftJson: leftJsonStr,
            rightJson: rightJsonStr,
            diffJson: diffJsonStr
          })
        }
        await this.$router.push({
          name: 'after',
          query: {id}
        })
      } catch (error) {
        console.log(error)
      }
    },
    setSample() {
      this.leftArea = '{ "name":"hello", "age":"20", "noKey":"I have no Key", "isGood" : 1}'
      this.rightArea = '{ "name":"world", "age":20, "isGood" : 1}'
    },

    setSample3() {
      this.leftArea = '{"Aidan Gillen": {"array": ["Game of Thron\\"es","The Wire"],"string": "some string","int": 2,"aboolean": true, "boolean": true,"object": {"foo": "bar","object1": {"new prop1": "new prop value"},"object2": {"new prop1": "new prop value"},"object3": {"new prop1": "new prop value"},"object4": {"new prop1": "new prop value"}}},"Amy Ryan": {"one": "In Treatment","two": "The Wire"},"Annie Fitzgerald": ["Big Love","True Blood"],"Anwan Glover": ["Treme","The Wire"],"Alexander Skarsgard": ["Generation Kill","True Blood"], "Clarke Peters": null}'
      this.rightArea = '{"Aidan Gillen": {"array": ["Game of Thrones","The Wire"],"string": "some string","int": "2","otherint": 4, "aboolean": "true", "boolean": false,"object": {"foo": "bar"}},"Amy Ryan": ["In Treatment","The Wire"],"Annie Fitzgerald": ["True Blood","Big Love","The Sopranos","Oz"],"Anwan Glover": ["Treme","The Wire"],"Alexander Skarsg?rd": ["Generation Kill","True Blood"],"Alice Farmer": ["The Corner","Oz","The Wire"]}'
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

    findDiffJson(leftJson, rightJsonCopy) {
      if (JSON.stringify(leftJson) == JSON.stringify(rightJsonCopy)) {
        return
      }
      const diffJson = {}
      for (let key in leftJson) {
        if (!rightJsonCopy.hasOwnProperty(key)) {
          diffJson[key] = 'not_key'
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
      if (obj == null) {
        return
      }
      for (let key in obj) {
        if (this.getType(obj[key]) == 'object') {
          if (obj[key] === null) {
            delete obj[key]
            continue
          }
          this.delNullInDiffJson(obj[key])
        }
      }
    },
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

.msg {
  text-align: center;
}
</style>