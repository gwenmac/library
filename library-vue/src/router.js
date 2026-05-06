import { createRouter, createWebHashHistory} from 'vue-router';
import { isLoggedIn } from './auth/api.js';

import Home from "./Home.vue";
import BookList from "./book/List.vue";
import BookEdit from "./book/Edit.vue";
import BookNew from "./book/New.vue";
import GaugeList from "./gauge/List.vue";
import GaugeDetail from "./gauge/Detail.vue";
import GaugeNew from "./gauge/New.vue";
import Login from "./auth/Login.vue";
import Bootstrap from "./auth/Bootstrap.vue";
import AdminUsers from "./admin/Users.vue";

const routes = [
    { path: '/', component: Home },
    { path: '/book/list', component: BookList },
    { path: '/book/new', component: BookNew },
    { path: '/book/edit/:id', component: BookEdit },
    { path: '/gauge/list', component: GaugeList },
    { path: '/gauge/new', component: GaugeNew },
    { path: '/gauge/:id', component: GaugeDetail },
    { path: '/login', component: Login, meta: { public: true } },
    { path: '/bootstrap', component: Bootstrap, meta: { public: true } },
    { path: '/admin/users', component: AdminUsers, meta: { requiresAdmin: true } }
];

const router = createRouter({
    history: createWebHashHistory(),
    routes,
});

router.beforeEach((to, from, next) => {
    if (to.meta.public) {
        next();
    } else if (!isLoggedIn()) {
        next('/login');
    } else {
        next();
    }
});

export default router;