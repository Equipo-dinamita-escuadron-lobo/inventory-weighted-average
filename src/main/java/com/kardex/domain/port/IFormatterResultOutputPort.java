package com.kardex.domain.port;

public interface IFormatterResultOutputPort {
    public void returnResponseError(int status, String message);
}
