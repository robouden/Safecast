package org.safecast.data;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;

public class TimeSeries
{
  private List<Integer> data;
  private List<Date> time;
  
  public TimeSeries()
  {
    data = new ArrayList<Integer>();
    time = new ArrayList<Date>();
  }
  
  public void addPoint(Date timestamp, int value)
  {
    data.add(value);
    time.add(timestamp);
  }
  
  public int getValue(int index)
  {
    return data.get(index);
  }
  
  public Date getTimestamp(int index)
  {
    return time.get(index);
  }
  
  public int getSize()
  {
    return data.size();
  }
}
