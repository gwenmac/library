import { createRouter, createWebHashHistory} from 'vue-router';

import Home from "./Home.vue";
import BookList from "./book/List.vue";
import BookEdit from "./book/Edit.vue";
import BookNew from "./book/New.vue";
import GaugeList from "./gauge/List.vue";
import GaugeDetail from "./gauge/Detail.vue";
import GaugeNew from "./gauge/New.vue";

const routes = [
    { path: '/', component: Home },
    { path: '/book/list', component: BookList },
    { path: '/book/new', component: BookNew },
    { path: '/book/edit/:id', component: BookEdit },
    { path: '/gauge/list', component: GaugeList },
    { path: '/gauge/new', component: GaugeNew },
    { path: '/gauge/:id', component: GaugeDetail }
];

const router = createRouter({
    history: createWebHashHistory(),
    routes, // short for `routes: routes`
});

export default router;