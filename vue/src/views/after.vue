<template>
  <div id="after">
    <div class="result">
      <div :class="item.err" :key="index" class="row" v-for="(item, index) in leftResultArr">
        {{ index }}. &nbsp; &nbsp;{{ item.str }}
      </div>
    </div>
    <div>
      <button @click="goBack" class="mid-btn">before 페이지로 이동</button>
    </div>
    <div class="result">
      <div :class="item.err" :key="index" class="row" v-for="(item, index) in rightResultArr">
        {{ index }}. &nbsp; &nbsp;{{ item.str }}
      </div>

    </div>

  </div>
</template>

<script>

export default {
  name: "after",
  data() {
    return {
      leftResultArr: Array,
      rightResultArr: Array,
    }
  },
  created() {
    this.getResult(this.$route.query.id)
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

    async getResult(id) {
      try {
        const {data} = await this.$axios.get(`api/getData?id=${id}`)
        if (data) {
          const {rightJson, leftJson, diffJson} = await JSON.parse(data.result)
          this.leftResultArr = this.makeResultArr(leftJson, diffJson)
          this.rightResultArr = this.makeResultArr(rightJson, diffJson)
        } else {
          await alert('잘못된 접근입니다.')
          this.goBack()
        }
      } catch (e) {
        console.log(e)
      }
    },
    makeResultArr(json, diffJson) {
      const jsonStrArr = JSON.stringify(json, null, 5).split('\n')
      const resultList = [];
      const pathList = [];
      console.log(JSON.stringify(json, null, 2))
      console.log(diffJson)
      /*
                  JSON 상황 :           Arr 상황 :
                    k : v ok             str,num ..
                    k : json ok          json
                    k : arr ok           arr
                    k : {}               {}
                    K : []               []
      */

      /*
        todo : diff_type 간 마지막 경로에 따라서 object인 경우, arr인 경우 구분 필요
        배열일 경우 : pop -> 마지막 경로는 number
        객체일 경우 : pop -> 마지막 경로는 string
      */
      let objTabIdx = 0, arrTabIdx = 0
      for (let i = 0; i < jsonStrArr.length; i++) {
        console.log(i, pathList, 'start')
        const str = jsonStrArr[i]
        // diff_type 연산중
        if (objTabIdx > 0) {
          if (str.indexOf('}') == objTabIdx || str.indexOf(']') == objTabIdx) {
            objTabIdx = 0
            pathList.pop()
          }
          resultList[i] = {"str": str, "err": null}
          continue
        } else if (arrTabIdx > 0) { // 마지막 경로는 number
          if (str.indexOf('}') == arrTabIdx || str.indexOf(']') == arrTabIdx) {
            arrTabIdx = 0
            pathList.pop()
            pathList[pathList.length - 1]++
          }
          resultList[i] = {"str": str, "err": null}
          continue
        }

        if (str.match(/^({|})$/)) {
          resultList[i] = {"str": str, "err": this.inspect(diffJson, pathList)}
        } else if (str.match(/^\[$/)) {
          pathList.push(-1)
        } else if (str.match(/^\]$/)) {
          pathList.pop()
          resultList[i] = {"str": str, "err": this.inspect(diffJson, pathList)}
        }

        if (typeof pathList[pathList.length - 1] != 'number') {
          if (str.match(/".+": \[$/)) { // k : arr
            pathList.push(this.getKey(str))
            const errCause = this.inspect(diffJson, pathList)
            if (errCause == 'diff_type') { //key만 들어간 상태
              objTabIdx = str.search('[^ ]')
              resultList[i] = {"str": str, "err": errCause}
              continue
            }
            resultList[i] = {"str": str, "err": errCause === undefined ? null : errCause}
            pathList.push(-1)
          } else if (str.match(/".+": {$/)) { // k : json
            pathList.push(this.getKey(str))
            const errCause = this.inspect(diffJson, pathList)
            if (errCause == 'diff_type') { // key만 들어간 상태
              objTabIdx = str.search('[^ ]')
            }
            resultList[i] = {"str": str, "err": errCause === undefined ? null : errCause}
          } else if (str.match(/".+":/)) { // k : [], {}, value
            pathList.push(this.getKey(str))
            resultList[i] = {"str": str, "err": this.inspect(diffJson, pathList)}
            pathList.pop()
          } else if (str.match(/\s+}/)) {
            resultList[i] = {"str": str, "err": this.inspect(diffJson, pathList)}
            pathList.pop()
          }
        } else { // Arr 시작
          const regExpArr = [/(true|false|\d+,?|".+",?$)/, /^\s+(\[\]|{}),?$/] // value, obj Start, [] {}
          if (regExpArr.find(regExp => str.match(regExp))) {
            pathList[pathList.length - 1]++
          } else if (str.match(/\s+{$/)) { // 배열에서 {로 시작할 때
            pathList[pathList.length - 1]++
            const errCause = this.inspect(diffJson, pathList)
            if (errCause == 'diff_type') { // diff_type skip 추가한 항목
              arrTabIdx = str.search('[^ ]')
              resultList[i] = {"str": str, "err": errCause}
              continue
            }
          } else if (str.match(/^\s+\[$/)) { // 배열에서 [로 시작할 때
            pathList[pathList.length - 1]++
            const errCause = this.inspect(diffJson, pathList)
            if (errCause == 'diff_type') { // diff_type skip 추가한 항목
              arrTabIdx = str.search('[^ ]')
              resultList[i] = {"str": str, "err": errCause}
              continue
            }
            resultList[i] = {"str": str, "err": errCause === undefined ? null : errCause}
            pathList.push(-1)
          } else if (str.match(/".+": {$/)) { // after {
            pathList.push(this.getKey(str))
          } else if (str.match(/".+": \[$/)) {
            pathList.push(this.getKey(str))
            resultList[i] = {"str": str, "err": this.inspect(diffJson, pathList)}
            pathList.push(-1)
          } else if (str.match(/^\s+\],?$/)) { // end Arr todo : 조건 필요 case1 : object, case2 : arr
            pathList.pop()
            resultList[i] = {"str": str, "err": this.inspect(diffJson, pathList)}
            if (typeof pathList[pathList.length - 1] != 'number') {
              pathList.pop()
            }
            continue
          }
          resultList[i] = {"str": str, "err": this.inspect(diffJson, pathList)}
        }
        console.log(i, pathList, 'end')
      }
      return resultList
    },

    inspect(diffJson, pathList) { //
      let position = diffJson
      for (let path of pathList) {
        if (position.hasOwnProperty(path)) {
          if (typeof position[path] == 'object') {
            position = position[path]
          } else {
            return position[path]
          }
        }
      }
    },
  },

}
</script>

<style scoped>
#after {
  display: flex;
  justify-content: space-around;
  width: 100%;
}

.result {
  width: 40%
}

.row {
  font-size: 14px;
  font-weight: bold;
  white-space: pre;
  margin: 2px 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
}

.mid-btn {
  width: max-content;
}

/* 일치 하는 키가 없을 경우 */
.not_key {
  background-color: rgba(128, 128, 128, 0.2) !important;
}

/* type가 일치하지 않을 경우*/
.diff_type {
  background-color: rgba(218, 26, 26, 0.5) !important;
}

/* value가 일치하지 않을 경우 */
.diff_val {
  background-color: rgba(255, 255, 0, 0.3) !important;
}


</style>