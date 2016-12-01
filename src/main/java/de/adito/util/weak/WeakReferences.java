package de.adito.util.weak;

import javax.annotation.*;
import java.lang.ref.*;
import java.util.*;
import java.util.function.Consumer;

/**
 * A bag that holds WeakReferences and notifies when there are elements available or when there are no longer elements
 * available.
 *
 * @author j.boesl, 03.11.2016
 */
public class WeakReferences<T> implements IBag<T>
{

  private final Set<WeakReference<T>> set;

  public WeakReferences()
  {
    set = new LinkedHashSet<>();
  }

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

  public boolean isEmpty()
  {
    boolean empty;
    synchronized (set) {
      empty = set.isEmpty();
    }
    return empty;
  }

  /**
   * Adds a Object to this Bag. The Object is stored weakly.
   *
   * @param pObject the Object to be added to this bag.
   */
  public void add(@Nonnull T pObject)
  {
    Objects.requireNonNull(pObject);
    boolean wasEmpty;
    synchronized (set) {
      wasEmpty = set.isEmpty();
      set.add(WeakReferenceFactory.get().create(pObject, this::remove));
    }
    if (wasEmpty)
      availabilityChanged(true);
  }

  /**
   * @param pObject the Object to be removed. This Object has to be the value of a WeakReference that is stored in this
   *                bag.
   */
  public void remove(@Nonnull T pObject)
  {
    Reference<T> reference = findReference(pObject);
    if (reference != null)
      remove(reference);
  }

  public void clear()
  {
    boolean wasEmpty;
    synchronized (set) {
      wasEmpty = set.isEmpty();
      set.clear();
    }
    if (!wasEmpty)
      availabilityChanged(false);
  }

  /**
   * @param pReference the Reference to be removed.
   */
  protected void remove(@Nonnull Reference<T> pReference)
  {
    boolean wasEmpty;
    boolean isEmpty;
    synchronized (set) {
      wasEmpty = set.isEmpty();
      set.remove(pReference);
      isEmpty = set.isEmpty();
    }
    if (!wasEmpty && isEmpty)
      availabilityChanged(false);
  }

  /**
   * Returns the reference that holds the given value.
   *
   * @param pObject the value.
   * @return the reference or null in case it was not found.
   */
  @Nullable
  protected Reference<T> findReference(@Nonnull T pObject)
  {
    Objects.requireNonNull(pObject);
    synchronized (set) {
      for (WeakReference<T> reference : set) {
        if (pObject.equals(reference.get()))
          return reference;
      }
    }
    return null;
  }

  /**
   * @return a List of all Objects this bag contains.
   */
  @Nonnull
  protected List<T> getObjects()
  {
    List<T> objects;
    synchronized (set) {
      objects = new ArrayList<>(set.size());
      for (WeakReference<T> entry : set) {
        T nextObject = entry.get();
        if (nextObject != null)
          objects.add(nextObject);
      }
    }
    return objects;
  }

  /**
   * Called when this bag change it's state to contain elements or no longer contains elements.
   *
   * @param pAvailable whether there are now elements and prior where not or vice versa.
   */
  protected void availabilityChanged(boolean pAvailable)
  {
  }

}
