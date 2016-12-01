package de.adito.util.weak;

import java.lang.ref.*;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Factory for WeakReferences that are notified about being collected.
 *
 * @author j.boesl, 03.11.2016
 */
public class WeakReferenceFactory
{

  private static final WeakReferenceFactory INSTANCE = new WeakReferenceFactory();

  private ListenableReferenceQueue referenceQueue = new ListenableReferenceQueue();
  private ExecutorService executorService = Executors.newSingleThreadExecutor();

  /**
   * @return the singleton instance.
   */
  public static WeakReferenceFactory get()
  {
    return INSTANCE;
  }

  private WeakReferenceFactory()
  {
    referenceQueue.start();
  }

  /**
   * Creates a WeakReference.
   *
   * @param pValue     the value for the reference to take.
   * @param pOnCollect consumer that is called when the reference was collected.
   * @param <T>        the reference's type.
   * @return the newly created WeakReference.
   */
  public <T> WeakReference<T> create(T pValue, Consumer<Reference<T>> pOnCollect)
  {
    Objects.nonNull(pOnCollect);
    return new _WR<>(pValue, referenceQueue, ref -> executorService.execute(() -> pOnCollect.accept(ref)));
  }

  /**
   * WeakReference implementation
   */
  private static class _WR<T> extends WeakReference<T> implements Runnable
  {
    private Consumer<Reference<T>> onCollect;

    _WR(T referent, ReferenceQueue<? super T> q, Consumer<Reference<T>> pOnCollect)
    {
      super(referent, q);
      onCollect = pOnCollect;
    }

    @Override
    public void run()
    {
      onCollect.accept(this);
    }
  }

}
