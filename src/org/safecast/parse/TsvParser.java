package org.safecast.parse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TsvParser extends Parser
{
  public static final String FIELD_SEPARATOR = "\t";
  public static final String COMMENT_MARKER = "#";  
  
  private long bytesTotal = 0;
  private long bytesParsed = 0;
   
  public TsvParser()
  {
    bytesTotal = 0;
    bytesParsed = 0;
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
    return (int)(bytesParsed * 100 / bytesTotal);
  }
  
  public ParsedData parseFile(String filename)
    throws IOException, ParseException
  {
    BufferedReader reader = new BufferedReader(new FileReader(filename));
    List<String> columns = parseHeader(reader.readLine());
    String line = reader.readLine();
    List<Map<String, String>> data = new ArrayList<Map<String, String>>();
    synchronized (this)
    {
      bytesParsed = 0;
      bytesTotal = (new File(filename)).length();
    }
    while (null != line)
    {
      data.add(parseRow(columns, line));
      synchronized (this) { bytesParsed += line.length(); }
      line = reader.readLine();
    }
    return new ParsedData(columns, data);
  }
  
  public void writeFile(String filename, ParsedData data)
    throws IOException
  {
    BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
    writer.write(outputHeader(data.columns));
    writer.newLine();
    for (int i = 0; i < data.rows.size(); i++)
    {
      writer.write(outputRow(data.columns, data.rows.get(i)));
      writer.newLine();
    }
  }
  
  private List<String> parseHeader(String header)
  {
    List<String> column = new ArrayList<String>();
    
    if (header.startsWith(COMMENT_MARKER))
      header = header.substring(COMMENT_MARKER.length());
    
    String[] fields = header.split(FIELD_SEPARATOR);
    for (int i = 0; i < fields.length; i++)
      column.add(fields[i]);
    
    return column;
  }
  
  private Map<String, String> parseRow(List<String> columns, String row)
    throws ParseException
  {
    String[] fields = row.split(FIELD_SEPARATOR);
    if (fields.length < columns.size())
      throw new ParseException("Not enough values in row");

    Map<String, String> value = new HashMap<String, String>();
    
    for (int i = 0; i < columns.size(); i++)
      value.put(columns.get(i), fields[i]);
    
    return value;
  }
  
  private String outputTsvRow(List<String> values)
  {
    StringBuffer buf = new StringBuffer();
    if (values.size() > 0)
      buf.append(values.get(0));
    for (int i = 1; i < values.size(); i++)
    {
      buf.append(FIELD_SEPARATOR);
      buf.append(values.get(i));
    }
    return buf.toString();    
  }
  
  public String outputRow(List<String> fields, Map<String, String> values)
  {
    List<String> output = new LinkedList<String>();
    for (int i = 0; i < fields.size(); i++)
    {
      output.add(values.get(fields.get(i)));
    }
    return outputTsvRow(output);
  }
  
  public String outputHeader(List<String> fields)
  {
    return COMMENT_MARKER + outputTsvRow(fields);
  }
  
}
