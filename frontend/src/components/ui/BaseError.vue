<template>
  <div
    class="flex flex-col items-center justify-center gap-2 rounded-xl border border-red-200 bg-red-50 px-6 py-10 text-center"
  >
    <div
      class="flex h-10 w-10 items-center justify-center rounded-full bg-red-100 text-red-700"
    >
      <svg viewBox="0 0 24 24" class="h-5 w-5" fill="none" aria-hidden="true">
        <path
          d="M12 9v4"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
        />
        <path
          d="M12 17h.01"
          stroke="currentColor"
          stroke-width="3"
          stroke-linecap="round"
        />
        <path
          d="M10.29 3.861-7.4 13.2A2 2 0 004.63 20h14.74a2 2 0 001.74-2.941-7.4-13.2a2 2 0 00-3.42 0z"
          stroke="currentColor"
          stroke-width="2"
        />
      </svg>
    </div>

    <div class="mt-1 text-base font-semibold text-red-900">
      {{ props.title }}
    </div>

    <p v-if="props.description" class="max-w-md text-sm text-red-700">
      {{ props.description }}
    </p>

    <div class="mt-4 flex items-center gap-2">
      <button
        v-if="props.onRetry"
        type="button"
        class="inline-flex items-center rounded-lg bg-red-600 px-3 py-2 text-sm font-medium text-white hover:bg-red-700 disabled:opacity-50"
        @click="props.onRetry?.()"
      >
        {{ props.retryText }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  title?: string;
  description?: string;
  retryText?: string;
  onRetry?: (() => void) | null;
}

const props = withDefaults(defineProps<Props>(), {
  title: "문제가 발생했습니다",
  retryText: "다시 시도",
  onRetry: null,
});
</script>
