package org.safecast.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.safecast.data.GeoCoord;
import org.safecast.data.Histogram;
import org.safecast.data.Route;
import org.safecast.data.Statistics;
import org.safecast.data.TimeSeries;
import org.safecast.parse.ISO8601DateParser;
import org.safecast.ui.qa.QaGui;

public class DriveDatabase extends SqliteDatabase
{
  public static final String TABLE_NAME = "drive";
  
  public static final String[] FIELDS =
  {
    "uid",
    "timestamp",
    "cpm",
    "cpm_valid",
    "lat",
    "lon",
    "alt",
    "gps_valid",
    "gps_dillution",
    "gps_quality"
  };
  
  public static final String TYPES [] =
  {
    "INTEGER PRIMARY KEY", // this will auto-increment in sqlite3
    "TEXT",
    "INTEGER",
    "TEXT",
    "REAL",
    "REAL",
    "REAL",
    "TEXT",
    "REAL",
    "INTEGER"
  };

  public static final String VALID_CLAUSE =
    "gps_valid=='A' AND cpm_valid=='A'";
  
  public static final String FROM_VALID =
    String.format("FROM %s WHERE %s", TABLE_NAME, VALID_CLAUSE);

  public static final int TIME_SERIES_MAX_POINTS = 100;
  
  public DriveDatabase() throws DatabaseException
  {
    //super("test.db", TABLE_NAME, Arrays.asList(FIELDS), Arrays.asList(TYPES));
    super(TABLE_NAME, Arrays.asList(FIELDS), Arrays.asList(TYPES));
    createIndices();
  }
  
  private void createIndices() throws DatabaseException
  {
    createIndex(TABLE_NAME, "timestamp");
    createIndex(TABLE_NAME, "cpm");
  }
  
  public void addRow(Map<String, String> row) throws DatabaseException
  {
    try
    {
      for (int i = 0; i < FIELDS.length; i++)
      {
        String value = FIELDS[i];
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

  public GeoCoord getNorthWestExtent() throws DatabaseException
  {
    GeoCoord coord = null;
    try
    {
      String query = String.format("SELECT MAX(lat) AS n, MIN(lon) AS w %s", FROM_VALID);
      ResultSet rs = select(query);
      coord = new GeoCoord(rs.getDouble("n"), rs.getDouble("w"));
    }
    catch(SQLException e)
    {
      throw new DatabaseException("Error processing query result");
    }
    return coord;
  }
  
  private int getRouteStep(int maxPoints) throws DatabaseException
  {
    int n = getNumberOfPoints();
    int step = 1;
    if (n > maxPoints)
    {
      step = 1 + (n / maxPoints);
    }
    return step;
  }
  
  public Route getRoute(int maxPoints) throws DatabaseException
  {
    Route route = null;
    int step = getRouteStep(maxPoints);
    try
    {
      String query = String.format("SELECT lat, lon %s ORDER BY timestamp", FROM_VALID);
      ResultSet rs = select(query);
      route = new Route();
      int i = 0;
      while (rs.next())
      {
        if (0 == (i++ % step))
        {
          GeoCoord c = new GeoCoord(rs.getDouble("lat"), rs.getDouble("lon"));
          route.addPoint(c);
        }
      }      
    }
    catch (SQLException e)
    {
      throw new DatabaseException("Error processing query result");
    }
    return route;
  }
  
  public Histogram getCpmHistogram(int bucketSize) throws DatabaseException
  {
    Histogram hist = null;
    try
    {
      String query = String.format("SELECT %d*(cpm/%d) AS bucket, COUNT(*) AS frequency FROM %s GROUP BY bucket HAVING %s", bucketSize, bucketSize, TABLE_NAME, VALID_CLAUSE);
      ResultSet rs = select(query);
      hist = new Histogram();
      while (rs.next())
      {
        hist.set(rs.getInt("bucket"), rs.getInt("frequency"));
      }
    }
    catch (SQLException e)
    {
      throw new DatabaseException("Error processing query result");
    }
    return hist;
  }

  public TimeSeries getCpmTimeSeries() throws DatabaseException
  {
    TimeSeries series = null;
    int m = getNumberOfPoints() / TIME_SERIES_MAX_POINTS;
    m = m < 1 ? 1 : m;
    try
    {
      String query = String.format("SELECT timestamp, cpm %s AND (uid %% %d) == 0", FROM_VALID, m);
      ResultSet rs = select(query);
      series = new TimeSeries();
      while (rs.next())
      {
        String rawDate = rs.getString("timestamp");
        Date date = ISO8601DateParser.parse(rawDate);
        series.addPoint(date, rs.getInt("cpm"));
      };
    }
    catch (SQLException e)
    {
      throw new DatabaseException("Error processing query result");
    }
    catch (ParseException e)
    {
      throw new DatabaseException("Invalid timestamp in database");
    }
    return series;
  }
  
  public int getNumberOfPoints() throws DatabaseException
  {
    int points = 0;
    try
    {
      String query = String.format("SELECT COUNT(*) AS points %s", FROM_VALID);
      ResultSet rs = select(query);
      points = rs.getInt("points");
    }
    catch (SQLException e)
    {
      throw new DatabaseException("Error processing query result");
    }
    return points;
  }
  
  public Calendar[] getStartAndFinish() throws DatabaseException
  {
    Calendar[] result = new Calendar[2];    
    try
    {
      String query = String.format("SELECT MIN(timestamp) AS start, MAX(timestamp) AS finish %s", FROM_VALID);
      ResultSet rs = select(query);
      rs.next();
      String start = rs.getString("start"); 
      String finish = rs.getString("finish");
      result[0] = javax.xml.bind.DatatypeConverter.parseDateTime(start);
      result[1] = javax.xml.bind.DatatypeConverter.parseDateTime(finish);      
    }
    catch (SQLException e)
    {
      throw new DatabaseException("Error processing query result");
    }
    return result;
  }

  private static final String STATS_NAME [] =
  {
    QaGui.STRINGS.getString("number_of_points"),
    QaGui.STRINGS.getString("start_time"),
    QaGui.STRINGS.getString("finish_time"),
    QaGui.STRINGS.getString("maximum_reading_cpm"),
    QaGui.STRINGS.getString("average_reading_cpm"),
    QaGui.STRINGS.getString("minimum_reading_cpm"),
    QaGui.STRINGS.getString("northernmost_latitude"),
    QaGui.STRINGS.getString("southernmost_latitude"),
    QaGui.STRINGS.getString("westernmost_latitude"),
    QaGui.STRINGS.getString("easternmost_latitude"),
    QaGui.STRINGS.getString("average_altitude_m"),
    QaGui.STRINGS.getString("lowest_altitude_m"),
    QaGui.STRINGS.getString("highest_latitude_m")
  };
  
  private static final String STATS_QUERY [] =
  {
    "COUNT(*)",
    "MIN(timestamp)",
    "MAX(timestamp)",
    "MAX(cpm)",
    "AVG(cpm)",
    "MIN(cpm)",
    "MAX(lon)",
    "MIN(lon)",
    "MIN(lat)",
    "MAX(lat)",
    "AVG(alt)",
    "MIN(alt)",
    "MAX(alt)",
  };
  
  
  public Statistics getStatistics() throws DatabaseException
  {
    Statistics stats = new Statistics();
    for (int i = 0; i < STATS_QUERY.length; i++)
    {
        String value = selectSingle(STATS_QUERY[i], VALID_CLAUSE);
        stats.setValue(STATS_NAME[i], value);
    }
    return stats;
  }
  
}
