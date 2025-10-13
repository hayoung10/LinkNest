<template>
  <div class="grid gap-5">
    <!-- Google (outline) -->
    <button
      type="button"
      class="w-full h-14 rounded-xl px-6 flex items-center justify-center gap-3 border border-gray-200 bg-white text-gray-800 hover:bg-gray-50 active:translate-y-[0.5px] focus-visible:ring-4 focus-visible:ring-blue-400/25 transition"
      @click="$emit('google')"
    >
      <svg class="w-5 h-5" viewBox="0 0 20 20" fill="none" aria-hidden="true">
        <path
          d="M19.8055 10.2292C19.8055 9.5542 19.75 8.875 19.6305 8.21875H10.2V12.0188H15.5945C15.3555 13.2917 14.5945 14.4104 13.4945 15.0854V17.5854H16.7945C18.7055 15.8354 19.8055 13.2729 19.8055 10.2292Z"
          fill="#4285F4"
        />
        <path
          d="M10.2 20C12.9 20 15.1695 19.1042 16.7945 17.5854L13.4945 15.0854C12.5695 15.6708 11.4055 16.0104 10.2 16.0104C7.59451 16.0104 5.38951 14.2417 4.57451 11.8542H1.17451V14.4292C2.82451 17.7083 6.33951 20 10.2 20Z"
          fill="#34A853"
        />
        <path
          d="M4.57451 11.8542C4.15701 10.5812 4.15701 9.41875 4.57451 8.14583V5.57083H1.17451C-0.125488 8.15833 -0.125488 11.8417 1.17451 14.4292L4.57451 11.8542Z"
          fill="#FBBC04"
        />
        <path
          d="M10.2 3.98958C11.4805 3.96875 12.7138 4.46875 13.6388 5.35417L16.5638 2.42917C14.0805 0.0729167 10.7055 -0.783333 7.68051 0.554167C6.09801 1.13125 4.69301 2.08958 3.59301 3.34375C2.49301 4.59792 1.73301 6.10833 1.37801 7.72917L4.57451 8.14583C5.38951 5.75833 7.59451 3.98958 10.2 3.98958Z"
          fill="#EA4335"
        />
      </svg>
      <span class="font-medium text-[15px]">구글로 계속하기</span>
    </button>

    <!-- Kakao -->
    <button
      type="button"
      class="w-full h-14 rounded-xl px-6 flex items-center justify-center gap-3 bg-[#F2DD4C] text-gray-900 hover:bg-[#E9D53E] active:translate-y-[0.5px] focus-visible:ring-4 focus-visible:ring-yellow-300/30 transition"
      @click="$emit('kakao')"
    >
      <svg class="w-5 h-5" viewBox="0 0 20 20" fill="none" aria-hidden="true">
        <path
          d="M10 0C4.477 0 0 3.517 0 7.86C0 10.515 1.663 12.845 4.184 14.24L3.155 18.082C3.054 18.458 3.485 18.754 3.804 18.531L8.286 15.402C8.851 15.474 9.422 15.512 10 15.512C15.523 15.512 20 11.995 20 7.652C20 3.309 15.523 0 10 0Z"
          fill="#000"
        />
      </svg>
      <span class="font-medium text-[15px]">카카오로 계속하기</span>
    </button>

    <!-- Naver -->
    <button
      type="button"
      class="w-full h-14 rounded-xl px-6 flex items-center justify-center gap-3 bg-[#56B86A] text-white hover:bg-[#4EAA60] active:translate-y-[0.5px] focus-visible:ring-4 focus-visible:ring-green-300/30 transition"
      @click="$emit('naver')"
    >
      <svg class="w-5 h-5" viewBox="0 0 20 20" fill="none" aria-hidden="true">
        <path
          d="M13.3333 10.8333L6.66667 0H0V20H6.66667V9.16667L13.3333 20H20V0H13.3333V10.8333Z"
          fill="white"
        />
      </svg>
      <span class="font-medium text-[15px]">네이버로 계속하기</span>
    </button>
  </div>
</template>

<script setup lang="ts">
defineEmits(["google", "kakao", "naver"]);
</script>
