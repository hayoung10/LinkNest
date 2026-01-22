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
