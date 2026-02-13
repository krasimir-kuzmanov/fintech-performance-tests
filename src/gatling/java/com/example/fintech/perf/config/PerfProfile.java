package com.example.fintech.perf.config;

public enum PerfProfile {
  SMOKE,
  BASELINE,
  STRESS;

  public static PerfProfile from(String value) {
    if (value == null || value.isBlank()) {
      return SMOKE;
    }

    return switch (value.trim().toLowerCase()) {
      case "smoke" -> SMOKE;
      case "baseline" -> BASELINE;
      case "stress" -> STRESS;
      default -> throw new IllegalArgumentException(
          "Unsupported perf.profile: " + value + ". Supported: smoke|baseline|stress");
    };
  }
}
