package de.adito.util.weak;

import javax.annotation.*;
import java.lang.ref.*;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author j.boesl, 03.11.2016
 */
public class WeakReferences<T> implements Iterable<T>
{

  private final Set<WeakReference<T>> set;

  public WeakReferences()
  {
    set = Collections.synchronizedSet(new LinkedHashSet<WeakReference<T>>());
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

  protected void availabilityChanged(boolean pAvailable)
  {
  }

}
