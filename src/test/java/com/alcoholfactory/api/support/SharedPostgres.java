package com.alcoholfactory.api.support;

import org.testcontainers.containers.PostgreSQLContainer;

/** Jedna instancja PostgreSQL na JVM testów (bez zatrzymywania między klasami testowymi). */
final class SharedPostgres {

  private static final Object LOCK = new Object();
  private static PostgreSQLContainer<?> instance;

  private SharedPostgres() {}

  static PostgreSQLContainer<?> get() {
    synchronized (LOCK) {
      if (instance == null) {
        instance =
            new PostgreSQLContainer<>("postgres:15-alpine")
                .withDatabaseName("alcohol_db")
                .withUsername("app")
                .withPassword("secret");
        instance.start();
      }
      return instance;
    }
  }
}
