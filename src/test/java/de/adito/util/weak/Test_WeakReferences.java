package de.adito.util.weak;

import org.junit.*;

import javax.annotation.Nonnull;
import java.lang.ref.Reference;
import java.util.*;

/**
 * @author j.boesl, 03.11.16
 */
public class Test_WeakReferences
{

  @Test
  public void simpleTest() throws InterruptedException
  {
    StringBuffer strBuf = new StringBuffer();

    WeakReferences<Object> references = new WeakReferences<Object>()
    {
      @Override
      protected void availabilityChanged(boolean pAvailable)
      {
        strBuf.append("av+").append(pAvailable).append(":");
      }

      @Override
      public void add(@Nonnull Object pObject)
      {
        super.add(pObject);
        strBuf.append("ad+").append(pObject).append(":");
      }

      @Override
      protected void remove(@Nonnull Reference<Object> pReference)
      {
        Object o = Optional.ofNullable(pReference.get()).orElse(pReference.getClass().getSimpleName());
        strBuf.append("re+").append(o).append(":");
        super.remove(pReference);
      }
    };


    List<Object> objects = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
      NamedObject namedObject = new NamedObject("" + i);
      objects.add(namedObject);
      references.add(namedObject);
    }

    System.gc();

    _waitForEmpty(references, 10);

    Assert.assertEquals("av+true:ad+NO{0}:ad+NO{1}:ad+NO{2}:ad+NO{3}:ad+NO{4}:ad+NO{5}:ad+NO{6}:ad+NO{7}:ad+NO{8}:ad+NO{9}:ad+NO{10}:ad+NO{11}:ad+NO{12}:ad+NO{13}:ad+NO{14}:ad+NO{15}:ad+NO{16}:ad+NO{17}:ad+NO{18}:ad+NO{19}:",
                        strBuf.toString());

    objects = null;

    System.gc();

    _waitForEmpty(references, 100);

    Assert.assertEquals("av+true:ad+NO{0}:ad+NO{1}:ad+NO{2}:ad+NO{3}:ad+NO{4}:ad+NO{5}:ad+NO{6}:ad+NO{7}:ad+NO{8}:ad+NO{9}:ad+NO{10}:ad+NO{11}:ad+NO{12}:ad+NO{13}:ad+NO{14}:ad+NO{15}:ad+NO{16}:ad+NO{17}:ad+NO{18}:ad+NO{19}:re+_WR:re+_WR:re+_WR:re+_WR:re+_WR:re+_WR:re+_WR:re+_WR:re+_WR:re+_WR:re+_WR:re+_WR:re+_WR:re+_WR:re+_WR:re+_WR:re+_WR:re+_WR:re+_WR:re+_WR:av+false:",
                        strBuf.toString());
  }

  private void _waitForEmpty(WeakReferences<Object> pReferences, int pCount) throws InterruptedException
  {
    for (int i = 0; i < pCount; i++)
    {
      Thread.sleep(10);
      if (pReferences.isEmpty())
        break;
    }
  }

  private static class NamedObject
  {
    private String name;

    public NamedObject(String pName)
    {
      name = pName;
    }

    @Override
    public String toString()
    {
      return "NO{" + name + "}";
    }
  }

}