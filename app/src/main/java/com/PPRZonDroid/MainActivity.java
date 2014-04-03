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

/*
 * This is the main Activity class
 */

package com.PPRZonDroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;


public class MainActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener {


  //Application Settings
  public static final String SERVER_IP_ADDRESS = "server_ip_adress_text";
  public static final String SERVER_PORT_ADDRESS = "server_port_number_text";
  public static final String LOCAL_PORT_ADDRESS = "local_port_number_text";
  public static final String MIN_AIRSPEED = "minimum_air_speed";
  public static final String USE_GPS = "use_gps_checkbox";
  public static final String Control_Pass = "app_password";
  public Telemetry AC_DATA;                       //Class to hold&proces AC Telemetry Data
  boolean AcLocked = false;
  TextView DialogTextWpName;
  Button DialogButtonConfirm;
  EditText DialogWpAltitude;
  TextView DialogTextWpLat;
  TextView DialogTextWpLon;
  Button DialogAltUp;
  Button DialogAltDown;
  boolean ShowOnlySelected = true;
  String AppPassword;
  //AP_STATUS
  TextView TextViewApMode;
  TextView TextViewGpsMode;
  TextView TextViewStateFilterMode;
  TextView TextViewFlightTime;
  TextView TextViewBattery;
  TextView TextViewSpeed;
  TextView TextViewAirspeed;
  //AC Blocks
  ArrayList<BlockModel> BlList = new ArrayList<BlockModel>();
  BlockListAdapter mBlListAdapter;
  ListView BlListView;
  SharedPreferences AppSettings;                  //App Settings Data
  Float MapZoomLevel = 14.0f;
  //Ac Names
  ArrayList<Model> AcList = new ArrayList<Model>();
  AcListAdapter mAcListAdapter;
  ListView AcListView;
  boolean TcpSettingsChanged;
  boolean UdpSettingsChanged;
  int DialogAcId;
  int DialogWpId;
  //UI components (needs to be bound onCreate
  private GoogleMap mMap;
  private TextView MapAlt;
  private TextView MapThrottle;
  private ImageView Pfd;
  private ToggleButton Button_ConnectToServer;
  private ToggleButton ChangeVisibleAcButon;
  private Switch LockToAcSwitch;
  private DrawerLayout mDrawerLayout;
  //Dialog components
  private Dialog WpDialog;
  //Position descriptions >> in future this needs to be an array or struct
  private LatLng AC_Pos = new LatLng(43.563958, 1.481391);
  private String SendStringBuf;
  private boolean AppStarted = false;             //App started indicator
  private CharSequence mTitle;
  //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
  //Background task to read and write telemery msgs
  private boolean isTaskRunning;

  private ArrayList<Model> generateDataAc() {
    AcList = new ArrayList<Model>();
    return AcList;
  }

  private ArrayList<BlockModel> generateDataBl() {
    BlList = new ArrayList<BlockModel>();
    return BlList;
  }

  /**
   * Setup TCP and UDP connections of Telemetry class
   */
  private void setup_telemetry_class() {

    //Create Telemetry class
    AC_DATA = (Telemetry) getApplication();
    //Read & setup Telemetry class
    AC_DATA.ServerIp = AppSettings.getString(SERVER_IP_ADDRESS, getString(R.string.pref_default_server_ip));
    AC_DATA.ServerTcpPort = Integer.parseInt(AppSettings.getString(SERVER_PORT_ADDRESS, getString(R.string.pref_default_server_port)));
    AC_DATA.UdpListenPort = Integer.parseInt(AppSettings.getString(LOCAL_PORT_ADDRESS, getString(R.string.pref_default_local_port)));
    AC_DATA.AirSpeedMinSetting = Double.parseDouble(AppSettings.getString(MIN_AIRSPEED, "10"));

    AC_DATA.prepare_class();
    AC_DATA.GraphicsScaleFactor = getResources().getDisplayMetrics().density;

    AC_DATA.tcp_connection();
    AC_DATA.mTcpClient.setup_tcp();
    AC_DATA.setup_udp();
  }

  /**
   * Bound UI items
   */
  private void set_up_app() {

    //Get app settings
    AppSettings = PreferenceManager.getDefaultSharedPreferences(this);
    AppSettings.registerOnSharedPreferenceChangeListener(this);

    AppPassword = new String(AppSettings.getString("app_password", ""));

    //Setup waypoint dialog
    WpDialog = new Dialog(this);
    WpDialog.setContentView(R.layout.wp_modified);
    DialogTextWpName = (TextView) WpDialog.findViewById(R.id.textViewWpName);
    DialogButtonConfirm = (Button) WpDialog.findViewById(R.id.buttonConfirm);
    DialogWpAltitude = (EditText) WpDialog.findViewById(R.id.editTextAltitude);
    DialogTextWpLat = (TextView) WpDialog.findViewById(R.id.textViewLatitude);
    DialogTextWpLon = (TextView) WpDialog.findViewById(R.id.textViewLongitude);
    DialogAltUp = (Button) WpDialog.findViewById(R.id.buttonplust);
    DialogAltDown = (Button) WpDialog.findViewById(R.id.buttonmint);

    DialogAltDown.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Double alt = Double.parseDouble(DialogWpAltitude.getText().toString()) - 10;
        DialogWpAltitude.setText(alt.toString());
      }
    });

    DialogAltUp.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Double alt = Double.parseDouble(DialogWpAltitude.getText().toString()) + 10;
        DialogWpAltitude.setText(alt.toString());
      }
    });

    DialogButtonConfirm.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        SendStringBuf = "PPRZonDroid MOVE_WAYPOINT " + DialogAcId + " " + DialogWpId +
                " " + DialogTextWpLat.getText() + " " + DialogTextWpLon.getText() + " " + DialogWpAltitude.getText();
        send_to_server(SendStringBuf, true);
        WpDialog.dismiss();
      }
    });

    //Setup left drawer
    mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

    //Setup AC List
    mAcListAdapter = new AcListAdapter(this, generateDataAc());

    // if extending Activity 2. Get ListView from activity_main.xml
    AcListView = (ListView) findViewById(R.id.AcList);
    View AppControls = getLayoutInflater().inflate(R.layout.appcontrols, null);

    //First item will be app controls
    //This workaround applied to for app controls sliding to top if user slides them.
    AcListView.addHeaderView(AppControls);

    // 3. setListAdapter
    AcListView.setAdapter(mAcListAdapter);
    //Create onclick listener
    AcListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position >= 1) {
          view.setSelected(true);
          set_selected_ac(position - 1);
          mDrawerLayout.closeDrawers();
        }

      }
    });


    //Block Listview
    mBlListAdapter = new BlockListAdapter(this, generateDataBl());

    //
    BlListView = (ListView) findViewById(R.id.BlocksList);
    View FlightControls = getLayoutInflater().inflate(R.layout.flight_controls, null);
    BlListView.addHeaderView(FlightControls);
    BlListView.setAdapter(mBlListAdapter);
    BlListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position >= 1) {
          view.setSelected(true);
          set_selected_block(position - 1);
          mDrawerLayout.closeDrawers();
        }
      }
    });


    //Set map zoom level variable (if any);
    if (AppSettings.contains("MapZoomLevel")) {
      MapZoomLevel = AppSettings.getFloat("MapZoomLevel", 17.0f);
    }

    //continue to bound UI items
    MapAlt = (TextView) findViewById(R.id.Alt_On_Map);
    MapThrottle = (TextView) findViewById(R.id.ThrottleText);
    Pfd = (ImageView) findViewById(R.id.imageView_Pfd);

    Button_ConnectToServer = (ToggleButton) findViewById(R.id.toggleButton_ConnectToServer);
    setup_map_ifneeded();


    LockToAcSwitch = (Switch) findViewById(R.id.switch_TraceAircraft);
    LockToAcSwitch.setSelected(true);
    ChangeVisibleAcButon = (ToggleButton) findViewById(R.id.toggleButtonVisibleAc);
    ChangeVisibleAcButon.setSelected(false);

    TextViewApMode = (TextView) findViewById(R.id.Ap_Status_On_Map);
    TextViewGpsMode = (TextView) findViewById(R.id.Gps_Status_On_Map);
    TextViewFlightTime = (TextView) findViewById(R.id.Flight_Time_On_Map);
    TextViewBattery = (TextView) findViewById(R.id.Bat_Vol_On_Map);
    TextViewStateFilterMode = (TextView) findViewById(R.id.State_Filter_On_Map);
    TextViewSpeed = (TextView) findViewById(R.id.SpeedText);
    TextViewAirspeed = (TextView) findViewById(R.id.AirSpeed_On_Map);


  }

  /**
   * Set Selected Block
   *
   * @param BlocId
   */
  private void set_selected_block(int BlocId) {

    AC_DATA.AircraftData[AC_DATA.SelAcInd].SelectedBlock = BlocId;
    mBlListAdapter.SelectedInd = BlocId + 1;
    mBlListAdapter.notifyDataSetChanged();
    //Notify server
    send_to_server("PPRZonDroid JUMP_TO_BLOCK " + AC_DATA.AircraftData[AC_DATA.SelAcInd].AC_Id + " " + BlocId, true);

  }

  /**
   * Set Selected airplane
   *
   * @param AcInd
   */
  private void set_selected_ac(int AcInd) {

    AC_DATA.SelAcInd = AcInd;
    //Set Title;
    setTitle(AC_DATA.AircraftData[AC_DATA.SelAcInd].AC_Name);

    mAcListAdapter.SelectedInd = AcInd;
    mAcListAdapter.notifyDataSetChanged();

    refresh_block_list();
    set_marker_visibility();

    for (int i = 0; i <= AC_DATA.IndexEnd; i++) {
      //Is AC ready to show on ui?
      //Check if ac i visible and its position is changed
      if (AC_DATA.AircraftData[i].AC_Enabled && AC_DATA.AircraftData[i].isVisible && AC_DATA.AircraftData[i].AC_Marker != null) {

        AC_DATA.AircraftData[i].AC_Logo = AC_DATA.muiGraphics.create_ac_icon(AC_DATA.AircraftData[i].AC_Type, AC_DATA.AircraftData[i].AC_Color, AC_DATA.GraphicsScaleFactor, (i == AC_DATA.SelAcInd));
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(AC_DATA.AircraftData[i].AC_Logo);
        AC_DATA.AircraftData[i].AC_Marker.setIcon(bitmapDescriptor);
        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(AC_DATA.AircraftData[AC_DATA.SelAcInd].Position, MapZoomLevel), 1500, null);
        center_aircraft();
      }

    }

  }

  /**
   * Refresh block lisst on right
   */
  private void refresh_block_list() {

    int i;
    BlList.clear();

    for (i = 0; i < AC_DATA.AircraftData[AC_DATA.SelAcInd].BlockCount; i++) {
      BlList.add(new BlockModel(AC_DATA.AircraftData[AC_DATA.SelAcInd].AC_Blocks[i].BlName));
    }

    mBlListAdapter.SelectedInd = AC_DATA.AircraftData[AC_DATA.SelAcInd].SelectedBlock;
    mBlListAdapter.notifyDataSetChanged();
  }

  //Bound map (if not bounded already)
  private void setup_map_ifneeded() {
    // Do a null check to confirm that we have not already instantiated the map.
    if (mMap == null) {
      // Try to obtain the map from the SupportMapFragment.
      mMap = ((MapFragment) getFragmentManager()
              .findFragmentById(R.id.map)).getMap();
      // Check if we were successful in obtaining the map.
      if (mMap != null) {
        setup_map();
      }
    }
  }

  //Setup map & zoom listener
  private void setup_map() {

    GoogleMapOptions mMapOptions = new GoogleMapOptions();

    mMap.getUiSettings().setTiltGesturesEnabled(false);
    //Read device settings for Gps usage.
    mMap.setMyLocationEnabled(AppSettings.getBoolean("use_gps_checkbox", true));

    //Set map type
    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

    //Set zoom level
    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(AC_Pos, MapZoomLevel));

    //Setup markers drag listeners
    mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {

      @Override
      public void onMarkerDragStart(Marker marker) {
      }

      @Override
      public void onMarkerDragEnd(Marker marker) {
        waypoint_changed(marker.getId(), marker.getPosition(), "Waypoint changed");
      }

      @Override
      public void onMarkerDrag(Marker marker) {

      }
    });

    //Set zoom listener to
    mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {

      private float currentZoom = -1;

      @Override
      public void onCameraChange(CameraPosition position) {
        if (position.zoom != currentZoom) {
          //Get zoom level
          currentZoom = position.zoom;

          //if app didn't started don't save MapzoomLevel (needs to be checked)
          if (AppStarted) {
            MapZoomLevel = currentZoom;
          }


        }
      }
    });

    //Setup onclick listener for marker infos
    mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

      public void onInfoWindowClick(Marker marker) {

        if (!(is_ac_marker(marker.getId()))) {
          waypoint_changed(marker.getId(), marker.getPosition(), "Modify Waypoint");
          //Toast.makeText(getApplicationContext(), "marker " + marker.getId() , Toast.LENGTH_SHORT).show();
        }
      }
    });

  }

  /**
   * Check if everthink is ok with modified wp
   *
   * @param WpID
   * @return
   */
  private boolean is_ac_marker(String WpID) {

    for (int AcInd = 0; AcInd <= AC_DATA.IndexEnd; AcInd++) {     //Find waypoint which is changed!

      if ((null == AC_DATA.AircraftData[AcInd].AC_Marker))
        continue; //we dont have data for this wp yet

      if (AC_DATA.AircraftData[AcInd].AC_Marker.getId().equals(WpID)) {
        set_selected_ac(AcInd);
        Toast.makeText(getApplicationContext(), "Selected A/C =  " + AC_DATA.AircraftData[AcInd].AC_Name, Toast.LENGTH_SHORT).show();
        return true;

      }


    }
    return false;

  }

  /**
   * Send string to server
   *
   * @param StrToSend
   * @param ControlString Whether String is to control AC or request Ac data.If string is a control string then app pass will be send also
   */
  private void send_to_server(String StrToSend, boolean ControlString) {
    //Is it a control string ? else ->Data request
    if (ControlString) {
      AC_DATA.SendToTcp = AppPassword + " " + StrToSend;
    } else {
      AC_DATA.SendToTcp = StrToSend;
    }
  }

  /**
   * Play warning sound if airspeed goes below the selected value
   *
   * @param context
   * @throws IllegalArgumentException
   * @throws SecurityException
   * @throws IllegalStateException
   * @throws IOException
   */
  public void play_sound(Context context) throws IllegalArgumentException,
          SecurityException,
          IllegalStateException,
          IOException {

    Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    MediaPlayer mMediaPlayer = new MediaPlayer();
    mMediaPlayer.setDataSource(context, soundUri);
    final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    //Set volume max!!!
    audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, audioManager.getStreamMaxVolume(audioManager.STREAM_SYSTEM), 0);

    if (audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM) != 0) {
      mMediaPlayer.setAudioStreamType(AudioManager.STREAM_SYSTEM);
      mMediaPlayer.setLooping(true);
      mMediaPlayer.prepare();
      mMediaPlayer.start();
    }
  }

  private void refresh_ac_list() {


    //Create or edit aircraft list
    int i;
    for (i = 0; i <= AC_DATA.IndexEnd; i++) {


      if (AC_DATA.AircraftData[i].AC_Enabled) {
        AcList.set(i, new Model(AC_DATA.AircraftData[i].AC_Logo, AC_DATA.AircraftData[i].AC_Name, AC_DATA.AircraftData[i].Battery));

      } else {
        if (AC_DATA.AircraftData[i].AcReady) {
          AcList.add(new Model(AC_DATA.AircraftData[i].AC_Logo, AC_DATA.AircraftData[i].AC_Name, AC_DATA.AircraftData[i].Battery));
          AC_DATA.AircraftData[i].AC_Enabled = true;
        } else {
          //AC data is not ready yet this should be
          return;
        }
      }
    }

    AC_DATA.BatteryChanged = false;
    AC_DATA.NewAcAdded = false;
    //mAcListAdapter.SelectedInd=AC_DATA.SelAcInd;

    set_selected_ac(AC_DATA.SelAcInd);

  }

  private void refresh_map_lines(int AcInd) {

    if (AC_DATA.AircraftData[AcInd].Ac_PolLine != null) {
      AC_DATA.AircraftData[AcInd].Ac_PolLine.remove();
    }


    PolylineOptions rectOptions = new PolylineOptions()
            .addAll(AC_DATA.AircraftData[AcInd].AC_Path)
            .width(2 * AC_DATA.GraphicsScaleFactor)
            .color(AC_DATA.muiGraphics.get_color(AC_DATA.AircraftData[AcInd].AC_Color))
            .geodesic(true);

    AC_DATA.AircraftData[AcInd].Ac_PolLine = mMap.addPolyline(rectOptions);

    if (AcInd == AC_DATA.SelAcInd || ShowOnlySelected == false) {
      AC_DATA.AircraftData[AcInd].Ac_PolLine.setVisible(true);
    } else {
      AC_DATA.AircraftData[AcInd].Ac_PolLine.setVisible(false);
    }

  }

  /**
   * Amm markers of given AcIndex to map. AcIndex is not Ac ID!!!
   *
   * @param AcIndex
   */
  private void add_markers_2_map(int AcIndex) {


    if (AC_DATA.AircraftData[AcIndex].isVisible && AC_DATA.AircraftData[AcIndex].AC_Enabled) {
      AC_DATA.AircraftData[AcIndex].AC_Marker = mMap.addMarker(new MarkerOptions()
                      .position(AC_DATA.AircraftData[AcIndex].Position)
                      .anchor((float) 0.5, (float) 0.5)
                      .flat(true).rotation(Float.parseFloat(AC_DATA.AircraftData[AcIndex].Heading))
                      .title(AC_DATA.AircraftData[AcIndex].AC_Name)
                      .draggable(true)
                      .icon(BitmapDescriptorFactory.fromBitmap(AC_DATA.AircraftData[AcIndex].AC_Logo))
      );

      AC_DATA.AircraftData[AcIndex].AC_Carrot_Marker = mMap.addMarker(new MarkerOptions()
                      .position(AC_DATA.AircraftData[AcIndex].AC_Carrot_Position)
                      .anchor((float) 0.5, (float) 0.5)
                      .draggable(true)
                      .icon(BitmapDescriptorFactory.fromBitmap(AC_DATA.AircraftData[AcIndex].AC_Carrot_Logo))
      );


    }

  }

  //REfresh markers
  private void refresh_markers() {

    //Method below is better..
    //
    int i;

    //if selected ac position changed and user wants to trace ac then
    if (AcLocked && AC_DATA.AircraftData[AC_DATA.SelAcInd].AC_Position_Changed) {
      mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(AC_DATA.AircraftData[AC_DATA.SelAcInd].Position, MapZoomLevel), 400, null);
      CameraPosition cameraPosition = CameraPosition.builder()
              .target(AC_DATA.AircraftData[AC_DATA.SelAcInd].Position)
              .zoom(MapZoomLevel)
              .bearing(Float.parseFloat(AC_DATA.AircraftData[AC_DATA.SelAcInd].Heading))
              .build();

      mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }


    for (i = 0; i <= AC_DATA.IndexEnd; i++) {

      //Is AC ready to show on ui?
      //if (!AC_DATA.AircraftData[i].AC_Enabled)  return;

      if (null == AC_DATA.AircraftData[i].AC_Marker) {
        add_markers_2_map(i);
      }

      //Check if ac i visible and its position is changed
      if (AC_DATA.AircraftData[i].AC_Enabled && AC_DATA.AircraftData[i].isVisible && AC_DATA.AircraftData[i].AC_Position_Changed) {
        AC_DATA.AircraftData[i].AC_Marker.setPosition(AC_DATA.AircraftData[i].Position);
        AC_DATA.AircraftData[i].AC_Marker.setRotation(Float.parseFloat(AC_DATA.AircraftData[i].Heading));
        AC_DATA.AircraftData[i].AC_Carrot_Marker.setPosition(AC_DATA.AircraftData[i].AC_Carrot_Position);
        refresh_map_lines(i);
        AC_DATA.AircraftData[i].AC_Position_Changed = false;
      }

    }


    //Check markers

    if (AC_DATA.NewMarkerAdded)   //Did we add any markers?
    {
      int AcInd;
      for (AcInd = 0; AcInd <= AC_DATA.IndexEnd; AcInd++) {     //Search thru all aircrafts and check if they have marker added flag

        if (AC_DATA.AircraftData[AcInd].NewMarkerAdded) {   //Does this aircraft has an added marker data?
          int MarkerInd = 1;
          //Log.d("PPRZ_info", "trying to show ac markers of "+AcInd);
          //Search aircraft markers which has name but doesn't have marker
          for (MarkerInd = 1; (MarkerInd < AC_DATA.AircraftData[AcInd].NumbOfWps - 1); MarkerInd++) {

            if ((AC_DATA.AircraftData[AcInd].AC_Markers[MarkerInd].WpPosition == null) || (AC_DATA.AircraftData[AcInd].AC_Markers[MarkerInd].WpMarker != null))
              continue; //we dont have data for this wp yet
            //Log.d("PPRZ_info", "New marker added for Ac id: " + AcInd + " wpind:" + MarkerInd);
            AC_DATA.AircraftData[AcInd].AC_Markers[MarkerInd].WpMarker = mMap.addMarker(new MarkerOptions()
                    .position(AC_DATA.AircraftData[AcInd].AC_Markers[MarkerInd].WpPosition)
                    .title(AC_DATA.AircraftData[AcInd].AC_Markers[MarkerInd].WpName)
                    .draggable(true)
                    .icon(BitmapDescriptorFactory.fromBitmap(AC_DATA.AircraftData[AcInd].AC_Markers[MarkerInd].WpMarkerIcon)));
            AC_DATA.AircraftData[AcInd].AC_Markers[MarkerInd].WpMarker.setVisible(AcMarkerVisible(AcInd));

            if ("_".equals(AC_DATA.AircraftData[AcInd].AC_Markers[MarkerInd].WpName.substring(0, 1))) {
              AC_DATA.AircraftData[AcInd].AC_Markers[MarkerInd].WpMarker.setVisible(false);
            }

            AC_DATA.AircraftData[AcInd].AC_Markers[MarkerInd].MarkerModified = false;
          }


          AC_DATA.AircraftData[AcInd].NewMarkerAdded = false;
        }
      }

      AC_DATA.NewMarkerAdded = false;
    }


    //Handle marker modified msg

    if (AC_DATA.MarkerModified)   //
    {

      //Log.d("PPRZ_info", "Marker modified for whom?");
      int AcInd;
      for (AcInd = 0; AcInd <= AC_DATA.IndexEnd; AcInd++) {     //Search thru all aircrafts and check if they have marker added flag

        if (AC_DATA.AircraftData[AcInd].MarkerModified) {   //Does this aircraft has an added marker data?
          Log.d("PPRZ_info", "Marker modified for AC= " + AcInd);
          int MarkerInd = 1;
          //Log.d("PPRZ_info", "trying to show ac markers of "+AcInd);
          //Search aircraft markers which has name but doesn't have marker
          for (MarkerInd = 1; (MarkerInd < AC_DATA.AircraftData[AcInd].NumbOfWps - 1); MarkerInd++) {
            //Log.d("PPRZ_info", "Searching Marker for AC= " + AcInd + " wpind:" + MarkerInd);
            if ((AC_DATA.AircraftData[AcInd].AC_Markers[MarkerInd] == null) || !(AC_DATA.AircraftData[AcInd].AC_Markers[MarkerInd].MarkerModified) || (null == AC_DATA.AircraftData[AcInd].AC_Markers[MarkerInd].WpMarker))
              continue; //we dont have data for this wp yet

            //Set new position for marker
            AC_DATA.AircraftData[AcInd].AC_Markers[MarkerInd].WpMarker.setPosition(AC_DATA.AircraftData[AcInd].AC_Markers[MarkerInd].WpPosition);

            //Clean MarkerModified flag
            AC_DATA.AircraftData[AcInd].AC_Markers[MarkerInd].MarkerModified = false;
            Log.d("PPRZ_info", "Marker modified for acid: " + AcInd + " wpind:" + MarkerInd);

          }
          //Clean AC MarkerModified flag
          AC_DATA.AircraftData[AcInd].MarkerModified = false;

        }
      }
      //Clean Class MarkerModified flag
      AC_DATA.MarkerModified = false;

    }

    AC_DATA.ViewChanged = false;
  }

  /**
   * @param WpID
   * @param NewPosition
   * @param DialogTitle
   */
  private void waypoint_changed(String WpID, LatLng NewPosition, String DialogTitle) {

    //Find the marker

    int AcInd;
    int MarkerInd = 1;
    for (AcInd = 0; AcInd <= AC_DATA.IndexEnd; AcInd++) {     //Find waypoint which is changed!


      for (MarkerInd = 1; (MarkerInd < AC_DATA.AircraftData[AcInd].NumbOfWps - 1); MarkerInd++) {

        if ((null == AC_DATA.AircraftData[AcInd].AC_Markers[MarkerInd].WpMarker))
          continue; //we dont have data for this wp yet
        //Log.d("PPRZ_info", "Marker drop!!  Searching for AC= " + AcInd + " wpind:" + MarkerInd + " mid: "+AC_DATA.AircraftData[AcInd].AC_Markers[MarkerInd].WpMarker.getId());
        //Search the marker

        if (AC_DATA.AircraftData[AcInd].AC_Markers[MarkerInd].WpMarker.getId().equals(WpID)) {
          //Log.d("PPRZ_info", "Marker found AC= " + AcInd + " wpind:" + MarkerInd);
          AC_DATA.AircraftData[AcInd].AC_Markers[MarkerInd].WpPosition = NewPosition;
          //show_dialog_menu(String DialogTitle,String WpName,String AirCraftId,String WayPointId,String Lat,String Long,String Alt)
          show_dialog_menu(DialogTitle, AC_DATA.AircraftData[AcInd].AC_Markers[MarkerInd].WpName, AC_DATA.AircraftData[AcInd].AC_Id, MarkerInd, String.valueOf(NewPosition.latitude), String.valueOf(NewPosition.longitude), AC_DATA.AircraftData[AcInd].AC_Markers[MarkerInd].WpAltitude);

          return;
        }

      }
    }

  }

  private boolean checkReady() {
    if (mMap == null) {
      Toast.makeText(this, R.string.map_not_ready, Toast.LENGTH_SHORT).show();
      return false;
    }
    return true;
  }

  public void onResetMap(View view) {
    if (!checkReady()) {
      return;
    }
    // Clear the map because we don't want duplicates of the markers.
    mMap.clear();
    //addMarkersToMap();
  }

  //Function to center aircraft on map (can be integrated with Center_AC funct. )
  private void center_aircraft() {
    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(AC_DATA.AircraftData[AC_DATA.SelAcInd].Position, MapZoomLevel), 1500, null);
  }

  @Override
  public void setTitle(CharSequence title) {
    mTitle = title;
    getActionBar().setTitle(mTitle);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {

    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;

  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    switch (item.getItemId()) {
      case R.id.action_settings:

        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);

        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onStop() {
    super.onStop();

    // We need an Editor object to make preference changes.
    // All objects are from android.context.Context
    SharedPreferences.Editor editor = AppSettings.edit();

    editor.putFloat("MapZoomLevel", MapZoomLevel);

    // Commit the edits!
    editor.commit();


  }

  @Override
  protected void onStart() {
    super.onStop();

    AppStarted = true;
    MapZoomLevel = AppSettings.getFloat("MapZoomLevel", 16.0f);

  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);

    set_up_app();
    setup_telemetry_class();
    new ReadTelemetry().execute();


  }


/* >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
* START OF UI FUNCTIONS >>>>>> START OF UI FUNCTIONS >>>>>> START OF UI FUNCTIONS >>>>>> START OF UI FUNCTIONS >>>>>>
>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/

  //Settings change listener
  @Override
  public void onSharedPreferenceChanged(SharedPreferences AppSettings, String key) {

    //Changed settings will be applied on nex iteration of async task

    if (key.equals(SERVER_IP_ADDRESS)) {
      AC_DATA.ServerIp = AppSettings.getString(SERVER_IP_ADDRESS, getString(R.string.pref_default_server_ip));
      //Log.d("PPRZ_info", "IP changed to: " + AppSettings.getString(SERVER_IP_ADDRESS, getString(R.string.pref_default_server_ip)));
      TcpSettingsChanged = true;

    }

    if (key.equals(SERVER_PORT_ADDRESS)) {
      AC_DATA.ServerTcpPort = Integer.parseInt(AppSettings.getString(SERVER_PORT_ADDRESS, getString(R.string.pref_default_server_port)));
      TcpSettingsChanged = true;
      //Log.d("PPRZ_info", "Server Port changed to: " + AppSettings.getString(SERVER_PORT_ADDRESS, getString(R.string.pref_default_server_port)));

    }

    if (key.equals(LOCAL_PORT_ADDRESS)) {

      AC_DATA.UdpListenPort = Integer.parseInt(AppSettings.getString(LOCAL_PORT_ADDRESS, getString(R.string.pref_default_local_port)));
      UdpSettingsChanged = true;
      //Log.d("PPRZ_info", "Local Listen Port changed to: " + AppSettings.getString(LOCAL_PORT_ADDRESS, getString(R.string.pref_default_local_port)));

    }

    if (key.equals(MIN_AIRSPEED)) {

      AC_DATA.AirSpeedMinSetting = Double.parseDouble(AppSettings.getString(MIN_AIRSPEED, "10"));
      //Log.d("PPRZ_info", "Local Listen Port changed to: " + AppSettings.getString(MIN_AIRSPEED, "10"));
    }

    if (key.equals(USE_GPS)) {
      mMap.setMyLocationEnabled(AppSettings.getBoolean(USE_GPS, true));
      //Log.d("PPRZ_info", "GPS Usage changed to: " + AppSettings.getBoolean(USE_GPS, true));
    }

    if (key.equals(Control_Pass)) {
      AppPassword = AppSettings.getString(Control_Pass, "");
      //Log.d("PPRZ_info", "App_password changed : " + AppSettings.getString(Control_Pass , ""));
    }


  }

  private void show_dialog_menu(String DialogTitle, String WpName, int AirCraftId, int WayPointId, String Lat, String Long, String Alt) {

    // set dialog components
    WpDialog.setTitle(DialogTitle);

    DialogTextWpName.setText(WpName);

    DialogAcId = AirCraftId;
    DialogWpId = WayPointId;

    DialogWpAltitude.setText(Alt);

    DialogTextWpLat.setText(Lat);
    DialogTextWpLon.setText(Long);

    WpDialog.show();

  }

  public void clear_ac_track(View Gorunus) {

    if (AC_DATA.SelAcInd < 0) {
      center_aircraft();
      Toast.makeText(getApplicationContext(), "No AC data yet!", Toast.LENGTH_SHORT).show();

      return;
    }


    if (ShowOnlySelected) {
      AC_DATA.AircraftData[AC_DATA.SelAcInd].AC_Path.clear();
    } else {
      for (int AcInd = 0; AcInd <= AC_DATA.IndexEnd; AcInd++) {
        AC_DATA.AircraftData[AcInd].AC_Path.clear();
      }
    }
    mDrawerLayout.closeDrawers();

  }

  //Function called when button_CenterAC (in left fragment) is pressed
  public void center_ac(View Gorunus) {
    if (AC_DATA.SelAcInd >= 0) {
      center_aircraft();
      mDrawerLayout.closeDrawers();
      return;
    }
    Toast.makeText(getApplicationContext(), "No AC data yet!", Toast.LENGTH_SHORT).show();
  }

  //Function called when toggleButton_ConnectToServer (in left fragment) is pressed
  public void connect_to_server(View Gorunus) {

    Toast.makeText(getApplicationContext(), "Trying to connectserver..", Toast.LENGTH_SHORT).show();
    //Force to reconnect
    TcpSettingsChanged = true;
    UdpSettingsChanged = true;

  }

  //Launch function
  public void launch_ac(View mView) {

    if (AC_DATA.SelAcInd >= 0) {
      send_to_server("dl DL_SETTING " + AC_DATA.AircraftData[AC_DATA.SelAcInd].AC_Id + " " + AC_DATA.AircraftData[AC_DATA.SelAcInd].AC_LaunchID + " 1.000000", true);
      //dl DL_SETTING 5 8 1.000000
    } else {
      Toast.makeText(getApplicationContext(), "No AC data yet!", Toast.LENGTH_SHORT).show();
    }
  }

  //Kill throttle function
  public void kill_ac(View mView) {

    if (AC_DATA.SelAcInd >= 0) {

      AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
      // Setting Dialog Title
      alertDialog.setTitle("Kill Throttle");

      // Setting Dialog Message
      alertDialog.setMessage("Kill throttle of A/C " + AC_DATA.AircraftData[AC_DATA.SelAcInd].AC_Name + "?");

      // Setting Icon to Dialog
      alertDialog.setIcon(R.drawable.kill);

      // Setting Positive "Yes" Button
      alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
          // User pressed YES button. Send kill string
          send_to_server("dl DL_SETTING " + AC_DATA.AircraftData[AC_DATA.SelAcInd].AC_Id + " " + AC_DATA.AircraftData[AC_DATA.SelAcInd].AC_KillID + " 1.000000", true);
          Toast.makeText(getApplicationContext(), AC_DATA.AircraftData[AC_DATA.SelAcInd].AC_Name + " ,mayday, kill mode!", Toast.LENGTH_SHORT).show();
          MapThrottle.setTextColor(Color.RED);
        }
      });

      alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {

        }
      });

      // Showing Alert Message
      alertDialog.show();

    } else {
      Toast.makeText(getApplicationContext(), "No AC data yet!", Toast.LENGTH_SHORT).show();
    }
  }

  //Resurrect function
  public void resurrect_ac(View mView) {
    //dl DL_SETTING 5 9 0.000000
    if (AC_DATA.SelAcInd >= 0) {
      send_to_server("dl DL_SETTING " + AC_DATA.AircraftData[AC_DATA.SelAcInd].AC_Id + " " + AC_DATA.AircraftData[AC_DATA.SelAcInd].AC_KillID + " 0.000000", true);
      MapThrottle.setTextColor(Color.WHITE);
    } else {
      Toast.makeText(getApplicationContext(), "No AC data yet!", Toast.LENGTH_SHORT).show();
    }

  }

  public void trace_ac(View mView) {
    //Will camera trace aircraft
    AcLocked = LockToAcSwitch.isChecked();
    mDrawerLayout.closeDrawers();

  }

  public void change_visible_acdata(View mView) {

    if (ChangeVisibleAcButon.isChecked()) {
      Toast.makeText(getApplicationContext(), "Showing only " + AC_DATA.AircraftData[AC_DATA.SelAcInd].AC_Name + " markers", Toast.LENGTH_SHORT).show();
      ShowOnlySelected = true;
    } else {
      Toast.makeText(getApplicationContext(), "Showing all markers", Toast.LENGTH_SHORT).show();
      ShowOnlySelected = false;
    }

    set_marker_visibility();
    mDrawerLayout.closeDrawers();

  }

  //Shows only selected ac markers & hide others
  private void set_marker_visibility() {

    if (ShowOnlySelected) {
      show_only_selected_ac();
    } else {
      show_all_acs();
    }


  }

  private void show_only_selected_ac() {


    for (int AcInd = 0; AcInd <= AC_DATA.IndexEnd; AcInd++) {

      for (int WpId = 1; (AC_DATA.AircraftData[AcInd].AC_Enabled && (WpId < AC_DATA.AircraftData[AcInd].NumbOfWps - 1)); WpId++) {

        if ((null == AC_DATA.AircraftData[AcInd].AC_Markers[WpId].WpMarker)) continue;

        if (AcInd == AC_DATA.SelAcInd) {
          AC_DATA.AircraftData[AcInd].AC_Markers[WpId].WpMarker.setVisible(true);

          if ("_".equals(AC_DATA.AircraftData[AcInd].AC_Markers[WpId].WpName.substring(0, 1))) {
            AC_DATA.AircraftData[AcInd].AC_Markers[WpId].WpMarker.setVisible(false);
          }

        } else {
          AC_DATA.AircraftData[AcInd].AC_Markers[WpId].WpMarker.setVisible(false);
        }
      }

    }

  }

  //Show all markers
  private void show_all_acs() {


    for (int AcInd = 0; AcInd <= AC_DATA.IndexEnd; AcInd++) {

      for (int WpId = 1; (AC_DATA.AircraftData[AcInd].AC_Enabled && (WpId < AC_DATA.AircraftData[AcInd].NumbOfWps - 1)); WpId++) {

        if ((null == AC_DATA.AircraftData[AcInd].AC_Markers[WpId].WpMarker)) continue;

        AC_DATA.AircraftData[AcInd].AC_Markers[WpId].WpMarker.setVisible(true);

        if ("_".equals(AC_DATA.AircraftData[AcInd].AC_Markers[WpId].WpName.substring(0, 1))) {
          AC_DATA.AircraftData[AcInd].AC_Markers[WpId].WpMarker.setVisible(false);
        }

      }

    }

  }

  private boolean AcMarkerVisible(int AcInd) {

    if (AcInd == AC_DATA.SelAcInd) {
      return true;
    } else if (ShowOnlySelected) {
      return false;
    }
    return true;

  }

  /**
   * background thread to read & write comm strings. Only changed UI items should be refreshed for smoother UI
   * Check telemetry class for whole UI change flags
   */
  class ReadTelemetry extends AsyncTask<String, String, String> {

    @Override
    protected void onPreExecute() {
      super.onPreExecute();

      isTaskRunning = true;
      //Log.d("PPRZ_info", "ReadTelemetry() function started.");
    }

    @Override
    protected String doInBackground(String... strings) {

      String TcpReceived = null;

      while (isTaskRunning) {

        //1 check if any string waiting to be send to tcp
        if (!(null == AC_DATA.SendToTcp)) {
          AC_DATA.mTcpClient.sendMessage(AC_DATA.SendToTcp);
          AC_DATA.SendToTcp = null;
        }

        TcpReceived = AC_DATA.mTcpClient.readMessage();

        //2 check if any tcp string waiting to be parsed
        if (!(null == TcpReceived)) {
          AC_DATA.parse_tcp_string(TcpReceived);
        }

        //3 try to read & parse udp data
        AC_DATA.read_udp_data();

        //4 check ui changes
        if (AC_DATA.ViewChanged) {
          publishProgress("ee");
          AC_DATA.ViewChanged = false;
        }

        //Check if settings changed
        if (TcpSettingsChanged || UdpSettingsChanged) {
          publishProgress("ee");
        }


      }

      //Log.d("PPRZ_info", "doInBackground exiting");
      return null;
    }

    @Override
    protected void onProgressUpdate(String... value) {
      super.onProgressUpdate(value);

      try {


        if (TcpSettingsChanged) {
          AC_DATA.tcp_connection();
          AC_DATA.mTcpClient.setup_tcp();
          TcpSettingsChanged = false;
          //Log.d("PPRZ_info", "TcpSettingsChanged applied");
        }

        if (UdpSettingsChanged) {
          AC_DATA.setup_udp();
          AC_DATA.tcp_connection();
          UdpSettingsChanged = false;
          //Log.d("PPRZ_info", "UdpSettingsChanged applied");
        }

        if (AC_DATA.SelAcInd < 0) {
          //no selected aircrafts yet! Return.
          return;
        }


        if (AC_DATA.AircraftData[AC_DATA.SelAcInd].ApStatusChanged) {
          TextViewApMode.setText(AC_DATA.AircraftData[AC_DATA.SelAcInd].ApMode);
          TextViewGpsMode.setText(AC_DATA.AircraftData[AC_DATA.SelAcInd].GpsMode);
          TextViewFlightTime.setText(AC_DATA.AircraftData[AC_DATA.SelAcInd].FlightTime);
          TextViewBattery.setText(AC_DATA.AircraftData[AC_DATA.SelAcInd].Battery + "v");
          TextViewStateFilterMode.setText(AC_DATA.AircraftData[AC_DATA.SelAcInd].StateFilterMode);
          TextViewSpeed.setText(AC_DATA.AircraftData[AC_DATA.SelAcInd].Speed + "m/s");
          AC_DATA.AircraftData[AC_DATA.SelAcInd].ApStatusChanged = false;
        }

        if (AC_DATA.AirspeedWarning) {
          try {
            play_sound(getApplicationContext());
          } catch (IOException e) {
            e.printStackTrace();
          }
        }


        if (AC_DATA.BlockChanged) {
          //Block changed for selected aircraft
          //Log.d("PPRZ_info", "Block Changed for selected AC.");
          mBlListAdapter.SelectedInd = AC_DATA.AircraftData[AC_DATA.SelAcInd].SelectedBlock;
          mBlListAdapter.notifyDataSetChanged();
          AC_DATA.BlockChanged = false;
        }


        if (AC_DATA.NewAcAdded || AC_DATA.BatteryChanged) {
          //new ac addedBattery value for an ac is changed
          refresh_ac_list();
        }

        //For a smooth gui we need refresh only changed gui controls
        refresh_markers();

        //Check  engine Status
        if (AC_DATA.AircraftData[AC_DATA.SelAcInd].EngineStatusChanged) {
          MapThrottle.setText("%" + AC_DATA.AircraftData[AC_DATA.SelAcInd].Throttle);
          AC_DATA.AircraftData[AC_DATA.SelAcInd].EngineStatusChanged = false;
        }

        if (AC_DATA.AircraftData[AC_DATA.SelAcInd].Altitude_Changed) {
          MapAlt.setText(AC_DATA.AircraftData[AC_DATA.SelAcInd].Altitude + "m");
          AC_DATA.AircraftData[AC_DATA.SelAcInd].Altitude_Changed = false;
          Drawable d = new BitmapDrawable(getResources(), AC_DATA.AcPfd);
          Pfd.setBackground(d);
          //Refresh_Pfd()
        }

        if (AC_DATA.AirspeedChanged) {
          //Airspeed Enabled
          TextViewAirspeed.setVisibility(View.VISIBLE);
          TextViewAirspeed.setText(AC_DATA.AircraftData[AC_DATA.SelAcInd].AirSpeed + " m/s");
        }
        //No error handling right now.
        Button_ConnectToServer.setText("Connected!");
        //}

      } catch (Exception ex) {
        Log.d("Pprz_info", "Exception occured: " + ex.toString());

      }

    }

    @Override
    protected void onPostExecute(String s) {
      super.onPostExecute(s);
      //Log.d("PPRZ_info", "onPostExecute");
    }

  }




/* <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
* END OF UI FUNCTIONS >>>>>> END OF UI FUNCTIONS >>>>>> END OF UI FUNCTIONS >>>>>> END OF UI FUNCTIONS >>>>>>
<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/


}


