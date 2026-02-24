import { useAuthStore } from "@/stores/auth";
import { createRouter, createWebHistory, RouteRecordRaw } from "vue-router";

const routes: RouteRecordRaw[] = [
  {
    path: "/",
    name: "home",
    component: () => import("@/views/HomeView.vue"),
  },
  {
    path: "/workspace",
    name: "workspace",
    component: () => import("@/views/WorkspaceView.vue"),
    meta: { requiresAuth: true },
  },
  {
    path: "/tags",
    name: "tags",
    component: () => import("@/views/TagManagementView.vue"),
    meta: { requiresAuth: true },
  },
  {
    path: "/trash",
    name: "trash",
    component: () => import("@/views/TrashView.vue"),
    meta: { requiresAuth: true },
  },

  // 게스트 전용
  {
    path: "/login",
    name: "login",
    component: () => import("@/views/LoginView.vue"),
    meta: { guestOnly: true },
  },

  // OAuth 리다이렉트 콜백
  {
    path: "/redirect",
    name: "redirect",
    component: () => import("@/views/Redirect.vue"),
  },

  // 404
  {
    path: "/:pathMatch(.*)*",
    name: "notfound",
    component: () => import("@/views/NotFound.vue"),
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// 전역 라우터 가드: 첫 진입 시 auth.restore를 1회 보장, 이후 접근 제어
router.beforeEach(async (to) => {
  const auth = useAuthStore();

  // 첫 진입이면 복원 1회
  if (!auth.restored) {
    await auth.restore();
  }

  // 보호 라우트 -> 로그인 필요
  if (to.meta.requiresAuth && !auth.isLoggedIn) {
    return { name: "login", query: { redirect: to.fullPath } };
  }

  // 게스트 전용 라우트 -> 로그인 상태면 홈으로
  if (to.meta.guestOnly && auth.isLoggedIn) {
    return { name: "home" };
  }

  return true;
});

export default router;
