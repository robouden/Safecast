package org.safecast.log;

import java.util.HashMap;
import java.util.Map;

public class Logger
{
  private LogListener listener;
  private Map<String, Long> tasks;
  
  public Logger()
  {
    listener = null;
    tasks = new HashMap<String, Long>();
  }
  
  public void setLogListener(LogListener listener)
  {
    this.listener = listener;
  }
  
  private void logString(String message)
  {
    if (null != listener)
    {
      listener.eventLogged(message);
    }
  }
  
  public void logEvent(String heading, String event)
  {
    logString(heading.toUpperCase() + ": " + event);
  }
  
  public void logTaskStarted(String task)
  {
    tasks.put(task, System.currentTimeMillis());
    logEvent("TASK", task + " started");
  }
  
  public void logTaskFinished(String task)
  {
    String event = task+" finished";
    if (tasks.containsKey(task))
    {
      long elapsed = System.currentTimeMillis() - tasks.get(task);
      event += " in "  + elapsed + "ms";
      tasks.remove(task);
    }
    logEvent("TASK", event);
  }
  
  public void logError(String error)
  {
    logEvent("ERROR", error);
  }
}
