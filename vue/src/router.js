import Vue from 'vue'
import Router from 'vue-router'
import before from './views/before'
import after from './views/after'

Vue.use(Router)

export default new Router({
    mode: 'history',
    routes:[
        {
            path: '/',
            name: 'before',
            component: before
        },
        {
            path: '/after',
            name: 'after',
            props:true,
            component: after,
        }
    ]
})