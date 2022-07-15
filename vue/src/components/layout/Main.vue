<template>
  <div>
    <v-container class="">
      <v-alert color="red lighten-2" :class="commonErr? 'visible' : 'hidden'" class="mx-auto mb-0 font-weight-bold"
               elevation="4"
               type="error">
        {{ commonErr }}
      </v-alert>
    </v-container>

    <v-form ref="form">
      <v-container class="d-flex flex-column flex-sm-row justify-space-between py-0">
        <v-textarea class="order-0" label="LeftJson" hint="enter Json" v-model="leftArea" filled
                    rows="15" no-resize :rules="this.rules.jsonRules" full-width/>

        <v-col class="text-center order-2 order-sm-1 col-sm-3 d-flex flex-column justify-space-around py-5">
          <v-btn class="mx-auto mb-3 mb-sm-0" :disabled="disabled" elevation="3" @click="compare" rounded>
            Compare
          </v-btn>
          <v-btn class="mx-auto mb-3 mb-sm-0" elevation="3" @click="reset" rounded>
            Reset
          </v-btn>
          <v-btn @click="test" class="d-none">test</v-btn>
          <a @click="setSample" class="d-block mb-3 mb-sm-0">Sample Data1</a>
          <a @click="setSample2" class="d-block mb-3 mb-sm-0">Sample Data2</a>
          <v-card v-if="searchList[0]">
            <v-list dense>
              <v-list-item v-for="(search,i) in searchList" :key="i" @click="goAfter(search.id)" class="py-0" ripple>
                <v-list-item-content v-text="i+1+'번째 결과'" >
                </v-list-item-content>
              </v-list-item>
            </v-list>
          </v-card>
        </v-col>

        <v-textarea class="order-1 order-sm-2" label="RightJson" hint="enter Json" v-model="rightArea"
                    filled rows="15" no-resize :rules="this.rules.jsonRules" full-width/>
      </v-container>

      <v-container>

      </v-container>

    </v-form>

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
      disabled: false,
      rules: {
        jsonRules: [v => (!!v) || 'Input Value', v => this.canJsonParse(v) || `Can't Parse JSON`]
      },
      searchList: null
    }
  },
  created() {
    try {
      if(this.$store.state.beforeStorage){
        this.searchList = this.$store.state.beforeStorage
        console.log(this.searchList)
      }
      for (let before of this.$store.state.beforeStorage) {
        if (this.$route.params.id == before.id) {
          this.leftArea = before.result.leftArea
          this.rightArea = before.result.rightArea
          return
        }
      }
    } catch (e) {
      console.log(e)
    }
  },
  methods: {
    test() {
      const temp = this.$store.state.afterStorage
      this.$store.commit("addBefore",{
        id : 1,
        result: null
      })
      console.log(temp)
    },
    async compare() {
      if (!this.$refs.form.validate()) {
        return
      }
      const [leftJson, rightJson] = [JSON.parse(this.leftArea), JSON.parse(this.rightArea)]
      if (this.getType(leftJson) != this.getType(rightJson)) {
        this.commonErr = `Can't Compare With Different Type`
        return
      }
      const [leftJsonStr, rightJsonStr] = [JSON.stringify(leftJson), JSON.stringify(rightJson)]
      const rightJsonCopy = JSON.parse(rightJsonStr);
      const id = this.hash(leftJsonStr + rightJsonStr);
      const afterStorage = this.$store.state.afterStorage
      if (afterStorage.length) { // afterStorage에 키가 존재한다면
        for (let obj of afterStorage) {
          if (id === obj.id) {
            await this.$router.push({
              name: 'after',
              query: {id},
            })
            return
          }
        }
      }
      try {
        const {data} = await this.$axios.get(`api/results/${id}`)
        if (data.id) { // DB에 데이터가 존재한다면
          const diffJson = this.findDiffJson(leftJson, rightJsonCopy)
          await this.$router.push({
            name: 'after',
            query: {id},
            params: {
              result: {
                leftJson, rightJson, diffJson
              }
            }
          })
        } else { // DB에 데이터가 존재하지 않는다면
          const diffJson = this.findDiffJson(leftJson, rightJsonCopy)
          this.delNullInDiffJson(diffJson)
          if (!this.$store.getters.contains(id)) { // 중복 방지
            this.$store.commit('addBefore', {
              id,
              result: {
                leftArea: leftJsonStr,
                rightArea: rightJsonStr
              }
            })
          }
          await this.posts(id, {leftJson, rightJson, diffJson})
        }
      } catch (e) {
        this.commonErr = e.response.statusText
      }
    },
    reset() {
      this.$refs.form.reset()
    },

    async posts(id, result) { // 수정
      try {
        await this.$axios.post(`api/results/posts`, {
          id: id,
          result: JSON.stringify(result)
        })
        await this.$router.push({
          name: 'after',
          query: {id},
          params: {
            result
          }
        })
      } catch (e) {
        this.commonErr = e.response.statusText
      }
    },

    setSample() {
      this.leftArea = '{ "name":"hello", "age":"20", "noKey":"I have no Key", "isGood" : 1}'
      this.rightArea = '{ "name":"world", "age":20, "isGood" : 1}'
      this.$refs.form.validate()
    },

    setSample2() {
      this.leftArea = '{"Aidan Gillen": {"array": ["Game of Thron\\"es","The Wire"],"string": "some string","int": 2,"aboolean": true, "boolean": true,"object": {"foo": "bar","object1": {"new prop1": "new prop value"},"object2": {"new prop1": "new prop value"},"object3": {"new prop1": "new prop value"},"object4": {"new prop1": "new prop value"}}},"Amy Ryan": {"one": "In Treatment","two": "The Wire"},"Annie Fitzgerald": ["Big Love","True Blood"],"Anwan Glover": ["Treme","The Wire"],"Alexander Skarsgard": ["Generation Kill","True Blood"], "Clarke Peters": null}'
      this.rightArea = '{"Aidan Gillen": {"array": ["Game of Thrones","The Wire"],"string": "some string","int": "2","otherint": 4, "aboolean": "true", "boolean": false,"object": {"foo": "bar"}},"Amy Ryan": ["In Treatment","The Wire"],"Annie Fitzgerald": ["True Blood","Big Love","The Sopranos","Oz"],"Anwan Glover": ["Treme","The Wire"],"Alexander Skarsg?rd": ["Generation Kill","True Blood"],"Alice Farmer": ["The Corner","Oz","The Wire"]}'
    },

    hash(str) {
      const cryptoJs = require('crypto-js')
      return cryptoJs.SHA3(str, {outputLength: 256}).toString()
    },
    getType(obj) {
      return Array.isArray(obj) && 'array' || typeof obj
    },
    goAfter(id){
      this.$router.push({
        name:'after',
        query:{id: id}
      })
    },


    canJsonParse(str) {
      try {
        if (str.match(/(^\[.*\]$|^{.*}$)/)) {
          JSON.parse(str)
        } else {
          this.disabled = true
          return false
        }
      } catch (e) {
        this.disabled = true
        return false
      }
      this.disabled = false
      return true
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

.visible {
  visibility: visible;
}

.hidden {
  visibility: hidden;
}

.min-height {
  min-height: 400px;
}

@media (max-width: 600px) {
  .min-height {
    min-height: 300px;
  }
}
</style>