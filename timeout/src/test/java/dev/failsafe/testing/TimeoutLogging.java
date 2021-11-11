package dev.failsafe.testing;

import dev.failsafe.TimeoutBuilder;
import dev.failsafe.testing.Logging.Stats;

public interface TimeoutLogging {
  /**
   * Note: The internal stats that are logged are not reset, even across multiple executions.
   */
  static <T> TimeoutBuilder<T> withLogs(TimeoutBuilder<T> builder) {
    return withStatsAndLogs(builder, new Stats(), true);
  }

  static <T> TimeoutBuilder<T> withStats(TimeoutBuilder<T> builder, Stats stats) {
    return withStatsAndLogs(builder, stats, false);
  }

  static <T> TimeoutBuilder<T> withStatsAndLogs(TimeoutBuilder<T> builder, Stats stats) {
    return withStatsAndLogs(builder, stats, true);
  }

  static <T> TimeoutBuilder<T> withStatsAndLogs(TimeoutBuilder<T> builder, Stats stats, boolean withLogging) {
    return builder.onSuccess(e -> {
      stats.executionCount++;
      stats.successCount++;
      if (withLogging)
        System.out.printf("Timeout %s success policy executions=%s, successes=%s%n", builder.hashCode(),
          stats.executionCount, stats.successCount);
    }).onFailure(e -> {
      stats.executionCount++;
      stats.failureCount++;
      if (withLogging)
        System.out.printf("Timeout %s exceeded policy executions=%s, failure=%s%n", builder.hashCode(),
          stats.executionCount, stats.failureCount);
    });
  }
}
