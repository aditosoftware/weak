package de.adito.util.weak;

import org.junit.*;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;
import java.util.*;

/**
 * @author j.boesl, 08.11.16
 */
public class Test_MixedReferences
{

  @Test
  public void simpleTest() throws InterruptedException
  {
    StringBuffer strBuf = new StringBuffer();

    MixedReferences<NamedObject> references = new MixedReferences<NamedObject>()
    {
      @Override
      protected void availabilityChanged(boolean pAvailable)
      {
        strBuf.append("av+").append(pAvailable).append(":");
      }

      @Override
      public void addWeak(@Nonnull NamedObject pObject)
      {
        super.addWeak(pObject);
        strBuf.append("aw+").append(pObject).append(":");
      }

      @Override
      public void addStrong(@Nonnull NamedObject pObject)
      {
        super.addStrong(pObject);
        strBuf.append("as+").append(pObject).append(":");
      }

      @Override
      public boolean remove(@Nonnull Object pObject)
      {
        boolean wasRemoved = super.remove(pObject);
        if (wasRemoved) {
          Object o = pObject instanceof WeakReference ?
              Optional.ofNullable(((WeakReference) pObject).get()).orElse(pObject.getClass().getSimpleName()) :
              pObject;
          strBuf.append("re+").append(o).append(":");
        }
        return wasRemoved;
      }
    };


    List<Object> objects = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
      NamedObject namedObject = new NamedObject("" + i);
      objects.add(namedObject);
      if (i % 2 == 0)
        references.addWeak(namedObject);
      else
        references.addStrong(namedObject);
    }

    NamedObject customRemove = new NamedObject("customRemove");
    references.addWeak(customRemove);
    references.remove(customRemove);

    System.gc();

    _waitForEmpty(references, 10);

    Assert.assertEquals("av+true:aw+NO{0}:as+NO{1}:aw+NO{2}:as+NO{3}:aw+NO{4}:as+NO{5}:aw+NO{6}:as+NO{7}:aw+NO{8}:as+NO{9}:aw+NO{10}:as+NO{11}:aw+NO{12}:as+NO{13}:aw+NO{14}:as+NO{15}:aw+NO{16}:as+NO{17}:aw+NO{18}:as+NO{19}:aw+NO{customRemove}:re+NO{customRemove}:",
                        strBuf.toString());

    strBuf.setLength(0);
    objects = null;
    System.gc();

    _waitForEmpty(references, 100);

    Assert.assertEquals("re+_WR:re+_WR:re+_WR:re+_WR:re+_WR:re+_WR:re+_WR:re+_WR:re+_WR:re+_WR:",
                        strBuf.toString());

    Assert.assertEquals(Arrays.toString(references.getObjects().toArray()), "[NO{1}, NO{3}, NO{5}, NO{7}, NO{9}, NO{11}, NO{13}, NO{15}, NO{17}, NO{19}]");

    strBuf.setLength(0);
    references.clear();

    Assert.assertTrue(references.isEmpty());

    Assert.assertEquals("av+false:",
                        strBuf.toString());
  }

  private void _waitForEmpty(MixedReferences<NamedObject> pReferences, int pCount) throws InterruptedException
  {
    for (int i = 0; i < pCount; i++) {
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