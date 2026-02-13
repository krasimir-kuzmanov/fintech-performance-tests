package com.example.fintech.perf.constants;

public final class ApiEndpoints {

  public static final String AUTH_REGISTER = "/auth/register";
  public static final String AUTH_LOGIN = "/auth/login";

  public static final String ACCOUNT_BALANCE = "/account/#{accountId}";
  public static final String ACCOUNT_FUND = "/account/#{accountId}/fund";

  public static final String TRANSACTION_PAYMENT = "/transaction/payment";
  public static final String TRANSACTION_HISTORY = "/transaction/#{accountId}";

  private ApiEndpoints() {
    // utility class
  }
}
