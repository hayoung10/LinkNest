import http from "./http";
import { UserPreferences } from "@/types/common";
import { UserPreferencesRes } from "./types";
import { mapUserPreferences } from "./mappers";

export interface UserPreferencesUpdateReq {
  defaultBookmarkSort?: string;
  defaultLayout?: string;
  openInNewTab?: boolean;
  keepSignedIn?: boolean;
}

/** 조회 */
export async function getUserPreferences(): Promise<UserPreferences> {
  const { data } = await http.get<UserPreferencesRes>(`/users/me/preferences`);
  return mapUserPreferences(data);
}

/** 수정 */
export async function updateUserPreferences(
  payload: UserPreferencesUpdateReq
): Promise<UserPreferences> {
  const { data } = await http.patch<UserPreferencesRes>(
    `/users/me/preferences`,
    payload
  );
  return mapUserPreferences(data);
}
