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
import static io.gatling.javaapi.core.CoreDsl.details;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.global;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;
import static com.example.fintech.perf.constants.HttpConstants.AUTHORIZATION_HEADER;
import static com.example.fintech.perf.constants.HttpConstants.bearerSessionToken;
import static com.example.fintech.perf.constants.RequestBodyTemplates.AUTH_BODY_USERNAME_PASSWORD;
import static com.example.fintech.perf.constants.RequestBodyTemplates.fundAmount;
import static com.example.fintech.perf.constants.TestDataConstants.DEFAULT_PASSWORD;

public class AccountFundingSimulation extends Simulation {

  private static final String DEFAULT_FUND_AMOUNT = "100.00";
  private static final String AUTH_BODY_TEMPLATE = AUTH_BODY_USERNAME_PASSWORD;
  private static final String FUND_BODY_TEMPLATE = fundAmount(DEFAULT_FUND_AMOUNT);
  private static final String REQ_AUTH_REGISTER = "funding.auth.register";
  private static final String REQ_AUTH_LOGIN = "funding.auth.login";
  private static final String REQ_ACCOUNT_FUND = "funding.account.fund";
  private static final String REQ_ACCOUNT_BALANCE = "funding.account.balance";

  private final PerfConfig config = PerfConfig.load();

  private final ChainBuilder fundingJourney = exec(session -> session
      .set("username", Users.username("perf_fund"))
      .set("password", DEFAULT_PASSWORD))
      .exec(http(REQ_AUTH_REGISTER)
          .post(ApiEndpoints.AUTH_REGISTER)
          .requestTimeout(config.requestTimeoutMs())
          .body(StringBody(AUTH_BODY_TEMPLATE))
          .check(status().in(200, 201))
          .check(jsonPath("$.id").saveAs("accountId")))
      .exec(http(REQ_AUTH_LOGIN)
          .post(ApiEndpoints.AUTH_LOGIN)
          .requestTimeout(config.requestTimeoutMs())
          .body(StringBody(AUTH_BODY_TEMPLATE))
          .check(status().is(200))
          .check(jsonPath("$.token").saveAs("token")))
      .exec(http(REQ_ACCOUNT_FUND)
          .post(ApiEndpoints.ACCOUNT_FUND)
          .requestTimeout(config.requestTimeoutMs())
          .header(AUTHORIZATION_HEADER, bearerSessionToken("token"))
          .body(StringBody(FUND_BODY_TEMPLATE))
          .check(status().is(200))
          .check(jsonPath("$.balance").exists()))
      .exec(http(REQ_ACCOUNT_BALANCE)
          .get(ApiEndpoints.ACCOUNT_BALANCE)
          .requestTimeout(config.requestTimeoutMs())
          .header(AUTHORIZATION_HEADER, bearerSessionToken("token"))
          .check(status().is(200))
          .check(jsonPath("$.balance").exists()));

  private final ScenarioBuilder fundingScenario = scenario("Account Funding Scenario")
      .exec(fundingJourney);

  private final PopulationBuilder population = fundingScenario.injectOpen(
      LoadProfile.userInjection(config.profile(), config.loadScale()));

  {
    setUp(population)
        .protocols(BaseSimulation.httpProtocol(config))
        .assertions(
            global().failedRequests().percent().lte(LoadProfile.maxErrorRatePercent(config.profile())),
            global().responseTime().percentile3().lte(LoadProfile.p95Ms(config.profile())),
            details(REQ_ACCOUNT_FUND).failedRequests().percent().is(0.0),
            details(REQ_ACCOUNT_BALANCE).failedRequests().percent().is(0.0)
        );
  }

}
