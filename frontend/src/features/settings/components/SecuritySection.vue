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
                <ProviderIcon v-if="provider" :provider="provider" :size="14" />
                <svg
                  v-else
                  class="w-3.5 h-3.5"
                  viewBox="0 0 20 20"
                  fill="none"
                  aria-hidden="true"
                >
                  <path
                    d="M3.75 4.5h12.5A1.75 1.75 0 0 1 18 6.25v7.5A1.75 1.75 0 0 1 16.25 15.5H3.75A1.75 1.75 0 0 1 2 13.75v-7.5A1.75 1.75 0 0 1 3.75 4.5Zm0 1.5 6.25 3.75L16.25 6h-12.5Zm0 1.31v6.44h12.5V7.31l-6.06 3.64a.75.75 0 0 1-.78 0L3.75 7.31Z"
                    fill="currentColor"
                  />
                </svg>
              </span>
              <span>{{ providerLabel }} 계정</span>
            </div>
          </div>
        </div>

        <p class="text-xs text-zinc-400">
          {{ passwordGuideText }}
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
            class="relative inline-flex h-6 w-11 items-center rounded-full transition disabled:opacity-60 disabled:cursor-not-allowed"
            :class="keepSignedIn ? 'bg-zinc-900' : 'bg-zinc-300'"
            @click="onToggleKeepSignedIn"
            :disabled="
              isUpdatingKeepSignedIn ||
              isProcessingAllSessions ||
              isDeletingAccount
            "
          >
            <span
              class="inline-block h-4 w-4 transform rounded-full bg-white transition"
              :class="keepSignedIn ? 'translate-x-5' : 'translate-x-1'"
            />
          </button>
        </label>

        <!-- 모든 기기에서 로그아웃 -->
        <div
          class="flex items-start justify-between gap-3 rounded-xl bg-zinc-50 px-4 py-3"
        >
          <div>
            <p class="text-sm font-medium text-zinc-900">
              모든 기기에서 로그아웃
            </p>
            <p class="text-xs text-zinc-500">
              사용 중인 모든 기기에서 로그인 상태를 해제합니다.
            </p>
          </div>
          <button
            type="button"
            class="rounded-lg border border-zinc-300 px-3 py-1.5 text-xs font-medium text-zinc-800 hover:bg-zinc-100 disabled:opacity-60 disabled:cursor-not-allowed"
            @click="onLogoutAllDevices"
            :disabled="isProcessingAllSessions || isDeletingAccount"
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
        class="rounded-xl bg-red-600 px-4 py-2 text-sm font-semibold text-white hover:bg-red-700 disabled:opacity-60 disabled:cursor-not-allowed"
        @click="onDeleteAccount"
        :disabled="isDeletingAccount || isProcessingAllSessions"
      >
        계정 삭제
      </button>
    </div>
  </section>
</template>

<script setup lang="ts">
import { useAuthStore } from "@/stores/auth";
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import * as UserApi from "@/api/users";
import ProviderIcon from "@/components/common/ProviderIcon.vue";
import type { Provider } from "@/types/common";
import { usePreferencesStore } from "@/stores/preferences";
import { storeToRefs } from "pinia";
import { ToastType, useToastStore } from "@/stores/toast";

const toast = useToastStore();
const auth = useAuthStore();
const router = useRouter();
const preferences = usePreferencesStore();

const { keepSignedIn, loaded } = storeToRefs(preferences);

const provider = computed<Provider | null>(() => auth.user?.provider ?? null);

const providerLabel = computed(() => {
  if (!provider.value) return "이메일";
  switch (provider.value) {
    case "GOOGLE":
      return "Google";
    case "KAKAO":
      return "카카오";
    case "NAVER":
      return "네이버";
    default:
      return "이메일";
  }
});

const passwordGuideText = computed(() => {
  if (!provider.value) {
    return "비밀번호와 보안 설정은 LinkNest 계정 설정에서 관리할 수 있습니다.";
  }
  return `비밀번호 관리 및 보안 설정은 ${providerLabel.value}에서 진행하세요.`;
});

// 상태
const isLoading = ref(false);
const isUpdatingKeepSignedIn = ref(false);
const isProcessingAllSessions = ref(false);
const isDeletingAccount = ref(false);

// 초기 로딩
const loadPreferences = async () => {
  if (loaded.value) return;

  isLoading.value = true;
  try {
    await preferences.load();
  } catch (e) {
    console.error("로그인 설정 불러오기 실패:", e);
    toast.error("보안 설정을 불러오지 못했습니다.");
  } finally {
    isLoading.value = false;
  }
};

onMounted(() => {
  loadPreferences();
});

const onToggleKeepSignedIn = async () => {
  if (isLoading.value || isUpdatingKeepSignedIn.value) return;
  const prev = keepSignedIn.value;
  const next = !prev;

  isUpdatingKeepSignedIn.value = true;
  keepSignedIn.value = next;

  try {
    await preferences.update({ keepSignedIn: next });
    await auth.refresh(); // RT 쿠키 갱신

    toast.success(
      next ? "로그인 상태 유지를 켰습니다." : "로그인 상태 유지를 껐습니다."
    );
  } catch (e) {
    console.error("로그인 상태 유지 설정 변경 실패:", e);
    keepSignedIn.value = prev;
    toast.error("로그인 설정 변경에 실패했습니다.");
  } finally {
    isUpdatingKeepSignedIn.value = false;
  }
};

const onLogoutAllDevices = async () => {
  if (isProcessingAllSessions.value) return;

  const ok = window.confirm(
    "정말 모든 기기에서 로그아웃하시겠어요?\n" +
      "현재 브라우저를 포함해 모든 세션이 해제됩니다."
  );
  if (!ok) return;

  isProcessingAllSessions.value = true;
  try {
    await auth.logoutAllSessions();
    await router.replace({
      path: "/login",
      state: {
        toast: {
          type: "success" as ToastType,
          message: "모든 기기에서 로그아웃되었습니다.",
        },
      },
    });
  } catch (e) {
    console.error("모든 기기에서 로그아웃 실패:", e);
    toast.error("전체 로그아웃에 실패했습니다.");
  } finally {
    isProcessingAllSessions.value = false;
  }
};

const onDeleteAccount = async () => {
  if (isDeletingAccount.value) return;

  const ok = window.confirm(
    "정말 계정을 삭제하시겠어요?\n" +
      "모든 데이터가 영구적으로 삭제되며 복구할 수 없습니다."
  );
  if (!ok) return;

  isDeletingAccount.value = true;
  try {
    await UserApi.deleteAccount();
    auth.clearSession();

    toast.success("계정이 삭제되었습니다.");
    await router.push({
      path: "/",
      state: { toast: { type: "success", message: "계정이 삭제되었습니다." } },
    });
  } catch (e) {
    console.error("계정 삭제 실패:", e);
    toast.error("계정 삭제에 실패했습니다.");
  } finally {
    isDeletingAccount.value = false;
  }
};
</script>
