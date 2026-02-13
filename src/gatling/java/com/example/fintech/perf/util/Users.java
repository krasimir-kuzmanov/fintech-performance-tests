package com.example.fintech.perf.util;

import java.util.UUID;

public final class Users {

  private Users() {
    // utility class
  }

  public static String username(String prefix) {
    return prefix + "_" + UUID.randomUUID();
  }
}
