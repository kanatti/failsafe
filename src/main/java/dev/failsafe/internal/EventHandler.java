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
package dev.failsafe.internal;

import dev.failsafe.event.EventListener;
import dev.failsafe.ExecutionContext;
import dev.failsafe.event.ExecutionAttemptedEvent;
import dev.failsafe.event.ExecutionCompletedEvent;
import dev.failsafe.event.ExecutionScheduledEvent;
import dev.failsafe.spi.ExecutionResult;

import java.time.Duration;

/**
 * Internal handling of events.
 *
 * @param <R> result type
 */
public interface EventHandler<R> {
  void handle(ExecutionResult<R> result, ExecutionContext<R> context);

  static <R> EventHandler<R> ofExecutionCompleted(EventListener<ExecutionCompletedEvent<R>> handler) {
    return handler == null ?
      null :
      (result, context) -> handler.acceptUnchecked(
        new ExecutionCompletedEvent<>(result.getResult(), result.getFailure(), context));
  }

  static <R> EventHandler<R> ofExecutionAttempted(EventListener<ExecutionAttemptedEvent<R>> handler) {
    return handler == null ?
      null :
      (result, context) -> handler.acceptUnchecked(
        new ExecutionAttemptedEvent<>(result.getResult(), result.getFailure(), context));
  }

  static <R> EventHandler<R> ofExecutionScheduled(EventListener<ExecutionScheduledEvent<R>> handler) {
    return handler == null ?
      null :
      (result, context) -> handler.acceptUnchecked(
        new ExecutionScheduledEvent<>(result.getResult(), result.getFailure(), Duration.ofNanos(result.getDelay()),
          context));
  }
}