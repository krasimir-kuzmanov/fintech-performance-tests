package com.example.fintech.perf.constants;

public final class RequestNames {

  private RequestNames() {
    // utility class
  }

  public static final class Auth {
    public static final String REGISTER = "auth.register";
    public static final String LOGIN = "auth.login";

    private Auth() {
      // constants holder
    }
  }

  public static final class Funding {
    public static final String REGISTER = "funding.auth.register";
    public static final String LOGIN = "funding.auth.login";
    public static final String FUND = "funding.account.fund";
    public static final String BALANCE = "funding.account.balance";

    private Funding() {
      // constants holder
    }
  }

  public static final class Payment {
    public static final String PAYER_REGISTER = "payment.payer.register";
    public static final String PAYEE_REGISTER = "payment.payee.register";
    public static final String PAYER_LOGIN = "payment.payer.login";
    public static final String PAYER_FUND = "payment.payer.fund";
    public static final String TRANSFER = "payment.transfer";
    public static final String PAYER_BALANCE = "payment.payer.balance";
    public static final String PAYER_TRANSACTIONS = "payment.payer.transactions";

    private Payment() {
      // constants holder
    }
  }
}
