# fintech-performance-tests

Performance test suite for the Fintech ecosystem using Java + Gradle + Gatling.

## Stack
- Java 21 (Gradle toolchain)
- Gradle Wrapper
- Gatling Java DSL

## Prerequisites
- JDK 21
- Running fintech backend

Backend default URL:
- `http://localhost:8080`

Health check:
- `http://localhost:8080/actuator/health`

## Configuration
Configuration precedence:
1. JVM system properties (`-D...`)
2. Environment variables
3. `src/gatling/resources/application-performance.properties`
4. Hardcoded defaults in `PerfConfig`

- `perf.profile` (`smoke|baseline|stress`) -> env: `PERF_PROFILE`
- `api.baseUrl` -> env: `API_BASE_URL` (default `http://localhost:8080`)
- `http.timeoutMs` -> env: `HTTP_TIMEOUT_MS` (default `10000`)

## Simulations
- `com.example.fintech.perf.simulation.AuthFlowSimulation`
- `com.example.fintech.perf.simulation.AccountFundingSimulation`
- `com.example.fintech.perf.simulation.PaymentFlowSimulation`

## Threshold Profiles
- `smoke`: p95 <= `150ms`, failed requests <= `0.5%`
- `baseline`: p95 <= `300ms`, failed requests <= `1.0%`
- `stress`: p95 <= `600ms`, failed requests <= `2.0%`

## Run Locally
Run one simulation:

```bash
sdk env
./gradlew gatlingRun \
  --non-interactive \
  --simulation com.example.fintech.perf.simulation.AuthFlowSimulation \
  -Dperf.profile=smoke \
  -Dapi.baseUrl=http://localhost:8080
```

Run with convenience tasks:

```bash
./gradlew perfSmokeAuth
./gradlew perfSmokeAccount
./gradlew perfSmokePayment
```

Run all three smoke simulations:

```bash
./gradlew perfSmoke
```

Run all baseline simulations:

```bash
./gradlew perfBaseline
```

Run all stress simulations:

```bash
./gradlew perfStress
```

## Reports
Gatling reports are generated under:
- `build/reports/gatling`

## CI
- Pull requests run `smoke` profile simulations.
- Manual GitHub Actions runs always run `smoke`, and can enable `baseline`/`stress` via workflow inputs:
  - `run_baseline=true`
  - `run_stress=true`
- Scheduled nightly runs execute `baseline` and `stress` profiles.

## Notes
- The suite follows the same separation style as existing fintech test projects:
  - config (`config`)
  - endpoint contracts (`constants`)
  - reusable utilities (`util`)
  - behavior scenarios (`simulation`)
- Scenarios use unique user identities to reduce cross-user state collisions.
