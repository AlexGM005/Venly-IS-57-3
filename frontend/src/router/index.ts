import { createRouter, createWebHistory } from 'vue-router';
import CreateProfileView from '@/views/CreateProfileView.vue';
import ConsultProfileView from '@/views/ConsultProfileView.vue';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/crear-perfil'
    },
    {
      path: '/crear-perfil',
      name: 'crear-perfil',
      component: CreateProfileView
    },
    {
      path: '/consultar-perfil',
      name: 'consultar-perfil',
      component: ConsultProfileView
    }
  ]
});

export default router;
