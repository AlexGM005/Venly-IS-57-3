import { createRouter, createWebHistory } from 'vue-router';
import CreateProfileView from '@/views/CreateProfileView.vue';
import MainMenuView from '@/views/MainMenuView.vue';
import ProfileView from '@/views/ProfileView.vue';
import { useSession } from '@/composables/useSession';

declare module 'vue-router' {
  interface RouteMeta {
    requiresAuth?: boolean;
    guestOnly?: boolean;
  }
}

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/menu' },
    { path: '/crear-perfil', name: 'crear-perfil', component: CreateProfileView, meta: { guestOnly: true } },
    { path: '/menu', name: 'menu', component: MainMenuView, meta: { requiresAuth: true } },
    { path: '/perfil', name: 'perfil', component: ProfileView, meta: { requiresAuth: true } },
    { path: '/consultar-perfil', redirect: '/perfil' },
    { path: '/:pathMatch(.*)*', redirect: '/menu' }
  ]
});

router.beforeEach(async (to) => {
  const { restore } = useSession();
  const loggedIn = await restore();
  if (to.meta.requiresAuth && !loggedIn) return { name: 'crear-perfil' };
  if (to.meta.guestOnly && loggedIn) return { name: 'menu' };
  return true;
});

export default router;
