package org.safecast.data;

import java.util.Iterator;
import java.util.TreeMap;
import java.util.SortedMap;

public class Histogram
{
  private SortedMap<Integer, Integer> data;
  
  public Histogram()
  {
    data = new TreeMap<Integer, Integer>();
  }
  
  public void set(int bucket, int value)
  {
    data.put(bucket, value);
  }
  
  public Iterator<Integer> getIterator()
  {
    return data.keySet().iterator();
  }
  
  public Integer getValue(Integer bucket)
  {
    return data.get(bucket);
  }
}
