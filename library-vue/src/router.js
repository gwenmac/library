import { createRouter, createWebHashHistory} from 'vue-router';

import Home from "./Home.vue";
import BookList from "./book/List.vue";
import LanguageList from "./language/List.vue";
import LanguageEdit from "./language/Edit.vue";
import SeriesList from "./series/List.vue";
import SeriesEdit from "./series/Edit.vue";
import StatusList from "./status/List.vue";
import StatusEdit from "./status/Edit.vue";
import TagList from "./tag/List.vue";
import TagEdit from "./tag/Edit.vue";

const routes = [
    { path: '/', component: Home },
    { path: '/book/list', component: BookList },
    { path: '/language/list', component: LanguageList },
    { path: '/language/edit/:id?', component: LanguageEdit },
    { path: '/series/list', component: SeriesList },
    { path: '/series/edit/:id?', component: SeriesEdit },
    { path: '/status/list', component: StatusList },
    { path: '/status/edit/:id?', component: StatusEdit },
    { path: '/tag/list', component: TagList },
    { path: '/tag/edit/:id?', component: TagEdit }
];

const router = createRouter({
    history: createWebHashHistory(),
    routes, // short for `routes: routes`
});

export default router;