package cz.vakabus.reminderbot.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class Result<T,E> {
    private boolean isError;
    private @Nullable T success = null;
    private @Nullable E error = null;

    private Result(@Nullable T success, E error) {
        this.success = success;
        this.error = error;
        isError = success == null;
    }

    public static <T,E> Result<T,E> error(@NotNull E error) {
        return new Result<>(null, error);
    }

    public static <T,E> Result<T,E> success(@NotNull T success) {
        return new Result<>(success, null);
    }

    public boolean isError() {
        return isError;
    }
    public boolean isSuccess() {
        return !isError;
    }

    @Nullable
    public T expect(@NotNull String errMsg) {
        if (isError)
            throw new ResultAccessException(errMsg, error);

        return success;
    }

    @NotNull
    public T unwrap() {
        return this.expect("Accessed Result containing error!");
    }

    @NotNull
    public E expectError(String errMsg) {
        if (!isError)
            throw new ResultAccessException(errMsg, error);

        return error;
    }

    @NotNull
    public E unwrapError() {
        return this.expectError("Accessed error of Result containing success!");
    }

    @NotNull
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
