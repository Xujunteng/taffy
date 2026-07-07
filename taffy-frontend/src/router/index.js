import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'
const routes=[{path:'/',redirect:'/dashboard'},{path:'/login',component:()=>import('../views/Login.vue'),meta:{guest:true}},{path:'/register',component:()=>import('../views/Register.vue'),meta:{guest:true}},{path:'/dashboard',component:()=>import('../views/Dashboard.vue')},{path:'/voices',component:()=>import('../views/Voices.vue')},{path:'/tts',component:()=>import('../views/TTS.vue')},{path:'/live',component:()=>import('../views/Live.vue')},{path:'/scripts',component:()=>import('../views/Scripts.vue')},{path:'/feedback',component:()=>import('../views/Feedback.vue')},{path:'/stats',component:()=>import('../views/Stats.vue')},{path:'/help',component:()=>import('../views/Help.vue')}]
const router=createRouter({history:createWebHistory(),routes})
router.beforeEach((to)=>{const store=useUserStore(); if(!to.meta.guest&&!store.isLogin)return '/login'; if(to.meta.guest&&store.isLogin)return '/dashboard'})
export default router
