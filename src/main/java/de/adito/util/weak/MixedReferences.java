package de.adito.util.weak;

import javax.annotation.*;
import java.lang.ref.*;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author j.boesl, 08.11.16
 */
public class MixedReferences<T> implements Iterable<T>
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

  protected void availabilityChanged(boolean pAvailable)
  {
  }

}
