# Refresh Token 무효화 시 Axios 인증 요청이 반복되는 문제

## 문제 사항

로그아웃 과정에서 `/auth/refresh` 요청이 반복적으로 발생했다. 브라우저 Network 탭에서 Refresh 요청이 계속 확인되었고, 백엔드에는 `INVALID_REFRESH_TOKEN` 코드의 401 오류가 반복적으로 기록되었다.

페이지를 수동으로 새로고침하면 반복 요청이 멈췄지만, 인증 실패 후 처리가 정상적으로 종료되지 않는 문제가 남아 있었다.

조사 과정에서는 여러 API가 동시에 401을 반환할 때 Refresh 요청이 중복 실행되는 현상도 확인했다.

<br>

## 원인 분석

### 1. 로그아웃 요청의 401이 다시 Refresh와 로그아웃을 호출했다

기존 Axios 응답 인터셉터는 401 응답을 받으면 Access Token을 재발급하고 원래 요청을 재시도했다. 재발급이 실패하면 `auth.logout()`을 호출했다.

```typescript
const isRefeshCall =
  typeof original?.url === "string" && original.url.includes("/auth/refresh");

if (error.response?.status === 401 && !original._retry && !isRefreshCall) {
  original._retry = true;
  try {
    await auth.refresh();
    return http(original);
  } catch (e) {
    await auth.logout();
    return Promise.reject(e);
  }
}
```

`/auth/refresh` 자체의 401은 재발급 대상에서 제외했지만, `/auth/logout`은 제외하지 않았다. 백엔드에서도 `/api/v1/auth/logout`이 `permitAll()` 대상에 포함되어 있지 않아 인증이 유효하지 않은 상태의 로그아웃 요청이 401을 반환했다.

이 두 조건이 결합하면서 다음 순환이 발생했다.

```text
/auth/logout → 401
→ 인터셉터에서 auth.refresh() 호출
→ /auth/refresh → 401 (INVALID_REFRESH_TOKEN)
→ 실패 처리에서 auth.logout() 호출
→ 새로운 /auth/logout → 401
→ 동일한 흐름 반복
...
```

`_retry`는 원래 요청을 반복 재시도하지 않도록 하는 표시였다. 하지만 `auth.logout()`이 생성한 새로운 요청에는 이전 요청의 `_retry`가 적용되지 않았다. 따라서 개별 요청의 재시도 제한만으로는 Refresh 실패와 로그아웃 실패 사이의 순환을 차단할 수 없었다.

### 2. RT 폐기 실패가 쿠키 삭제를 막을 수 있었다

백엔드의 로그아웃 메서드는 쿠키에서 RT를 추출해 `revokeToken()`을 호출한 뒤 쿠키 삭제 응답을 생성했다.

기존에는 RT 처리 중 발생하는 `JwtException`과 `IllegalArgumentException`을 해당 메서드에서 처리하지 않았다. 이 예외가 발생하면 이후의 쿠키 삭제 코드까지 진행할 수 없었다.

이는 로그아웃 401의 인터셉터 재진입과 구분되는 세션 정리 문제였다. 서버의 RT 폐기 처리와 브라우저의 RT 쿠키 삭제는 별도의 작업으로, 유효하지 않은 RT도 쿠키에서는 제거할 수 있도록 처리할 필요가 있었다.

### 3. 동시 401과 세션 복원에서도 중복 실행을 줄일 필요가 있었다

인증 Store의 `refresh()`에는 이미 진행 중인 요청을 `_refreshPromise`로 공유하는 처리가 있었다. 이 처리는 여러 요청이 재발급 진행 중에 실패하는 경우 동일한 Refresh 결과를 기다리게 했다.

다만 재발급이 완료되어 `_refreshPromise`가 해제된 뒤, 이전 Access Token으로 보낸 다른 요청의 401이 늦게 도착할 수 있었다. 기존 인터셉터는 요청에 사용한 토큰과 현재 토큰을 비교하지 않아 이 경우에도 다시 Refresh를 시도했다.

또한 여러 요청의 Refresh 실패 처리에서 각각 `logout()`을 호출할 수 있었고, `App.vue`와 라우터 가드 양쪽에 세션 복원 호출이 존재했다. 이 부분은 최초 무한 반복의 직접 원인과 별개로, 조사 과정에서 함께 정리한 인증 처리의 중복 실행 문제였다.

<br>

## 해결

### 1. 인증 API의 401이 재발급 흐름에 재진입하지 않도록 했다

자동 Refresh 제외 대상을 `/auth/refresh`, `/auth/logout`, `/auth/test-login`으로 확장했다.

```typescript
const url = original?.url?.split("?")[0];

const isAuthCall =
  typeof url === "string" &&
  ["/auth/refresh", "/auth/logout", "/auth/test-login"].includes(url);

if (error.response?.status === 401 && !original._retry && !isAuthCall) {
  original._retry = true;
  // Access Token 재발급 및 원래 요청 재시도
}
```

이 API들이 401로 실패했을 때는 응답 인터셉터에서 자동 재발급을 실행하지 않고 오류를 호출부로 전달하도록 했다. 특히 로그아웃 실패가 다시 Refresh를 호출하지 않게 하여 순환을 차단했다.

### 2. 인증이 유효하지 않아도 로그아웃과 쿠키 삭제를 진행하도록 했다

기존에는 `/auth/logout` 요청이 401로 차단되면 백엔드의 로그아웃 메서드가 실행되지 않아 RT 쿠키가 삭제되지 않았다. `/api/v1/auth/logout`에 `permitAll()`을 적용하고, RT 폐기 과정에서 JWT 관련 예외가 발생해도 쿠키 삭제를 진행하도록 수정했다.

```java
@PostMapping("/logout")
public ResponseEntity<ApiResponse<Void>> logout(
        HttpServletRequest request, HttpServletResponse response) {
    try {
        CookieUtils.getCookieValue(request, RT_COOKIE)
                .ifPresent(tokenService::revokeToken);
    } catch (JwtException | IllegalArgumentException e) {
        // 유효하지 않은 RT라도 브라우저 쿠키 삭제는 진행
    }

    ResponseCookie delCookie = CookieUtils.deleteCookie(RT_COOKIE);
    response.addHeader(HttpHeaders.SET_COOKIE, delCookie.toString());

    return ResponseEntity.ok(ApiResponse.ok("로그아웃 완료"));
}
```

Refresh 실패 시에는 `auth.logout()`을 호출해 서버에서 RT 폐기를 시도하고, 유효하지 않은 RT도 브라우저 쿠키에서는 삭제하도록 했다. Store에서는 기존과 동일하게 로그아웃 요청의 성공 여부와 관계없이 `finally`에서 `clearSession()`을 호출해 클라이언트의 인증 상태를 초기화했다.

세션 복원 과정의 실패 처리도 보완했다. 기존 `restore()`는 Refresh 실패 시 `clearSession()`만 호출했지만, 수정 후에는 Refresh가 401로 실패하면 `logout()`을 호출해 서버의 로그아웃 처리까지 진행하도록 했다. 그 외 오류에서는 `clearSession()`으로 클라이언트의 인증 상태를 초기화한 뒤 복원을 종료했다.

### 3. Refresh와 로그아웃의 중복 실행을 제어했다

기존 `_refreshPromise` 공유는 유지하면서, 인터셉터에서 실패한 요청의 토큰과 Store의 현재 토큰을 비교하도록 했다.

```typescript
const requestToken = original.headers?.Authorization;
const currentToken = auth.accessToken ? `Bearer ${auth.accessToken}` : null;

// 다른 요청이 이미 AT를 갱신한 경우 refresh 생략
if (!currentToken || requestToken === currentToken) {
  try {
    await auth.refresh();
  } catch (e) {
    await auth.logout();
    return Promise.reject(e);
  }
}

if (!auth.accessToken) {
  return Promise.reject(error);
}

original.headers = {
  ...original.headers,
  Authorization: `Bearer ${auth.accessToken}`,
};

return http(original);
```

다른 요청이 이미 토큰을 갱신했다면 추가 Refresh를 생략하고 현재 토큰으로 재시도했다. 재시도 직전에는 최신 Access Token을 원래 요청의 `Authorization` 헤더에 명시적으로 반영했다.

진행 중인 Refresh를 공유하는 처리와, 갱신 완료 후 늦게 도착한 401을 처리하는 토큰 비교는 서로 다른 시점의 중복 재발급을 줄이는 역할을 했다.

`logout()`에는 `_logoutPromise`를 추가했다. 로그아웃이 진행 중이면 같은 Promise를 반환하고, 완료 후에는 참조를 해제하도록 하여 여러 실패 처리에서 겹쳐 호출한 로그아웃을 공유했다.

### 4. 세션 복원과 로그인 상태 변화 처리의 책임을 구분했다

`App.vue`의 초기화 훅에서 수행하던 `auth.restore()` 호출을 제거하고, 라우터 가드에서 `auth.restored`를 확인해 복원을 수행하도록 정리했다.

```typescript
if (!auth.restored) {
  await auth.restore();
}

if (to.meta.requiresAuth && !auth.isLoggedIn) {
  return { name: "login", query: { redirect: to.fullPath } };
}
```

라우터 가드는 세션 복원 이후 목적지에 접근할 수 있는지 판단했다. `App.vue`는 `auth.restored`를 기준으로 세션 복원 완료 여부를 판단해 화면을 표시하고, 인증이 필요한 화면에 머무는 중 로그인 상태가 해제되면 로그인 화면으로 이동하도록 했다.

이를 통해 초기 진입 시의 인증 판단과, 화면이 열린 이후 인증 상태가 변경되는 상황을 각각 처리했다.

<br>

## 검증

RT가 무효화된 상태에서 브라우저 Network와 쿠키, 화면 이동 결과를 확인했다.

| 확인 항목            | 최종 결과                 |
| -------------------- | ------------------------- |
| `/auth/refresh` 요청 | 1회 발생, 401 응답        |
| `/auth/logout` 요청  | 1회 발생, 200 응답        |
| 브라우저 RT 쿠키     | 삭제됨                    |
| 화면 이동            | 로그인 화면으로 정상 이동 |
| 추가 Refresh 반복    | 발생하지 않음             |

인증 실패 후 Refresh와 로그아웃 요청이 반복되지 않고, 쿠키와 클라이언트 세션을 정리한 뒤 로그인 화면으로 이동하는 것을 확인했다.

<br>

## 배운 점

- Axios 인터셉터에서 401을 처리할 때는 요청의 역할에 따라 자동 재발급 대상을 구분해야 했다. 특히 로그아웃 실패가 Refresh를 호출하고, Refresh 실패가 새로운 로그아웃 요청을 생성하는 구조에서는 개별 요청의 재시도 제한만으로 순환을 막을 수 없었다.

- 여러 API가 동시에 401을 반환하는 상황에서는 진행 중인 Refresh를 공유하는 것뿐 아니라, 토큰 갱신 후 늦게 도착한 이전 요청의 401도 구분해야 했다. 원래 요청을 재시도할 때는 최신 Access Token을 적용하고, 여러 실패 처리에서 호출하는 Logout도 중복 실행을 제어할 필요가 있었다.

- 세션 복원과 로그인 상태 변화 처리의 책임을 구분해야 했다. 라우터 가드는 세션 복원과 페이지 접근 제어를 담당하고, `App.vue`는 복원 완료 여부와 로그인 상태 변화에 따른 화면 처리를 담당하도록 정리했다.

- 로그아웃은 RT가 만료되거나 유효하지 않은 상황에서도 쿠키를 삭제할 수 있어야 했다. 개별 API 응답뿐 아니라 Refresh 실패부터 로그아웃, 쿠키 삭제, 클라이언트 인증 상태 초기화, 로그인 화면 이동까지 전체 인증 흐름을 함께 검증해야 했다.
