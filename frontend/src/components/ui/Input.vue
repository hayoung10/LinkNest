<template>
  <input
    :type="props.type ?? 'text'"
    :value="props.modelValue ?? ''"
    :placeholder="props.placeholder"
    :disabled="props.disabled"
    @input="
      emit('update:modelValue', ($event.target as HTMLInputElement).value)
    "
    :class="
      cn(
        // base
        'h-9 w-full rounded-md border px-3 py-2 text-sm transition-colors',
        // colors
        'border-zinc-200 bg-white text-zinc-900 placeholder:text-zinc-400',
        'focus:outline-none focus:ring-2 focus:ring-zinc-900/10 focus:border-zinc-300',
        // disabled
        'disabled:cursor-not-allowed disabled:opacity-50',
        props.class,
      )
    "
  />
</template>

<script setup lang="ts">
import { cn } from "@/lib/utils";

type Props = {
  modelValue?: string;
  type?: string;
  placeholder?: string;
  disabled?: boolean;
  class?: string;
};

const props = defineProps<Props>();

const emit = defineEmits<{ (e: "update:modelValue", v: string): void }>();
</script>
