package org.safecast.data;

import java.util.ArrayList;
import java.util.List;

public class Route
{

    private List<GeoCoord> points;
    
    public Route()
    {
      points = new ArrayList<GeoCoord>();
    }
    
    public GeoCoord getPoint(int i)
    {
      return points.get(i);
    }
    
    public void addPoint(GeoCoord c)
    {
      points.add(c);
    }
    
    public int getNumberOfPoints()
    {
      return points.size();
    }
    
    public GeoCoord getStartPoint()
    {
      return getPoint(0);
    }
    
    public GeoCoord getEndPoint()
    {
      return getPoint(getNumberOfPoints() - 1);
    }
  
}
