<template>
  <div id="after">

    <div class="result">
      <div :key="index" :class="item.err" class="row" v-for="(item, index) in leftResultArr">
        {{ index }}. &nbsp; &nbsp;{{ item.str }}
      </div>
    </div>

    <div>
      <button @click="goBack" class="mid-btn">before 페이지로 이동</button>
    </div>

    <div class="result">
        <div :key="index" :class="item.err" class="row" v-for="(item, index) in rightResultArr">
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

      /*
                  JSON 상황 :           Arr 상황 :
                    k : v ok             str,num ..
                    k : json ok          json
                    k : arr ok           arr
                    k : {}               {}
                    K : []               []
      */

      let tabIdxObj = {'tabIdx': 0, 'prevType': null}

      for (let i = 0; i < jsonStrArr.length; i++) {
        const str = jsonStrArr[i]

        // diff_type, not_key 연산
        if (tabIdxObj.tabIdx !== 0) {
          if (str.indexOf('}') === tabIdxObj.tabIdx || str.indexOf(']') === tabIdxObj.tabIdx) {
            tabIdxObj.tabIdx = 0
            if (tabIdxObj.prevType === 'json') {
              pathList.pop()
            }
          }
          resultList[i] = {"str": str, "err": null}
          continue
        }
        // diff_type, not_key 연산 종료
        
        if (str.match(/^({|})/)) { // {}도 포함
          resultList[i] = {"str": str, "err": this.getErrCause(diffJson, pathList)}
        } else if (str.match(/^\[$/)) {
          pathList.push(-1)
        } else if (str.match(/^\[\]$/)) {
          pathList.push(-1)
          resultList[i] = {"str": str, "err": null}
          continue
        } else if (str.match(/^\]$/)) {
          pathList.pop()
          resultList[i] = {"str": str, "err": this.getErrCause(diffJson, pathList)}
        }

        if (typeof pathList[pathList.length - 1] != 'number') {
          if (str.match(/".+": \[$/)) { // k : arr
            pathList.push(this.getKey(str))
            const errCause = this.getErrCause(diffJson, pathList)
            if (errCause === 'diff_type' || errCause === 'not_key') { //key만 들어간 상태
              tabIdxObj = {tabIdx: str.search('[^ ]'), prevType: 'json'}
              resultList[i] = {"str": str, "err": errCause}
              continue
            }
            resultList[i] = {"str": str, "err": errCause}
            pathList.push(-1)
          } else if (str.match(/".+": {$/)) { // k : json
            pathList.push(this.getKey(str))
            const errCause = this.getErrCause(diffJson, pathList)
            if (errCause === 'diff_type' || errCause === 'not_key') { // key만 들어간 상태
              tabIdxObj = {tabIdx: str.search('[^ ]'), prevType: 'json'}
            }
            resultList[i] = {"str": str, "err": errCause}
          } else if (str.match(/".+":/)) { // k : [], {}, value
            pathList.push(this.getKey(str))
            resultList[i] = {"str": str, "err": this.getErrCause(diffJson, pathList)}
            pathList.pop()
          } else if (str.match(/\s+}/)) {
            resultList[i] = {"str": str, "err": this.getErrCause(diffJson, pathList)}
            pathList.pop()
          }
        } else { // Arr 시작
          const regExpArr = [/(true|false|\d+,?|".+",?$)/, /^\s+(\[\]|{}),?$/] // value, [] {}
          if (regExpArr.find(regExp => str.match(regExp))) {
            pathList[pathList.length - 1]++
          } else if (str.match(/\s+{$/)) { // 배열에서 {로 시작할 때
            pathList[pathList.length - 1]++
            const errCause = this.getErrCause(diffJson, pathList)
            if (errCause === 'diff_type' || errCause === 'not_key') { // diff_type skip 추가한 항목
              tabIdxObj = {tabIdx: str.search('[^ ]'), prevType: 'arr'}
              resultList[i] = {"str": str, "err": errCause}
              continue
            }
          } else if (str.match(/^\s+\[$/)) { // 배열에서 [로 시작할 때
            pathList[pathList.length - 1]++
            const errCause = this.getErrCause(diffJson, pathList)
            if (errCause === 'diff_type' || errCause === 'not_key') { // diff_type skip 추가한 항목
              tabIdxObj = {tabIdx: str.search('[^ ]'), prevType: 'arr'}
              resultList[i] = {"str": str, "err": errCause}
              continue
            }
            resultList[i] = {"str": str, "err": errCause}
            pathList.push(-1)
          } else if (str.match(/".+": {$/)) { // after {
            pathList.push(this.getKey(str))
          } else if (str.match(/".+": \[$/)) {
            pathList.push(this.getKey(str))
            resultList[i] = {"str": str, "err": this.getErrCause(diffJson, pathList)}
            pathList.push(-1)
          } else if (str.match(/^\s+\],?$/)) { // end Arr todo : 조건 필요 case1 : object, case2 : arr
            pathList.pop()
            resultList[i] = {"str": str, "err": this.getErrCause(diffJson, pathList)}
            if (typeof pathList[pathList.length - 1] != 'number') {
              pathList.pop()
            }
            continue
          }
          resultList[i] = {"str": str, "err": this.getErrCause(diffJson, pathList)} // arr 일 경우 만 실행됨
        }
      }
      return resultList
    },

    getErrCause(diffJson, pathList) { //
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
  width: 400px;
  overflow-x: auto;
}

.row {
  font-size: 14px;
  font-weight: bold;
  white-space: pre;
  margin: 2px 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
  display: inline-block;
  min-width: 400px;
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