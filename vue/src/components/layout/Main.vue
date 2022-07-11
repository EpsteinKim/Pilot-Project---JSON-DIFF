<template>
  <div id="main">
    <textarea @blur="setErrMsg" placeholder="Enter JSON" v-model="leftArea"/>  <!--blur 처리-->
    <div>

      <span v-if="msg" class="msg">{{ msg }}</span>
      <button :disabled="msg" @click="compare">Compare</button>
      <p><a @click="setSample">Sample Data1</a></p>
      <p><a @click="setSample2">Sample Data2</a></p>
    </div>

    <textarea @blur="setErrMsg" placeholder="Enter JSON" v-model="rightArea"/>
  </div>

</template>

<script>
export default {
  name: "Main",
  data() {
    return {
      leftArea: '',
      rightArea: '',
      msg: ''
    }
  },
  methods: {
    async compare() {
      const [leftJson, rightJson] = [JSON.parse(this.leftArea), JSON.parse(this.rightArea)]
      const [leftJsonStr, rightJsonStr] = [JSON.stringify(leftJson), JSON.stringify(rightJson)]
      const rightJsonCopy = JSON.parse(rightJsonStr);
      const id = this.hash(leftJsonStr + rightJsonStr);
      const {data} = await this.$axios.get(`api/isExist?id=${id}`)
      if (data) {
        await this.$router.push({
          name: 'after',
          query: {id}
        })
      } else {
        const diffJson = this.findDiffJson(leftJson, rightJsonCopy)
        this.delNullInDiffJson(diffJson)
        await this.save(id, {leftJson, rightJson, diffJson})
      }
    },
    async save(id, result) { // 수정
      try {
        await this.$axios.post(`api/save`, {
          id: id,
          result: JSON.stringify(result)
        })
        await this.$router.push({
          name: 'after',
          query: {id}
        })
      } catch (e) {
        console.log(e)
      }
    },
    setSample() {
      this.leftArea = '{ "name":"hello", "age":"20", "noKey":"I have no Key", "isGood" : 1}'
      this.rightArea = '{ "name":"world", "age":20, "isGood" : 1}'
      this.setErrMsg()
    },

    setSample2() {
      this.leftArea = '{"Aidan Gillen": {"array": ["Game of Thron\\"es","The Wire"],"string": "some string","int": 2,"aboolean": true, "boolean": true,"object": {"foo": "bar","object1": {"new prop1": "new prop value"},"object2": {"new prop1": "new prop value"},"object3": {"new prop1": "new prop value"},"object4": {"new prop1": "new prop value"}}},"Amy Ryan": {"one": "In Treatment","two": "The Wire"},"Annie Fitzgerald": ["Big Love","True Blood"],"Anwan Glover": ["Treme","The Wire"],"Alexander Skarsgard": ["Generation Kill","True Blood"], "Clarke Peters": null}'
      this.rightArea = '{"Aidan Gillen": {"array": ["Game of Thrones","The Wire"],"string": "some string","int": "2","otherint": 4, "aboolean": "true", "boolean": false,"object": {"foo": "bar"}},"Amy Ryan": ["In Treatment","The Wire"],"Annie Fitzgerald": ["True Blood","Big Love","The Sopranos","Oz"],"Anwan Glover": ["Treme","The Wire"],"Alexander Skarsg?rd": ["Generation Kill","True Blood"],"Alice Farmer": ["The Corner","Oz","The Wire"]}'
      this.setErrMsg()
    },

    hash(str) {
      const cryptoJs = require('crypto-js')
      return cryptoJs.SHA3(str, {outputLength: 256}).toString()
    },
    getType(obj) {
      return Array.isArray(obj) && 'array' || typeof obj
    },
    setErrMsg() {
      this.msg = null
      if (this.leftArea.length == 0 || this.rightArea.length == 0) {
        this.msg = 'input value'
      } else {
        try {
          const leftJson = JSON.parse(this.leftArea)
          const rightJson = JSON.parse(this.rightArea)
          if (this.getType(leftJson) != this.getType(rightJson)) {
            this.msg = 'diff_type'
          }
        } catch (e) {
          this.msg = `can't parse JSON`
        }
      }
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
          delete rightJsonCopy[key] // 키 삭제
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