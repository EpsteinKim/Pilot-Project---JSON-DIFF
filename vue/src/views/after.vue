<template>
  <div id="after">
    <v-container class="d-flex flex-column flex-sm-row justify-space-between py-0 mt-10">

      <div class="order-0 overflow-auto col-auto col-sm-5 result">
          <v-tooltip bottom :key="index" v-for="(item, index) in leftResultArr" close-delay="100" attach>
            <template v-slot:activator="{on, err}">
              <div :class="paintColor(item.err)" class="row" v-bind="err" v-on="item.err? on: null" >{{ index }}. &nbsp; &nbsp;{{ item.str }}</div>
            </template>
            <span>{{item.err}}</span>
          </v-tooltip>
      </div>

      <div class="d-block d-sm-none" style="height:50px"/>

      <v-col class="text-center order-2 order-sm-1">
        <v-btn class="mb-5" elevation="3" @click="goBack" rounded>BACK</v-btn>
      </v-col>

      <div class="order-1 order-sm2 overflow-auto col-auto col-sm-5 result">
        <v-tooltip bottom :key="index" v-for="(item, index) in rightResultArr" close-delay="100" attach>
          <template v-slot:activator="{on, err}">
            <div :class="paintColor(item.err)" class="row" v-bind="err" v-on="item.err? on: null" >{{ index }}. &nbsp; &nbsp;{{ item.str }}</div>
          </template>
          <span>{{item.err}}</span>
        </v-tooltip>
      </div>

    </v-container>
  </div>
</template>

<script>
import colorObj from "@/assets/color";

export default {
  name: "after",
  data() {
    return {
      leftResultArr: Array,
      rightResultArr: Array,
      mouse: {
        X: 0,
        Y: 0,
        show: false,
        type: null
      }
    }
  },
  created() {
    const afterStorage = this.$store.state.afterStorage
    if (afterStorage.length) {
      for (let obj of afterStorage) {
        if (this.$route.query.id === obj.id) {
          this.leftResultArr = JSON.parse(obj.result.leftResultArr)
          this.rightResultArr = JSON.parse(obj.result.rightResultArr)
          return
        }
      }
    }

    try {
      if (this.$route.params.hasOwnProperty('result')) {
        const {leftJson, rightJson, diffJson} = this.$route.params.result
        this.leftResultArr = this.makeResultArr(leftJson, diffJson)
        this.rightResultArr = this.makeResultArr(rightJson, diffJson)
      } else { // uri를 입력하고 들어올때
        this.makeResult(this.$route.query.id)
      }
    } catch (e) {
      alert(e.response)
    }
    this.$store.commit('addAfter', {
      id: this.$route.query.id,
      result: {
        leftResultArr: JSON.stringify(this.leftResultArr),
        rightResultArr: JSON.stringify(this.rightResultArr)
      }
    })

  },
  methods: {
    async goBack() {
      await this.$router.push({
        name: 'before',
        params: {
          id: this.$route.query.id
        }
      })
    },
    getKey(str) {
      return str.match('".+"(?=:)')[0].replace(/"/g, '')
    },
    getType(obj) {
      return Array.isArray(obj) && 'array' || typeof obj
    },
    paintColor(str) {
      if (colorObj.hasOwnProperty(str)) {
        return colorObj[str]
      } else {
        return null
      }
    },
    addErrMouseEvent(event) {
      const errArr = ['diff_type', 'diff_val', 'not_key']
      this.$refs["move-chip"].$el.style.left = event.pageX + 'px'
      this.$refs["move-chip"].$el.style.top = (event.pageY - 40) + 'px'
      for (let err of errArr) {
        if (event.target.classList.contains(err)) {
          this.mouse.show = true
          this.mouse.type = err
          return
        }
      }
      this.mouse.show = false
    },
    async makeResult(id) {
      try {
        const {data} = await this.$axios.get(`api/results/${id}`)
        console.log('hi')
        if (data.id) {
          const {rightJson, leftJson, diffJson} = JSON.parse(data.result)
          this.leftResultArr = this.makeResultArr(leftJson, diffJson)
          this.rightResultArr = this.makeResultArr(rightJson, diffJson)
        } else {
          await alert('잘못된 접근입니다.')
          this.goBack()
        }
      } catch (e) {
        alert(e.response)
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
.test {
  position: absolute;
  top: 0px;
  left: 0px;
  font-size: 14px;
}

.result {
  height: unset;
}

.row {
  font-size: 14px;
  font-weight: bold;
  white-space: pre;
  margin: 2px 0;
  display: inline-block;
  min-width: 400px;
  cursor: pointer;
}

@media (max-width: 600px) {
  .result {
    height: 300px
  }
}

</style>