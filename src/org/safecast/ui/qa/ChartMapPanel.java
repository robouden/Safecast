package org.safecast.ui.qa;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.SwingUtilities;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.util.ShapeUtilities;
import org.safecast.data.GeoCoord;
import org.safecast.data.Route;

@SuppressWarnings("serial")
public class ChartMapPanel extends PlaceholderPanel
{
  public static final Color [] SERIES_COLORS =
  {
    Color.RED,
    Color.GREEN,
    Color.BLACK, 
  };
  
  public ChartMapPanel()
  {
    super(QaGui.STRINGS.getString("map"));
  }

  private void configureJFreeChart()
  {
    ChartFactory.setChartTheme(StandardChartTheme.createLegacyTheme());    
    BarRenderer.setDefaultBarPainter(new StandardBarPainter());
  }
  
  private void setMarkerShape(XYPlot plot)
  {
    XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)plot.getRenderer();
    for (int i = 0; i < plot.getSeriesCount(); i++)
    {
      renderer.setSeriesShapesVisible(i, true);
      renderer.setSeriesShape(i, ShapeUtilities.createDiagonalCross(4, 1));
      renderer.setSeriesPaint(i, SERIES_COLORS[i % SERIES_COLORS.length]);
    }
  }

  private XYSeriesCollection createSeries(Route route)
  {    
    XYSeries routeSeries = new XYSeries("Drive Route");
    for (int i = 1; i < route.getNumberOfPoints() - 1; i++)
    {
      GeoCoord c = route.getPoint(i);
      routeSeries.add(c.getLongitude(), c.getLatitude());
    }
    
    XYSeries start = new XYSeries("Start Point");
    GeoCoord s = route.getStartPoint();
    start.add(s.getLongitude(), s.getLatitude());
    
    XYSeries finish = new XYSeries("Finish Point");
    GeoCoord f = route.getEndPoint();
    finish.add(f.getLongitude(), f.getLatitude());
    
    XYSeriesCollection dataset = new XYSeriesCollection();
    dataset.addSeries(finish);
    dataset.addSeries(start);
    dataset.addSeries(routeSeries);    
    
    return dataset;
  }
      
  private JFreeChart createChart(Route route)
  {
    JFreeChart chart = ChartFactory.createScatterPlot( 
        "Map", "Longitude", "Latitude", createSeries(route), 
        PlotOrientation.VERTICAL, false, false, false);
    XYPlot plot = (XYPlot)chart.getPlot();
    setMarkerShape(plot);
    return chart;
  }
  
  public void renderMap(Route route)
  {
    configureJFreeChart();
    final JFreeChart chart = createChart(route);
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
