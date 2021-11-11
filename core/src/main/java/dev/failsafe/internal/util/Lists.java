package dev.failsafe.internal.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * List utilities.
 *
 * @author Jonathan Halterman
 */
public final class Lists {
  private Lists() {
  }

  public static <T> List<T> of(T first, T[] rest) {
    List<T> result = new ArrayList<>(rest.length + 1);
    result.add(first);
    Collections.addAll(result, rest);
    return result;
  }
}
