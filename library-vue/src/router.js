import { createRouter, createWebHashHistory} from 'vue-router';

import Home from "./Home.vue";
import BookList from "./book/List.vue";
import LanguageList from "./language/List.vue";
import LanguageEdit from "./language/Edit.vue";
import SeriesList from "./series/List.vue";
import StatusList from "./status/List.vue";
import TagList from "./tag/List.vue";

const routes = [
    { path: '/', component: Home },
    { path: '/book/list', component: BookList },
    { path: '/language/list', component: LanguageList },
    { path: '/language/edit:id?', component: LanguageEdit },
    { path: '/series/list', component: SeriesList },
    { path: '/status/list', component: StatusList },
    { path: '/tag/list', component: TagList },
];

const router = createRouter({
    history: createWebHashHistory(),
    routes, // short for `routes: routes`
});

export default router;