/** === 백엔드 응답 DTO (Res) === */
export interface BookmarkRes {
  id: number;
  collectionId: number;
  url: string;
  title: string;
  description: string;
  createdAt: string;
  updatedAt: string;
}

export interface CollectionRes {
  id: number;
  name: string;
  icon: string | null;
  parentId: number | null;
  sortOrder: number;
  createdAt: string;
  updatedAt: string;
  bookmarkCount: number;
}
