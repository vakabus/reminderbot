package cz.vakabus.reminderbot.utils;

import lombok.NonNull;

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

    public static <T,E> Result<T,E> error(@NonNull E error) {
        return new Result<>(null, error);
    }

    public static <T,E> Result<T,E> success(@NonNull T success) {
        return new Result<>(success, null);
    }

    public boolean isError() {
        return isError;
    }
    public boolean isSuccess() {
        return !isError;
    }

    public T expect(@NonNull String errMsg) {
        if (isError)
            throw new ResultAccessException(errMsg, error);

        return success;
    }

    @NonNull
    public T unwrap() {
        return this.expect("Accessed Result containing error!");
    }

    @NonNull
    public E expectError(String errMsg) {
        if (!isError)
            throw new ResultAccessException(errMsg, error);

        return error;
    }

    @NonNull
    public E unwrapError() {
        return this.expectError("Accessed error of Result containing success!");
    }

    @NonNull
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
