<template>
  <div class="h-screen w-full bg-background flex">
    <!-- 좌측: 사이드바 -->
    <WorkspaceSidebar
      class="w-64 border-r border-border"
      @add-collection="onAddCollection"
      @rename-collection="onRenameCollection"
      @update-emoji="onUpdateCollectionEmoji"
      @delete-collection="onDeleteCollection"
      @open-all="onOpenAllBookmarks"
      @open-settings="onOpenSettings"
      @logout="onLogout"
    />

    <!-- 우측: 메인 콘텐츠 -->
    <section class="relative flex-1 flex flex-col overflow-hidden min-h-0">
      <!-- 목록 -->
      <BookmarkList
        v-if="isListLayout"
        :key="'list-' + (selectedCollectionId || 'none')"
        :collection="selectedCollection"
        :selected-bookmark-id="selectedBookmarkId"
        @open-add="openAddPanel"
        @select-bookmark="onSelectBookmark"
      />

      <BookmarkCard
        v-else
        :key="'card-' + (selectedCollectionId || 'none')"
        :collection="selectedCollection"
        :selected-bookmark-id="selectedBookmarkId"
        @open-add="openAddPanel"
        @select-bookmark="onSelectBookmark"
      />

      <!-- 북마크 상세 패널 -->
      <SidePanel
        :open="selectedBookmarkId != null"
        width="min(640px, 92vw)"
        side="right"
        @close="selectedBookmarkId = null"
      >
        <!-- 상태 분기 -->
        <BaseError
          v-if="hasBookmarksError"
          title="북마크를 불러올 수 없습니다"
          :description="bookmarksError ?? undefined"
          :onRetry="retryFetchBookmarks"
        />

        <BaseLoading
          v-else-if="isDetailPending"
          label="북마크 정보를 불러오는 중…"
        />

        <BaseEmpty
          v-else-if="selectedBookmark == null"
          title="북마크를 선택해주세요."
          description="북마크를 선택하면 상세 정보를 볼 수 있습니다."
        />

        <BookmarkDetail
          v-else
          :bookmark="selectedBookmark"
          :collection-name="selectedCollection?.name"
          @close="selectedBookmarkId = null"
          @update-bookmark="onUpdateBookmark"
          @delete-bookmark="onDeleteBookmark"
          @replace-bookmark="onReplaceBookmark"
        />
      </SidePanel>

      <!-- 북마크 추가 패널 -->
      <SidePanel
        :open="isAddOpen"
        width="min(640px, 92vw)"
        side="right"
        @after-open="addRef?.focusUrl()"
        @close="isAddOpen = false"
      >
        <AddBookmarkForm
          ref="addRef"
          :open="isAddOpen"
          :collection-id="selectedCollectionId"
          @close="isAddOpen = false"
          @submit="onAddBookmark"
        />
      </SidePanel>

      <!-- 설정 패널 -->
      <SidePanel
        :open="isSettingsOpen"
        width="min(640px, 92vw)"
        side="right"
        @close="isSettingsOpen = false"
      >
        <Settings @close="isSettingsOpen = false" />
      </SidePanel>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import {
  WorkspaceSidebar,
  BookmarkList,
  BookmarkCard,
  BookmarkDetail,
  AddBookmarkForm,
} from "@/features/workspace";
import { Settings } from "@/features/settings";
import SidePanel from "@/components/overlays/SidePanel.vue";
import type { Bookmark, CollectionNode, ID, ImageMode } from "@/types/common";
import { useWorkspaceStore } from "@/stores/workspace";
import * as BookmarkApi from "@/api/bookmarks";
import { storeToRefs } from "pinia";
import { useAuthStore } from "@/stores/auth";
import { useRouter } from "vue-router";
import { usePreferencesStore } from "@/stores/preferences";
import { BaseEmpty, BaseError, BaseLoading } from "@/components/ui";
import { useToastStore } from "@/stores/toast";

type UpdateBookmarkPayload = {
  id: ID;
  title?: string | null;
  url: string;
  description?: string | null;
  emoji?: string | null;
};

const toast = useToastStore();
const workspace = useWorkspaceStore();
const preferences = usePreferencesStore();

const { selectedCollectionId, bookmarks, isLoading, error, collectionById } =
  storeToRefs(workspace);
const { defaultLayout } = storeToRefs(preferences);

const auth = useAuthStore();
const router = useRouter();

const selectedCollection = computed<CollectionNode | null>(() => {
  const sid = selectedCollectionId.value;
  if (sid == null) return null;
  return collectionById.value[sid] ?? null;
});

const isListLayout = computed(() => defaultLayout.value === "LIST");

const selectedBookmarkId = ref<ID | null>(null);
const selectedBookmark = computed(() => {
  if (!selectedBookmarkId.value) return null;
  return bookmarks.value.find((b) => b.id === selectedBookmarkId.value) ?? null;
});

const isAddOpen = ref(false);
const isSettingsOpen = ref(false);

const addRef = ref<{ focusUrl: () => void } | null>(null);

const isLoadingBookmarks = computed(() => isLoading.value.bookmarks);
const bookmarksError = computed(() => error.value.bookmarks);
const hasBookmarksError = computed(() => !!bookmarksError.value);

const isDetailOpen = computed(() => selectedBookmarkId.value != null);
const isDetailPending = computed(() => {
  // 선택했지만 아직 로딩 중이라 북마크 상세를 못 찾는 상태
  return (
    isDetailOpen.value &&
    selectedBookmark.value == null &&
    isLoadingBookmarks.value &&
    !hasBookmarksError.value
  );
});

onMounted(() => {
  workspace.fetchCollectionTree();
});

// 핸들러
async function onAddCollection(payload: { name: string; parentId: ID | null }) {
  try {
    await workspace.createCollection({
      name: payload.name,
      parentId: payload.parentId,
      emoji: null,
    });
    toast.success("컬렉션을 추가했습니다.");
  } catch (e) {
    toast.error("컬렉션 추가에 실패했습니다.");
  }
}

async function onRenameCollection(payload: { id: ID; newName: string }) {
  try {
    await workspace.updateCollection(payload.id, { name: payload.newName });
  } catch (e) {
    toast.error("이름 변경에 실패했습니다.");
  }
}

async function onUpdateCollectionEmoji(payload: {
  id: ID;
  emoji: string | null;
}) {
  try {
    await workspace.updateCollectionEmoji(payload.id, { emoji: payload.emoji });
  } catch (e) {
    toast.error("컬렉션 이모지 설정에 실패했습니다.");
  }
}

async function onDeleteCollection(id: ID) {
  try {
    await workspace.deleteCollection(id);
    toast.success("컬렉션이 삭제되었습니다.");

    if (selectedCollectionId.value === id) {
      workspace.selectCollection(null);
      selectedBookmarkId.value = null;
      isAddOpen.value = false;

      await workspace.fetchBookmarks();
    }
  } catch (e) {
    toast.error("컬렉션 삭제에 실패했습니다.");
  }
}

async function onOpenAllBookmarks(collectionId: ID) {
  try {
    let target = bookmarks.value;

    if (selectedCollectionId.value !== collectionId) {
      target = await BookmarkApi.listBookmarks(collectionId);
    }

    const urls = target
      .map((b) => b.url?.trim())
      .filter((u): u is string => !!u);

    if (!urls.length) return;

    // 10개 초과인 경우, 확인 대화창
    if (urls.length > 10) {
      const ok = window.confirm(
        `총 ${urls.length}개의 북마크를 새 탭에서 여시겠습니까?\n` +
          "브라우저 설정에 따라 일부 탭은 차단될 수 있습니다."
      );
      if (!ok) return;
    }

    // 탭 열기 시도 + 차단된 개수 카운트
    let blockedCount = 0;
    for (const url of urls) window.open(url, "_blank", "noopener,noreferrer");
  } catch (e) {
    console.error("[WorkspaceView] failed to open all bookmarks", e);
    toast.error("북마크를 여는 중 오류가 발생했습니다.");
  }
}

function onSelectBookmark(id: ID) {
  selectedBookmarkId.value = id;
  isSettingsOpen.value = false;
}

function openAddPanel() {
  if (selectedCollectionId.value == null) {
    toast.info("북마크를 추가할 컬렉션을 선택해주세요.");
    return;
  }
  isAddOpen.value = true;
}

async function onAddBookmark(payload: {
  title?: string | null;
  url: string;
  description?: string | null;
  emoji?: string | null;
  imageMode?: ImageMode;
  collectionId: ID;
}) {
  try {
    await workspace.createBookmark({
      collectionId: payload.collectionId,
      url: payload.url,
      title: payload.title ?? null,
      description: payload.description ?? null,
      emoji: payload.emoji ?? null,
      imageMode: payload.imageMode ?? "AUTO",
    });
    await workspace.fetchBookmarks(payload.collectionId);
    isAddOpen.value = false;
  } catch (e) {
    toast.error("북마크 추가에 실패했습니다.");
  }
}

async function onUpdateBookmark(payload: UpdateBookmarkPayload) {
  try {
    await workspace.updateBookmark(payload.id, {
      url: payload.url,
      title: payload.title,
      description: payload.description,
      emoji: payload.emoji ?? null,
    });

    const cid = selectedCollectionId.value;
    if (cid != null) await workspace.fetchBookmarks(cid);
  } catch (e) {
    toast.error("북마크 수정에 실패했습니다.");
  }
}

async function onDeleteBookmark(id: ID) {
  const cid = selectedCollectionId.value;

  try {
    await workspace.deleteBookmark(id);

    if (cid != null) await workspace.fetchBookmarks(cid);

    if (selectedBookmarkId.value === id) {
      selectedBookmarkId.value = null;
    }

    toast.success("북마크가 삭제되었습니다.");
  } catch (e) {
    toast.error("북마크 삭제에 실패했습니다.");
  }
}

function onReplaceBookmark(updated: Bookmark) {
  workspace.replaceBookmark(updated);
}

function onOpenSettings() {
  isAddOpen.value = false;
  selectedBookmarkId.value = null;
  isSettingsOpen.value = true;
}

async function onLogout() {
  try {
    await auth.logout();
    await router.replace({
      path: "/login",
      state: { type: "info", message: "로그아웃되었습니다." },
    });
    toast.info("로그아웃 되었습니다.");
  } catch (e) {
    console.error("[WorkspaceView] 로그아웃 실패:", e);
    toast.error("로그아웃에 실패했습니다.");
  }
}

function retryFetchBookmarks() {
  const cid = selectedCollectionId.value;
  if (cid != null) {
    workspace.fetchBookmarks(cid);
  }
}

watch(
  selectedCollectionId,
  async (cid, prev) => {
    if (cid === prev) return;

    selectedBookmarkId.value = null;
    isAddOpen.value = false;
    isSettingsOpen.value = false;

    try {
      if (cid == null) {
        await workspace.fetchBookmarks();
        return;
      }

      await workspace.fetchBookmarks(cid);
    } catch (e) {
      toast.error("북마크 목록을 불러오지 못했습니다.");
    }
  },
  { immediate: true }
);
</script>
