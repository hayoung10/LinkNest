<template>
  <section id="features" class="py-20 px-4 bg-gray-50 dark:bg-gray-900/50">
    <div class="container mx-auto max-w-6xl">
      <div class="text-center mb-16">
        <h2 class="mb-4">LinkNest의 주요 기능</h2>
        <p class="text-muted-foreground max-w-2xl mx-auto">
          컬렉션과 태그로 링크를 체계적으로 정리하고,<br />북마크 관리와 복구,
          환경 설정 기능을 제공합니다.
        </p>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
        <article
          class="rounded-xl border-2 bg-white dark:bg-gray-900 hover:border-primary/50 transition-colors p-6"
        >
          <div
            class="size-12 rounded-lg bg-gradient-to-br from-blue-500 to-indigo-600 flex items-center justify-center mb-4"
          >
            <FolderIcon :size="24" klass="text-white" />
          </div>
          <h3 class="text-lg font-semibold mb-2">컬렉션으로 정리</h3>
          <p class="text-muted-foreground text-sm leading-6">
            컬렉션과 이모지로 북마크를 직관적으로 정리할 수 있어요.
          </p>
        </article>

        <article
          class="rounded-xl border-2 bg-white dark:bg-gray-900 hover:border-primary/50 transition-colors p-6"
        >
          <div
            class="size-12 rounded-lg bg-gradient-to-br from-blue-500 to-indigo-600 flex items-center justify-center mb-4"
          >
            <svg
              class="size-6 text-white"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <BookmarkIcon :size="24" klass="text-white" />
            </svg>
          </div>
          <h3 class="text-lg font-semibold mb-2">북마크 관리</h3>
          <p class="text-muted-foreground text-sm leading-6">
            이모지, 커비 이미지, 태그, 즐겨찾기를 활용해 북마크를 세밀하게
            관리할 수 있어요.
          </p>
        </article>

        <article
          class="rounded-xl border-2 bg-white dark:bg-gray-900 hover:border-primary/50 transition-colors p-6"
        >
          <div
            class="size-12 rounded-lg bg-gradient-to-br from-blue-500 to-indigo-600 flex items-center justify-center mb-4"
          >
            <svg
              class="size-6 text-white"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <TagIcon :size="24" klass="text-white" />
            </svg>
          </div>
          <h3 class="text-lg font-semibold mb-2">태그로 분류</h3>
          <p class="text-muted-foreground text-sm leading-6">
            태그를 생성하고 정리하며, 태그별로 북마크를 모아 볼 수 있어요.
          </p>
        </article>

        <article
          class="rounded-xl border-2 bg-white dark:bg-gray-900 hover:border-primary/50 transition-colors p-6"
        >
          <div
            class="size-12 rounded-lg bg-gradient-to-br from-blue-500 to-indigo-600 flex items-center justify-center mb-4"
          >
            <svg
              class="size-6 text-white"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <TrashIcon :size="24" klass="text-white" />
            </svg>
          </div>
          <h3 class="text-lg font-semibold mb-2">휴지통 복구</h3>
          <p class="text-muted-foreground text-sm leading-6">
            삭제한 컬렉션, 북마크, 태그를 휴지통에서 복원하거나 정리할 수
            있어요.
          </p>
        </article>

        <article
          class="rounded-xl border-2 bg-white dark:bg-gray-900 hover:border-primary/50 transition-colors p-6"
        >
          <div
            class="size-12 rounded-lg bg-gradient-to-br from-blue-500 to-indigo-600 flex items-center justify-center mb-4"
          >
            <svg
              class="size-6 text-white"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <SettingsIcon :size="24" klass="text-white" />
            </svg>
          </div>
          <h3 class="text-lg font-semibold mb-2">환경 설정</h3>
          <p class="text-muted-foreground text-sm leading-6">
            정렬 방식, 레이아웃, 보안 등 환경을 원하는 방식으로 설정할 수
            있어요.
          </p>
        </article>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import BookmarkIcon from "@/components/icons/BookmarkIcon.vue";
import FolderIcon from "@/components/icons/FolderIcon.vue";
import SettingsIcon from "@/components/icons/SettingsIcon.vue";
import TagIcon from "@/components/icons/TagIcon.vue";
import TrashIcon from "@/components/icons/TrashIcon.vue";
</script>
