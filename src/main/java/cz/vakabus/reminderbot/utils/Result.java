package cz.vakabus.reminderbot.utils;

import java.util.stream.Stream;

public class Result<T,E> {
    private boolean isError;
    private T success = null;
    private E error = null;

    private Result(T success, E error) {
        this.success = success;
        this.error = error;
        isError = success == null;
    }

    public static <T,E> Result<T,E> error(E error) {
        return new Result<>(null, error);
    }

    public static <T,E> Result<T,E> success(T success) {
        return new Result<>(success, null);
    }

    public boolean isError() {
        return isError;
    }
    public boolean isSuccess() {
        return !isError;
    }

    public T expect(String errMsg) {
        if (isError)
            throw new ResultAccessException(errMsg, error);

        return success;
    }

    public T unwrap() {
        return this.expect("Accessed Result containing error!");
    }

    public E expectError(String errMsg) {
        if (!isError)
            throw new ResultAccessException(errMsg, error);

        return error;
    }

    public E unwrapError() {
        return this.expectError("Accessed error of Result containing success!");
    }

    public Stream<T> stream() {
        if (isError)
            return Stream.empty();
        else
            return Stream.of(success);
    }

    public static class ResultAccessException extends RuntimeException {
        Object error;

        public ResultAccessException(String errorMsg, Object error) {
            super(errorMsg);
            this.error = error;
        }
    }
}
