import { defineStore } from "pinia";

export type ToastType = "info" | "success" | "error";

export interface ToastItem {
  id: string;
  message: string;
  type: ToastType;
  durationMs: number;
  createdAt: number;
}

function uid() {
  return `${Date.now()}-${Math.random().toString(36).slice(2)}`;
}

export const useToastStore = defineStore("toast", {
  state: () => ({
    toasts: [] as ToastItem[],
  }),

  actions: {
    show(message: string, type: ToastType = "info", durationMs = 2500) {
      const id = uid();
      const item: ToastItem = {
        id,
        message,
        type,
        durationMs,
        createdAt: Date.now(),
      };
      this.toasts.push(item);

      if (durationMs > 0) {
        window.setTimeout(() => this.dismiss(id), durationMs);
      }

      return id;
    },

    info(message: string, durationMs = 2500) {
      return this.show(message, "info", durationMs);
    },

    success(message: string, durationMs = 2000) {
      return this.show(message, "success", durationMs);
    },

    error(message: string, durationMs = 3000) {
      return this.show(message, "error", durationMs);
    },

    dismiss(id: string) {
      this.toasts = this.toasts.filter((t) => t.id !== id);
    },

    clear() {
      this.toasts = [];
    },
  },
});
