package com.example.fintech.perf.config;

import java.io.InputStream;
import java.util.Properties;

public final class PerfConfig {

  private static final String CONFIG_FILE = "application-performance.properties";

  private static final String DEFAULT_PROFILE = "smoke";

  private static final String DEFAULT_API_BASE_URL = "http://localhost:8080";
  private static final int DEFAULT_REQUEST_TIMEOUT_MS = 10_000;
  private static final int DEFAULT_LOAD_SCALE = 1;

  private static final Properties FILE_PROPERTIES = loadFileProperties();

  private final PerfProfile profile;
  private final String apiBaseUrl;
  private final RuntimeTuning runtimeTuning;

  private PerfConfig(
      PerfProfile profile,
      String apiBaseUrl,
      RuntimeTuning runtimeTuning
  ) {
    this.profile = profile;
    this.apiBaseUrl = apiBaseUrl;
    this.runtimeTuning = runtimeTuning;
  }

  public static PerfConfig load() {
    PerfProfile profile = PerfProfile.from(read(Keys.PROFILE, Envs.PROFILE, DEFAULT_PROFILE));
    String apiBaseUrl = read(Keys.API_BASE_URL, Envs.API_BASE_URL, DEFAULT_API_BASE_URL);
    int timeoutMs = readInt(Keys.HTTP_TIMEOUT_MS, Envs.HTTP_TIMEOUT_MS, DEFAULT_REQUEST_TIMEOUT_MS);
    int loadScale = readInt(Keys.LOAD_SCALE, Envs.LOAD_SCALE, DEFAULT_LOAD_SCALE);
    RuntimeTuning runtimeTuning = new RuntimeTuning(
        timeoutMs,
        sanitizePositive(loadScale, DEFAULT_LOAD_SCALE));

    return new PerfConfig(profile, apiBaseUrl, runtimeTuning);
  }

  public PerfProfile profile() {
    return profile;
  }

  public String apiBaseUrl() {
    return apiBaseUrl;
  }

  public int requestTimeoutMs() {
    return runtimeTuning.requestTimeoutMs();
  }

  public int loadScale() {
    return runtimeTuning.loadScale();
  }

  private static String read(String systemProperty, String envVar, String defaultValue) {
    String value = readOptional(systemProperty, envVar);
    return value == null ? defaultValue : value.trim();
  }

  private static int readInt(String systemProperty, String envVar, int defaultValue) {
    String defaultValueText = String.valueOf(defaultValue);
    String value = read(systemProperty, envVar, defaultValueText);
    return parseInt(value, defaultValue);
  }

  private static String readOptional(String systemProperty, String envVar) {
    String fromSystemProperty = System.getProperty(systemProperty);
    if (fromSystemProperty != null && !fromSystemProperty.isBlank()) {
      return fromSystemProperty;
    }

    String fromEnv = System.getenv(envVar);
    if (fromEnv != null && !fromEnv.isBlank()) {
      return fromEnv;
    }

    String fromFile = FILE_PROPERTIES.getProperty(systemProperty);
    if (fromFile != null && !fromFile.isBlank()) {
      return fromFile;
    }

    return null;
  }

  private static int parseInt(String value, int defaultValue) {
    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException exception) {
      return defaultValue;
    }
  }

  private static int sanitizePositive(int value, int defaultValue) {
    return value > 0 ? value : defaultValue;
  }

  private static Properties loadFileProperties() {
    Properties properties = new Properties();

    try (InputStream input = PerfConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
      if (input != null) {
        properties.load(input);
      }
    } catch (Exception ignored) {
      // Keep defaults/overrides behavior if the file is absent or unreadable.
    }

    return properties;
  }

  private static final class Keys {
    private static final String PROFILE = "perf.profile";
    private static final String API_BASE_URL = "api.baseUrl";
    private static final String HTTP_TIMEOUT_MS = "http.timeoutMs";
    private static final String LOAD_SCALE = "perf.scale";

    private Keys() {
      // constants holder
    }
  }

  private static final class Envs {
    private static final String PROFILE = "PERF_PROFILE";
    private static final String API_BASE_URL = "API_BASE_URL";
    private static final String HTTP_TIMEOUT_MS = "HTTP_TIMEOUT_MS";
    private static final String LOAD_SCALE = "PERF_SCALE";

    private Envs() {
      // constants holder
    }
  }

  private record RuntimeTuning(int requestTimeoutMs, int loadScale) {
  }
}
