package org.safecast.parse;

/**
  * Data.
  * This is the class to store the radiation and
  * positioning data collected from some device.
  */

class Data
{
  /* device data */
  String header;              // device header
  String id;                  // device id
  /* Radiation Data */
  String cpm;                 // Counts-per-minute
  String cp5s;                // Counts-per-5-seconds
  String totc;                // Total count
  String rnStatus;            // Radiation reading status, 1=valid, 0=invalid

  /* Positioning Data */
  String latitude;            // RMC and GGA (DDDMM.MM..)
  String northsouthindicator; // RMC and GGA
  String longitude;           // RMC and GGA (DDDMM.MM..)
  String eastwestindicator;   // RMC and GGA

  /* Time Data */
  String time;                // RMC and GGA - time, ISO8601 format, yyyy-MM-ddThh:mm:ssZ

  /* GPS position quality data */
  String gpsStatus;              // RMC - GPS fix status A=available V=void
  String satellites;          // GGA - # of satellites
  String dop;                 // GGA - horizontal dillution of position
  int quality;                // GGA - fix quality indicator
  final static String[] qualityDesc = { // quality description
    "0 - fix not available",
    "1 - GPS fix",
    "2 - Differential GPS fix",
    "3 = PPS fix",
    "4 = Real Time Kinematic",
    "5 = Float RTK",
    "6 = estimated (dead reckoning)",
    "7 = Manual input mode",
    "8 = Simulation mode"
  };

  /* Maybe for later use */
  String speedKnots;    // RMC - Speed over the ground in knots
  String altitude;      // GGA

  /* indicate data have started to arrive */
  boolean radDataReceived;
  boolean posDataReceived;
  boolean GGAReceived;
  boolean RMCReceived;

  /* constructor */
  Data()
  {
    // this should take care of all other data
    reset();
  }

  /* construct from a previously logged string */
  Data(String logString)
  {
    this();
    fromString(logString);
  }
  
  void reset()
  {
    // device data
    header = "";
    id = "000";
    // radiation data
    cpm = "";
    cp5s = "";
    totc = "";
    rnStatus = "V"; // default to 'Void', i.e. not available
    // position data
    latitude = "";
    northsouthindicator = "";
    longitude = "";
    eastwestindicator = "";
    // time data
    time = "1970-01-01T00:00:00Z";
    // GPS junk
    gpsStatus = "V"; // default to 'Void', i.e. not available
    satellites = "";
    dop = "";
    quality = 0;
    // more GPS junk
    speedKnots = "";
    altitude = "";

    // initialize flags
    radDataReceived = false;
    posDataReceived = false;
    GGAReceived = false;
    RMCReceived = false;
  }

  /* reconstruct entry from logged string */
  public void fromString(String data)
  {
    String items[] = data.split(",");
    time = items[0];
    cpm = items[1];
    setLatitude(Double.parseDouble(items[2]));
    northsouthindicator = items[3];
    setLongitude(Double.parseDouble(items[4]));
    eastwestindicator = items[5];
    quality = Integer.parseInt(items[6]);
    satellites = items[7];
    dop = items[8];
    altitude = items[9];
  }
  
  /* sets the latitude from a decimal degrees value */
  private void setLatitude(double latitude)
  {
    northsouthindicator = latitude < 0.0 ? "S" : "N";    
    latitude = Math.abs(latitude);
    int deg = (int)latitude;
    double min = (latitude - deg) * 60.0; 
    this.latitude = String.format("%03d", deg) + "."
             + String.format("%f", min);
  }

  /* sets the longitude from a decimal degrees value */
  private void setLongitude(double longitude)
  {
    eastwestindicator = longitude < 0.0 ? "W" : "E";    
    longitude = Math.abs(longitude);
    int deg = (int)longitude;
    double min = (longitude - deg) * 60.0; 
    this.longitude = String.format("%03d", deg) + "."
             + String.format("%f", min);
  }
  
  /* get a formatted entry for logging */
  public String toString()
  {
    String s = time + ","
             + (cpm) + ","
             + getLatitude() + "," + (northsouthindicator) + ","
             + getLongitude() + "," + (eastwestindicator) + ","
             + quality + "," + (satellites) + "," 
             + (dop) + "," + (altitude);

    if (!radDataReceived)
      s = s + " **Radiation data missing";

    if (!posDataReceived)
      s = s + " **GPS data missing";
             
    return s;
  }

  /* returns true when the Data is ready */
  public boolean ready()
  {
    return (posDataReceived && radDataReceived);
  }

  /* indicate we read the data and we are waiting for next one */
  public void requestNext()
  {
    radDataReceived = false;
    posDataReceived = false;
    GGAReceived = false;
    RMCReceived = false;
  }

  /* get a formatted entry for logging */
  public String getLogEntry()
  {
    String s = header + "," 
             + id + ","
             + time + ","
             + cpm + ","
             + cp5s + ","
             + totc + ","
             + rnStatus + ","
             + latitude + ","
             + northsouthindicator + ","
             + longitude + ","
             + eastwestindicator + ","
             + altitude + ","
             + gpsStatus + ","
             + dop + ","
             + quality;

    String chk = checksum(s);
    if (chk == null)
      chk = "";

    return "$" + s + "*" + chk;
  }

  /**
    * parseGPS.
    * Parse data from GPS.
    * return: 0 if nothing was read.
    */
  int parseData(String dataSentence) {
    System.out.println("received: " + dataSentence);
    // basic check
    if (dataSentence.length() == 0)
      return 0;

    // find '*' indicating start of checksum
    int starPos = dataSentence.indexOf("*");
    if (starPos == -1)
      return 0;

    // split data and checksum, drop leading '$'
    String data = dataSentence.substring(1,starPos);
    String chk = dataSentence.substring(starPos+1);
    //System.out.println(checksum(data,chk));

    //sets up an array and splits the gps info by comma
    String items[] = data.split(",");
    if (items[0].equals("GPGGA") && items.length >= 10)
    {  
      //looks for the GPGGA info
      if (checksum(data, chk)) getGGA(items);
    } else if (items[0].equals("GPRMC") && items.length >= 9) {
      // Looks for GPRMC info
      if (checksum(data,chk)) getRMC(items);
    } else if (items[0].equals("BGRDD") && items.length >= 0) {
      // get Radiation data from bGeigie
      if (checksum(data,chk)) getBGeigie(items);
    } else if (items[0].equals("BMRDD") && items.length >= 9) {
      // get Radiation and GPS data from Akiba's contraption (bGeigie mini)
      if (checksum(data,chk)) getBGeigieMini(items);
    } else {
      // it was something else
      return 0;
    }

    return 1;
  }

  /* read GGA from parsed GPS data */
  void getGGA(String[] gpsdata)
  {
    // only log when needed
    if (posDataReceived)
      return;

    // update position data
    setGPS2ISO8601Time(gpsdata[1]);
    latitude = gpsdata[2];
    northsouthindicator = gpsdata[3];
    longitude = gpsdata[4];
    eastwestindicator = gpsdata[5];
    quality = Integer.parseInt(gpsdata[6]);
    satellites = gpsdata[7];
    dop = gpsdata[8];
    altitude = gpsdata[9];

    // data arrived
    GGAReceived = true;
    if (RMCReceived)
      posDataReceived = true;
  }

  /* read RMC from parsed GPS data */
  void getRMC(String[] gpsdata)
  {
    // only log when needed
    if (posDataReceived)
      return;

    // update position data
    setGPS2ISO8601DateTime(gpsdata[9], gpsdata[1]);
    gpsStatus = gpsdata[2];
    latitude = gpsdata[3];
    northsouthindicator = gpsdata[4];
    longitude = gpsdata[5];
    eastwestindicator = gpsdata[6];
    speedKnots = gpsdata[7];

    // data arrived
    RMCReceived = true;
    if (GGAReceived)
      posDataReceived = true;
  }

  /* read Data from arduino connected Geiger counter */
  void getBGeigie(String[] rdtndata)
  {
    // update Radiation data
    header = rdtndata[0];
    id = rdtndata[1];
    cp5s = rdtndata[2];
    cpm = rdtndata[3];
    totc = rdtndata[4];
    rnStatus = rdtndata[5];

    // rad data arrived
    radDataReceived = true;
    // need gps data now
    GGAReceived = false;
    RMCReceived = false;
    posDataReceived = false;
  }

  /* read Radiation+GPS data from Akiba's ninja Geigie */
  void getBGeigieMini(String[] data)
  {
    // update all data
    header = data[0];
    id = data[1];
    time = data[2]; // ISO8601 yyyy-MM-ddThh:mm:ssZ
    cpm = data[3];
    cp5s = data[4];
    totc = data[5];
    rnStatus = data[6];
    latitude = data[7];
    northsouthindicator = data[8];
    longitude = data[9];
    eastwestindicator = data[10];
    altitude = data[11];
    gpsStatus = data[12];
    dop = data[13];
    quality = Integer.parseInt(data[14]);

    // data arrived
    radDataReceived = true;
    posDataReceived = true;
    GGAReceived = true;
    RMCReceived = true;
  }

  void setGPS2ISO8601Time(String t)
  {
    time = time.substring(0,11)
         + t.substring(0,2) + ":"         // hour
         + t.substring(2,4) + ":"         // minute
         + t.substring(4,6) + "Z";        // seconds
  }

  void setGPS2ISO8601DateTime(String d, String t)
  {
    time = "20" + d.substring(4,6) + "-"  // year
         + d.substring(2,4) + "-"         // month
         + d.substring(0,2) + "T"         // day
         + t.substring(0,2) + ":"         // hour
         + t.substring(2,4) + ":"         // minute
         + t.substring(4,6) + "Z";        // seconds
  }

  /* format Out - avoid to print 'null' when string is empty */
  String noNull(String s)
  {
    if (s == null)
      return "";
    else
      return s;
  }

  boolean checksum(String s, String rxk)
  {
    String chk = checksum(s);
    if (chk == null)
      return false;
    else
      return chk.toLowerCase().equals(rxk.toLowerCase());
  }

  /* return hex checksum of argument string */
  String checksum(String s)
  {
    // check length first
    if (s.length() == 0)
      return null;
    /* XOR all character after '$', compare to number 
       following '*' at the end of the string */
    char chk = s.charAt(0);
    for (int i=1 ; i < s.length() ; i++)
      chk ^= s.charAt(i);

    // convert to hex string
    String hexStr = Integer.toHexString(chk);
    if (chk < 16)
      hexStr = "0" + hexStr;

    return hexStr;
  }

  /* Now all get methods */
  /* device id */
  String getDevId()
  {
    return id;
  }
  /* CPM */
  int getCPM()
  {
    int c_p_m = -1;
    try { c_p_m = Integer.parseInt(cpm); }
    catch (NumberFormatException nfe) { }
    return c_p_m;
  }

  /* total dose */
  int getDose()
  {
    int dose = -1;
    try { dose = Integer.parseInt(totc); }
    catch (NumberFormatException nfe) { }
    return dose;
  }

  /* return validity of Geiger reading */
  boolean isGeigerValid()
  {
    if (rnStatus.equals("A"))
      return true;
    else if (rnStatus.equals("V"))
      return false;
    else
      return false;
  }

  /* get latitude data */
  double getLatitude()
  {
    if (latitude.equals(""))
      return 0.0;

    double deg, min;
    /* parse the string */
    try
    {
      deg = Double.parseDouble(latitude.substring(0,2));
      min = Double.parseDouble(latitude.substring(2));
    }
    catch (NumberFormatException nfe) { return 0; }

    /* compute degrees */
    double lat = deg + min/60.0;

    /* add sign to distinguish north and south */
    if (northsouthindicator.equals("S"))
      lat *= -1.0;

    return lat;
  }

  /* get longitude data */
  double getLongitude()
  {
    if (longitude.equals(""))
      return 0.0;

    double deg, min;
    try
    {
      deg = Double.parseDouble(longitude.substring(0,3));
      min = Double.parseDouble(longitude.substring(3));
    }
    catch (NumberFormatException nfe) { return 0; }
    
    /* compute degrees */
    double lon = deg + min/60.0;

    /* add sign to distinguish east and west */
    if (eastwestindicator.equals("W"))
      lon *= -1.0;

    return lon;
  }

  /* get altitude */
  double getAltitude()
  {
    double alt = 0.0;
    try
    {
      alt = Double.parseDouble(altitude);
    }
    catch (NumberFormatException nfe) { return 0.0; }

    return alt;
  }

  /* get date */
  String getDate() { return time.substring(0,10); }

  String getTime() { return time.substring(11,19); }

  /* GPS position quality data */
  boolean isFixAvailable()
  {
    if (gpsStatus.equals("A"))
      return true;
    else if (gpsStatus.equals("V"))
      return false;
    else
      return false;
  }

  /* number of satellites */
  int getSatellites()
  {
    int nSat = -1;
    try { nSat = Integer.parseInt(satellites); }
    catch (NumberFormatException nfe) { }
    return nSat;
  }

  /* Horizontal dillution of precision */
  int getDOP()
  {
    int d_o_p = -1;
    try { d_o_p = Integer.parseInt(dop); }
    catch (NumberFormatException nfe) { }
    return d_o_p;
  }

  /* fix quality description */
  String getFixQuality() { return qualityDesc[quality]; }
}
