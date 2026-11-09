import './assets/main.css';
import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import { useSession } from '@/composables/useSession';
import { onSessionRejected } from '@/services/httpClient';

// If the backend no longer recognises the stored profile, close the simulated session.
onSessionRejected(() => {
  useSession().logout();
  router.push({ name: 'login' });
});

createApp(App).use(router).mount('#app');
