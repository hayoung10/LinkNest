export type PageMeta = {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type PageResponse<T> = {
  items: T[];
  meta: PageMeta;
};

export type SliceMeta = {
  page: number;
  size: number;
  hasNext: boolean;
};

export type SliceResponse<T> = {
  items: T[];
  meta: SliceMeta;
};
