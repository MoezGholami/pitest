package org.pitest.mutationtest.build;

import org.pitest.mutationtest.TimeoutLengthStrategy;

public class ConstantTimeoutStrategy implements TimeoutLengthStrategy {

  public static final long  DEFAULT_CONSTANT = PercentAndConstantTimeoutStrategy.DEFAULT_CONSTANT;

  private final long        constant;

  public ConstantTimeoutStrategy(final long constant) {
    this.constant = constant;
  }

  public ConstantTimeoutStrategy() {
    this.constant = DEFAULT_CONSTANT;
  }

  @Override
  public long getAllowedTime(final long normalDuration) {
    return this.constant;
  }

}
