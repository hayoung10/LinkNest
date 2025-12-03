<template>
  <section class="space-y-6">
    <!-- 카드 1: 프로필/이름/이메일 -->
    <div
      class="rounded-2xl border border-zinc-200 bg-white px-6 py-5 space-y-6"
    >
      <!-- 제목 -->
      <header class="space-y-1">
        <h2 class="text-lg font-semibold text-zinc-900">프로필</h2>
        <p class="text-sm text-zinc-500">프로필 사진과 이름을 관리합니다</p>
      </header>

      <!-- 아바타 + 버튼 -->
      <div class="flex items-center gap-6">
        <!-- 아바타 / 프로필 이미지 -->
        <div class="relative">
          <div
            v-if="profileImageUrl"
            class="h-24 w-24 overflow-hidden rounded-full bg-zinc-100"
          >
            <img
              :src="profileImageUrl"
              alt="프로필 이미지"
              class="h-full w-full object-cover"
            />
          </div>

          <div
            v-else
            class="flex h-24 w-24 items-center justify-center rounded-full bg-gradient-to-br from-blue-500 to-indigo-600 text-3xl font-semibold text-white"
          >
            <span>{{ avatar }}</span>
          </div>
        </div>

        <!-- 사진 변경 / 삭제 버튼 -->
        <div class="space-y-2">
          <div class="flex items-center gap-4">
            <button
              type="button"
              class="inline-flex items-center gap-2 rounded-xl border border-zinc-200 bg-white px-4 py-2 text-sm font-medium text-zinc-800 shadow-sm hover:bg-zinc-50"
              @click="onClickChangePhoto"
              :disabled="isPhotoUpdating"
            >
              <!-- 업로드 아이콘 -->
              <span class="flex h-5 w-5 items-center justify-center">
                <svg class="h-5 w-5" viewBox="0 0 16 16" aria-hidden="true">
                  <path
                    d="M8 3.333 4.667 6.667h2V10h2.666V6.667h2L8 3.333Z"
                    fill="currentColor"
                  />
                  <path
                    d="M3.333 11.333h9.334v1.334H3.333z"
                    fill="currentColor"
                  />
                </svg>
              </span>
              <span>사진 변경</span>
            </button>

            <button
              v-if="profileImageUrl"
              type="button"
              class="text-xs text-zinc-400 hover:text-zinc-600 underline-offset-2 hover:underline disabled:cursor-not-allowed disabled:opacity-60"
              @click="onDeletePhoto"
              :disabled="isPhotoUpdating"
            >
              사진 삭제
            </button>
          </div>

          <p class="text-xs text-zinc-400">JPG, PNG 또는 GIF (최대 5MB)</p>

          <input
            ref="fileInputRef"
            type="file"
            accept="image/*"
            class="hidden"
            @change="onFileChange"
          />
        </div>
      </div>

      <hr class="border-zinc-200" />

      <!-- 이름 / 이메일 폼 -->
      <form class="space-y-4" @submit.prevent="onSubmit">
        <!-- 이름 -->
        <div class="space-y-2">
          <label class="block text-sm font-medium text-zinc-800">이름</label>
          <div class="flex gap-3">
            <input
              v-model="editableName"
              type="text"
              class="flex-1 rounded-xl border border-transparent bg-zinc-50 px-4 py-3 text-sm text-zinc-900 outlnie-none ring-1 ring-zinc-200 focus:border-indigo-500 focus:ring-indigo-500"
            />
            <button
              type="submit"
              class="inline-flex items-center justify-center rounded-xl bg-zinc-950 px-5 text-sm font-smibold text-white hover:bg-zinc-900"
              :disabled="!isNameChanged || isSavingName"
            >
              저장
            </button>
          </div>
        </div>

        <!-- 이메일 -->
        <div class="space-y-2">
          <label class="block text-sm font-medium text-zinc-800">이메일</label>
          <div class="relative">
            <input
              :value="email"
              type="email"
              disabled
              class="w-full cursor-not-allowed rounded-xl border border-transparent bg-zinc-50 px-4 py-3 text-sm text-zinc-400 outline-none ring-1 ring-zinc-200"
            />
            <!-- 이메일 아이콘 -->
            <div
              class="pointer-events-none absolute inset-y-0 right-4 flex items-center text-zinc-400"
            >
              <svg class="h-7 w-7" viewBox="0 0 20 20" aria-hidden="true">
                <path
                  d="M3.75 4.5h12.5A1.75 1.75 0 0 1 18 6.25v7.5A1.75 1.75 0 0 1 16.25 15.5H3.75A1.75 1.75 0 0 1 2 13.75v-7.5A1.75 1.75 0 0 1 3.75 4.5Zm0 1.5 6.25 3.75L16.25 6h-12.5Zm0 1.31v6.44h12.5V7.31l-6.06 3.64a.75.75 0 0 1-.78 0L3.75 7.31Z"
                  fill="currentColor"
                />
              </svg>
            </div>
          </div>
          <p class="text-xs text-zinc-400">
            이메일은 Google 계정과 연동되어 변경할 수 없습니다
          </p>
        </div>
      </form>
    </div>

    <!-- 카드 2: 연동된 계정 -->
    <section
      class="rounded-2xl border border-zinc-200 bg-white px-6 py-5 shadow-sm"
    >
      <div class="space-y-1">
        <h3 class="text-base font-semibold text-zinc-900">연동된 계정</h3>
        <p class="text-sm text-zinc-500">
          소셜 로그인 계정 연동 상태를 확인합니다.
        </p>
      </div>

      <div
        class="mt-4 flex items-center justify-between gap-4 rounded-2xl border border-zinc-200 bg-zinc-50 px-4 py-3 sm:px-5 sm:py-4"
      >
        <div class="flex items-center gap-3">
          <div
            class="flex h-11 w-11 items-center justify-center rounded-full bg-indigo-100 text-indigo-600"
          >
            <!-- 이메일 아이콘 -->
            <svg class="h-7 w-7" viewBox="0 0 20 20" aria-hidden="true">
              <path
                d="M3.75 4.5h12.5A1.75 1.75 0 0 1 18 6.25v7.5A1.75 1.75 0 0 1 16.25 15.5H3.75A1.75 1.75 0 0 1 2 13.75v-7.5A1.75 1.75 0 0 1 3.75 4.5Zm0 1.5 6.25 3.75L16.25 6h-12.5Zm0 1.31v6.44h12.5V7.31l-6.06 3.64a.75.75 0 0 1-.78 0L3.75 7.31Z"
                fill="currentColor"
              />
            </svg>
          </div>
          <div>
            <p class="text-sm font-medium text-zinc-900">Google</p>
            <p class="text-xs text-zinc-500">{{ email }}</p>
          </div>
        </div>

        <span
          class="inline-flex items-center rounded-full bg-emerald-100 px-4 py-2 text-xs font-medium text-emerald-700"
          >연동됨</span
        >
      </div>
    </section>
  </section>
</template>

<script setup lang="ts">
import { useAuthStore } from "@/stores/auth";
import { computed, onMounted, ref, watch } from "vue";
import * as UserApi from "@/api/users";

const auth = useAuthStore();

// 이름 편집 상태
const editableName = ref("");

// 로딩 상태
const isSavingName = ref(false);
const isPhotoUpdating = ref(false);

// 파일 input
const fileInputRef = ref<HTMLInputElement | null>(null);

const profileImageUrl = computed(() => auth.user?.profileImageUrl ?? null);
const email = computed(() => auth.user?.email ?? "");

// 아바타 이니셜
const avatar = computed(() => {
  const n = editableName.value?.trim() || auth.user?.name || "";
  return n ? n[0] : "U";
});

onMounted(() => {
  if (auth.user) {
    editableName.value = auth.user.name;
  }
});

watch(
  () => auth.user?.name,
  (newName) => {
    if (newName && !isSavingName.value) {
      editableName.value = newName;
    }
  }
);

// 이름 변경 여부
const isNameChanged = computed(() => {
  const current = auth.user?.name ?? "";
  return editableName.value.trim() !== current.trim();
});

// 이름 저장
const onSubmit = async () => {
  if (!auth.user) return;
  const nextName = editableName.value.trim();
  if (!nextName || !isNameChanged.value) return;

  isSavingName.value = true;
  try {
    const updated = await UserApi.updateUser({ name: nextName });
    auth.setUser(updated);
    // TODO: 성공 토스트 알림 연결
  } catch (e) {
    console.error("이름 변경 실패:", e);
    // TODO: 에러 토스트 알림 연결
  } finally {
    isSavingName.value = false;
  }
};

// 사진 변경 버튼 클릭 시 input 열기
const onClickChangePhoto = () => {
  if (isPhotoUpdating.value) return;
  fileInputRef.value?.click();
};

// 프로필 이미지 변경
const onFileChange = async (event: Event) => {
  const target = event.target as HTMLInputElement;
  const file = target.files?.[0];
  if (!file) return;

  const maxSize = 5 * 1024 * 1024; // 5MB 제한
  if (file.size > maxSize) {
    console.error("파일 크기 제한 초과 (최대 5MB)");
    // TODO: 에러 토스트 알림 연결
    target.value = "";
    return;
  }

  isPhotoUpdating.value = true;
  try {
    const updated = await UserApi.updateProfileImage(file);
    auth.setUser(updated);
    // TODO: 에러 토스트 알림 연결
  } catch (e) {
    console.error("프로필 이미지 업로드 실패:", e);
    // TODO: 에러 토스트 알림 연결
  } finally {
    isPhotoUpdating.value = false;
    target.value = "";
  }
};

// 프로필 이미지 삭제
const onDeletePhoto = async () => {
  if (!profileImageUrl.value) return;

  isPhotoUpdating.value = true;
  try {
    const updated = await UserApi.deleteProfileImage();
    auth.setUser(updated);
  } catch (e) {
    console.error("프로필 이미지 삭제 실패:", e);
    // TODO: 에러 토스트 알림 연결
  } finally {
    isPhotoUpdating.value = false;
  }
};
</script>
