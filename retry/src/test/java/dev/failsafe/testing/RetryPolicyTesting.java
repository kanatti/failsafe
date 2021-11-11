package dev.failsafe.testing;

import dev.failsafe.RetryPolicy;

public interface RetryPolicyTesting {
  RetryPolicy<Boolean> retryAlways = RetryPolicy.<Boolean>builder().withMaxRetries(-1).build();
  RetryPolicy<Boolean> retryNever = RetryPolicy.<Boolean>builder().withMaxRetries(0).build();
  RetryPolicy<Boolean> retryTwice = RetryPolicy.ofDefaults();
}
