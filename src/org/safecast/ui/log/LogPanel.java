package org.safecast.ui.log;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.safecast.log.LogListener;

@SuppressWarnings("serial")
public class LogPanel extends JPanel implements LogListener
{
  public JTextArea data;
  
  public LogPanel(int preferredSize)
  {
    super();
    data = new JTextArea();
    data.setEditable(false);
    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, new JScrollPane(data));
    Dimension p = new Dimension(preferredSize, preferredSize);
    setPreferredSize(p);
    setFont();
  }
  
  private void setFont()
  {
    Font f = getFont();
    f = f.deriveFont(f.getSize() / 2.0f);
    setFont(f);
  }
  
  public void eventLogged(final String event)
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        data.append(event + "\n");
      }
    });
  }
}
