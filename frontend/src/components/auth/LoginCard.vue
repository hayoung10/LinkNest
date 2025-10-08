<template>
  <div
    class="login-card bg-white rounded-2x1 shadow-xl ring-1 ring-black/5 overflow-hidden"
  >
    <div class="card-header">
      <h2>{{ title }}</h2>
      <p v-if="description">{{ description }}</p>
    </div>

    <div class="card-body">
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{ title: string; description?: string }>();
</script>

<style scoped>
.login-card {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  max-width: 448px;
  margin: 0 auto;
}

.card-header {
  padding: 48px 32px 16px;
  text-align: center;
}

.card-body {
  padding: 0 32px 48px;
}

.card-header h2 {
  font-size: 1.5rem;
  font-weight: 700;
  color: #111827;
}

.card-header p {
  margin-top: 0.5rem;
  color: #6b7280;
  font-size: 0.95rem;
}
</style>
