package dev.failsafe.testing;

import dev.failsafe.CircuitBreakerBuilder;
import dev.failsafe.testing.Logging.Stats;

public interface CircuitBreakerLogging {
  /**
   * Note: The internal stats that are logged are not reset, even across multiple executions.
   */
  static <T> CircuitBreakerBuilder<T> withLogs(CircuitBreakerBuilder<T> builder) {
    return withStatsAndLogs(builder, new Stats(), true);
  }

  static <T> CircuitBreakerBuilder<T> withStats(CircuitBreakerBuilder<T> builder, Stats stats) {
    return withStatsAndLogs(builder, stats, false);
  }

  static <T> CircuitBreakerBuilder<T> withStatsAndLogs(CircuitBreakerBuilder<T> builder, Stats stats) {
    return withStatsAndLogs(builder, stats, true);
  }

  static <T> CircuitBreakerBuilder<T> withStatsAndLogs(CircuitBreakerBuilder<T> builder, Stats stats,
    boolean withLogging) {
    builder.onOpen(e -> {
      stats.openCount++;
      if (withLogging)
        System.out.println("CircuitBreaker opening");
    }).onHalfOpen(e -> {
      stats.halfOpenCount++;
      if (withLogging)
        System.out.println("CircuitBreaker half-opening");
    }).onClose(e -> {
      stats.closedCount++;
      if (withLogging)
        System.out.println("CircuitBreaker closing");
    });
    Logging.withStatsAndLogs(builder, stats, withLogging);
    return builder;
  }
}
