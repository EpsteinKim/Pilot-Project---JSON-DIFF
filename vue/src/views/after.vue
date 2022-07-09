<template>
  <div id="after">
    <div>
      <div :class="item.err" :key="index" class="result" v-for="(item, index) in leftResultArr">
       {{index}}. &nbsp; &nbsp;{{ item.str }}
      </div>
    </div>
    <div>
      <button @click="goBack">before 페이지로 이동</button>
    </div>
    <div>
      <div :class="item.err" :key="index" class="result" v-for="(item, index) in rightResultArr">
        {{index}}. &nbsp; &nbsp;{{ item.str }}
      </div>

    </div>

  </div>
</template>

<script>

export default {
  name: "after",
  data() {
    return {
      leftJson: {},
      rightJson: {},
      diffJson: {},
    }
  },
  computed: {
    rightResultArr(){
      return this.makeResultJson(this.rightJson, this.diffJson)
    },
    leftResultArr(){
      return this.makeResultJson(this.leftJson, this.diffJson)
    }
  },
  created() {
    this.getJsonStrArr(this.$route.query.id)
  },
  methods: {
    goBack() {
      this.$router.push('/')
    },
    getKey(str) {
      return str.match('".+"(?=:)')[0].replace(/"/g, '')
    },
    getType(obj) {
      return Array.isArray(obj) && 'array' || typeof obj
    },

    async getJsonStrArr(id) {
      try {
        const {data} = await this.$axios.get(`api/getData?id=${id}`)
        this.rightJson = await JSON.parse(data.rightJson)
        this.leftJson = await JSON.parse(data.leftJson)
        this.diffJson = await JSON.parse(data.diffJson)
      } catch (e) {
        const {isExist} = await this.$axios.get(`api/isExist?id=${id}`)

        if(!isExist) {
          await alert('잘못된 접근입니다.')
          this.goBack()
        }

      }
    },
    isMatch(str, regexp) {
      return str.match(regexp) !== null
    },
    makeResultJson(json, diffJson) {
      const jsonStrArr = JSON.stringify(json, null, 10).split('\n')
      const resultList = [];
      const whereList = [];
      /*
                  JSON 상황 :           Arr 상황 :
                    k : v ok             str,num ..
                    k : json ok          json
                    k : arr ok           arr
                    k : {}               {}
                    K : []               []
      */

      for (let i = 0; i < jsonStrArr.length; i++) {
        const str = jsonStrArr[i]

        if (this.isMatch(str, /^({|})/)) { // JSON 시작
          resultList[i] = {"str": str, "err": this.inspect(diffJson, whereList)}
        } else if (this.isMatch(str, /^\[/)) {
          whereList.push(-1)
        } else if (this.isMatch(str, /^\]/)) {
          whereList.pop()
          resultList[i] = {"str": str, "err": this.inspect(diffJson, whereList)}
        }

        if (typeof whereList[whereList.length - 1] != 'number') {
          if (this.isMatch(str, /".+": \[$/)) { // k : arr
            whereList.push(this.getKey(str))
            resultList[i] = {"str": str, "err": this.inspect(diffJson, whereList)}
            whereList.push(-1)
          } else if (this.isMatch(str, /".+": {$/)) { // k : json
            whereList.push(this.getKey(str))
            resultList[i] = {"str": str, "err": this.inspect(diffJson, whereList)}
          } else if (this.isMatch(str, /".+":/)) { // k : [], {}, value
            whereList.push(this.getKey(str))
            resultList[i] = {"str": str, "err": this.inspect(diffJson, whereList)}
            whereList.pop()
          } else if (this.isMatch(str, /\s+}/)) {
            whereList.pop()
            resultList[i] = {"str": str, "err": this.inspect(diffJson, whereList)}
          }
        } else { // Arr 시작
          if (this.isMatch(str, /(true|false|\d+|".+",?$)/)) {
            whereList[whereList.length - 1]++
          } else if (this.isMatch(str, /\s+{$/)) {
            whereList[whereList.length - 1]++
          } else if (this.isMatch(str, /\s+\[/)) {
            whereList[whereList.length - 1]++
          } else if (this.isMatch(str, /".+": {$/)) {
            whereList.push(this.getKey(str))
          } else if (this.isMatch(str, /\s+\]/)) {
            whereList.pop()
            whereList.pop()
          }
          resultList[i] = {"str": str, "err": this.inspect(diffJson, whereList)}
        }
        console.log(i + 'end', whereList, this.inspect(diffJson,whereList))
      }
      return resultList
    },

    inspect(diffJson, whereList) { //
      let position = diffJson
      for (let path of whereList) {
        if (position.hasOwnProperty(path)) {
          if (typeof position[path] == 'object') {
            position = position[path]
          } else {
            return position[path]
          }
        }
      }
    }
  },

}
</script>

<style scoped>
#after {
  display: flex;
  justify-content: space-around;
}

.result {
  font-size: 14px;
  font-weight: bold;
  white-space: pre;
  margin: 2px 0;
}
.middle_bar{
  background-color:black;
  height:20px;
  position: sticky;
  top:100px;
  width:20px
}

/* 일치 하는 키가 없을 경우 */
.not_key {
  background-color: rgba(128, 128, 128, 0.2);
}

/* type가 일치하지 않을 경우*/
.diff_type {
  background-color: rgba(218, 26, 26, 0.5);
}

/* value가 일치하지 않을 경우 */
.diff_val {
  background-color: rgba(255, 255, 0, 0.3);
}


</style>