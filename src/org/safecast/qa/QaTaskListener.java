package org.safecast.qa;

import org.safecast.task.HasProgress;

public interface QaTaskListener
{
  public void taskStarted(HasProgress task);
  public void taskCompleted();
  public void taskError(String error);
}
