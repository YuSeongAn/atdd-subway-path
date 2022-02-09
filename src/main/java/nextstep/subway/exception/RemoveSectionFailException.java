package nextstep.subway.exception;

public class RemoveSectionFailException extends RuntimeException {
    private static final String MSG_CANNOT_REMOVE = "노선을 제거할 수 없습니다";

    public RemoveSectionFailException() {
        super(MSG_CANNOT_REMOVE);
    }
}