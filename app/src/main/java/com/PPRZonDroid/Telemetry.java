/*
 * Copyright (C) 2014 Savas Sen - ENAC UAV Lab
 *
 * This file is part of paparazzi..
 *
 * paparazzi is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * paparazzi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with paparazzi; see the file COPYING.  If not, write to
 * the Free Software Foundation, 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 */

package com.PPRZonDroid;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Telemetry {

  boolean DEBUG;
  public String SendToTcp = null;
  public int SelAcInd = -1;  //

  //For now these values will be static
  public int MaxNumbOfAC = 25;   //Max waypoint numb
  public int MaxNumbOfWp = 25;    //Max waypoint numb
  public int MaxNumbOfBl = 25;    //Max block number
  public float GraphicsScaleFactor = 1;  //Will be used to resize graphics

  //Visual change flags
  public boolean AttitudeChanged = false;
  public boolean NewMarkerAdded = false;
  public boolean MarkerModified = false;
  public boolean ViewChanged = false;  //Every function -willing to change UI needs to raise this flag
  public boolean BatteryChanged = false;
  public boolean NewAcAdded;
  public boolean BlockChanged = false;
  public boolean ApStatusChanged = false;

  public Bitmap AcPfd;     //Pfd bitmap
  /*
  Comm variables
   */
  public String ServerIp;
  public int UdpListenPort;
  public TCPClient mTcpClient;
  public int ServerTcpPort;
  public uiGraphics muiGraphics = new uiGraphics();
  public double AirSpeedMinSetting;    //Speed
  public double AIRSPEED_MIN = 5;
  public boolean AirspeedWarning = false;
  public AirCraft[] AircraftData;
  int IndexEnd;         //Biggest index of AircraftData[]
  private DatagramPacket packet;
  private String String2parse;
  private String String2parse_buf = "";
  private DatagramSocket socket = null;



  /**
   * Prepares tcp connection
   */
  public void tcp_connection() {
    mTcpClient = new TCPClient();
    mTcpClient.DEBUG= DEBUG;

    mTcpClient.SERVERIP = ServerIp;
    mTcpClient.SERVERPORT = ServerTcpPort;
  }

  /**
   * Prepare Aircraft class to prevent null point exceptions at app starts
   */
  public void prepare_class() {
    //Initial creation
    AircraftData = new AirCraft[MaxNumbOfAC];

    //Create & recycle bitmap to prevent dalvik allocating too much ram for it..
    Bitmap.Config conf = Bitmap.Config.ARGB_4444;
    AcPfd = Bitmap.createBitmap((int)(240*GraphicsScaleFactor), (int)(140*GraphicsScaleFactor), conf);
  }

  /**
   * Setup udp listen port
   */
  public void setup_udp() {
    try {
      socket = new DatagramSocket(UdpListenPort);
      socket.setSoTimeout(150); //This is needed to prevent udp read lock
    } catch (SocketException e) {
      e.printStackTrace();
      if (DEBUG) Log.d("PPRZ_exception", "Udp SocketException");
    }
    byte[] buf = new byte[1024];
    packet = new DatagramPacket(buf, buf.length);

  }

  public void read_udp_data() {

    try {

      socket.receive(packet);

      //Log.d("PPRZ_info", "inp2");

      //String2parse= (String) (packet.getData(), 0, packet.getLength());
      String2parse = string_from_datagrampacket(packet);
      //Log.d("PPRZ_info", "inp3");
      if ((String2parse != null) && (!String2parse.equals(String2parse_buf))) {
        String2parse_buf = String2parse;
        parse_udp_string(String2parse);
      }


    } catch (Exception e) {
        //ignore java.net.SocketTimeoutException
        //if (DEBUG) Log.d("PPRZ_exception", "Error#3 .. Udp Package Read Error:" + e.toString());
    }

  }

  /**
  * Converts a given datagram packet's contents to a String.
  */
  static String string_from_datagrampacket(DatagramPacket packet) {

     return new String(packet.getData(), 0, packet.getLength());
  }

  //Draw pfd for selected aircraft
  private void draw_pfd(int AcInd) {

    muiGraphics.create_pfd2(AcPfd, Double.parseDouble(AircraftData[SelAcInd].Roll), Double.parseDouble(AircraftData[SelAcInd].Pitch), Double.parseDouble(AircraftData[SelAcInd].Heading), AircraftData[SelAcInd].Altitude, AircraftData[SelAcInd].Battery, AircraftData[SelAcInd].GpsMode,AircraftData[SelAcInd].AC_DlAlt, GraphicsScaleFactor);

  }

  /**
   * Method to parse telemetry string.
   *
   * @param LastTelemetryString Input string to be parsed.
   * @return void
   */
  public void parse_udp_string(String LastTelemetryString) {


    //Parse AP_STATUS
    if (LastTelemetryString.matches("(^ground AP_STATUS .*)")) {

      String[] ParsedData = LastTelemetryString.split(" ");
      int AcIndex = get_indexof_ac(Integer.parseInt(ParsedData[2]));

      if (AcIndex >= 0) {

        AircraftData[AcIndex].ApMode = ParsedData[3];
        AircraftData[AcIndex].GpsMode = ParsedData[7];
        AircraftData[AcIndex].StateFilterMode = ParsedData[10];
        Long FlightTime = Long.parseLong(ParsedData[9]);
        Long Hours, Minutes;

        Hours = TimeUnit.SECONDS.toHours(FlightTime);
        FlightTime = FlightTime - TimeUnit.HOURS.toSeconds(Hours);
        Minutes = TimeUnit.SECONDS.toMinutes(FlightTime);
        FlightTime = FlightTime - TimeUnit.MINUTES.toSeconds(Minutes);

        AircraftData[AcIndex].FlightTime = Long.toString(Hours) + ":" + Long.toString(Minutes) + ":" + Long.toString(FlightTime);

        AircraftData[AcIndex].ApStatusChanged = true;

        if (AircraftData[AcIndex].AC_Enabled && AcIndex == SelAcInd) {
          ViewChanged = true;
        }
        return;
      } else {
        if (DEBUG) Log.d("PPRZ_info", "NAV_STATUS can't be parsed!");
        return;
      }
    }//END OF AP_STATUS


    //Parse Nav_Status
    if (LastTelemetryString.matches("(^ground NAV_STATUS .*)")) {

      String[] ParsedData = LastTelemetryString.split(" ");
      int AcIndex = get_indexof_ac(Integer.parseInt(ParsedData[2]));
      int BlockId = Integer.parseInt(ParsedData[3]);

      if (AcIndex >= 0) {

        AircraftData[AcIndex].AC_Carrot_Position = new LatLng(Double.parseDouble(ParsedData[7]), Double.parseDouble(ParsedData[8]));

        if (AircraftData[AcIndex].AC_Enabled && AcIndex == SelAcInd && BlockId != AircraftData[AcIndex].SelectedBlock) {
          //Block changed
          AircraftData[AcIndex].SelectedBlock = BlockId;


          BlockChanged = true;
          ViewChanged = true;
          return;
        }
        ViewChanged = true;
        AircraftData[AcIndex].SelectedBlock = BlockId;
        return;
      } else {
        if (DEBUG) Log.d("PPRZ_info", "NAV_STATUS can't be parsed!");
        return;
      }
    }//END OF NAV_STATUS


    //Parse ENGINE_STATUS messages
    if (LastTelemetryString.matches("(^ground ENGINE_STATUS .*)")) {
      String[] ParsedData = LastTelemetryString.split(" ");


      int AcIndex = get_indexof_ac(Integer.parseInt(ParsedData[2]));
      check_ac_datas(AcIndex);

      if (AcIndex >= 0) {

        AircraftData[AcIndex].Throttle = ParsedData[3];

        String bat = ParsedData[7].substring(0, (ParsedData[7].indexOf(".") + 2));

        //If battery is changed this will impact ui
        if (!(bat.equals(AircraftData[AcIndex].Battery))) {
            if (DEBUG) Log.d("PPRZ_info", "Old Battery=" + AircraftData[AcIndex].Battery + " New Battery:" + bat);
          AircraftData[AcIndex].Battery = bat;
          BatteryChanged = true;
          ViewChanged = true;
          //check_marker_options(AcIndex); //Check if AC is ready to be shown on ui
        }

        AircraftData[AcIndex].EngineStatusChanged = true;
        if (AircraftData[AcIndex].AC_Enabled) {
          ViewChanged = true;
        }
        return;
      } else {
        if (DEBUG) Log.d("PPRZ_info", "ENGINE_STATUS can't be parsed!");
        return;
      }
    }//END OF ENGINE_STATUS


    //Parse FLIGHT_PARAM messages
    if (LastTelemetryString.matches("(^ground FLIGHT_PARAM .*)")) {

      String[] ParsedData = LastTelemetryString.split(" ");
      int AcIndex = get_indexof_ac(Integer.parseInt(ParsedData[2]));
      if (AcIndex >= 0) {

        check_ac_datas(AcIndex);

        AircraftData[AcIndex].Roll = ParsedData[3];
        AircraftData[AcIndex].Pitch = ParsedData[4];
        AircraftData[AcIndex].Heading = ParsedData[5];
        AircraftData[AcIndex].Position = new LatLng(Double.parseDouble(ParsedData[6]), Double.parseDouble(ParsedData[7]));
        AircraftData[AcIndex].Speed = ParsedData[8].substring(0, (ParsedData[8].indexOf(".") + 2));
        AircraftData[AcIndex].Altitude = ParsedData[10].substring(0, ParsedData[10].indexOf("."));
        AircraftData[AcIndex].AGL = ParsedData[12].substring(0, ParsedData[12].indexOf("."));

        if (!AircraftData[AcIndex].Altitude.equals(AircraftData[AcIndex].AGL)) {
            AircraftData[AcIndex].Altitude = AircraftData[AcIndex].Altitude + " m (AGL:" + AircraftData[AcIndex].AGL + ")";
        }
          else
        {
            AircraftData[AcIndex].Altitude = AircraftData[AcIndex].Altitude + " m";
        }

        String BufAirspeed= ParsedData[15].substring(0, ParsedData[15].indexOf(".") + 1);

        if ( !BufAirspeed.equals(AircraftData[AcIndex].AirSpeed) && (Double.parseDouble(BufAirspeed))>=0 ) {
          AircraftData[AcIndex].AirSpeed= ParsedData[15].substring(0, ParsedData[15].indexOf(".") + 1);
          AircraftData[AcIndex].AirspeedEnabled = true;
          AircraftData[AcIndex].AirspeedChanged = true;
          if (DEBUG) Log.d("PPRZ_info", "Airspeed Enabled.");

        }
        //Add position to queue (for map lines)

        if (AircraftData[AcIndex].AC_Path.size() >= 150) {
          AircraftData[AcIndex].AC_Path.remove(0);
        }
          AircraftData[AcIndex].AC_Path.add(AircraftData[AcIndex].Position);

        if (AircraftData[AcIndex].AC_Enabled) {
          AircraftData[AcIndex].AC_Position_Changed = true;
          //TODO all ui flags need to check this
          if (AcIndex == SelAcInd) {
            ViewChanged = true;
            AttitudeChanged = true;
            AircraftData[AcIndex].Altitude_Changed = true;
            draw_pfd(AcIndex);
          }
        }
        return;
      } else {
        if (DEBUG) Log.d("PPRZ_info", "ENGINE_STATUS can't be parsed!");
        return;
      }
    }//END OF FLIGHT_PARAM


    //PARSE WAYPOINT_MOVED MESSAGES
    if (LastTelemetryString.matches("(^ground WAYPOINT_MOVED .*)")) {

      String[] ParsedData = LastTelemetryString.split(" ");
      //Get AC  index
      int AcIndex = get_indexof_ac(Integer.parseInt(ParsedData[2]));
      check_ac_datas(AcIndex);

      if (AcIndex >= 0) {

        int WpInd = Integer.parseInt(ParsedData[3]);
        check_wpind_of_ac(AcIndex, WpInd);

        //No need to check marker altitude (no differance in ui)
        AircraftData[AcIndex].AC_Markers[WpInd].WpAltitude = ParsedData[6];

        //We need to check latlong variables if they are different then they should be shown in ui;
        LatLng LatLngBuf = new LatLng(Double.parseDouble(ParsedData[4]), Double.parseDouble(ParsedData[5]));

        //if incoming latlong is different then before
        if (!(LatLngBuf.equals(AircraftData[AcIndex].AC_Markers[WpInd].WpPosition))) {
          if (DEBUG) Log.d("PPRZ_info", "Marker changed  for Ac id: " + AcIndex + " wpind:" + WpInd);
          AircraftData[AcIndex].AC_Markers[WpInd].WpPosition = LatLngBuf;
          AircraftData[AcIndex].AC_Markers[WpInd].MarkerModified = true;
          AircraftData[AcIndex].MarkerModified = true;
          MarkerModified = true;
          ViewChanged = true;
        }

        //If marker haven't been added then raise the flag to add it in the ui
        if ((AircraftData[AcIndex].MarkersEnabled) && (AircraftData[AcIndex].AC_Markers[WpInd].WpMarker == null)) {
          NewMarkerAdded = true;
          AircraftData[AcIndex].NewMarkerAdded = true;
          ViewChanged = true;
        }
        return;
      } else {
        if (DEBUG) Log.d("PPRZ_info", "WAYPOINT_MOVED can't be parsed!");
        return;
      }


    }//END OF  WAYPOINT_MOVED


      //PARSE DL_VALUES MESSAGES
      if (LastTelemetryString.matches("(^ground DL_VALUES .*)")) {

          String[] ParsedData = LastTelemetryString.split(" ");
          //Get AC  index
          int AcIndex = get_indexof_ac(Integer.parseInt(ParsedData[2]));
          check_ac_datas(AcIndex);

          if (AcIndex >= 0) {

              if (AircraftData[AcIndex].AC_AltID>0) {

                  String[] DlData = ParsedData[3].split(",");

                  if ( (DlData.length > AircraftData[AcIndex].AC_AltID) && (!"42".equals(DlData[AircraftData[AcIndex].AC_AltID]))  ) {
                      //Log.d("PPRZ_info", "DLData Length >>" + DlData.length);
                      AircraftData[AcIndex].AC_DlAlt= DlData[AircraftData[AcIndex].AC_AltID];
                      AircraftData[AcIndex].AC_DlAlt= AircraftData[AcIndex].AC_DlAlt.substring(0, AircraftData[AcIndex].AC_DlAlt.indexOf((".")));
                      //ParsedData[7].substring(0, (ParsedData[7].indexOf(".") + 2));
                  }

              }

              return;
          } else {
              if (DEBUG) Log.d("PPRZ_info", "DL_VALUES can't be parsed!");
              return;
          }


      }//END OF DL_VALUES


  } //End of parse_udp_string

  /**
   * Method to parse tcp strings coming from server app
   *
   * @param LastTelemetryString
   */
  public void parse_tcp_string(String LastTelemetryString) {

      Log.d("PPRZ_info", "Incoming str!" + LastTelemetryString);
    //Parse app server data
    int AcIndex;

    //Parse AC data (name, color ..)
    if (LastTelemetryString.matches("(^AppServer ACd .*)")) {
      String[] ParsedData = LastTelemetryString.split(" ");
      //Get AC  index
      AcIndex = get_indexof_ac(Integer.parseInt(ParsedData[2]));

      if (AcIndex >= 0) {
        //Set AC_Enabled flag;

        AircraftData[AcIndex].isVisible = true;
        AircraftData[AcIndex].AC_Type = ParsedData[4];
        AircraftData[AcIndex].AC_Name = ParsedData[3];
        AircraftData[AcIndex].AC_Color = ParsedData[5];
        AircraftData[AcIndex].AC_LaunchID = ParsedData[6];
        AircraftData[AcIndex].AC_KillID = ParsedData[7];
        AircraftData[AcIndex].AC_AltID = Integer.parseInt(ParsedData[8]);

        AircraftData[AcIndex].AC_Logo = muiGraphics.create_ac_icon(AircraftData[AcIndex].AC_Type, AircraftData[AcIndex].AC_Color, GraphicsScaleFactor, (AcIndex == SelAcInd));
        AircraftData[AcIndex].AC_Carrot_Logo = muiGraphics.create_ac_carrot(AircraftData[AcIndex].AC_Color, GraphicsScaleFactor);

        if (DEBUG) Log.d("PPRZ_info", "AC Data received. Ind: " + AcIndex + " Name" + AircraftData[AcIndex].AC_Name + " Color" + AircraftData[AcIndex].AC_Color);
        return;
      }
      if (DEBUG) Log.d("PPRZ_info", "AppServer ACd can't be parsed!");
      return;
    }

    //Parse waypoint data
    if (LastTelemetryString.matches("(^AppServer WPs .*)")) {
      String[] ParsedData = LastTelemetryString.split(" ");

      AcIndex = get_indexof_ac(Integer.parseInt((ParsedData[2])));

      if (AcIndex >= 0) {

        if (null == AircraftData[AcIndex].AC_Color) return;

        //Re split items for name and id values
        for (int i = 3; i < ParsedData.length; i++) {
          String[] ParsedWpData = ParsedData[i].split(",");
          check_wpind_of_ac(AcIndex, Integer.parseInt(ParsedWpData[0]));
          AircraftData[AcIndex].AC_Markers[Integer.parseInt(ParsedWpData[0])].WpName = ParsedWpData[1];
          AircraftData[AcIndex].AC_Markers[Integer.parseInt(ParsedWpData[0])].WpMarkerIcon = muiGraphics.create_marker_icon(AircraftData[AcIndex].AC_Color, ParsedWpData[1], GraphicsScaleFactor);

          if (DEBUG) Log.d("PPRZ_info", "WP parsed for ac= " + AcIndex + " marker ind=" + Integer.parseInt(ParsedWpData[0]));

        }
        AircraftData[AcIndex].MarkersEnabled = true;
      }
      return;
    }

    //Parse block data

    if (LastTelemetryString.matches("(^AppServer BLs .*)")) {
      String[] ParsedData = LastTelemetryString.split(" ");

      //First crop string to get ACId
      AcIndex = get_indexof_ac(Integer.parseInt((ParsedData[2])));

      //Get the part after ACId (14 is 'AppServer BLs ' lenght plus space after ACId
      String mystr = LastTelemetryString.substring((14 + ParsedData[2].length()));

      //For every piece lets crop the string again to get block names
      if (AcIndex >= 0) {
        //Re split items for name and id values
        String[] ParsedBlData = mystr.split("(</n>)");
        for (int i = 1; (i < ParsedBlData.length); i++) {
          //ParsedBlData[0] is empty
          check_blockind_of_ac(AcIndex, (i - 1));
          AircraftData[AcIndex].AC_Blocks[(i - 1)].BlName = ParsedBlData[i];

        }
        AircraftData[AcIndex].BlocksEnabled = true;
        AircraftData[AcIndex].BlockCount = ParsedBlData.length - 1;
      }
      return;
    }
    //This will be handy in future. (Sending IVY messages with TCP)
    parse_udp_string(LastTelemetryString);
  } //End of parse_tcp_string

  //Check all needed ac datas are here to show on ui
  private boolean check_ac_datas(int IndexOfAc) {
    //TODO this function needs to be simplified

    //This function should be called to determine whether ac has some missing
    //paremeters to shown in ui.

    //Check if these checks are made before;
    if (AircraftData[IndexOfAc].AC_Enabled) return true;
    if (AircraftData[IndexOfAc].AcReady) return true;

    if ((null == AircraftData[IndexOfAc].Battery)) {
      return false;
    }

    if ((null == AircraftData[IndexOfAc].Position)) {
      return false;
    }

    if ((null == AircraftData[IndexOfAc].AC_Carrot_Position)) {
      return false;
    }

    //Check that aircraft has its name , position vs
    if ((null == AircraftData[IndexOfAc].AC_Name)) {
      //Missing AC data ;
      get_new_aircraft_data(AircraftData[IndexOfAc].AC_Id);
      return false;
    }

    //Check if AC has Markers data
    //TODO Check AircraftData[IndexOfAc].MarkersEnabled=true; before coming here..
    if (AircraftData[IndexOfAc].MarkersEnabled == false) {
      //Missing markers data
      if (DEBUG) Log.d("PPRZ_info", "wp request=" + AircraftData[IndexOfAc].AC_Id);
      get_new_waypoint_data(AircraftData[IndexOfAc].AC_Id);
      return false;
    }

    //Check if AC has block data
    //TODO Check AircraftData[IndexOfAc].BlockEnabled=true; before coming here..
    if (!AircraftData[IndexOfAc].BlocksEnabled) {
      //Missing Blocks data
      get_new_block_data(AircraftData[IndexOfAc].AC_Id);
      return false;
    }

    //If evertything is ok with first aircraft then choose it (if none is selected)
    if (SelAcInd < 0) {
      SelAcInd = IndexOfAc;
    }

    //AC

    AircraftData[IndexOfAc].AcReady = true;
    NewAcAdded = true;
    ViewChanged = true;
    return true;
  }

  /**
   * Request server for Ac data
   *
   * @param AcId
   */
  private void get_new_aircraft_data(int AcId) {
    SendToTcp = ("getac " + AcId);
  }

  /**
   * Request server for waypoint data of AC
   *
   * @param AcId
   */
  private void get_new_waypoint_data(int AcId) {
    SendToTcp = ("getwp " + AcId);
  }

  /**
   * Request server for block data of AC
   *
   * @param AcId
   */
  private void get_new_block_data(int AcId) {
    SendToTcp = ("getbl " + AcId);
  }

  /**
   * Check & create block index
   *
   * @param IndexOfAc
   * @param BlId
   * @return
   */
  private boolean check_blockind_of_ac(int IndexOfAc, int BlId) {

    //first check whether data exists
    if (null == AircraftData[IndexOfAc].AC_Blocks[BlId]) {
      //record is empty create & return it
      AircraftData[IndexOfAc].AC_Blocks[BlId] = new mBlock();
      AircraftData[IndexOfAc].AC_Blocks[BlId].BlId = BlId;
      AircraftData[IndexOfAc].AC_Blocks[BlId].BlEnabled = true;
      //TODO create marker icon
      AircraftData[IndexOfAc].NewBlockAdded = true; //Raise AC's new marker edit flag
      return true;
    }
    return true;
  }

  /**
   * Check & create waypoint index
   *
   * @param IndexOfAc
   * @param WpId
   * @return
   */
  private boolean check_wpind_of_ac(int IndexOfAc, int WpId) {

    //first check whether data exists
    if (null == AircraftData[IndexOfAc].AC_Markers[WpId]) {
      //record is empty create & return it
      AircraftData[IndexOfAc].AC_Markers[WpId] = new mMarker();
      AircraftData[IndexOfAc].AC_Markers[WpId].WpId = WpId;
      AircraftData[IndexOfAc].AC_Markers[WpId].WpEnabled = true;
      AircraftData[IndexOfAc].NumbOfWps++;
      //get_new_waypoint_data(AircraftData[IndexOfAc].AC_Id);
      return true;
    }
    if (!AircraftData[IndexOfAc].MarkersEnabled)
      get_new_waypoint_data(AircraftData[IndexOfAc].AC_Id);

    return true;
  }

  /**
   * returns the array index of aircraft if none found prepares the index and returns it.
   *
   * @param AcId
   * @return
   */
  public int get_indexof_ac(int AcId) {
    int index;

    for (index = 0; index < MaxNumbOfAC; index++) {

      //first check whether data exists
      if (null == AircraftData[index]) {
        //record is empty create & return it
        AircraftData[index] = new AirCraft();
        AircraftData[index].AC_Markers = new mMarker[MaxNumbOfWp];
        AircraftData[index].AC_Blocks = new mBlock[MaxNumbOfBl];
        //AircraftData[index].AC_Markers = new mMarker[MaxNumbOfWp];
        AircraftData[index].AC_Id = AcId;
        IndexEnd = index;
        //get_new_aircraft_data(AcId);
        return index;
      }

      //Check id
      if (AircraftData[index].AC_Id == AcId) {
        return index;
      }
    }
    //no place for new aircraft
    if (DEBUG) Log.d("PPRZ_info", "No place for new ac record");
    return -1;
  }

  public class AirCraft {     //Class to hold aircraft data

    public boolean EngineStatusChanged = false;
    boolean isVisible = true;
    boolean AC_Enabled = false;     //Is AC being shown on ui? This also confirms that AC has all its data to shown in ui
    boolean MarkersEnabled = false;    //Markers data has been received and parsed
    boolean BlocksEnabled = false;  //Block data has been received and parsed
    boolean AcReady = false;   //AC has its all data to be ready to show in UI. After showing it AC_Enabled flag will be true.
    int AC_Id;
    String AC_Name;
    String AC_Color;
    String AC_Type;
    String AC_LaunchID;
    String AC_KillID;
    int AC_AltID;
    String AC_DlAlt;
    Marker AC_Marker;
    Bitmap AC_Logo;
    Bitmap AC_Carrot_Logo;
    Marker AC_Carrot_Marker;
    LatLng AC_Carrot_Position;
    //Queue<LatLng> AC_Path;
    ArrayList<LatLng> AC_Path = new ArrayList<LatLng>();
    Polyline Ac_PolLine;
      PolylineOptions Ac_PolLine_Options;
    String Altitude;
    boolean Altitude_Changed = false;
    String AGL;
    LatLng Position;
    String Heading = "0";
    String Speed;
    String Roll = "0";
    String Pitch = "0";
    String Throttle;
    String AirSpeed = "N/A";
    boolean ApStatusChanged = false;
    String FlightTime;
    String ApMode;
    String GpsMode;
    String StateFilterMode;
    String Battery;
    boolean AC_Position_Changed = false;

    int NumbOfWps = 1;
    mMarker[] AC_Markers;
    boolean NewMarkerAdded = false;
    boolean MarkerModified = false;

    int SelectedBlock = 1;
    mBlock[] AC_Blocks;
    int BlockCount;
    boolean NewBlockAdded = false;

    boolean AirspeedEnabled = false;
    boolean AirspeedChanged = false;
  }

  //Marker sub class
  public class mMarker {
    int WpId;
    String WpName;
    Bitmap WpMarkerIcon;
    boolean MarkerModified = false;
    LatLng WpPosition;
    Marker WpMarker;
    String WpAltitude;
    boolean WpEnabled = false;

  }

  //Block sub class
  public class mBlock {
    int BlId;
    String BlName;
    boolean BlEnabled;
  }


}
