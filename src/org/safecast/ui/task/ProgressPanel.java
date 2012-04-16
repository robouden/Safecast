package org.safecast.ui.task;

import java.awt.BorderLayout;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.safecast.task.HasProgress;

@SuppressWarnings("serial")
public class ProgressPanel extends JPanel
{
  public static final int PROGRESS_UPDATE_MS = 50;
  
  private JLabel taskLabel;
  private JProgressBar progressBar;
  private Timer updateTimer;
  
  public ProgressPanel()
  {
    setLayout(new BorderLayout());
    taskLabel = new JLabel("", JLabel.CENTER);
    progressBar = new JProgressBar(0, 100);
    add(BorderLayout.NORTH, taskLabel);
    add(BorderLayout.CENTER, progressBar);
  }
  
  public void taskStarted(final HasProgress task)
  {
    updateTimer = new Timer("Progress Panel Update Timer");
    TimerTask tTask = new TimerTask()
    {
      public void run()
      {
        javax.swing.SwingUtilities.invokeLater(new Runnable() 
        {
          public void run()
          {        
            taskLabel.setText(task.getTaskName());
            progressBar.setValue(task.getProgressPercent());
          }
        });
      }
    };
    updateTimer.schedule(tTask, 0, PROGRESS_UPDATE_MS);
  }
  
  public void cancelUpdateTimer()
  {
    updateTimer.cancel();
  }
}
