package com.jobifycvut.backend.exception;


//this class extends RuntimeException and outcomes the message as "Invalid credentials" or allows you to create an exception error message.
//super calls the method and provides you to call RuntimeException
public class AuthException extends RuntimeException {
    public AuthException(String message) {
        super(message);
    }
    public AuthException(String message,  Throwable cause) {
        super(message,  cause);
    }
}
