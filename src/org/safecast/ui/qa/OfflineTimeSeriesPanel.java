package org.safecast.ui.qa;

import java.awt.BorderLayout;

import javax.swing.SwingUtilities;

import org.safecast.data.TimeSeries;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeriesCollection;

@SuppressWarnings("serial")
public class OfflineTimeSeriesPanel extends PlaceholderPanel
{
 
  public OfflineTimeSeriesPanel()
  {
    super(QaGui.STRINGS.getString("time_series"));
  }

  private void configureJFreeChart()
  {
    ChartFactory.setChartTheme(StandardChartTheme.createLegacyTheme());    
  }
  
  private TimeSeriesCollection generateDataFull(TimeSeries series)
  {
    TimeSeriesCollection data = new TimeSeriesCollection();
    org.jfree.data.time.TimeSeries jfcSeries = new org.jfree.data.time.TimeSeries("CPM", "seconds", "CPM");
    for (int i = 0; i < series.getSize(); i++)
    {
      Second sec = new Second(series.getTimestamp(i));
      jfcSeries.add(sec, series.getValue(i));
    }
    data.addSeries(jfcSeries);
    return data;
  }
  
  private TimeSeriesCollection generateData(TimeSeries series, int points)
  {
    TimeSeriesCollection data = new TimeSeriesCollection();
    org.jfree.data.time.TimeSeries jfcSeries = new org.jfree.data.time.TimeSeries("CPM", "seconds", "CPM");
    int s = series.getSize() - points;
    if (s < 0)
      s = 0;
    int f = series.getSize();
    for (int i = s; i < f; i++)
    {
      Second sec = new Second(series.getTimestamp(i));
      jfcSeries.add(sec, series.getValue(i));  
    }
    data.addSeries(jfcSeries);
    return data;
  }
  
  private JFreeChart generateChart(TimeSeries series)
  {
    TimeSeriesCollection dataset = generateDataFull(series);
    JFreeChart chart = ChartFactory.createTimeSeriesChart(
        QaGui.STRINGS.getString("radiation_measurements"),
        QaGui.STRINGS.getString("time"), 
        QaGui.STRINGS.getString("cpm"),
        dataset, 
        false, 
        false, 
        false
    );
    return chart;
  }  

  public void renderTimeSeries(TimeSeries series)
  {
    configureJFreeChart();
    final JFreeChart chart = generateChart(series);
    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        ChartPanel panel = new ChartPanel(chart);
        panel.setPreferredSize(getSize());
        removeAll();
        add(panel, BorderLayout.CENTER);
        revalidate();
      }
    });    
  }

}
