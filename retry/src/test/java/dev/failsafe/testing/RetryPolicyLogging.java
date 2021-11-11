package dev.failsafe.testing;

import dev.failsafe.RetryPolicyBuilder;
import dev.failsafe.testing.Logging.Stats;

public interface RetryPolicyLogging {
  /**
   * Note: The internal stats that are logged are not reset, even across multiple executions.
   */
  static <T> RetryPolicyBuilder<T> withLogs(RetryPolicyBuilder<T> retryPolicy) {
    return withStatsAndLogs(retryPolicy, new Stats(), true);
  }

  static <T> RetryPolicyBuilder<T> withStats(RetryPolicyBuilder<T> builder, Stats stats) {
    return withStatsAndLogs(builder, stats, false);
  }

  static <T> RetryPolicyBuilder<T> withStatsAndLogs(RetryPolicyBuilder<T> builder, Stats stats) {
    return withStatsAndLogs(builder, stats, true);
  }

  static <T> RetryPolicyBuilder<T> withStatsAndLogs(RetryPolicyBuilder<T> builder, Stats stats, boolean withLogging) {
    builder.onFailedAttempt(e -> {
      stats.executionCount++;
      stats.failedAttemptCount++;
      if (withLogging)
        System.out.printf("RetryPolicy %s failed attempt [result: %s, failure: %s, attempts: %s, executions: %s]%n",
          builder.hashCode(), e.getLastResult(), e.getLastFailure(), e.getAttemptCount(), e.getExecutionCount());
    }).onRetry(e -> {
      stats.retryCount++;
      if (withLogging)
        System.out.printf("RetryPolicy %s retrying [result: %s, failure: %s]%n", builder.hashCode(), e.getLastResult(),
          e.getLastFailure());
    }).onRetryScheduled(e -> {
      stats.retryScheduledCount++;
      if (withLogging)
        System.out.printf("RetryPolicy %s scheduled [delay: %s ms]%n", builder.hashCode(), e.getDelay().toMillis());
    }).onRetriesExceeded(e -> {
      stats.retriesExceededCount++;
      if (withLogging)
        System.out.printf("RetryPolicy %s retries exceeded%n", builder.hashCode());
    }).onAbort(e -> {
      stats.abortCount++;
      if (withLogging)
        System.out.printf("RetryPolicy %s abort%n", builder.hashCode());
    });
    Logging.withStatsAndLogs(builder, stats, withLogging);
    return builder;
  }
}
