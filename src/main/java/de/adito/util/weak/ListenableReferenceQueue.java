package de.adito.util.weak;

import java.lang.ref.*;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author j.boesl, 03.11.2016
 */
public class ListenableReferenceQueue extends ReferenceQueue<Object>
{

  private final static int POLL_TIMEOUT = Optional
      .ofNullable(((Supplier<Integer>) () -> {
        String pollTimeoutString = System.getProperty("de.adito.util.weak.polltimeout");
        if (pollTimeoutString != null)
          try {
            return Integer.parseInt(pollTimeoutString);
          }
          catch (NumberFormatException ignored) {
          }
        return null;
      }).get())
      .orElse(10000);


  private _Thread thread;


  public synchronized void start()
  {
    if (thread == null) {
      thread = new _Thread();
      thread.start();
    }
  }

  public synchronized void stop()
  {
    if (thread != null) {
      thread.halt();
      thread = null;
    }
  }


  /**
   * Thread impl
   */
  private class _Thread extends Thread
  {
    private boolean running = true;

    _Thread()
    {
      setDaemon(true);
      setName("ReferenceQueueThread");
    }

    @Override
    public void run()
    {
      while (running) {
        try {
          Reference ref = remove(POLL_TIMEOUT);
          if (ref != null && ref instanceof Runnable)
            ((Runnable) ref).run();
        }
        catch (InterruptedException pE) {
          // lets kill it ...
          return;
        }
      }
    }

    void halt()
    {
      running = false;
    }
  }

}
