package org.safecast.ui.qa;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.JDialog;

import org.safecast.task.HasProgress;

@SuppressWarnings("serial")
public class ProgressDialog extends JDialog
{
  public static final int DIALOG_WIDTH = 200;
  public static final int DIALOG_HEIGHT = 100;
  
  ProgressPanel panel;
  
  public ProgressDialog(Frame owner)
  {
    super(owner, true);
    setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
    setLayout(new BorderLayout());
    panel = new ProgressPanel();
    add(BorderLayout.CENTER, panel);
  }
  
  public void taskStarted(final HasProgress task)
  {
    panel.taskStarted(task);
    javax.swing.SwingUtilities.invokeLater(new Runnable() 
    {
      public void run()
      {
        setVisible(true);
      }
    });
  }
  
  public void taskCompleted()
  {
    panel.cancelUpdateTimer();    
    javax.swing.SwingUtilities.invokeLater(new Runnable() 
    {
      public void run()
      {    
        setVisible(false);
      }
    });
  }
}
