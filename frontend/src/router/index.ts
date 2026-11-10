import { createRouter, createWebHistory } from 'vue-router';
<<<<<<< HEAD
import LoginView from '@/views/LoginView.vue';
import AdminLoginView from '@/views/AdminLoginView.vue';
import CreateProfileView from '@/views/CreateProfileView.vue';
import MainMenuView from '@/views/MainMenuView.vue';
import ProfileView from '@/views/ProfileView.vue';
=======
>>>>>>> origin/feature/Release-1
import { useSession } from '@/composables/useSession';

declare module 'vue-router' {
  interface RouteMeta {
    /** Accessible without session; logged-in users are sent to the menu. */
    guestOnly?: boolean;
    /** Only GERENTE and JEFE. */
    staffOnly?: boolean;
    title?: string;
  }
}

const router = createRouter({
  history: createWebHistory(),
  scrollBehavior: () => ({ top: 0 }),
  routes: [
<<<<<<< HEAD
    { path: '/', redirect: '/login' },
    { 
      path: '/login', 
      name: 'login', 
      component: LoginView, 
      meta: { guestOnly: true } 
    },
    { 
      path: '/admin-login', 
      name: 'admin-login', 
      component: AdminLoginView, 
      meta: { guestOnly: true } 
    },
    { 
      path: '/crear-perfil', 
      name: 'crear-perfil', 
      component: CreateProfileView, 
      meta: { guestOnly: true } 
    },
    { 
      path: '/menu', 
      name: 'menu', 
      component: MainMenuView, 
      meta: { requiresAuth: true } 
    },
    { 
      path: '/perfil', 
      name: 'perfil', 
      component: ProfileView, 
      meta: { requiresAuth: true } 
    },
    { path: '/consultar-perfil', redirect: '/perfil' },
    { path: '/:pathMatch(.*)*', redirect: '/login' }
  ]
});

router.beforeEach(async (to) => {
  const { restore } = useSession();
  const loggedIn = await restore();
  
  if (to.meta.requiresAuth && !loggedIn) return { name: 'login' };
  if (to.meta.guestOnly && loggedIn) return { name: 'menu' };
  
  return true;
});

export default router;
=======
    { path: '/', redirect: '/menu' },
    { path: '/login', name: 'login', component: () => import('@/views/LoginView.vue'), meta: { guestOnly: true, title: 'Ingresar' } },
    { path: '/menu', name: 'menu', component: () => import('@/views/MainMenuView.vue'), meta: { title: 'Menú principal' } },
    { path: '/perfil', name: 'mi-perfil', component: () => import('@/views/MyProfileView.vue'), meta: { title: 'Mi perfil' } },
    {
      path: '/perfiles',
      name: 'perfiles',
      component: () => import('@/views/ProfileDirectoryView.vue'),
      meta: { staffOnly: true, title: 'Gestión de perfiles' }
    },
    { path: '/peliculas', name: 'peliculas', component: () => import('@/views/MovieCatalogView.vue'), meta: { title: 'Películas' } },
    {
      path: '/peliculas/nueva',
      name: 'pelicula-nueva',
      component: () => import('@/views/MovieFormView.vue'),
      meta: { staffOnly: true, title: 'Añadir película' }
    },
    {
      path: '/peliculas/:id(\\d+)',
      name: 'pelicula-detalle',
      component: () => import('@/views/MovieDetailView.vue'),
      props: (route) => ({ id: Number(route.params.id) }),
      meta: { title: 'Detalle de película' }
    },
    {
      path: '/peliculas/:id(\\d+)/editar',
      name: 'pelicula-editar',
      component: () => import('@/views/MovieFormView.vue'),
      props: (route) => ({ id: Number(route.params.id) }),
      meta: { staffOnly: true, title: 'Editar película' }
    },
    { path: '/:pathMatch(.*)*', redirect: '/menu' }
  ]
});

router.beforeEach((to) => {
  const { isLoggedIn, isStaff } = useSession();
  if (!isLoggedIn.value && !to.meta.guestOnly) return { name: 'login' };
  if (isLoggedIn.value && to.meta.guestOnly) return { name: 'menu' };
  if (to.meta.staffOnly && !isStaff.value) return { name: 'menu' };
  return true;
});

router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} · Venly` : 'Venly';
});

export default router;
>>>>>>> origin/feature/Release-1
