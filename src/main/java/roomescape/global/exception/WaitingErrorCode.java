package roomescape.global.exception;

import org.springframework.http.HttpStatus;

public enum WaitingErrorCode implements ErrorCode {

    WAITING_NOT_FOUND(HttpStatus.NOT_FOUND, "대기를 찾을 수 없습니다."),
    WAITING_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "대기자 이름을 입력해 주세요."),
    WAITING_DATE_REQUIRED(HttpStatus.BAD_REQUEST, "대기 날짜를 선택해 주세요."),
    WAITING_TIME_REQUIRED(HttpStatus.BAD_REQUEST, "대기 시간을 선택해 주세요."),
    WAITING_THEME_REQUIRED(HttpStatus.BAD_REQUEST, "대기 테마를 선택해 주세요."),
    WAITING_CREATE_IN_PAST(HttpStatus.BAD_REQUEST, "지난 일정으로 대기를 신청할 수 없습니다."),
    WAITING_OWNER_MISMATCH(HttpStatus.FORBIDDEN, "대기를 취소할 수 있는 권한이 없습니다.");

    private final HttpStatus status;
    private final String message;

    WaitingErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
