package org.safecast.parse;

import java.io.IOException;

import org.safecast.task.ProgressTask;
import org.safecast.task.TaskListener;

public abstract class Parser implements ProgressTask<String, ParsedData>
{
  private String backgroundFilename;
  private TaskListener<String, ParsedData> listener;
  
  public abstract ParsedData parseFile(String filename)
    throws IOException, ParseException;
 
  public String getTaskName()
  {
    if (null != backgroundFilename)
      return "Parsing " + backgroundFilename;
    else
      return "";
  }  
 
  public void startTask(String filename, TaskListener<String, ParsedData> listener)
  {
    backgroundFilename = filename;
    this.listener = listener;
    (new Thread(this, "Parser Thread")).start();
  }
  
  public void run()
  {
    ParsedData backgroundOutput;
    listener.taskStarted(this);
    try
    {
      backgroundOutput = parseFile(backgroundFilename);
      listener.taskCompleted(this, backgroundOutput);
    }
    catch (IOException e)
    {
      listener.taskError(this, e.getLocalizedMessage());
    }
    catch (ParseException e)
    {
      listener.taskError(this, e.getLocalizedMessage());      
    }
    backgroundFilename = null;
    listener = null;
  }
  
}
