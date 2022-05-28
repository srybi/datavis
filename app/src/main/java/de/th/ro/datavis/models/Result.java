package de.th.ro.datavis.models;

public class Result<T> {
    private boolean success;
    private T data;
    private String message;

    private Result(boolean success, T data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
    }

    public static <T> Result<T> success(T data){
        return new Result<T>(true, data, null);
    }
    public static <T> Result<T> error(String message){
        return new Result<T>(false, null, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }
}
