# API 문서

## 1. 기본 정보

### Base Path

```text
/api/v1
```

### 인증

인증이 필요한 API는 Access Token을 다음 Header에 포함합니다.

```http
Authorization: Bearer {accessToken}
```

Refresh Token은 `HttpOnly` Cookie로 관리합니다.

### 공통 성공 응답

```json
{
  "status": 200,
  "code": "OK",
  "message": "요청 처리 결과",
  "data": {}
}
```

`data`가 없는 경우 `null`이 반환됩니다.

### 공통 에러 응답

```json
{
  "timestap": "2026-09-21T10:00:00Z",
  "status": 400,
  "code": "INVALID_INPUT_VALUE",
  "message": "잘못된 입력입니다.",
  "path": "/api/v1/bookmarks",
  "errors": []
}
```

입력값 검증 오류가 발생한 경우 `errors`에 필드별 오류 정보가 포함될 수 있습니다.

| HTTP Status | Code                              | 설명                                          |
| ----------: | --------------------------------- | --------------------------------------------- |
|         400 | `INVALID_INPUT_VALUE`             | 잘못된 입력입니다.                            |
|         400 | `INVALID_BOOKMARK_URL`            | 북마크 URL이 유효하지 않습니다.               |
|         400 | `INVALID_IMAGE_MODE`              | 유효하지 않은 이미지 모드입니다.              |
|         400 | `CUSTOM_IMAGE_REQUIRED`           | CUSTOM 모드에서는 커스텀 이미지가 필요합니다. |
|         400 | `TAG_LIMIT_EXCEEDED`              | 태그는 최대 3개까지 지정할 수 있습니다.       |
|         401 | `UNAUTHORIZED`                    | 인증이 필요합니다.                            |
|         401 | `INVALID_REFRESH_TOKEN`           | 유효하지 않은 리프레시 토큰입니다.            |
|         401 | `EXPIRED_REFRESH_TOKEN`           | 리프레시 토큰이 만료되었습니다.               |
|         401 | `MISMATCH_REFRESH_TOKEN`          | 리프레시 토큰이 일치하지 않습니다.            |
|         401 | `OAUTH2_AUTHENTICATION_FAILED`    | OAuth2 인증에 실패했습니다.                   |
|         403 | `ACCESS_DENIED`                   | 접근 권한이 없습니다.                         |
|         403 | `TEST_ACCOUNT_CANNOT_BE_DELETED`  | 테스트 계정은 삭제할 수 없습니다.             |
|         404 | `USER_NOT_FOUND`                  | 사용자를 찾을 수 없습니다.                    |
|         404 | `BOOKMARK_NOT_FOUND`              | 북마크를 찾을 수 없습니다.                    |
|         404 | `COLLECTION_NOT_FOUND`            | 컬렉션을 찾을 수 없습니다.                    |
|         404 | `TAG_NOT_FOUND`                   | 태그를 찾을 수 없습니다.                      |
|         409 | `EMAIL_ALREADY_EXISTS`            | 이미 사용 중인 이메일입니다.                  |
|         409 | `TAG_NAME_DUPLICATED`             | 이미 사용 중인 태그 이름입니다.               |
|         409 | `COLLECTION_TRASH_RESTORE_FAILED` | 컬렉션 복구에 실패했습니다.                   |
|         400 | `COLLECTION_PARENT_SELF`          | 컬렉션을 자기 자신으로 이동할 수 없습니다.    |
|         400 | `COLLECTION_CYCLE_DETECTED`       | 컬렉션 이동으로 인해 순환 구조가 발생합니다.  |
|         400 | `COLLECTION_MAX_DEPTH_EXCEEDED`   | 컬렉션 깊이는 최대 5단계까지 허용됩니다.      |
|         400 | `TAG_IN_USE`                      | 사용 중인 태그는 삭제할 수 없습니다.          |
|         400 | `TAG_NAME_INVALID`                | 태그 이름은 비어 있을 수 없습니다.            |
|         400 | `TAG_NAME_TOO_LONG`               | 태그 이름은 50자를 초과할 수 없습니다.        |
|         400 | `FILE_EMPTY`                      | 업로드할 파일이 비어 있습니다.                |
|         405 | `METHOD_NOT_ALLOWED`              | 허용되지 않은 메서드입니다.                   |
|         500 | `INTERNAL_ERROR`                  | 서버 오류가 발생했습니다.                     |

---

<br>

## 2. 인증 (Auth)

| 기능                | Method   | Endpoint                       | 인증 | Request              | Response            |
| ------------------- | -------- | ------------------------------ | ---- | -------------------- | ------------------- |
| Goolge 로그인       | `GET`    | `/oauth2/authorization/google` | ❌   | OAuth 인증           | Redirect            |
| Kakao 로그인        | `GET`    | `/oauth2/authorization/kakao`  | ❌   | OAuth 인증           | Redirect            |
| 테스트 로그인       | `POST`   | `/auth/test-login`             | ❌   | 없음                 | ApiResponse<Void>   |
| Access Token 재발급 | `POST`   | `/auth/refresh`                | ❌   | Refresh Token Cookie | `TokenRefreshRes`   |
| 로그아웃            | `POST`   | `/auth/logout`                 | ❌   | Refresh Token Cookie | `ApiResponse<Void>` |
| 전체 기기 로그아웃  | `DELETE` | `/auth/sessions`               | ✅   | 없음                 | `ApiResponse<Void>` |

### 토큰 재발급

Refresh Token을 Cookie에서 읽어 Access Token과 Refresh Token을 재발급합니다.

`TokenRefreshRes`

| 필드          | 타입     | 설명                                |
| ------------- | -------- | ----------------------------------- |
| `accessToken` | `String` | 새 Access Token                     |
| `tokenType`   | `String` | `Bearer`                            |
| `expiresIn`   | `long`   | Access Token 만료까지 남은 시간(초) |

---

<br>

## 3. 사용자 (User)

| 기능               | Method   | Endpoint                  | 인증 | Request          | Response            |
| ------------------ | -------- | ------------------------- | ---- | ---------------- | ------------------- |
| 내 정보 조회       | `GET`    | `/users/me`               | ✅   | 없음             | `UserRes`           |
| 내 정보 수정       | `UPDATE` | `/users/me`               | ✅   | `UserUpdateReq`  | `UserRes`           |
| 프로필 이미지 수정 | `PATCH`  | `/users/me/profile-image` | ✅   | Multipart `file` | `UserRes`           |
| 프로필 이미지 삭제 | `DELETE` | `/users/me/profile-image` | ✅   | 없음             | `UserRes`           |
| 회원 탈퇴          | `DELETE` | `/users/me`               | ✅   | 없음             | `ApiResponse<Void>` |

### `UserUpdateReq`

| 필드   | 타입     | 필수 | 설명                    |
| ------ | -------- | ---- | ----------------------- |
| `name` | `String` | ❌   | 사용자 이름, 최대 100자 |

---

<br>

## 4. 사용자 환경설정 (Preferences)

| 기능          | Method  | Endpoint                | 인증 | Request                    | Response             |
| ------------- | ------- | ----------------------- | ---- | -------------------------- | -------------------- |
| 환경설정 조회 | `GET`   | `/users/me/preferences` | ✅   | 없음                       | `UserPreferencesRes` |
| 환경설정 수정 | `PATCH` | `/users/me/preferences` | ✅   | `UserPreferencesUpdateReq` | `UserPreferencesRes` |

### `UserPreferencesRes`

| 필드                  | 타입      | 설명                  |
| --------------------- | --------- | --------------------- |
| `defaultBookmarkSort` | `String`  | 기본 북마크 정렬 방식 |
| `defaultLayout`       | `String`  | 기본 목록 레이아웃    |
| `openInNewTab`        | `boolean` | 링크 새 탭 열기 여부  |
| `keepSignedIn`        | `boolean` | 로그인 유지 여부      |

---

<br>

## 5. 컬렉션 (Collection)

| 기능                  | Method   | Endpoint                     | 인증 | Request                    | Response                  |
| --------------------- | -------- | ---------------------------- | ---- | -------------------------- | ------------------------- |
| 컬렉션 생성           | `POST`   | `/collections`               | ✅   | `CollectionCreateReq`      | `CollectionRes`           |
| 컬렉션 조회           | `GET`    | `/collections/{id}`          | ✅   | Path `id`                  | `CollectionRes`           |
| 컬렉션 수정           | `PATCH`  | `/collections/{id}`          | ✅   | `CollectionUpdateReq`      | `CollectionRes`           |
| 컬렉션 이모지 변경    | `PATCH`  | `/collections/{id}/emoji`    | ✅   | `CollectionEmojiUpdateReq` | `CollectionRes`           |
| 컬렉션 삭제           | `DELETE` | `/collections/{id}`          | ✅   | Path `id`                  | `ApiResponse<Void>`       |
| 하위 컬렉션 목록 조회 | `GET`    | `/collections?parentId={id}` | ✅   | Query `parentId`           | `List<CollectionRes>`     |
| 컬렉션 이동           | `PATCH`  | `/collections/{id}/move`     | ✅   | `CollectionMoveReq`        | `CollectionPositionRes`   |
| 컬렉션 순서 변경      | `PATCH`  | `/collections/{id}/reorder`  | ✅   | `CollectionReorderReq`     | `CollectionPositionRes`   |
| 컬렉션 트리 조회      | `GET`    | `/collections/tree`          | ✅   | 없음                       | `List<CollectionNodeRes>` |
| 컬렉션 경로 조회      | `GET`    | `/collections/{id}/path`     | ✅   | Path `id`                  | `List<CollectionPathRes>` |

### 주요 컬렉션 오류

- `COLLECTION_PARENT_SELF`
- `COLLECTION_CYCLE_DETECTED`
- `COLLECTION_MAX_DEPTH_EXCEEDED`
- `COLLECTIOn_TRASH_RESTORE_FAILED`
- `COLLECTION_NOT_FOUND`

---

<br>

## 6. 북마크 (Bookmark)

| 기능                 | Method   | Endpoint                       | 인증 | Request                                   | Response                     |
| -------------------- | -------- | ------------------------------ | ---- | ----------------------------------------- | ---------------------------- |
| 북마크 생성          | `POST`   | `/bookmarks`                   | ✅   | `BookmarkCreateReq`                       | `BookmarkRes`                |
| 북마크 상세 조회     | `GET`    | `/bookmarks/{id}`              | ✅   | Path `id`                                 | `BookmarkRes`                |
| 북마크 수정          | `PATCH`  | `/bookmarks/{id}`              | ✅   | `BookmarkUpdateReq`                       | `BookmarkRes`                |
| 북마크 삭제          | `DELETE` | `/bookmarks/{id}`              | ✅   | Path `id`                                 | `ApiResponse<Void>`          |
| 컬렉션별 북마크 목록 | `GET`    | `/bookmarks?collectionId={id}` | ✅   | Query `collectionId`, `q`, `page`, `size` | `SliceResponse<BookmarkRes>` |
| 북마크 이동          | `PATCH`  | `/bookmarks/{id}/move`         | ✅   | `BookmarkMoveReq`                         | `ApiResponse<Void>`          |
| 이모지 변경          | `PATCH`  | `/bookmarks/{id}/emoji`        | ✅   | `BookmarkEmojiUpdateReq`                  | `BookmarkRes`                |
| 이모지 삭제          | `DELETE` | `/bookmarks/{id}/emoji`        | ✅   | Path `id`                                 | `ApiResponse<Void>`          |
| 커버 이미지 업로드   | `POST`   | `/bookmarks/{id}/cover`        | ✅   | Multipart `file`                          | `BookmarkRes`                |
| 커버 이미지 삭제     | `DELETE` | `/bookmarks/{id}/cover`        | ✅   | Path `id`                                 | `BookmarkRes`                |
| 이미지 모드 변경     | `PATCH`  | `/bookmarks/{id}/image-mode`   | ✅   | `BookmarkImageModeUpdateReq`              | `BookmarkRes`                |
| 즐겨찾기 변경        | `PATCH`  | `/bookmarks/{id}/favorite`     | ✅   | `BookmarkFavoriteUpdateReq`               | `BookmarkRes`                |
| 즐겨찾기 목록        | `GET`    | `/bookmarks/favorites`         | ✅   | Query `q`, `page`, `size`                 | `SliceResponse<BookmarkRes>` |

### 북마크 목록 Query Parameter

| 파라미터       | 타입     | 필수 | 기본값 | 설명             |
| -------------- | -------- | ---- | ------ | ---------------- |
| `collectionId` | `Long`   | ✅   | -      | 조회할 컬렉션 ID |
| `q`            | `String` | ❌   | -      | 검색어           |
| `page`         | `int`    | ❌   | `0`    | 페이지 번호      |
| `size`         | `int`    | ❌   | `20`   | 페이지 크기      |

즐겨찾기 목록은 `collectionId` 없이 `q`, `page`, `size`를 사용합니다.

### `BookmarkCreateReq`

| 필드           | 타입           | 필수 | 설명                 |
| -------------- | -------------- | ---- | -------------------- |
| `collectionId` | `Long`         | ✅   | 소속 컬렉션 ID       |
| `url`          | `String`       | ✅   | 북마크 URL, 2~2048자 |
| `title`        | `String`       | ❌   | 제목, 최대 255자     |
| `description`  | `String`       | ❌   | 설명, 최대 1000자    |
| `emoji`        | `String`       | ❌   | 이모지, 최대 16자    |
| `imageMode`    | `ImageMode`    | ❌   | 이미지 표시 모드     |
| `tags`         | `List<String>` | ❌   | 태그 목록, 최대 3개  |

### 주요 북마크 오류

- `BOOKMARK_NOT_FOUND`
- `INVALID_BOOKMARK_URL`
- `INVALID_IMAGE_MODE`
- `CUSTOM_IMAGE_REQUIRED`
- `TAG_LIMIT_EXCEEDED`
- `FILE_EMPTY`

---

<br>

## 7. 태그 (Tag)

| 기능                      | Method   | Endpoint               | 인증 | Request                           | Response                           |
| ------------------------- | -------- | ---------------------- | ---- | --------------------------------- | ---------------------------------- |
| 태그 생성                 | `POST`   | `/tags`                | ✅   | `TagCreateReq`                    | `TagCreateResultRes`               |
| 태그 목록 조회            | `GET`    | `/tags`                | ✅   | Query `q`, `sort`, `page`, `size` | `SliceResponse<TagRes>`            |
| 태그 이름 변경            | `PATCH`  | `/tags/{id}`           | ✅   | `TagUpdateReq`                    | `TagRes`                           |
| 태그 병합                 | `POST`   | `/tags/{id}/merge`     | ✅   | `TagMergeReq`                     | `ApiResponse<Void>`                |
| 태그 삭제                 | `DELETE` | `/tags/{id}`           | ✅   | Path `id`                         | `ApiResponse<Void>`                |
| 태그가 지정된 북마크 조회 | `GET`    | `/tags/{id}/bookmarks` | ✅   | Query `q`, `page`, `size`         | `SliceResponse<TaggedBookmarkRes>` |
| 북마크에서 태그 제거      | `POST`   | `/tags/{id}/detach`    | ✅   | `TagDetachReq`                    | `ApiResponse<Void>`                |
| 북마크의 태그 교체        | `POST`   | `/tags/{id}/replace`   | ✅   | `TagReplaceReq`                   | `ApiResponse<Void>`                |
| 태그 집계 조회            | `GET`    | `/tags/summary`        | ✅   | 없음                              | `TagSummaryRes`                    |

### 태그 목록 Query Parameter

| 파라미터 | 타입      | 필수 | 기본값     | 설명        |
| -------- | --------- | ---- | ---------- | ----------- |
| `q`      | `String`  | ❌   | -          | 태그 검색어 |
| `sort`   | `TagSort` | ❌   | `NAME_ASC` | 정렬 기준   |
| `page`   | `int`     | ❌   | `0`        | 페이지 번호 |
| `size`   | `int`     | ❌   | `20`       | 페이지 크기 |

### 주요 태그 오류

- `TAG_NOT_FOUND`
- `TAG_NAME_DUPLICATED`
- `TAG_IN_USE`
- `TAG_NAME_INVALID`
- `TAG_NAME_TOO_LONG`

---

<br>

## 8. 휴지통 (Trash)

| 기능                     | Method   | Endpoint                     | 인증 | Request                      | Response                    |
| ------------------------ | -------- | ---------------------------- | ---- | ---------------------------- | --------------------------- |
| 휴지통 목록 조회         | `GET`    | `/trash`                     | ✅   | Query `type`, `page`, `size` | SliceResponse<TrashItemRes> |
| 휴지통 비우기            | `DELETE` | `/trash/empty`               | ✅   | Query `type`                 | `ApiResponse<Void>`         |
| 단일 항목 복구           | `POST`   | `/trash/{type}/{id}/restore` | ✅   | Path `type`, `id`            | `ApiResponse<Void>`         |
| 단일 항목 영구 삭제      | `DELETE` | `/trash/{type}/{id}`         | ✅   | Path `type`, `id`            | `ApiResponse<Void>`         |
| 여러 유형 항목 일괄 복구 | `POST`   | `/trash/restore`             | ✅   | `TrashMixedBulkReq`          | `ApiResponse<Void>`         |
| 여러 유형 항목 일괄 삭제 | `POST`   | `/trash/delete`              | ✅   | `TrashMixedBulkReq`          | `ApiResponse<Void>`         |
| 동일 유형 항목 일괄 복구 | `POST`   | `/trash/{type}/restore`      | ✅   | `TrashBulkReq`               | `ApiResponse<Void>`         |
| 동일 유형 항목 일괄 삭제 | `POST`   | `/trash/{type}/delete`       | ✅   | `TrashBulkReq`               | `ApiResponse<Void>`         |

`type`은 서버에서 정의한 `TrashType` 값을 사용합니다.

---

<br>

## 9. 주요 DTO

### `UserRes`

| 필드                    | 타입           | 설명                    |
| ----------------------- | -------------- | ----------------------- |
| `id`                    | `Long`         | 사용자 ID               |
| `email`                 | `String`       | 이메일                  |
| `name`                  | `String`       | 이름                    |
| `profileImageUrl`       | `String`       | 프로필 이미지 URL       |
| `hasCustomProfileImage` | `boolean`      | 사용자 지정 이미지 여부 |
| `role`                  | `Role`         | 사용자 권한             |
| `provider`              | `AuthProvider` | 인증 제공자             |
| `createdAt`             | `Instant`      | 생성 시각               |
| `updatedAt`             | `Instant`      | 수정 시각               |

### `CollectionRes`

| 필드            | 타입      | 설명           |
| --------------- | --------- | -------------- |
| `id`            | `Long`    | 컬렉션 ID      |
| `name`          | `String`  | 컬렉션 이름    |
| `emoji`         | `String`  | 컬렉션 이모지  |
| `providerId`    | `Long`    | 상위 컬렉션 ID |
| `sortOrder`     | `int`     | 정렬 순서      |
| `createdAt`     | `Instant` | 생성 시각      |
| `updatedAt`     | `Instant` | 수정 시각      |
| `bookmarkCount` | `long`    | 포함 북마크 수 |
| `childCount`    | `long`    | 하위 컬렉션 수 |

### `BookmarkRes`

| 필드              | 타입              | 설명              |
| ----------------- | ----------------- | ----------------- |
| `id`              | `Long`            | 북마크 ID         |
| `collectionId`    | `Long`            | 컬렉션 ID         |
| `url`             | `String`          | URL               |
| `title`           | `String`          | 제목              |
| `description`     | `String`          | 설명              |
| `emoji`           | `String`          | 이모지            |
| `autoImageUrl`    | `String`          | 자동 이미지 URL   |
| `customImageUrl`  | `String`          | 커스텀 이미지 URL |
| `imageMode`       | `ImageMode`       | 이미지 모드       |
| `autoImageStatus` | `AutoImageStatus` | 자동 이미지 상태  |
| `isFavorite`      | `boolean`         | 즐겨찾기 여부     |
| `tags`            | `List<String>`    | 태그 목록         |
| `createdAt`       | `Instant`         | 생성 시각         |
| `updatedAt`       | `Instant`         | 수정 시각         |

---

<br>

## 10. 페이징

목록 API는 `SliceResponse`를 사용하며 기본 페이지 번호는 `0`, 기본 페이지 크기는 `20`입니다.

주요 목록 API:

- 북마크 목록
- 즐겨찾기 목록
- 태그 목록
- 태그별 북마크 목록
- 휴지통 목록

검색/정렬이 지원되는 API에서는 해당 Query Parameter를 함께 사용합니다.
