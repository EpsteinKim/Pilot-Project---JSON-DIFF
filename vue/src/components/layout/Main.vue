<template>
  <div>
    <v-container class="">
      <v-alert color="red lighten-2" :class="commonErr? 'visible' : 'hidden'" class="mx-auto mb-0 font-weight-black" elevation="4"
               type="error">
        {{ commonErr }}
      </v-alert>
    </v-container>

    <v-container id="main" class="d-flex flex-column flex-sm-row justify-space-between py-0">
      <div class="order-0">
        <v-textarea label="LeftJson" @blur="setErrMsg()" hint="enter Json" v-model="leftArea" filled rows="15"
                    no-resize/>
        <v-alert color="blue lighten-4" :class="leftErr? 'visible' : 'hidden'" class="mx-auto mb-5 my-sm-0" elevation="4"
                 type="error">
          {{ leftErr }}
        </v-alert>
      </div>


      <v-col class="text-center order-2 order-sm-1">
        <v-btn class="mb-5" elevation="3" :disabled="leftErr != null || rightErr != null" @click="compare" rounded>Compare</v-btn>
        <p><a @click="setSample">Sample Data1</a></p>
        <p><a @click="setSample2">Sample Data2</a></p>
      </v-col>

      <div class="order-1 order-sm-2">
        <v-textarea label="RightJson" @blur="setErrMsg()" hint="enter Json" v-model="rightArea" filled rows="15"
                    no-resize class="row"/>
        <v-alert color="blue lighten-4" :class="rightErr? 'visible' : 'hidden'" class="mx-auto" elevation="4"
                 type="error">
          {{ rightErr }}
        </v-alert>
      </div>

    </v-container>

  </div>

</template>

<script>
export default {
  name: "Main",
  data() {
    return {
      leftArea: '',
      rightArea: '',
      commonErr: null,
      leftErr: null,
      rightErr: null
    }
  },
  methods: {
    async compare() {
      if(this.leftArea.length === 0 && this.rightArea.length === 0){
        this.leftErr = 'Input Value'
        this.rightErr = 'Input Value'
        return
      }
      const [leftJson, rightJson] = [JSON.parse(this.leftArea), JSON.parse(this.rightArea)]

      if(this.getType(leftJson) != this.getType(rightJson)){
        this.commonErr = `Can't Compare With Different Type`
        return
      }

      const [leftJsonStr, rightJsonStr] = [JSON.stringify(leftJson), JSON.stringify(rightJson)]
      const rightJsonCopy = JSON.parse(rightJsonStr);
      const id = this.hash(leftJsonStr + rightJsonStr);
      try {
        const {data} = await this.$axios.get(`api/isExist?id=${id}`)
        if (data) {
          const diffJson = this.findDiffJson(leftJson, rightJsonCopy)
          await this.$router.push({
            name: 'after',
            query: {id},
            params:{
              result:{
                leftJson, rightJson, diffJson
              }
            }
          })
        } else {
          const diffJson = this.findDiffJson(leftJson, rightJsonCopy)
          this.delNullInDiffJson(diffJson)
          await this.save(id, {leftJson, rightJson, diffJson})
        }
      } catch (e) {
        this.commonErr = e.response.statusText
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
      this.leftErr = null
      this.rightErr = null

      if (this.leftArea.length === 0) {
        this.leftErr = 'Input Value'
      }else{
        try{
          JSON.parse(this.leftArea)
        }catch(e){
          this.leftErr = `Can't Parse JSON`
        }
      }
      if (this.rightArea.length === 0) {
        this.rightErr = 'Input Value'
      }else{
        try{
          JSON.parse(this.rightArea)
        }catch(e){
          this.rightErr = `Can't Parse JSON`
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
a {
  text-decoration-line: underline;
  text-decoration-color: cornflowerblue;
}

#main > div {
  width: 100%;
}

.visible {
  visibility: visible;
}

.hidden {
  visibility: hidden;
}

.msg {
  text-align: center;
}
</style>