package com.example.fintech.perf.constants;

public final class RequestBodyTemplates {

  public static final String AUTH_BODY_USERNAME_PASSWORD = auth("username", "password");

  private RequestBodyTemplates() {
    // utility class
  }

  public static String auth(String usernameExpression, String passwordExpression) {
    return "{\"username\":\"#{" + usernameExpression + "}\",\"password\":\"#{" + passwordExpression + "}\"}";
  }

  public static String fundAmount(String amountLiteral) {
    return "{\"amount\":" + amountLiteral + "}";
  }

  public static String payment(String fromAccountExpression, String toAccountExpression, String amountLiteral) {
    return "{\"fromAccountId\":\"#{" + fromAccountExpression + "}\",\"toAccountId\":\"#{" + toAccountExpression + "}\",\"amount\":\"" + amountLiteral + "\"}";
  }
}
