package com.example.fintech.perf.constants;

public final class HttpConstants {

  public static final String AUTHORIZATION_HEADER = "Authorization";
  public static final String BEARER_PREFIX = "Bearer ";

  private HttpConstants() {
    // utility class
  }

  public static String bearerSessionToken(String sessionTokenExpression) {
    return BEARER_PREFIX + "#{" + sessionTokenExpression + "}";
  }
}
