package com.linknest.backend.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public enum ErrorCode {
    // 공통
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 입력입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않은 메서드입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),
    OAUTH2_AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "OAuth2 인증에 실패했습니다."),
    FILE_EMPTY(HttpStatus.BAD_REQUEST, "업로드할 파일이 비어 있습니다."),

    // user
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),

    // bookmark
    BOOKMARK_NOT_FOUND(HttpStatus.NOT_FOUND, "북마크를 찾을 수 없습니다."),
    INVALID_BOOKMARK_URL(HttpStatus.BAD_REQUEST, "북마크 URL이 유효하지 않습니다."),
    INVALID_IMAGE_MODE(HttpStatus.BAD_REQUEST, "유효하지 않은 이미지 모드입니다."),
    CUSTOM_IMAGE_REQUIRED(HttpStatus.BAD_REQUEST, "CUSTOM 모드에서는 커스텀 이미지가 필요합니다."),
    TAG_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "태그는 최대 3개까지 지정할 수 있습니다."),

    // collection
    COLLECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "컬렉션을 찾을 수 없습니다."),
    COLLECTION_PARENT_SELF(HttpStatus.BAD_REQUEST, "컬렉션을 자기 자신으로 이동할 수 없습니다."),
    COLLECTION_CYCLE_DETECTED(HttpStatus.BAD_REQUEST, "컬렉션 이동으로 인해 순환 구조가 발생합니다."),
    COLLECTION_TRASH_RESTORE_FAILED(HttpStatus.CONFLICT, "컬렉션 복구에 실패했습니다."),
    COLLECTION_MAX_DEPTH_EXCEEDED(HttpStatus.BAD_REQUEST, "컬렉션 깊이는 최대 5단계까지 허용됩니다."),

    // tag
    TAG_NOT_FOUND(HttpStatus.NOT_FOUND, "태그를 찾을 수 없습니다."),
    TAG_NAME_DUPLICATED(HttpStatus.CONFLICT, "이미 사용 중인 태그 이름입니다."),
    TAG_IN_USE(HttpStatus.BAD_REQUEST, "사용 중인 태그는 삭제할 수 없습니다."),
    TAG_NAME_INVALID(HttpStatus.BAD_REQUEST, "태그 이름은 비어 있을 수 없습니다."),
    TAG_NAME_TOO_LONG(HttpStatus.BAD_REQUEST, "태그 이름은 50자를 초과할 수 없습니다."),

    // refresh token
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 만료되었습니다."),
    MISMATCH_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 일치하지 않습니다.");


    @Getter
    private final HttpStatus status;
    @Getter
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
