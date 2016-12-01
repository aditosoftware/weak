package de.adito.util.weak;

import javax.annotation.*;
import java.lang.ref.*;
import java.util.*;
import java.util.function.Consumer;

/**
 * A bag that holds WeakReferences as well as plain Objects and notifies when there are elements available or when there
 * are no longer elements available.
 *
 * @author j.boesl, 08.11.16
 */
public class MixedReferences<T> implements IBag<T>
{

  private final Set set;

  public MixedReferences()
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
  public void addWeak(@Nonnull T pObject)
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
   * Adds a Object to this Bag. The Object is stored strongly.
   *
   * @param pObject the Object to be added to this bag.
   */
  public void addStrong(@Nonnull T pObject)
  {
    Objects.requireNonNull(pObject);
    if (pObject instanceof WeakReference)
      throw new IllegalArgumentException("Weak references can't be added as strong ones.");

    boolean wasEmpty;
    synchronized (set) {
      wasEmpty = set.isEmpty();
      set.add(pObject);
    }
    if (wasEmpty)
      availabilityChanged(true);
  }

  /**
   * @param pObject the Object to be removed. This Object can be the value of a WeakReference that is stored in this
   *                bag or the WeakReference itself.
   */
  public boolean remove(@Nonnull Object pObject)
  {
    boolean wasEmpty;
    boolean wasRemoved;
    boolean isEmpty;
    synchronized (set) {
      wasEmpty = set.isEmpty();
      wasRemoved = set.remove(pObject);
      if (!wasRemoved) {
        Reference<T> reference = findReference(pObject);
        if (reference != null)
          wasRemoved = set.remove(reference);
      }
      isEmpty = set.isEmpty();
    }
    if (!wasEmpty && isEmpty)
      availabilityChanged(false);

    return wasRemoved;
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
   * Returns the reference that holds the given value.
   *
   * @param pObject the value.
   * @return the reference or null in case it was not found.
   */
  @Nullable
  protected Reference<T> findReference(@Nonnull Object pObject)
  {
    Objects.requireNonNull(pObject);
    if (pObject instanceof WeakReference) {
      synchronized (set) {
        for (Object o : set) {
          if (o instanceof WeakReference && pObject.equals(((WeakReference) o).get()))
            return (Reference<T>) o;
        }
      }
    }
    return null;
  }

  /**
   * @return a List of all Objects this bag contains. No WeakReferences are returned but just the plain objects.
   */
  @Nonnull
  protected List<T> getObjects()
  {
    List<T> objects;
    synchronized (set) {
      objects = new ArrayList<>(set.size());
      for (Object o : set) {
        if (o instanceof WeakReference)
          o = ((WeakReference) o).get();
        if (o != null)
          objects.add((T) o);
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
