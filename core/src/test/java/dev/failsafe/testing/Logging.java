/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
package dev.failsafe.testing;

import dev.failsafe.PolicyBuilder;
import dev.failsafe.PolicyConfig;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Logging and stats for tests.
 */
public class Logging extends Mocking {
  public static class Stats {
    // Common
    public volatile int executionCount;
    public volatile int failureCount;
    public volatile int successCount;

    // RetryPolicy
    public volatile int failedAttemptCount;
    public volatile int retryCount;
    public volatile int retryScheduledCount;
    public volatile int retriesExceededCount;
    public volatile int abortCount;

    // CircuitBreaker
    public volatile int openCount;
    public volatile int halfOpenCount;
    public volatile int closedCount;

    public void reset() {
      executionCount = 0;
      failureCount = 0;
      successCount = 0;
      failedAttemptCount = 0;
      retryCount = 0;
      retryScheduledCount = 0;
      retriesExceededCount = 0;
      abortCount = 0;
      openCount = 0;
      halfOpenCount = 0;
      closedCount = 0;
    }
  }

  static volatile long lastTimestamp;

  public static void log(Object object, String msg, Object... args) {
    Class<?> clazz = object instanceof Class ? (Class<?>) object : object.getClass();
    log(clazz.getSimpleName() + " " + String.format(msg, args));
  }

  public static void log(Class<?> clazz, String msg) {
    log(clazz.getSimpleName() + " " + msg);
  }

  public static void log(String msg) {
    long currentTimestamp = System.currentTimeMillis();
    if (lastTimestamp + 80 < currentTimestamp)
      System.out.printf("%n%n");
    lastTimestamp = currentTimestamp;
    String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("H:mm:ss.SSS"));
    StringBuilder threadName = new StringBuilder(Thread.currentThread().getName());
    for (int i = threadName.length(); i < 35; i++)
      threadName.append(" ");
    System.out.println("[" + time + "] " + "[" + threadName + "] " + msg);
  }

  /**
   * Note: The internal stats that are logged are not reset, even across multiple executions.
   */
  public static <T extends PolicyBuilder<?, ? extends PolicyConfig<R>, R>, R> T withLogs(T policy) {
    return withStatsAndLogs(policy, new Stats(), true);
  }

  public static <T extends PolicyBuilder<?, ? extends PolicyConfig<R>, R>, R> T withStats(T builder, Stats stats) {
    return withStatsAndLogs(builder, stats, false);
  }

  public static <T extends PolicyBuilder<?, ? extends PolicyConfig<R>, R>, R> T withStatsAndLogs(T builder,
    Stats stats) {
    return withStatsAndLogs(builder, stats, true);
  }

  public static <T extends PolicyBuilder<?, ? extends PolicyConfig<R>, R>, R> T withStatsAndLogs(T builder, Stats stats,
    boolean withLogging) {
    builder.onSuccess(e -> {
      stats.executionCount++;
      stats.successCount++;
      if (withLogging)
        System.out.printf("%s success [result: %s, attempts: %s, executions: %s]%n", builder.getClass().getSimpleName(),
          e.getResult(), e.getAttemptCount(), e.getExecutionCount());
    });
    builder.onFailure(e -> {
      stats.executionCount++;
      stats.failureCount++;
      if (withLogging)
        System.out.printf("%s failure [result: %s, failure: %s, attempts: %s, executions: %s]%n",
          builder.getClass().getSimpleName(), e.getResult(), e.getFailure(), e.getAttemptCount(),
          e.getExecutionCount());
    });
    return builder;
  }
}
