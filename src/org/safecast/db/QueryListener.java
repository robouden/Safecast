package org.safecast.db;

public interface QueryListener
{
  
  public void queryIssued(String query);

  public void queryCompleted(String query, long elapsedMilliseconds);
  
  public void queryFailed(String query);
}
