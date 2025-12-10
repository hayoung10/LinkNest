import { defineStore } from "pinia";
import {
  UserPreferences,
  BookmarkSortOption,
  LayoutOption,
} from "@/types/common";
import * as UserPreferencesApi from "@/api/preferences";
import type { UserPreferencesUpdateReq } from "@/api/preferences";

interface PreferencesState {
  loaded: boolean;
  defaultBookmarkSort: BookmarkSortOption;
  defaultLayout: LayoutOption;
  openInNewTab: boolean;
  keepSignedIn: boolean;
}

export const usePreferencesStore = defineStore("preferences", {
  state: (): PreferencesState => ({
    loaded: false,
    defaultBookmarkSort: "NEWEST",
    defaultLayout: "LIST",
    openInNewTab: true,
    keepSignedIn: true,
  }),
  actions: {
    async load(force = false) {
      if (this.loaded && !force) return;

      const preferences: UserPreferences =
        await UserPreferencesApi.getUserPreferences();

      this.defaultBookmarkSort = preferences.defaultBookmarkSort;
      this.defaultLayout = preferences.defaultLayout;
      this.openInNewTab = preferences.openInNewTab;
      this.keepSignedIn = preferences.keepSignedIn;
      this.loaded = true;
    },

    async update(payload: UserPreferencesUpdateReq) {
      const updated = await UserPreferencesApi.updateUserPreferences(payload);

      this.defaultBookmarkSort = updated.defaultBookmarkSort;
      this.defaultLayout = updated.defaultLayout;
      this.openInNewTab = updated.openInNewTab;
      this.keepSignedIn = updated.keepSignedIn;
    },
  },
});
