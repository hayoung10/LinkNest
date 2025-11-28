<template>
  <section class="space-y-6">
    <!-- 카드 1: 로그인 보안 -->
    <div
      class="rounded-2xl border border-zinc-200 bg-white px-6 py-5 space-y-6"
    >
      <header class="space-y-1">
        <h2 class="text-lg font-semibold text-zinc-900">로그인 보안</h2>
        <p class="text-sm text-zinc-500">
          LinkNest는 소셜 로그인을 사용하며 별도의 비밀번호를 저장하지 않습니다.
        </p>
      </header>

      <div class="space-y-3">
        <div class="space-y-1">
          <p class="text-sm font-medium text-zinc-800">소셜 로그인</p>
          <div class="flex flex-wrap gap-2">
            <div
              class="inline-flex items-center gap-2 rounded-full border border-zinc-200 bg-zinc-50 px-3 py-1 text-xs font-medium text-zinc-700"
            >
              <span
                class="inline-flex h-5 w-5 items-center justify-center rounded-full bg-white shadow-sm"
              >
                <svg
                  class="w-3.5 h-3.5"
                  viewBox="0 0 20 20"
                  fill="none"
                  aria-hidden="true"
                >
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
              </span>
              <span>Google 계정</span>
            </div>
          </div>
        </div>

        <p class="text-xs text-zinc-400">
          비밀번호 관리 및 보안 설정은 Google에서 진행하세요.
        </p>
      </div>
    </div>

    <!-- 카드 2: 로그인 상태 / 세션 관리 -->
    <div class="rounded-2xl border border-zinc-200 bg-white p-6 space-y-4">
      <header class="space-y-1">
        <h3 class="text-base font-semibold text-zinc-900">로그인 상태</h3>
        <p class="text-sm text-zinc-500">
          이 브라우저에서 로그인 상태 유지 여부와 다른 기기에서의 세션을
          관리합니다.
        </p>
      </header>

      <div class="space-y-4">
        <!-- 로그인 상태 유지 토글 -->
        <label class="flex items-start justify-between gap-3">
          <div>
            <p class="text-sm font-medium text-zinc-900">
              이 브라우저에서 로그인 상태 유지
            </p>
            <p class="text-xs text-zinc-500">
              개인 기기에서는 켜 두고, 공용 PC에서는 끄는 것을 추천합니다.
            </p>
          </div>
          <button
            type="button"
            class="relative inline-flex h-6 w-11 items-center rounded-full transition"
            :class="keepSignedIn ? 'bg-zinc-900' : 'bg-zinc-300'"
            @click="toggleKeepSignedIn"
          >
            <span
              class="inline-block h-4 w-4 transform rounded-full bg-white transition"
              :class="keepSignedIn ? 'translate-x-5' : 'translate-x-1'"
            />
          </button>
        </label>

        <!-- 다른 기기에서 로그아웃 -->
        <div
          class="flex items-start justify-between gap-3 rounded-xl bg-zinc-50 px-4 py-3"
        >
          <div>
            <p class="text-sm font-medium text-zinc-900">
              다른 모든 기기에서 로그아웃
            </p>
            <p class="text-xs text-zinc-500">
              현재 사용 중인 기기를 제외한 모든 기기에서 로그인 상태를
              해제합니다.
            </p>
          </div>
          <button
            type="button"
            class="rounded-lg border border-zinc-300 px-3 py-1.5 text-xs font-medium text-zinc-800 hover:bg-zinc-100"
            @click="logoutOtherDevices"
          >
            로그아웃
          </button>
        </div>
      </div>
    </div>

    <!-- 카드 3: 위험 구역 -->
    <div class="rounded-2xl border border-red-200 bg-red-50 p-6 space-y-4">
      <header class="space-y-1">
        <h3 class="text-base font-semibold text-red-700">위험 구역</h3>
        <p class="text-sm text-red-500">
          계정을 삭제하면 모든 데이터가 영구적으로 삭제됩니다.
        </p>
      </header>

      <button
        type="button"
        class="rounded-xl bg-red-600 px-4 py-2 text-sm font-semibold text-white hover:bg-red-700"
        @click="onDeleteAccount"
      >
        계정 삭제
      </button>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref } from "vue";

const keepSignedIn = ref(true);

const toggleKeepSignedIn = () => {
  keepSignedIn.value = !keepSignedIn.value;
  console.log("keepSignedIn:", keepSignedIn.value);
  // TODO: API 연동
};

const logoutOtherDevices = () => {
  console.log("다른 기기 로그아웃 요청");
  // TODO: API 호출
};

const onDeleteAccount = () => {
  console.log("계정 삭제 클릭");
  // TODO: 계정 삭제 확인 모달 + API 연동
};
</script>
