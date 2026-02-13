package com.example.fintech.perf.config;

import io.gatling.javaapi.core.OpenInjectionStep;

import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;

public final class LoadProfile {

  private static final int P95_MS_SMOKE = 150;
  private static final int P95_MS_BASELINE = 300;
  private static final int P95_MS_STRESS = 600;

  private static final double ERROR_RATE_PERCENT_SMOKE = 0.5;
  private static final double ERROR_RATE_PERCENT_BASELINE = 1.0;
  private static final double ERROR_RATE_PERCENT_STRESS = 2.0;

  private LoadProfile() {
    // utility class
  }

  public static OpenInjectionStep[] userInjection(PerfProfile profile, int scale) {
    int safeScale = Math.max(1, scale);

    return switch (profile) {
      case SMOKE -> new OpenInjectionStep[]{
          rampUsers(5 * safeScale).during(20),
          constantUsersPerSec(2.0 * safeScale).during(20)
      };
      case BASELINE -> new OpenInjectionStep[]{
          rampUsers(20 * safeScale).during(60),
          constantUsersPerSec(8.0 * safeScale).during(120)
      };
      case STRESS -> new OpenInjectionStep[]{
          rampUsers(60 * safeScale).during(120),
          constantUsersPerSec(20.0 * safeScale).during(180)
      };
    };
  }

  public static int p95Ms(PerfProfile profile) {
    return switch (profile) {
      case SMOKE -> P95_MS_SMOKE;
      case BASELINE -> P95_MS_BASELINE;
      case STRESS -> P95_MS_STRESS;
    };
  }

  public static double maxErrorRatePercent(PerfProfile profile) {
    return switch (profile) {
      case SMOKE -> ERROR_RATE_PERCENT_SMOKE;
      case BASELINE -> ERROR_RATE_PERCENT_BASELINE;
      case STRESS -> ERROR_RATE_PERCENT_STRESS;
    };
  }
}
