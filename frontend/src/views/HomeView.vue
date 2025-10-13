<template>
  <div
    class="min-h-screen bg-gradient-to-br from-white to-blue-50 dark:from-gray-900 dark:to-gray-800"
  >
    <AppHeader />
    <main class="pt-16">
      <HeroSection />
      <FeaturesSection />
    </main>
    <AppFooter />
  </div>
</template>

<script setup>
import AppHeader from "@/components/layout/AppHeader.vue";
import AppFooter from "@/components/layout/AppFooter.vue";
import HeroSection from "@/components/sections/landing/HeroSection.vue";
import FeaturesSection from "@/components/sections/landing/FeaturesSection.vue";
</script>
