<template>
  <section id="features" class="py-20 px-4 bg-gray-50 dark:bg-gray-900/50">
    <div class="container mx-auto max-w-6xl">
      <div class="text-center mb-16">
        <h2 class="mb-4">LinkNest의 주요 기능</h2>
        <p class="text-muted-foreground max-w-2xl mx-auto">
          링크를 더 쉽고 체계적으로 관리할 수 있는 기능들을 만나보세요.
        </p>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-6 max-w-3xl mx-auto">
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
              <path
                d="M3 7h5l2 2h11v9a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V9a2 2 0 0 1 2-2z"
              />
            </svg>
          </div>
          <h3 class="text-lg font-semibold">스마트 정리</h3>
          <p class="text-muted-foreground text-sm">
            폴더와 태그로 링크를 체계적으로 분류하고 관리하세요.
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
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
            </svg>
          </div>
          <h3 class="text-lg font-semibold">안전한 저장</h3>
          <p class="text-muted-foreground text-sm">
            로그인 계정 기반으로 안전하게 링크를 보관하세요.
          </p>
        </article>
      </div>
    </div>
  </section>
</template>
