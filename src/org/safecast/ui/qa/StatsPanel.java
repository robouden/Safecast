package org.safecast.ui.qa;

import java.awt.BorderLayout;

import javax.swing.SwingUtilities;

import org.safecast.data.Statistics;

@SuppressWarnings("serial")
public class StatsPanel extends PlaceholderPanel
{
  public StatsPanel()
  {
    super(QaGui.STRINGS.getString("statistics"));
  }
  
  public void renderStatistics(Statistics stats)
  {
    final StatsTable table = new StatsTable(stats);
    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        removeAll();
        setPreferredSize(table.getPreferredScrollableViewportSize());
        add(table, BorderLayout.CENTER);
        revalidate();
      }
    });    
  }
}
