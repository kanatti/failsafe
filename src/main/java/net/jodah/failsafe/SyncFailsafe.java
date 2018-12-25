/*
 * Copyright 2016 the original author or authors.
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
package net.jodah.failsafe;

import net.jodah.failsafe.function.CheckedRunnable;
import net.jodah.failsafe.function.ContextualCallable;
import net.jodah.failsafe.function.ContextualRunnable;
import net.jodah.failsafe.internal.util.Assert;
import net.jodah.failsafe.util.concurrent.Scheduler;
import net.jodah.failsafe.util.concurrent.Schedulers;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;

/**
 * Performs synchronous executions with failures handled according to a configured {@link FailsafePolicy).
 *
 * @author Jonathan Halterman
 * @param <R> listener result type
 */
public class SyncFailsafe<R> extends FailsafeConfig<R, SyncFailsafe<R>> {
  SyncFailsafe(CircuitBreaker circuitBreaker) {
    this.circuitBreaker = circuitBreaker;
  }

  SyncFailsafe(RetryPolicy retryPolicy) {
    this.retryPolicy = retryPolicy;
  }

  SyncFailsafe(List<FailsafePolicy> policies) {
    super(policies);
  }

  /**
   * Executes the {@code callable} until a successful result is returned or the configured {@link RetryPolicy} is
   * exceeded.
   *
   * @throws NullPointerException if the {@code callable} is null
   * @throws FailsafeException if the {@code callable} fails with a checked Exception or if interrupted while waiting to
   *           perform a retry.
   * @throws CircuitBreakerOpenException if a configured circuit is open.
   */
  public <T> T get(Callable<T> callable) {
    return call(execution -> Assert.notNull(callable, "callable"));
  }

  /**
   * Executes the {@code callable} until a successful result is returned or the configured {@link RetryPolicy} is
   * exceeded.
   *
   * @throws NullPointerException if the {@code callable} is null
   * @throws FailsafeException if the {@code callable} fails with a checked Exception or if interrupted while waiting to
   *           perform a retry.
   * @throws CircuitBreakerOpenException if a configured circuit is open.
   */
  public <T> T get(ContextualCallable<T> callable) {
    return call(execution -> Functions.callableOf(callable, execution));
  }

  /**
   * Executes the {@code runnable} until successful or until the configured {@link RetryPolicy} is exceeded.
   *
   * @throws NullPointerException if the {@code runnable} is null
   * @throws FailsafeException if the {@code callable} fails with a checked Exception or if interrupted while waiting to
   *           perform a retry.
   * @throws CircuitBreakerOpenException if a configured circuit is open.
   */
  public void run(CheckedRunnable runnable) {
    call(execution -> Functions.callableOf(runnable));
  }

  /**
   * Executes the {@code runnable} until successful or until the configured {@link RetryPolicy} is exceeded.
   *
   * @throws NullPointerException if the {@code runnable} is null
   * @throws FailsafeException if the {@code runnable} fails with a checked Exception or if interrupted while waiting to
   *           perform a retry.
   * @throws CircuitBreakerOpenException if a configured circuit is open.
   */
  public void run(ContextualRunnable runnable) {
    call(execution -> Functions.callableOf(runnable, execution));
  }

  /**
   * Creates and returns a new AsyncFailsafe instance that will perform executions and retries asynchronously via the
   * {@code executor}.
   *
   * @throws NullPointerException if {@code executor} is null
   */
  public AsyncFailsafe<R> with(ScheduledExecutorService executor) {
    return new AsyncFailsafe<>(this, Schedulers.of(executor));
  }

  /**
   * Creates and returns a new AsyncFailsafe instance that will perform executions and retries asynchronously via the
   * {@code scheduler}.
   *
   * @throws NullPointerException if {@code scheduler} is null
   */
  public AsyncFailsafe<R> with(Scheduler scheduler) {
    return new AsyncFailsafe<>(this, Assert.notNull(scheduler, "scheduler"));
  }

  /**
   * Calls the {@code callable} synchronously, performing retries according to the {@code retryPolicy}.
   *
   * @throws FailsafeException if the {@code callable} fails with a checked Exception or if interrupted while waiting to
   *           perform a retry.
   * @throws CircuitBreakerOpenException if a configured circuit breaker is open
   */
  @SuppressWarnings("unchecked")
  private <T> T call(Function<Execution, Callable<T>> callableFn) {
    Execution execution = new Execution(this);
    Callable<T> callable = callableFn.apply(execution);
    execution.inject(callable);

    ExecutionResult result = execution.executeSync();
    if (result.failure != null)
      throw result.failure instanceof RuntimeException ? (RuntimeException) result.failure
          : new FailsafeException(result.failure);
    return (T) result.result;
  }
}