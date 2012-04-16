package org.safecast.ui.qa;

import java.awt.BorderLayout;
import java.util.Iterator;

import javax.swing.SwingUtilities;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.safecast.data.Histogram;


@SuppressWarnings("serial")
public class HistogramPanel extends PlaceholderPanel
{
  public HistogramPanel()
  {
    super(QaGui.STRINGS.getString("cpm_histogram"));
  }

  private void configureJFreeChart()
  {
    ChartFactory.setChartTheme(StandardChartTheme.createLegacyTheme());    
    BarRenderer.setDefaultBarPainter(new StandardBarPainter());
  }
  
  private JFreeChart createChart(Histogram hist)
  {
    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    Iterator<Integer> iter = hist.getIterator();
    while (iter.hasNext())
    {
      Integer bucket = iter.next();
      dataset.setValue(hist.getValue(bucket),
          QaGui.STRINGS.getString("cpm"), bucket.toString());
    }
    JFreeChart chart = ChartFactory.createBarChart(
        QaGui.STRINGS.getString("histogram"),
        QaGui.STRINGS.getString("cpm"),
        QaGui.STRINGS.getString("frequency"),
        dataset, PlotOrientation.VERTICAL, false, true, false);
/*    CategoryPlot plot = chart.getCategoryPlot();
    plot.setRangeAxis(new LogAxis()); */
    return chart;
  }
  
  public void renderHistogram(Histogram hist)
  {
    configureJFreeChart();
    final JFreeChart chart = createChart(hist);
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
