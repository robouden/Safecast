package org.safecast.ui.qa;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JPanel;

import org.safecast.data.Histogram;
import org.safecast.data.Route;
import org.safecast.data.Statistics;
import org.safecast.data.TimeSeries;
import org.safecast.qa.QaResultsListener;

@SuppressWarnings("serial")
public class QaPanel extends JPanel implements QaResultsListener
{
  HistogramPanel hist;
  GoogleMapPanel map;
  OfflineTimeSeriesPanel timeSeries;
  StatsPanel stats;
  JPanel graphicsPanel;
  
  
  public QaPanel()
  {
    setLayout(new BorderLayout());
    graphicsPanel = new JPanel();
    graphicsPanel.setLayout(new GridLayout(2, 2));
    hist = new HistogramPanel();
    map = new GoogleMapPanel();
    timeSeries = new OfflineTimeSeriesPanel();
    stats = new StatsPanel();
    graphicsPanel.add(map);
    graphicsPanel.add(timeSeries);
    graphicsPanel.add(hist);
    graphicsPanel.add(stats);
    add(BorderLayout.CENTER, graphicsPanel);
  }

  public void histogramReady(Histogram hist)
  {
    this.hist.renderHistogram(hist);
  }

  public void statisticsReady(Statistics stats)
  {
    this.stats.renderStatistics(stats);
  }
  
  public void routeReady(Route route)
  {
    this.map.renderMap(route);
  }

  public void timeSeriesReady(TimeSeries series)
  {
    this.timeSeries.renderTimeSeries(series);
  }
  
}
