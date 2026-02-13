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
import static com.example.fintech.perf.constants.RequestBodyTemplates.AUTH_BODY_USERNAME_PASSWORD;
import static com.example.fintech.perf.constants.RequestBodyTemplates.fundAmount;
import static com.example.fintech.perf.constants.TestDataConstants.DEFAULT_PASSWORD;

public class AccountFundingSimulation extends Simulation {

  private static final String DEFAULT_FUND_AMOUNT = "100.00";
  private static final String AUTH_BODY_TEMPLATE = AUTH_BODY_USERNAME_PASSWORD;
  private static final String FUND_BODY_TEMPLATE = fundAmount(DEFAULT_FUND_AMOUNT);
  private static final double AMOUNT_TOLERANCE = 0.0001;

  private final PerfConfig config = PerfConfig.load();

  private final ChainBuilder fundingJourney = exec(session -> session
      .set("username", Users.username("perf_fund"))
      .set("password", DEFAULT_PASSWORD))
      .exec(http("fund_register")
          .post(ApiEndpoints.AUTH_REGISTER)
          .requestTimeout(config.requestTimeoutMs())
          .body(StringBody(AUTH_BODY_TEMPLATE))
          .check(status().in(200, 201))
          .check(jsonPath("$.id").saveAs("accountId")))
      .exec(http("fund_login")
          .post(ApiEndpoints.AUTH_LOGIN)
          .requestTimeout(config.requestTimeoutMs())
          .body(StringBody(AUTH_BODY_TEMPLATE))
          .check(status().is(200))
          .check(jsonPath("$.token").saveAs("token")))
      .exec(http("account_fund")
          .post(ApiEndpoints.ACCOUNT_FUND)
          .requestTimeout(config.requestTimeoutMs())
          .header(AUTHORIZATION_HEADER, bearerSessionToken("token"))
          .body(StringBody(FUND_BODY_TEMPLATE))
          .check(status().is(200))
          .check(jsonPath("$.balance").exists())
          .check(jsonPath("$.balance").saveAs("fundedBalance")))
      .exec(http("account_get_balance")
          .get(ApiEndpoints.ACCOUNT_BALANCE)
          .requestTimeout(config.requestTimeoutMs())
          .header(AUTHORIZATION_HEADER, bearerSessionToken("token"))
          .check(status().is(200))
          .check(jsonPath("$.balance").exists())
          .check(jsonPath("$.balance").saveAs("balanceAfterFund")))
      .exec(session -> {
        double fundedBalance = parseAmount(session.getString("fundedBalance"));
        double balanceAfterFund = parseAmount(session.getString("balanceAfterFund"));
        boolean invalidAmounts = !Double.isFinite(fundedBalance) || !Double.isFinite(balanceAfterFund);
        if (invalidAmounts || fundedBalance <= 0.0 || Math.abs(fundedBalance - balanceAfterFund) > AMOUNT_TOLERANCE) {
          return session.markAsFailed();
        }
        return session;
      });

  private final ScenarioBuilder fundingScenario = scenario("Account Funding Scenario")
      .exec(fundingJourney);

  private final PopulationBuilder population = fundingScenario.injectOpen(
      LoadProfile.userInjection(config.profile(), config.loadScale(), config.durationMultiplier()));

  {
    setUp(population)
        .protocols(BaseSimulation.httpProtocol(config))
        .assertions(
            global().failedRequests().percent().lte(LoadProfile.maxErrorRatePercent(config.profile())),
            global().responseTime().percentile3().lte(LoadProfile.p95Ms(config.profile()))
        );
  }

  private static double parseAmount(String value) {
    try {
      return Double.parseDouble(value);
    } catch (Exception exception) {
      return Double.NaN;
    }
  }
}
