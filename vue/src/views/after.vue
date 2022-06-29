<template>
  <div id="after">
    <div>
      <div v-bind:class="item.err" class="result" v-for="item in this.getLeftJsonStr()">
        {{ item.str.startsWith('\"') ? "&nbsp &nbsp" + item.str : item.str }}
      </div>
    </div>
    <div>
      <button @click="goBefore">before 페이지로 이동</button>
    </div>
    <div>
      <div v-bind:class="item.err" class="result" v-for="item in this.getRightJsonStr()">
        {{ item.str.startsWith('\"') ? "&nbsp &nbsp" + item.str : item.str }}
      </div>
    </div>

  </div>
</template>

<script>

export default {
  name: "after",
  methods: {
    goBefore() {
      this.$router.push('/');
    },
    getLeftJsonStr() {
      const leftJsonStr = JSON.stringify(this.$route.params.leftJson, null, 2);
      const diffJson = this.$route.params.diffJson;
      const resultList = [];

      const tmpArr = leftJsonStr.split('\n');
      for (let str of tmpArr) {
        str = str.trim();
        if (str.startsWith('\"')) {
          const key = str.match('"\\w*"')[0].replaceAll('"', '')
          // 문제가 있는 경우
          if(diffJson.hasOwnProperty(key)){
            const reason = diffJson[key].description;
            resultList.push({
              "str" : str,
              "err" : reason
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
    },
    getRightJsonStr() {
      const rightJsonStr = JSON.stringify(this.$route.params.rightJson, null, 2);
      const diffJson = this.$route.params.diffJson;
      const resultList = [];

      const tmpArr = rightJsonStr.split('\n');
      for (let str of tmpArr) {
        str = str.trim();
        if (str.startsWith('\"')) {
          const key = str.match('"\\w*"')[0].replaceAll('"', '')
          // 문제가 있는 경우
          if(diffJson.hasOwnProperty(key)){
            const reason = diffJson[key].description;
            resultList.push({
              "str" : str,
              "err" : reason
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
#after {
  display: flex;
  justify-content: space-around;
}

.result {
  font-size: 14px;
  font-weight: bold;
}
/* 일치 하는 키가 없을 경우 */
.nk{ background-color : rgba(255, 0, 0, 0.2); }
/* type이 일치하지 않을 경우*/
.dt{ background-color : rgba(128, 128, 128, 0.2); }
/* value가 일치하지 않을 경우 */
.dv{ background-color : rgba(255, 255, 0, 0.3); }

</style>