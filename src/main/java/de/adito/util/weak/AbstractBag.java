package de.adito.util.weak;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author j.boesl, 31.03.17
 */
public abstract class AbstractBag<T> implements IBag<T>
{

  @Nonnull
  @Override
  public Iterator<T> iterator()
  {
    return getObjects().iterator();
  }

  @Override
  public void forEach(@Nonnull Consumer<? super T> pAction)
  {
    Objects.requireNonNull(pAction);
    getObjects().forEach(pAction);
  }

  /**
   * @return a List of all Objects this bag contains.
   */
  @Nonnull
  protected abstract List<T> getObjects();

  /**
   * Called when this bag change it's state to contain elements or no longer contains elements.
   *
   * @param pAvailable whether there are now elements and prior where not or vice versa.
   */
  protected void availabilityChanged(boolean pAvailable)
  {
  }


}
