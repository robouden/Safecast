package org.safecast.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Statistics
{

  private Map<String, Integer> map;
  private List<String> keys;
  private List<String> values;
  
  public Statistics()
  {
    map = new HashMap<String, Integer>();
    keys = new ArrayList<String>();
    values = new ArrayList<String>();
  }

  public String getStatName(int i)
  {
    return keys.get(i);
  }
  
  public void setValue(String stat, String value)
  {
    map.put(stat, keys.size());
    keys.add(stat);
    values.add(value);
  }
  
  public String getValue(String stat)
  {
    return values.get(map.get(stat));
  }
  
  public String getValue(int i)
  {
    return values.get(i);
  }
  
  public int getNumberOfStats()
  {
    return values.size();
  }
  
}
