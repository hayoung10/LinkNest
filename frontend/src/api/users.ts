import http, { unwrap } from "./http";
import type { User } from "@/types/common";
import type { UserRes } from "./types";
import { mapUserRes } from "./mappers";

export interface UserUpdateReq {
  name: string;
}

/** 조회 */
export async function getMe(): Promise<User> {
  const data = await unwrap<UserRes>(http.get(`/users/me`));
  return mapUserRes(data);
}

/** 이름 수정 */
export async function updateUser(payload: UserUpdateReq): Promise<User> {
  const data = await unwrap<UserRes>(http.patch(`/users/me`, payload));
  return mapUserRes(data);
}

/** 프로필 이미지 수정 */
export async function updateProfileImage(file: File): Promise<User> {
  const formData = new FormData();
  formData.append("file", file);

  const data = await unwrap<UserRes>(
    http.patch(`/users/me/profile-image`, formData),
  );

  return mapUserRes(data);
}

/** 프로필 이미지 삭제 */
export async function deleteProfileImage(): Promise<User> {
  const data = await unwrap<UserRes>(http.delete(`/users/me/profile-image`));
  return mapUserRes(data);
}

/** 계정 삭제 */
export async function deleteAccount(): Promise<void> {
  await unwrap<void>(http.delete(`/users/me`));
}
