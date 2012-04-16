package org.safecast.parse;

import java.util.List;
import java.util.Map;

public class ParsedData
{
  public ParsedData(List<String> columns, List<Map<String, String>> rows)
  {
    this.columns = columns;
    this.rows = rows;
  }
  
  public List<String> columns;
  public List<Map<String, String>> rows;
}
