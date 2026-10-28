import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import './style.css';
import { initTheme } from './utils/theme.js';

initTheme();

createApp(App).use(router).mount('#app');
