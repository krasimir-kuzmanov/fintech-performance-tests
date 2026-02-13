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
import static com.example.fintech.perf.constants.RequestBodyTemplates.AUTH_BODY_USERNAME_PASSWORD;
import static com.example.fintech.perf.constants.TestDataConstants.DEFAULT_PASSWORD;

public class AuthFlowSimulation extends Simulation {

  private static final String AUTH_BODY_TEMPLATE = AUTH_BODY_USERNAME_PASSWORD;

  private final PerfConfig config = PerfConfig.load();

  private final ChainBuilder registerAndLogin = exec(session -> session
      .set("username", Users.username("perf_auth"))
      .set("password", DEFAULT_PASSWORD))
      .exec(http("auth_register")
          .post(ApiEndpoints.AUTH_REGISTER)
          .requestTimeout(config.requestTimeoutMs())
          .body(StringBody(AUTH_BODY_TEMPLATE))
          .check(status().in(200, 201))
          .check(jsonPath("$.id").exists()))
      .exec(http("auth_login")
          .post(ApiEndpoints.AUTH_LOGIN)
          .requestTimeout(config.requestTimeoutMs())
          .body(StringBody(AUTH_BODY_TEMPLATE))
          .check(status().is(200))
          .check(jsonPath("$.token").exists())
          .check(jsonPath("$.userId").exists()));

  private final ScenarioBuilder authScenario = scenario("Auth Flow Scenario")
      .exec(registerAndLogin);

  private final PopulationBuilder population = authScenario.injectOpen(
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
