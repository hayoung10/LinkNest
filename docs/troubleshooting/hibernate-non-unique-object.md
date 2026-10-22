# Bookmark 태그 수정 시 `NonUniqueObjectException`이 발생하는 문제

## 문제 사항

Bookmark의 태그를 수정할 때 기존 태그 관계를 모두 제거한 뒤, 요청받은 태그를 다시 생성하는 방식으로 동기화하고 있었다.

태그 수정 과정에서 다음과 같은 예외가 발생했다.

- `org.springframework.dao.DuplicateKeyException`
- 원인 예외: `org.hibernate.NonUniqueObjectException`

```text
A different object with the same identifier value was already associated with the session:
[com.linknest.backend.bookmark.BookmarkTag#com.linknest.backend.bookmark.BookmarkTagId@43e]
```

즉, 동일한 식별자를 가진 `BookmarkTag` 객체가 이미 Hibernate Session에 존재하는 상태에서 같은 식별자를 가진 다른 `BookmarkTag` 객체를 다시 생성하면서 예외가 발생했다.

<br>

## 원인 분석

### 1. `BookmarkTag`는 복합 키를 사용하고 있었다

`BookmarkTag`는 `Bookmark`와 `Tag`의 조합인 `(bookmark_id, tag_id)`를 복합 식별자로 사용하고 있었다.

따라서 동일한 `Bookmark`와 `Tag`의 조합으로 생성된 `BookmarkTag`는 동일한 식별자를 갖는다.

<br>

### 2. 기존 관계를 제거한 뒤 동일한 식별자의 `BookmarkTag`를 다시 생성하고 있었다

기존 태그 수정 로직은 다음과 같이 `Bookmark`의 태그 관계를 전체 교체하는 방식이었다.

```java
bookmark.getBookmarkTags().clear();

for (String name : names) {
        Tag tag = byName.get(name);
        bookmark.getBookmarkTags().add(BookmarkTag.create(bookmark, tag));
}
```

`clear()`를 수행하면 `Bookmark`와 기존 `BookmarkTag`의 연관관계가 제거되고, `orphanRemoval = true`에 따라 기존 `BookmarkTag`는 삭제 대상으로 처리된다.

하지만 기존 `BookmarkTag` 객체가 해당 트랜잭션의 영속성 컨텍스트에서 관리되는 상태에서, 동일한 `Bookmark`와 `Tag` 조합의 새로운 `BookmarkTag`를 생성하면 동일한 식별자를 가진 서로 다른 엔티티 인스턴스가 충돌하게 된다.

결과적으로 Hibernate가 동일한 식별자를 가진 서로 다른 엔티티 인스턴스를 하나의 영속성 컨텍스트에서 관리하려 하면서 `NonUniqueObjectException`이 발생했다.

<br>

## 해결

### 1. 기존 엔티티 재사용

기존 `BookmarkTag`를 무조건 삭제하고 다시 생성하는 방식 대신, 기존 관계를 재사용하면서 변경된 관계만 추가·삭제하는 방식으로 동기화하도록 수정했다.

태그 수정 시 다음과 같이 처리한다.

1. 기존 `BookmarkTag` 중 요청된 태그는 그대로 재사용
2. 기존에 없던 태그만 새로운 `BookmarkTag` 생성
3. 요청에서 제외된 기존 `BookmarkTag`는 제거
4. 동일한 Bookmark-Tag 조합의 `BookmarkTag`를 다시 생성하지 않음

이를 통해 동일한 복합 키를 가진 `BookmarkTag` 엔티티를 같은 Persistence Context에서 중복 생성하는 문제를 방지했다.

### 2. 고아 `Tag` 정리를 스케줄러로 분리

Bookmark 수정 과정에서 관계가 끊어진 `Tag`를 즉시 삭제하는 대신, 사용되지 않은 태그를 별도의 스케줄러에서 정리하도록 분리했다.

`TagCleanupScheduler`에서 더 이상 Bookmark와 연결되지 않은 Tag를 일정 기간 이후 정리하도록 구성했다.

이를 통해 Bookmark의 태그 관계 수정과 사용되지 않는 Tag 데이터의 정리를 서로 분리했다.

<br>

## 검증

Bookmark 수정 시 태그를 추가하거나 제거하는 동작을 수행하여 기존 태그와 새로운 태그가 정상적으로 동기화되는 것을 확인했다.

동일한 복합 키를 가진 `BookmarkTag` 엔티티가 중복 생성되지 않았으며, 기존에 발생하던 `NonUniqueObjectException` 및 `DuplicateKeyException`이 발생하지 않는 것을 확인했다.

## 배운 점

- Hibernate의 영속성 컨텍스트에서는 동일한 식별자를 가진 엔티티를 하나의 객체 인스턴스로 관리한다.

- 연관 엔티티를 동기화할 때 기존 엔티티를 무시하고 동일한 식별자를 가진 객체를 새로 생성하면 `NonUniqueObjectException`이 발생할 수 있다.

- 전체 교체(sync) 방식 자체가 문제가 되는 것은 아니며, 기존 엔티티를 재사용하고 실제로 필요한 경우에만 새로운 엔티티를 생성하는 것이 중요하다.
