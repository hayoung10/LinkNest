<template>
  <dialog
    v-if="open"
    class="fixed inset-0 flex items-center justify-center bg-black/50 z-50"
  >
    <div class="bg-card rounded-xl shadow-xl w-[500px] p-6">
      <!-- 헤더 -->
      <header class="mb-4">
        <h2 class="text-lg font-semibold">새 북마크 추가</h2>
        <p class="text-sm text-muted-foreground">
          저장하고 싶은 링크의 정보를 입력해주세요.
        </p>
      </header>

      <!-- 입력 폼 -->
      <div class="space-y-4">
        <div>
          <label class="block text-sm mb-1">제목 *</label>
          <input
            type="text"
            placeholder="북마크 제목"
            class="w-full border rounded-md px-3 py-2 text-sm bg-background"
          />
        </div>

        <div>
          <label class="block text-sm mb-1">링크 *</label>
          <input
            type="url"
            placeholder="https://example.com"
            class="w-full border rounded-md px-3 py-2 text-sm bg-background"
          />
        </div>

        <div>
          <label class="block text-sm mb-1">설명</label>
          <textarea
            rows="4"
            placeholder="북마크에 대한 설명을 입력하세요"
            class="w-full border rounded-md px-3 py-2 text-sm bg-background"
          ></textarea>
        </div>
      </div>

      <!-- 버튼 -->
      <footer class="mt-6 flex justify-end gap-2">
        <button
          class="px-4 py-2 border rounded-md text-sm hover:bg-accent"
          @click="$emit('close')"
        >
          취소
        </button>
        <button
          class="px-4 py-2 rounded-md bg-primary text-white hover:bg-primary/90"
        >
          추가
        </button>
      </footer>
    </div>
  </dialog>
</template>

<script setup lang="ts">
defineProps<{ open: boolean }>();
defineEmits<{ close: [] }>();
</script>
