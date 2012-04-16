package org.safecast.ui.qa;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.safecast.data.Statistics;

@SuppressWarnings("serial")
public class StatsTable extends JTable
{
  private class StatsTableModel extends AbstractTableModel
  {
    private Statistics stats;
    
    public StatsTableModel(Statistics stats)
    {
      super();
      this.stats = stats;
    }
    
    public String getColumnName(int col)
    {
        return 0 == col ? "Statistic" : "Value";
    }
    
    public int getRowCount()
    {
      return stats.getNumberOfStats();
    }
    
    public int getColumnCount()
    {
      return 2;
    }
    
    public Object getValueAt(int row, int col)
    {
      if (0 == col)
      {
        return stats.getStatName(row);
      }
      else
      {
        return stats.getValue(row);
      }
    }
    
    public boolean isCellEditable(int row, int col)
    {
      return false;
    }
    
    public void setValueAt(Object value, int row, int col)
    {
        /* should never be called.. */
    }
  }
  
  public StatsTable(Statistics stats)
  {
    super();
    setModel(new StatsTableModel(stats));
  }  
}
