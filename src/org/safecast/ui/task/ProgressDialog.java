package org.safecast.ui.task;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JDialog;

import org.safecast.task.HasProgress;

@SuppressWarnings("serial")
public class ProgressDialog extends JDialog implements WindowListener
{
  public static final int DIALOG_WIDTH = 200;
  public static final int DIALOG_HEIGHT = 100;
  
  ProgressPanel panel;
  HasProgress runningTask;
  TaskCancelListener listener;
  
  public ProgressDialog(Frame owner)
  {
    super(owner, true);
    addWindowListener(this);
    setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
    setLayout(new BorderLayout());
    panel = new ProgressPanel();
    add(BorderLayout.CENTER, panel);
  }
  
  public void taskStarted(final HasProgress task, TaskCancelListener listener)
  {
    this.listener = listener;
    runningTask = task;
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

  public void windowClosing(WindowEvent arg0)
  {
    listener.taskCancelled();
  }
  public void windowActivated(WindowEvent arg0) {}
  public void windowClosed(WindowEvent arg0) {}
  public void windowDeactivated(WindowEvent arg0) {}
  public void windowDeiconified(WindowEvent arg0) {}
  public void windowIconified(WindowEvent arg0) {}
  public void windowOpened(WindowEvent arg0){}
}
