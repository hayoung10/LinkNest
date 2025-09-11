<script setup lang="ts">
import { ref, onMounted } from "vue";
import http from "./api/http";

interface HealthResponse {
  status: string;
  time: string;
}
const result = ref<HealthResponse | null>(null);
const error = ref<string | null>(null);

onMounted(async () => {
  try {
    const { data } = await http.get<HealthResponse>("/api/health");
    result.value = data;
  } catch (e: any) {
    error.value = e?.message ?? "request failed";
    console.error(e);
  }
});
</script>

<template>
  <h1>FE ↔ BE 연결 테스트</h1>
  <pre v-if="result">{{ result }}</pre>
  <pre v-else-if="error">ERROR: {{ error }}</pre>
  <p v-else>Loading...</p>
</template>
