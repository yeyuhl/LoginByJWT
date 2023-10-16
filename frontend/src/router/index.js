import {createRouter, createWebHistory} from "vue-router";
import {unauthorized} from "@/net";

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: "/",
            name: "welcome",
            component: () => import("@/views/WelcomeView.vue"),
            children: [
                {
                    path: "",
                    name: "welcome-login",
                    component: () => import("@/views/welcome/LoginPage.vue")
                },
                {
                    path: "register",
                    name: "welcome-register",
                    component: () => import("@/views/welcome/RegisterPage.vue")
                }, {
                    path: "forget",
                    name: "welcome-forget",
                    component: () => import("@/views/welcome/ForgetPage.vue")
                }
            ]
        }, {
            path: "/index",
            name: "index",
            component: () => import("@/views/IndexView.vue"),
        }
    ]
})

// 路由守卫，防止未登录用户访问需要登录的页面或者重复登录
router.beforeEach((to, from, next) => {
    const isUnauthorized = unauthorized()
    // 已经登录，但是访问登录页面，直接跳转到index
    if (to.name.startsWith('welcome') && !isUnauthorized) {
        next('/index')
    }
    // 未登录，但是访问需要登录的页面，直接跳转到登录页面
    else if (to.fullPath.startsWith('/index') && isUnauthorized) {
        next('/')
    } else {
        next()
    }
})

export default router