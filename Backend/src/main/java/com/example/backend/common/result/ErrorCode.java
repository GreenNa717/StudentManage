package com.example.backend.common.result;

public final class ErrorCode {

    private ErrorCode() {}

    public static final int PARAM_ERROR = 40001;
    public static final int UNAUTHORIZED = 40101;
    public static final int FORBIDDEN = 40301;
    public static final int NOT_FOUND = 40401;
    public static final int CONFLICT = 40901;
    public static final int SERVER_ERROR = 50001;
}
