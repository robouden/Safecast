package org.safecast.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * General purpose wrapper for simple SQLite databases
 * @author Bruce Sutherland
 */
public class SqliteDatabase
{
  String tableName;  
  Connection con;
  PreparedStatement insert;
  Statement select;
  List<String> fields;
  int progressPercent;
  QueryListener listener;
  long queryStartTime;

  /**
   * Create an in-memory single table SQLite3 database
   * @param fields names of fields for the table
   */
  public SqliteDatabase(String table, List<String> fields, List<String> types) throws DatabaseException
  {
    this(":memory:", table, fields, types);
  }

  /**
   * Create an in-file single table SQLite3 database
   * @param filename database filename
   * @param fields names of fields for the table
   */
  public SqliteDatabase(String filename, String table, List<String> fields, List<String> types) throws DatabaseException
  {
    synchronized (this) { progressPercent = 0; }
    if (fields.size() != types.size())
      throw new DatabaseException("Number of fields and types differ");
    tableName = table;
    con = connect(filename);
    createTables(fields, types);
    prepareSelectStatement();
    prepareInsertStatement(fields.size());
    this.fields = new ArrayList<String>();
    this.fields.addAll(fields);
  }
  
  public void setQueryListener(QueryListener listener)
  {
    this.listener = listener;
  }
  
  public synchronized int getProgressPercent()
  {
    return progressPercent;
  }
  
  /**
   * Insert data (but don't commit)
   * @param data List of rows, with each row mapping field names to values.
   * @throws DatabaseException
   */
  public void addRows(List<Map<String, String>> data) throws DatabaseException
  {
    synchronized (this) { progressPercent = 0; }
    for (int i = 0; i < data.size(); i++)
    {
      addRow(data.get(i));
      synchronized (this) { progressPercent = 50 * i / data.size(); }
    }
  }

  public void commitRows() throws DatabaseException
  {
    try
    {
      con.setAutoCommit(false);
      insert.executeBatch();
      con.setAutoCommit(true);
    }
    catch (SQLException e)
    {
      throw new DatabaseException("Error while committing data");
    }
  }
   
  public void addRow(Map<String, String> row) throws DatabaseException
  {
    try
    {
      for (int i = 0; i < fields.size(); i++)
      {
        String value = fields.get(i);
        if (null == value)
          throw new DatabaseException("Missing value in row");
        insert.setString(i + 1, row.get(value));
      }
      insert.addBatch();
    }
    catch (SQLException e)
    {
      throw new DatabaseException("Error while adding row");
    }
  }
  
  private void notifyListenersIssued(String query)
  {
    if (null != listener)
    {
      queryStartTime = System.currentTimeMillis();
      listener.queryIssued(query);
    }
  }
  
  private void notifyListenersCompleted(String query)
  {
    if (null != listener)
    {
      long elapsed = System.currentTimeMillis() - queryStartTime;
      listener.queryCompleted(query, elapsed);
    }
  }
  
  private void notifyListenersFailed(String query)
  {
    if (null != listener)
    {
      listener.queryFailed(query);
    }    
  }
  
  public ResultSet select(String query) throws DatabaseException
  {
    ResultSet r = null;
    try
    {
      notifyListenersIssued(query);
      r = select.executeQuery(query);
      notifyListenersCompleted(query);
    }
    catch (SQLException e)
    {
      notifyListenersFailed(query);
      throw new DatabaseException("Error while executing query");
    }
    return r;
  }
  
  /**
   * Execute a query which returns a single column in a single row
   * @param SQL clause between SELECT and FROM
   * @return The first column in the first row of the result
   * @throws DatabaseException
   */
  public String selectSingle(String queryItem) throws DatabaseException
  {
    return selectSingle(queryItem, null);
  }
  
  /**
   * Execute a query which returns a single column in a single row
   * @param queryItem SQL clause between SELECT and FROM
   * @param whereClause SQL clause after WHERE
   * @return The first column in the first row of the result
   * @throws DatabaseException
   */  
  public String selectSingle(String queryItem, String whereClause) throws DatabaseException
  {
    String result = null;
    try
    {
      String query = String.format("SELECT %s FROM %s", queryItem, tableName);
      if (null != whereClause)
        query += " WHERE " + whereClause;
      ResultSet r = select(query);
      result = r.getString(1);
    }
    catch (SQLException e)
    {
      throw new DatabaseException("Error processing query result");
    }
    return result;
  }
  
  private Connection connect(String filename) throws DatabaseException
  {
    Connection con = null;
    try
    {
      Class.forName("org.sqlite.JDBC");
      con = DriverManager.getConnection("jdbc:sqlite:"+filename);
    }
    catch(ClassNotFoundException e)
    {
      throw new DatabaseException("SQLite JDBC driver class not found");
    }
    catch(SQLException e)
    {
      throw new DatabaseException("Could not connect to SQLite database " + filename);
    }   
    return con;
  }
  
  private void prepareInsertStatement(int nFields) throws DatabaseException
  {
    try
    {
      String qMarks = getCsvQuestionMarkList(nFields);
      insert = con.prepareStatement(
        String.format("INSERT INTO " + tableName + " VALUES (%s);", qMarks));
    }
    catch(SQLException e)
    {
      throw new DatabaseException("Error preparing insert statement");
    }
  }
  
  private void prepareSelectStatement() throws DatabaseException
  {
    try
    {
      select = con.createStatement();
    }
    catch (SQLException e)
    {
      throw new DatabaseException("Error preparing select statement");
    }    
  }
  
  public void close() throws DatabaseException
  {
    try
    {
      con.close();
    }
    catch(SQLException e)
    {
      throw new DatabaseException("Error closing database connection");
    }
  }
  
  private String getCsvFieldList(List<String> fields)
  {
    StringBuffer lst = new StringBuffer();
    if (fields.size() > 0)
      lst.append(fields.get(0));
    for (int i = 1; i < fields.size(); i++)
    {
      lst.append(",");
      lst.append(fields.get(i));
    }
    return lst.toString();
  }
  
  private String getCsvFieldAndTypeList(List<String> fields, List<String> types)
  {
    List<String> combined = new ArrayList<String>();
    for (int i = 0; i < fields.size(); i++)
      combined.add(String.format("%s %s", fields.get(i), types.get(i)));
    return getCsvFieldList(combined);
  }
  
  private String getCsvQuestionMarkList(int n)
  {
    ArrayList<String> lst = new ArrayList<String>();
    for (int i = 0; i < n; i++)
      lst.add("?");
    return getCsvFieldList(lst);
  }
  
  private void createTables(List<String> fields, List<String> types)
    throws DatabaseException
  {
    try
    {
      Statement stat = con.createStatement();
      stat.executeUpdate("DROP TABLE IF EXISTS " + tableName + ";");
      String fieldList = getCsvFieldAndTypeList(fields, types);
      String create = String.format("CREATE TABLE %s (%s);", tableName, fieldList);
      stat.executeUpdate(create);
    }
    catch(SQLException e)
    {
      throw new DatabaseException("Error creating tables");
    }
  }
  
  public void createIndex(String table, String field) throws DatabaseException
  {
    try
    {
      Statement stat = con.createStatement();
      String index = String.format("CREATE INDEX %s_idx ON %s (%s);", field, table, field);
      stat.executeUpdate(index);
    }
    catch(SQLException e)
    {
      throw new DatabaseException("Error creating index");
    }
  }
}
