package org.safecast.db;

import org.safecast.parse.ParsedData;
import org.safecast.task.ProgressTask;
import org.safecast.task.TaskListener;

public class DatabaseCreationTask implements ProgressTask<ParsedData, DriveDatabase>
{

  private ParsedData input = null;
  private DriveDatabase db = null;
  private TaskListener<ParsedData, DriveDatabase> listener;
  private String taskName;
  
  public void run()
  {
    listener.taskStarted(this);    
    try
    {
      initialiseDatabase();
      fillDatabase(input);
    }
    catch (DatabaseException e)
    {
      listener.taskError(this, e.getLocalizedMessage());
    }
    listener.taskCompleted(this, db);
  }
    
  private void initialiseDatabase() throws DatabaseException
  {
    synchronized (this) { taskName = "Creating database"; }
    db = new DriveDatabase();
  }
  
  private void fillDatabase(ParsedData input) throws DatabaseException
  {
    synchronized (this) { taskName = "Batching insertions"; }
    db.addRows(input.rows);
    synchronized (this) { taskName = "Committing data"; }
    db.commitRows();    
  }

  public synchronized int getProgressPercent()
  {
    if (null == db)
      return 0;
    else
      return db.getProgressPercent();
  }

  public synchronized String getTaskName()
  {
    return taskName;
  }

  public void startTask(ParsedData input,
      TaskListener<ParsedData, DriveDatabase> listener)
  {
    this.input = input;
    this.listener = listener; 
    (new Thread(this, "Database Creation Thread")).start();
  }

}
