package org.safecast.ui.qa;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import org.safecast.data.TimeSeries;

@SuppressWarnings("serial")
public class OnlineTimeSeriesPanel extends PlaceholderPanel
{
  public static final int POINTS = 50;
  public static final String TITLE_STRING =
    String.format("Final%%20%d%%20CPM%%20measurements", POINTS);
  
  public static final String URL_BASE = "http://chart.apis.google.com/chart";
  public static final String URL_BASE_PARAMS = "?cht=lc&chxt=x,y&chtt=" + TITLE_STRING;
  public static final String URL_SIZE = "&chs=%dx%d";

  private Image image;
  
  public OnlineTimeSeriesPanel()
  {
    super("CPM time series..");
  }

  private String generateUrl(Dimension size, TimeSeries series)
  {
    String url = URL_BASE;
    url += URL_BASE_PARAMS;
    url += String.format(URL_SIZE, size.width, size.height);
    url += generateScaleParameters(series, POINTS);
    url += generateData(series, POINTS);
    return url;
  }
  
  private String generateScaleParameters(TimeSeries series, int points)
  {
    String param = "&chds=%d,%d&chxr=0,%d,%d|1,%d,%d";
    int s = series.getSize() - POINTS;
    if (s < 0)
      s = 0;
    int f = series.getSize();
    
    int min = series.getValue(s);
    int max = series.getValue(s);
    for (int i = s; i < f; i++)
    {
      int v = series.getValue(i);
      if (v > max)
        max = v;
      else if (v < min)
        min = v;
    }
     
    return String.format(param, min, max, 0, POINTS, min, max);
  }
  
  private String generateData(TimeSeries series, int points)
  {
    StringBuffer path = new StringBuffer("&chd=t:");
    
    int s = series.getSize() - points;
    if (s < 0)
      s = 0;
    int f = series.getSize();
    for (int i = s; i < f; i++)
    {
      path.append(series.getValue(i));
      path.append(",");
    }
    path.deleteCharAt(path.length() - 1);
    
    return path.toString();
  }
  
  private Image generateChart(TimeSeries series)
  {
    Image map = null;
    try
    {
      URL imgUrl = new URL(generateUrl(getSize(), series));
      ImageIcon imageIcon = new ImageIcon(imgUrl);
      map = imageIcon.getImage();
    }
    catch (MalformedURLException e) {}
    return map;
  }
  
  public void renderTimeSeries(TimeSeries series)
  {
    image = generateChart(series);
    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        removeAll();
        revalidate();
        repaint();
      }
    });
  }
  
  public void paintComponent(Graphics g)
  {
    if (null != image)
      g.drawImage(image, 0, 0, null);
    else
      super.paintComponent(g);
  }
}
