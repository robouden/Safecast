package org.safecast.task;

public interface ProgressTask<I, O> extends HasProgress, Runnable
{
  public void startTask(I input, TaskListener<I, O> listener);
}
