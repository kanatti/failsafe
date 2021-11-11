package dev.failsafe.testing;

import dev.failsafe.CircuitBreaker;
import dev.failsafe.internal.CircuitBreakerImpl;
import dev.failsafe.internal.CircuitState;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

public interface CircuitBreakerTesting {
  @SuppressWarnings("unchecked")
  default <T extends CircuitState<?>> T stateFor(CircuitBreaker<?> breaker) {
    Field stateField;
    try {
      stateField = CircuitBreakerImpl.class.getDeclaredField("state");
      stateField.setAccessible(true);
      return ((AtomicReference<T>) stateField.get(breaker)).get();
    } catch (Exception e) {
      throw new IllegalStateException("Could not get circuit breaker state");
    }
  }

  default void resetBreaker(CircuitBreaker<?> breaker) {
    breaker.close();
    CircuitState<?> state = stateFor(breaker);
    state.getStats().reset();
  }
}
