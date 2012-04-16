package org.safecast.qa;

import org.safecast.data.Histogram;
import org.safecast.data.Route;
import org.safecast.data.Statistics;
import org.safecast.data.TimeSeries;

public interface QaResultsListener
{
  public void histogramReady(Histogram hist);
  
  public void statisticsReady(Statistics stats);

  public void routeReady(Route route);

  public void timeSeriesReady(TimeSeries series);
}
