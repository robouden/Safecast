package org.safecast.qa;

import org.safecast.data.Histogram;
import org.safecast.data.Route;
import org.safecast.data.Statistics;
import org.safecast.data.TimeSeries;
import org.safecast.db.DatabaseCreationTask;
import org.safecast.db.DatabaseException;
import org.safecast.db.DriveDatabase;
import org.safecast.db.QueryListener;
import org.safecast.log.Logger;
import org.safecast.parse.NinjaParser;
import org.safecast.parse.ParsedData;
import org.safecast.task.ProgressTask;
import org.safecast.task.TaskListener;
import org.safecast.ui.task.TaskCancelListener;

public class QaBackend implements TaskCancelListener, QueryListener
{
  private String inputFile;
  private QaTaskListener taskListener;
  private QaResultsListener resultsListener;
  private DriveDatabase db;
  private Logger log;
  
  public QaBackend(String inputFile)
  {
    this.inputFile = inputFile;
    this.log = new Logger();
  } 
  
  public void start(QaTaskListener taskListener, QaResultsListener resultsListener)
  {
    this.taskListener = taskListener;
    this.resultsListener = resultsListener;
    processInput(inputFile);
  }
  
  public void cleanup()
  {
    try
    {
      db.close();
    }
    catch (DatabaseException e)
    {
      handleDatabaseException(e);
    }
  }
  
  public Logger getLogger()
  {
    return this.log;
  }
  
  private class ParseListener implements TaskListener<String, ParsedData>
  {
      public void taskStarted(ProgressTask<String, ParsedData> task)
      {
        log.logEvent("INFO", "Parsing input");
        QaBackend.this.taskListener.taskStarted(task);
      }

      public void taskCompleted(ProgressTask<String, ParsedData> task,
          ParsedData output)
      {
        QaBackend.this.taskListener.taskCompleted();
        createDatabase(output);
      }

      public void taskError(ProgressTask<String, ParsedData> task,
          String error)
      {
        QaBackend.this.taskListener.taskError(error);
      } 
  }
  
  private void processInput(String inputFile)
  {
    NinjaParser parser = new NinjaParser();
    parser.startTask(inputFile, new ParseListener());
  }
  
  private class DatabaseCreationListener implements TaskListener<ParsedData, DriveDatabase>
  {

    public void taskStarted(ProgressTask<ParsedData, DriveDatabase> task)
    {
      log.logEvent("INFO", "Creating database");
      QaBackend.this.taskListener.taskStarted(task);
    }

    public void taskCompleted(ProgressTask<ParsedData, DriveDatabase> task,
        DriveDatabase output)
    {
      QaBackend.this.taskListener.taskCompleted();
      db = output;
      db.setQueryListener(QaBackend.this);
      
      log.logTaskStarted("statistics");
      generateStatistics(db);
      log.logTaskFinished("statistics");
      
      log.logTaskStarted("route");
      generateRoute(db);
      log.logTaskFinished("route");
      
      log.logTaskStarted("time series");
      generateTimeSeries(db);
      log.logTaskFinished("time series");
      
      log.logTaskStarted("histogram");
      generateHistogram(db);
      log.logTaskFinished("histogram");
      try
      {
        db.close();
      }
      catch(DatabaseException e)
      {
        e.printStackTrace();
      }
    }

    public void taskError(ProgressTask<ParsedData, DriveDatabase> task,
        String error)
    {
      QaBackend.this.taskListener.taskError(error);
    }
    
  }
  
  private void createDatabase(ParsedData data)
  {
    DatabaseCreationTask dbTask = new DatabaseCreationTask();
    dbTask.startTask(data, new DatabaseCreationListener());
  }
  
  private void generateHistogram(DriveDatabase db)
  {
    try
    {
      Histogram hist = db.getCpmHistogram(100);
      resultsListener.histogramReady(hist);
    }
    catch (DatabaseException e)
    {
      handleDatabaseException(e);
    }
  }
  
  private void generateStatistics(DriveDatabase db)
  {
    try
    {
      Statistics stats = db.getStatistics();
      resultsListener.statisticsReady(stats);
    }
    catch (DatabaseException e)
    {
      handleDatabaseException(e);
    }
  }
  
  private void generateRoute(DriveDatabase db)
  {
    try
    {
      Route route = db.getRoute(20);
      resultsListener.routeReady(route);
    }
    catch (DatabaseException e)
    {
      handleDatabaseException(e);
    }
  }

  private void generateTimeSeries(DriveDatabase db)
  {
    try
    {
      TimeSeries series = db.getCpmTimeSeries();
      resultsListener.timeSeriesReady(series);
    }
    catch (DatabaseException e)
    {
      handleDatabaseException(e);
    }
  }
  
  private void handleDatabaseException(DatabaseException e)
  {
    log.logError(e.getMessage());
  }
  
  public void taskCancelled()
  {
    // TODO: handle task cancellation
  }

  public void queryIssued(String query)
  {
    log.logEvent("SQL", query);
  }

  public void queryCompleted(String query, long elapsedMilliseconds)
  {
    log.logEvent("SQL", "completed in "+elapsedMilliseconds+" ms");
  }

  public void queryFailed(String query)
  {
    log.logEvent("SQL", "failed");
  }

}
