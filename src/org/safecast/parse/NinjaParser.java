package org.safecast.parse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JOptionPane; 

import org.safecast.db.DriveDatabase;

public class NinjaParser extends Parser
{

  public static final String [] FIELDS =
  {
    "dev_tag",
    "dev_id",
    "timestamp",
    "cpm",
    "cp5s",
    "count_total",
    "cpm_valid",
    "lat",
    "n_s",
    "lon",
    "e_w",
    "alt",
    "gps_valid",
    "gps_dillution",
    "gps_quality"    
  };
   
  public static final String FIELD_SEPARATOR = ",";
  public static final String COMMENT_MARKER = "#";  
  public static final String NO_DATA = " "; 

  private long bytesTotal = 0;
  private long bytesParsed = 0;
  private long lineNumbers = 1;
  private final Set<String> extractFields;
  
  public NinjaParser()
  {
    bytesTotal = 0;
    bytesParsed = 0;
    extractFields = new HashSet<String>();
    for (int i = 0; i < DriveDatabase.FIELDS.length; i++)
    {
      extractFields.add(DriveDatabase.FIELDS[i]);
    }
  }

  public synchronized long getBytesParsed()
  {
    return bytesParsed;
  }
  
  public synchronized long getBytesTotal()
  {
    return bytesTotal;
  }
  
  public synchronized int getProgressPercent()
  {
    return 0 == bytesTotal ? 0 : (int)(bytesParsed * 100 / bytesTotal);
  }
  
  public ParsedData parseFile(String filename) throws IOException,
      ParseException
  {
    BufferedReader reader = new BufferedReader(new FileReader(filename));
    List<String> columns = Arrays.asList(FIELDS);
    String line = reader.readLine();
    List<Map<String, String>> data = new ArrayList<Map<String, String>>();
    synchronized (this)
    {
      bytesParsed = 0;
      bytesTotal = (new File(filename)).length();
    }
    while (null != line)
    {
      if (!line.startsWith(COMMENT_MARKER))
      {
        data.add(parseRow(columns, line));
        /** Added for counting lines rob@yr-design.biz)
         * 
         */
        lineNumbers++;
      }
      synchronized (this) { bytesParsed += line.length(); }
      line = reader.readLine();      
    }
    return new ParsedData(columns, data);
  }
  
  private Map<String, String> parseRow(List<String> columns, String row)
    throws IOException, ParseException
  {
    String[] fields = row.split(FIELD_SEPARATOR);
    
    if (fields.length < columns.size())
    /**  Show error if corrupt data rob@yr-design.biz
     * 
     */
    	JOptionPane.showMessageDialog(null,"Error in line number :" + lineNumbers, "Data Corrupt", JOptionPane.ERROR_MESSAGE);

    
	    Map<String, String> rowMap = new HashMap<String, String>();
	    createRowMap(rowMap, columns, fields);
	    fixLatLon(rowMap);
	    
    return rowMap;
  }
  
  /**
   * Convert from GPS format (DDDMM.MMMM..) to decimal degrees
   * @param rowMap the map containing the GPS formatted values
   */
  private void fixLatLon(Map<String, String> rowMap)
  {   
    rowMap.put("lat", convertLatLon(rowMap.get("lat"), 2));
    rowMap.put("lon", convertLatLon(rowMap.get("lon"), 3));
  }
  /**
   * Convert from GPS format (DDDMM.MMMM..) to decimal degrees
   * @param gpsFormat the GPS formatted string
   * @param degChars number of characters representing degrees
   */
  private String convertLatLon(String gpsFormat, int degChars)
  {
    String deg = gpsFormat.substring(0, degChars);
    String min = gpsFormat.substring(degChars);
    Double decimal = Double.valueOf(deg);
    decimal += (Double.valueOf(min) / 60.0);
    return String.format("%.6f", decimal);    
  }
  
  private void createRowMap(Map<String, String> rowMap, List<String> columns, String[] fields)
  {
    int i = 0;
    for (String col : columns)
    {
      if (extractFields.contains(col))
      {
        rowMap.put(col, fields[i]);
      }
      else if ("n_s".equals(col) && "s".equalsIgnoreCase(fields[i]))
      {
        rowMap.put("lat", "-" + rowMap.get("lat"));
      }
      else if ("e_w".equals(col) && "w".equalsIgnoreCase(fields[i]))
      {
        rowMap.put("lon", "-" + rowMap.get("lat"));
      }      
      i++;
    }
  }

}
