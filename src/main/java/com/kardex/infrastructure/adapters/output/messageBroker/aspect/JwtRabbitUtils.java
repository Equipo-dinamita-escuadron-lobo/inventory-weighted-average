package com.kardex.infrastructure.adapters.output.messageBroker.aspect;

public class JwtRabbitUtils {

    private static String jwtToken;

    public static String getJwtToken() {
        return jwtToken;
    }

    public static void setJwtToken(String jwtToken) {
        JwtRabbitUtils.jwtToken = jwtToken;
    }
}
