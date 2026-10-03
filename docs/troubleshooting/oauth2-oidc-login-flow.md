# OAuth2 로그인 성공 후 `providerId`와 `userId`가 `null`로 전달되는 문제

## 문제 사항

OAuth2 로그인 성공 후 응답 데이터에서 `providerId`와 `userId`가 `null`로 전달되는 문제가 발생했다.

로그인 성공 자체는 정상적으로 처리되었지만, `SuccessHandler`에서 인증된 사용자 정보의 커스텀 속성을 조회했을 때 다음 값이 정상적으로 전달되지 않았다.

```json
{
  "providerId": null,
  "userId": null
}
```

처음에는 `CustomOAuth2UserService`에서 사용자 정보를 정상적으로 추가하고 있다고 판단했지만, 실제 인증 과정에서 해당 서비스가 호출되지 않은 것을 확인했다.

<br>

## 원인 분석

### 1. `openid` 스코프에 따른 인증 흐름 차이

OAuth2 로그인 설정에서 `openid` 스코프를 사용하고 있었다. ,

Spring Security에서는 `openid` 스코프가 포함된 경우 일반 OAuth2 사용자 정보 처리 과정이 아닌 OIDC 인증 흐름이 사용되며, `CustomOAuth2UserService`가 아니라 `OidcUserService`가 호출된다.

기존에는 `CustomOAuth2UserService`에서 사용자 정보를 보강하고 있었기 때문에, OIDC 인증 흐름에서는 해당 로직이 실행되지 않았다.

따라서 `SuccessHandler`에서 필요한 `providerId`와 `userId`가 `principal`에 포함되지 않은 상태였다.

### 2. OIDC 사용자 정보에 커스텀 claims 전달 누락

`CustomOidcUserService`를 추가한 이후에도 커스텀 정보가 정상적으로 전달되지 않는 문제가 있었다.

사용자 정보에 다음과 같이 커스텀 claims를 추가했지만,

```java
Map<String, Object> claims = new HashMap<>(oidcUser.getClaims());
claims.put("providerId", providerId);
claims.put("userId", user.getId());
```

`DefaultOidcUser`를 생성할 때 해당 `claims`를 실제 사용자 정보로 전달하지 않고 있었다.

그 결과 `claims.put(...)`으로 값을 추가했음에도 최종적으로 생성된 `OidcUser`의 attributes에서는 해당 값이 확인되지 않았다.

<br>

## 해결

### 1. `CustomOidcUserService` 구현 및 등록

OIDC 인증 흐름에서도 사용자 정보를 보강할 수 있도록 `CustomOidcUserService`를 구현했다.

그리고 `SecurityConfig`에서 OAuth2와 OIDC 사용자 정보 서비스를 각각 등록했다.

```java
.oauth2Login(o -> o
    .userInfoEnpoint(u -> u
        .oidcUserService(customOidcUserService)
        .userService(customOAuth2UserService)
    )
)
```

이를 통해 인증 방식에 따라 적절한 사용자 정보 처리 로직이 실행되도록 했다.

<br>

### 2. `OidcUserInfo`를 통해 커스텀 claims 전달

`DefaultOidcUser` 생성 시 커스텀 claims가 실제 사용자 정보에 포함되도록 `OidcUserInfo`로 감싸서 전달했다.

```java
Map<String, Object> claims = new HashMap<>(oidcUser.getClaims());
claims.put("providerId", providerId);
claims.put("userId", user.getId());

OidcUserInfo userInfo = new OidcUserInfo(claims);

return new DefaultOidcUser(
    authorities,
    oidcUser.getIdToken(),
    userInfo,
    nameAttributeKey
);
```

<br>

## 검증

로그인 성공 후 `SuccessHandler`에서 다음과 같이 `principal`의 attributes를 확인했다.

```java
principal.getAttributes();
```

확인 결과 `providerId`와 `userId`가 정상적으로 포함되어 있었으며, 로그인 성공 후 필요한 사용자 정보가 정상적으로 전달되는 것을 확인했다.

<br>

## 배운 점

- OAuth2와 OIDC는 Spring Security에서 사용자 정보를 처리하는 흐름이 다르다.

- `openid` 스코프가 포함되면 `CustomOAuth2UserService`가 아닌 `OidcUserService`가 사용될 수 있으므로 인증 제공자의 설정과 실제 실행 경로를 함께 확인해야 한다.

- `claims`에 값을 추가하는 것만으로는 충분하지 않고, 최종적으로 생성되는 `OidcUser`에 해당 정보가 전달되는지 확인해야 한다.
