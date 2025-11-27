<template>
  <section class="space-y-4">
    <header>
      <h2 class="text-sm font-semibold text-foreground">계정</h2>
      <p class="mt-1 text-xs text-muted-foreground">
        기본 프로필 정보와 이메일을 관리합니다.
      </p>
    </header>

    <div
      class="rounded-xl border border-border/80 bg-muted/40 px-4 py-4 space-y-4"
    >
      <div class="space-y-1.5">
        <label class="block text-xs font-medium text-muted-foreground">
          이름
        </label>
        <input
          type="text"
          class="w-full rounded-md px-3 py-2 text-sm bg-background border border-border/70 focus:outline-none focus:ring-2 focus:ring-ring/50 focus:border-ring/60"
          placeholder="이름"
          disabled
        />
        <p class="text-[11px] text-muted-foreground">TODO: 프로필 편집 기능</p>
      </div>

      <div class="space-y-1.5">
        <label class="block text-xs font-medium text-muted-foreground">
          이메일
        </label>
        <input
          type="email"
          class="w-full rounded-md px-3 py-2 text-sm bg-background border border-border/70 focus:outline-none focus:ring-2 focus:ring-ring/50 focus:border-ring/60"
          placeholder="you@example.com"
          disabled
        />
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
// TODO: 계정 기본 정보 설정 기능
</script>
