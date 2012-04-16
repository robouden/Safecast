package org.safecast.db;

@SuppressWarnings("serial")
public class DatabaseException extends Exception
{
  public DatabaseException(String error)
  {
    super(error);
  }
}
