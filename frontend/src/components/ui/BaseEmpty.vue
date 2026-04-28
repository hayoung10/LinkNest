<template>
  <div
    class="flex flex-col items-center justify-center gap-2 rounded-xl border border-zinc-200 bg-white px-6 py-10 text-center"
  >
    <!-- Icon -->
    <div
      class="flex h-10 w-10 items-center justify-center rounded-full bg-zinc-100 text-zinc-500"
    >
      <svg viewBox="0 0 24 24" class="h-5 w-5" fill="none" aria-hidden="true">
        <path
          d="M7 10h10M7 15h6"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
        />
        <path
          d="M6 4h12a2 2 0 012 2v12a2 2 0 01-2 2H6a2 2 0 01-2-2V6a2 2 0 012-2z"
          stroke="currentColor"
          stroke-width="2"
        />
      </svg>
    </div>

    <div class="mt-1 text-base font-semibold text-zinc-900">
      {{ props.title }}
    </div>

    <p v-if="props.description" class="max-w-md text-sm text-zinc-500">
      {{ props.description }}
    </p>

    <div v-if="$slots.action" class="mt-4"><slot name="action" /></div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  title: string;
  description?: string;
}

const props = defineProps<Props>();
</script>
