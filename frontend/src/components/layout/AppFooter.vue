<template>
  <footer class="py-12 px-4 border-t border-border">
    <div class="container mx-auto max-w-6xl">
      <div class="grid grid-cols-1 md:grid-cols-2 gap-8 mb-8">
        <div>
          <div class="flex items-center gap-2 mb-4">
            <div
              class="size-8 bg-gradient-to-br from-blue-500 to-indigo-600 rounded-lg flex items-center justify-center"
            >
              <LogoIcon :size="20" klass="text-white" />
            </div>
            <span class="font-semibold">LinkNest</span>
          </div>
          <!-- <p class="text-sm text-muted-foreground">링크 관리의 새로운 기준</p> -->
        </div>

        <div>
          <h4 class="mb-4">제품</h4>
          <ul class="space-y-2 text-sm text-muted-foreground">
            <li>
              <a
                href="#features"
                class="hover:text-foreground transition-colors"
                >기능</a
              >
            </li>
          </ul>
        </div>
      </div>

      <div
        class="pt-8 border-t border-border text-center text-sm text-muted-foreground"
      >
        © 2026 LinkNest. All rights reserved.
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
import LogoIcon from "../icons/LogoIcon.vue";
</script>
