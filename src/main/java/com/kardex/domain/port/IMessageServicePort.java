package com.kardex.domain.port;

public interface IMessageServicePort {
    public String getMessage(String key, Object... args);
    public String getMessage(String key, String defaultMessage, Object... args);
}
