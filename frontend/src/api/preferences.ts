import http, { unwrap } from "./http";
import {
  UserPreferences,
  BookmarkSortOption,
  LayoutOption,
} from "@/types/common";
import type { UserPreferencesRes } from "./types";
import { mapUserPreferences } from "./mappers";

export interface UserPreferencesUpdateReq {
  defaultBookmarkSort?: BookmarkSortOption;
  defaultLayout?: LayoutOption;
  openInNewTab?: boolean;
  keepSignedIn?: boolean;
}

/** 조회 */
export async function getUserPreferences(): Promise<UserPreferences> {
  const data = await unwrap<UserPreferencesRes>(
    http.get(`/users/me/preferences`),
  );
  return mapUserPreferences(data);
}

/** 수정 */
export async function updateUserPreferences(
  payload: UserPreferencesUpdateReq,
): Promise<UserPreferences> {
  const data = await unwrap<UserPreferencesRes>(
    http.patch(`/users/me/preferences`, payload),
  );
  return mapUserPreferences(data);
}
