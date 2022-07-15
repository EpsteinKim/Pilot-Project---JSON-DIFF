import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

export default new Vuex.Store({
    state: {
        beforeStorage: [],
        afterStorage: [],
    },
    mutations: {
        addBefore(state, payload) {
            state.beforeStorage.push({
                id: payload.id,
                result: payload.result
            })
        },
        addAfter(state, payload) {
            state.afterStorage.push({
                id: payload.id,
                result: payload.result
            })
        }
    },
    getters:{
        contains: state => (id) => {
            for(let obj of state.beforeStorage){
                if(obj.id === id){
                    return true
                }
            }
            return false
        }
    }

})