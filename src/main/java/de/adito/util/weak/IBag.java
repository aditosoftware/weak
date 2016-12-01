package de.adito.util.weak;

/**
 * A Container for Objects.
 *
 * @author j.boesl, 01.12.16
 */
public interface IBag<T> extends Iterable<T>
{

  /**
   * @return wether this bag contains no elements.
   */
  boolean isEmpty();

  /**
   * Clears this bag and such removes all elements.
   */
  void clear();

}
