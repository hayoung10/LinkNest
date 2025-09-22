<template>
  <div style="padding: 16px">
    <h2>Login</h2>
    <button @click="login('google')">Google로 로그인</button>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from "@/stores/auth";
import { useRoute } from "vue-router";

const auth = useAuthStore();
const route = useRoute();

function login(provider: "google" | "kakao" | "naver") {
  // 필요하면 ?redirect= 유지
  // startOAuth sㅐ부에서 redirect_uri=/redirect로 보냄
  auth.startOAuth(provider);
}
</script>
