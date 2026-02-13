package com.example.fintech.perf.simulation;

import com.example.fintech.perf.config.LoadProfile;
import com.example.fintech.perf.config.PerfConfig;
import com.example.fintech.perf.constants.ApiEndpoints;
import com.example.fintech.perf.util.Users;
import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.PopulationBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.global;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;
import static com.example.fintech.perf.constants.HttpConstants.AUTHORIZATION_HEADER;
import static com.example.fintech.perf.constants.HttpConstants.bearerSessionToken;
import static com.example.fintech.perf.constants.RequestBodyTemplates.auth;
import static com.example.fintech.perf.constants.RequestBodyTemplates.fundAmount;
import static com.example.fintech.perf.constants.RequestBodyTemplates.payment;
import static com.example.fintech.perf.constants.TestDataConstants.DEFAULT_PASSWORD;

public class PaymentFlowSimulation extends Simulation {

  private static final String DEFAULT_FUND_AMOUNT = "100.00";
  private static final String DEFAULT_PAYMENT_AMOUNT = "40.00";
  private static final String PAYER_AUTH_BODY_TEMPLATE = auth("payerUsername", "password");
  private static final String PAYEE_AUTH_BODY_TEMPLATE = auth("payeeUsername", "password");
  private static final String FUND_BODY_TEMPLATE = fundAmount(DEFAULT_FUND_AMOUNT);
  private static final String PAYMENT_BODY_TEMPLATE = payment("payerAccountId", "payeeAccountId", DEFAULT_PAYMENT_AMOUNT);

  private final PerfConfig config = PerfConfig.load();

  private final ChainBuilder paymentJourney = exec(session -> session
      .set("payerUsername", Users.username("perf_payer"))
      .set("payeeUsername", Users.username("perf_payee"))
      .set("password", DEFAULT_PASSWORD))
      .exec(http("payment_register_payer")
          .post(ApiEndpoints.AUTH_REGISTER)
          .requestTimeout(config.requestTimeoutMs())
          .body(StringBody(PAYER_AUTH_BODY_TEMPLATE))
          .check(status().in(200, 201))
          .check(jsonPath("$.id").saveAs("payerAccountId")))
      .exec(http("payment_register_payee")
          .post(ApiEndpoints.AUTH_REGISTER)
          .requestTimeout(config.requestTimeoutMs())
          .body(StringBody(PAYEE_AUTH_BODY_TEMPLATE))
          .check(status().in(200, 201))
          .check(jsonPath("$.id").saveAs("payeeAccountId")))
      .exec(http("payment_login_payer")
          .post(ApiEndpoints.AUTH_LOGIN)
          .requestTimeout(config.requestTimeoutMs())
          .body(StringBody(PAYER_AUTH_BODY_TEMPLATE))
          .check(status().is(200))
          .check(jsonPath("$.token").saveAs("payerToken")))
      .exec(session -> session.set("accountId", session.getString("payerAccountId")))
      .exec(http("payment_fund_payer")
          .post(ApiEndpoints.ACCOUNT_FUND)
          .requestTimeout(config.requestTimeoutMs())
          .header(AUTHORIZATION_HEADER, bearerSessionToken("payerToken"))
          .body(StringBody(FUND_BODY_TEMPLATE))
          .check(status().is(200)))
      .exec(http("payment_transfer")
          .post(ApiEndpoints.TRANSACTION_PAYMENT)
          .requestTimeout(config.requestTimeoutMs())
          .header(AUTHORIZATION_HEADER, bearerSessionToken("payerToken"))
          .body(StringBody(PAYMENT_BODY_TEMPLATE))
          .check(status().is(200))
          .check(jsonPath("$.transactionId").exists())
          .check(jsonPath("$.status").is("SUCCESS")))
      .exec(http("payment_get_payer_balance")
          .get(ApiEndpoints.ACCOUNT_BALANCE)
          .requestTimeout(config.requestTimeoutMs())
          .header(AUTHORIZATION_HEADER, bearerSessionToken("payerToken"))
          .check(status().is(200))
          .check(jsonPath("$.balance").exists()))
      .exec(http("payment_get_payer_transactions")
          .get(ApiEndpoints.TRANSACTION_HISTORY)
          .requestTimeout(config.requestTimeoutMs())
          .header(AUTHORIZATION_HEADER, bearerSessionToken("payerToken"))
          .check(status().is(200)));

  private final ScenarioBuilder paymentScenario = scenario("Payment Flow Scenario")
      .exec(paymentJourney);

  private final PopulationBuilder population = paymentScenario.injectOpen(
      LoadProfile.userInjection(config.profile(), 1));

  {
    setUp(population)
        .protocols(BaseSimulation.httpProtocol(config))
        .assertions(
            global().failedRequests().percent().lte(LoadProfile.maxErrorRatePercent(config.profile())),
            global().responseTime().percentile3().lte(LoadProfile.p95Ms(config.profile()))
        );
  }
}
