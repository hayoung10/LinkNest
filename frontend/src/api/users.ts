import http from "./http";
import type { User } from "@/types/common";
import { UserRes } from "./types";
import { mapUserRes } from "./mappers";

export interface UserUpdateReq {
  name: string;
}

/** 조회 */
export async function getMe(): Promise<User> {
  const { data } = await http.get<UserRes>(`/users/me`);
  return mapUserRes(data);
}

/** 이름 수정 */
export async function updateUser(payload: UserUpdateReq): Promise<User> {
  const { data } = await http.patch<UserRes>(`/users/me`, payload);
  return mapUserRes(data);
}

/** 프로필 이미지 수정 */
export async function updateProfileImage(file: File): Promise<User> {
  const formData = new FormData();
  formData.append("file", file);

  const { data } = await http.patch<UserRes>(
    `/users/me/profile-image`,
    formData,
    { headers: { "Content-Type": "multipart/form-data" } }
  );

  return mapUserRes(data);
}

/** 프로필 이미지 삭제 */
export async function deleteProfileImage(): Promise<User> {
  const { data } = await http.delete(`/users/me/profile-image`);
  return mapUserRes(data);
}
