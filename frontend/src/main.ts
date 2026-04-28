import "@/assets/tailwind.css";
import { createApp } from "vue";
import { createPinia } from "pinia";
import App from "./App.vue";
import router from "./router";
import VueDndKit from "@vue-dnd-kit/core";

const app = createApp(App);

const pinia = createPinia();
app.use(pinia);

app.use(router);

app.use(VueDndKit);

app.mount("#app");
