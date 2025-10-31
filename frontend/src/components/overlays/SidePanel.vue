<template>
  <teleport :to="teleportTo">
    <transition name="fade">
      <div
        v-if="open"
        class="fixed inset-0"
        :style="{ zIndex: String(zIndex) }"
        aria-hidden="true"
      >
        <!-- 오버레이 -->
        <div
          class="absolute inset-0 bg-black/40"
          @click="closeOnOverlay ? $emit('close') : undefined"
        />
      </div>
    </transition>

    <!-- 패널 -->
    <transition :name="side === 'right' ? 'slide-right' : 'slide-left'">
      <aside
        v-if="open"
        ref="panelRef"
        class="fixed top-0 bottom-0 bg-white dark:bg-neutral-900 text-card-foreground border-l border-border shadow-xl outline-none"
        :style="panelStyle"
        role="dialog"
        aria-modal="true"
        :aria-label="ariaLabel"
      >
        <slot />
      </aside>
    </transition>
  </teleport>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from "vue";

const props = withDefaults(
  defineProps<{
    open: boolean;
    side?: "left" | "right";
    width?: string | number;
    closeOnOverlay?: boolean;
    closeOnEsc?: boolean;
    lockScroll?: boolean;
    teleportTo?: string;
    zIndex?: number;
    ariaLabel?: string;
  }>(),
  {
    side: "right",
    width: 420,
    closeOnOverlay: true,
    closeOnEsc: true,
    lockScroll: true,
    teleportTo: "#modals",
    zIndex: 120,
    ariaLabel: "사이드 패널",
  }
);

const emit = defineEmits<{ (e: "close"): void }>();

const panelRef = ref<HTMLElement | null>(null);

const normalizedWidth = computed(() =>
  typeof props.width === "number" ? `${props.width}px` : props.width
);

const panelStyle = computed(() => {
  const base = {
    width: normalizedWidth.value,
    zIndex: String(props.zIndex + 1),
  } as Record<string, string>;
  if (props.side === "right") {
    base.right = "0";
  } else {
    base.left = "0";
  }
  return base;
});

function onKeydown(e: KeyboardEvent) {
  if (!props.open || !props.closeOnEsc) return;
  if (e.key === "Escape") emit("close");
}

function lockBodyScroll(lock: boolean) {
  if (!props.lockScroll) return;
  const cls = "overflow-hidden";
  const el = document.documentElement; // 스크롤 루트
  lock ? el.classList.add(cls) : el.classList.remove(cls);
}

onMounted(() => {
  if (props.open) {
    lockBodyScroll(true);
    window.addEventListener("keydown", onKeydown);
  }
});
onUnmounted(() => {
  lockBodyScroll(false);
  window.removeEventListener("keydown", onKeydown);
});

watch(
  () => props.open,
  async (o) => {
    lockBodyScroll(o);
    if (o) {
      // 패널 포커스
      requestAnimationFrame(() => panelRef.value?.focus?.());
      window.addEventListener("keydown", onKeydown);
    } else {
      window.removeEventListener("keydown", onKeydown);
    }
  },
  { immediate: true }
);
</script>

<style scoped>
/* Overlay */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.18s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* Slide (right) */
.slide-right-enter-active,
.slide-right-leave-active {
  transition: transform 0.22s ease;
}
.slide-right-enter-from,
.slide-right-leave-to {
  transform: translateX(100%);
}

/* Slide (left) */
.slide-left-enter-active,
.slide-left-leave-active {
  transition: transform 0.22s ease;
}
.slide-left-enter-from,
.slide-left-leave-to {
  transform: translateX(-100%);
}
</style>
