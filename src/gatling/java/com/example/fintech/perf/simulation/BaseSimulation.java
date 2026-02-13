package com.example.fintech.perf.simulation;

import com.example.fintech.perf.config.PerfConfig;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.http.HttpDsl.http;

abstract class BaseSimulation {

  protected static HttpProtocolBuilder httpProtocol(PerfConfig config) {
    return http
        .baseUrl(config.apiBaseUrl())
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")
        .userAgentHeader("fintech-gatling-tests")
        .disableCaching();
  }
}
