# LinkNest

나만의 북마크를 체계적으로 관리하고, 필요한 정보를 빠르게 찾아보는 북마크 관리 서비스

<br>

## 프로젝트 소개

LinkNest는 웹에서 발견한 유용한 정보를 북마크, 컬렉션, 태그를 활용해 체계적으로 관리할 수 있는 서비스입니다.<br>
북마크를 컬렉션별로 정리하고 태그를 활용해 분류할 수 있으며, 검색과 휴지통 기능을 통해 저장한 북마크를 편리하게 관리할 수 있습니다.

### 프로젝트 기간

- 2025.09 ~ 2026.04 (실 개발 기간 약 6개월)
- 2026.04 ~ 현재 (기능 확장 및 리팩토링)

### 배포 링크

[LinkNest 바로가기](https://linknest.io.kr)

### 기술 스택

#### FRONT

`Vue 3`, `TypeScript`, `Vite`, `Pinia`, `Axios`, `Tailwind CSS`

#### BACK

`Java 21`, `Spring Boot`, `Spring Security`, `Spring Data JPA`, `JWT`, `MySQL`, `Redis`, `OAuth 2.0` / `OIDC`, `Docker`

#### DEPLOYMENT

`GitHub Actions`, `AWS ECS Fargate`, `RDS`, `ElastiCache`, `S3`, `CloudFront`, `ALB`, `Route 53`

### 시스템 아키텍처

![아키텍처 다이어그램](docs/images/architecture.png)
GitHub Actions를 활용하여 PR에서는 빌드 및 테스트를 수행하고,
develop 병합 시 스테이징 환경에, 버전 태그 생성 시 프로덕션 환경에 배포합니다.

### ERD

<p>
    <img src="docs/images/erd.png" width="60%">
</p>

<br>

## 주요 기능

### 🔐 로그인

Google, Kakao 소셜 로그인을 지원하며 테스트 계정을 통해 별도의 소셜 로그인 없이 서비스 기능을 확인할 수 있습니다.<br>
![로그인 화면](docs/images/login.png)

---

<br>

### 📁 컬렉션 관리

컬렉션을 생성하여 북마크를 주제별로 관리할 수 있습니다.<br>
컬렉션의 순서를 변경하거나 상•하위 관계를 구성하여 계층적으로 정리할 수 있습니다.<br>
![컬렉션 화면](docs/images/collection-overview.png)

<p>
    <img src="docs/images/collection-reorder.gif" width="32%">
    <img src="docs/images/collection-hierarchy.gif" width="32%">
</p>

---

<br>

### 🔖 북마크 관리

웹 페이지의 URL을 저장하고 제목, 설명, 컬렉션, 태그 등의 정보를 관리할 수 있습니다.<br>
저장된 북마크의 상세 정보를 확인하고 편집할 수 있으며, 자주 사용하는 북마크는 즐겨찾기로 지정할 수 있습니다.<br>
![북마크 상세 화면](docs/images/bookmark-detail.png)

<p>
    <img src="docs/images/bookmark-add.png" width="45%">
    <img src="docs/images/bookmark-edit.png" width="45%">
</p>

---

<br>

### 🏷️ 태그 관리

북마크에 태그를 지정하여 다양한 기준으로 분류할 수 있습니다.<br>
태그 이름 변경, 병합 및 제거를 지원하며, 태그가 적용된 북마크를 모아 태그를 일괄 변경하거나 제거할 수 있습니다.<br>
![태그 관리 화면 1](docs/images/tag-management-1.png)
![태그 관리 화면 2](docs/images/tag-management-2.png)

---

<br>

### 🔍 북마크 검색

북마크 목록에서 제목, URL, 태그를 기준으로 북마크를 검색할 수 있습니다.<br>
![북마크 검색 화면](docs/images/bookmark-search.png)

---

<br>

### 🗑️ 휴지통

삭제한 북마크를 휴지통에서 확인하고 선택적으로 복구하거나 영구 삭제할 수 있습니다.<br>
![휴지통 화면](docs/images/trash-selected.png)

---

<br>

### ⚙️ 설정

계정, 보안, 사용자 환경설정 등의 기능을 관리할 수 있습니다.<br>
![설정 화면](docs/images/settings-overview.png)

<p>
    <img src="docs/images/settings-account.png" width="32%">
    <img src="docs/images/settings-security.png" width="32%">
    <img src="docs/images/settings-preferences.png" width="32%">
</p>
