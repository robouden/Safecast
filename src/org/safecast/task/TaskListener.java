package org.safecast.task;

public interface TaskListener<I, O>
{
  public void taskStarted(ProgressTask<I, O> task);
  public void taskCompleted(ProgressTask<I, O> task, O output);
  public void taskError(ProgressTask<I, O> task, String error);
}
