<template>
  <div
    class="bg-white dark:bg-gray-900 rounded-2x1 shadow-lg ring-1 ring-black/5 overflow-hidden max-w-md mx-auto"
  >
    <div class="text-center px-8 pt-10 pb-2">
      <h2 class="text-6x1 font-extrabold text-gray-900 dark:text-white mb-2">
        {{ title }}
      </h2>
      <p
        v-if="description"
        class="mt=2 text-gray-500 dark:text-gray-400 text-base"
      >
        {{ description }}
      </p>
    </div>

    <div class="px-8 pb-12">
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{ title: string; description?: string }>();
</script>
