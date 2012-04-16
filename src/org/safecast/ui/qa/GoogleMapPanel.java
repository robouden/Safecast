package org.safecast.ui.qa;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import org.safecast.data.GeoCoord;
import org.safecast.data.Route;

@SuppressWarnings("serial")
public class GoogleMapPanel extends PlaceholderPanel
{

  public static final String URL_BASE = "https://maps.googleapis.com/maps/api/staticmap?sensor=false"; 
  public static final String URL_SIZE = "&size=%dx%d";
  public static final String URL_MARKER = "&markers=color:%s|label:%s|%f,%f";

  private Image image;
  
  public GoogleMapPanel()
  {
    super(QaGui.STRINGS.getString("map"));
  }

  private String generateUrl(Dimension size, Route route)
  {
    String url = URL_BASE;
    url += String.format(URL_SIZE, size.width, size.height);
    GeoCoord s = route.getStartPoint();
    GeoCoord f = route.getEndPoint();
    url += String.format(URL_MARKER, "green", "S", s.getLatitude(), s.getLongitude());
    url += String.format(URL_MARKER, "red", "F", f.getLatitude(), f.getLongitude());
    url += generatePath(route);
    return url;
  }
  
  private String generatePath(Route route)
  {
    StringBuffer path = new StringBuffer("&path=");
    
    for (int i = 0; i < route.getNumberOfPoints(); i++)
    {
      GeoCoord c = route.getPoint(i);
      path.append(c.getLatitude());
      path.append(",");
      path.append(c.getLongitude());
      path.append("|");
    }
    path.deleteCharAt(path.length() - 1);
    
    return path.toString();
  }
  
  private Image generateMap(Route route)
  {
    Image map = null;
    try
    {
      URL imgUrl = new URL(generateUrl(getSize(), route));
      ImageIcon imageIcon = new ImageIcon(imgUrl);
      map = imageIcon.getImage();
    }
    catch (MalformedURLException e) {}
    return map;
  }
  
  public void renderMap(Route route)
  {
    image = generateMap(route);
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
