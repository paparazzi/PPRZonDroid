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

/**
 * Handle Settings
 */
package com.PPRZonDroid;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

/**
 * SettingsActivity content
 */

public class SettingsActivity extends Activity {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Display the fragment as the main content.
    getFragmentManager().beginTransaction()
            .replace(android.R.id.content, new SettingsFragment())
            .commit();
  }

}

class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

  public static final String USE_GPS = "use_gps_checkbox";
  public static final String SERVER_IP_ADDRESS = "server_ip_adress_text";
  public static final String SERVER_PORT_ADDRESS = "server_port_number_text";
  public static final String LOCAL_PORT_ADDRESS = "local_port_number_text";
  public static final String MIN_AIRSPEED = "minimum_air_speed";
  public static final String BLOCK_C_TIMEOUT = "block_change_timeout";


  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Load the preferences from an XML resource
    addPreferencesFromResource(R.xml.preferences);
    Preference connectionPref = findPreference(SERVER_IP_ADDRESS);
    connectionPref.setSummary(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(SERVER_IP_ADDRESS, ""));

    Preference connectionPref2 = findPreference(SERVER_PORT_ADDRESS);
    connectionPref2.setSummary(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(SERVER_PORT_ADDRESS, ""));

    Preference connectionPref3 = findPreference(LOCAL_PORT_ADDRESS);
    connectionPref3.setSummary(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(LOCAL_PORT_ADDRESS, ""));

    Preference connectionPref4 = findPreference(MIN_AIRSPEED);
    connectionPref4.setSummary((PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(MIN_AIRSPEED, "")) + " m/s");

      Preference connectionPref5 = findPreference(BLOCK_C_TIMEOUT);
      connectionPref5.setSummary((PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(BLOCK_C_TIMEOUT, "")) + " ms");
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    if (key.equals(SERVER_IP_ADDRESS)) {
      Preference connectionPref = findPreference(key);
      //Set summary to be the user-description for the selected value
      connectionPref.setSummary(sharedPreferences.getString(key, ""));
    }

    if (key.equals(SERVER_PORT_ADDRESS)) {
      Preference connectionPref = findPreference(key);
      //Set summary to be the user-description for the selected value
      connectionPref.setSummary(sharedPreferences.getString(key, ""));
    }

    if (key.equals(LOCAL_PORT_ADDRESS)) {
      Preference connectionPref = findPreference(key);
      //Set summary to be the user-description for the selected value
      connectionPref.setSummary(sharedPreferences.getString(key, ""));
    }

    if (key.equals(MIN_AIRSPEED)) {
      Preference connectionPref = findPreference(key);
      //Set summary to be the user-description for the selected value
      connectionPref.setSummary(sharedPreferences.getString(key, "") + " m/s");
    }

    if (key.equals(BLOCK_C_TIMEOUT)) {
        Preference connectionPref = findPreference(key);
        //Set summary to be the user-description for the selected value
        assert connectionPref != null;
        connectionPref.setSummary(sharedPreferences.getString(key, "") + " ms");
    }

    //Log.d("PPRZ_info", "Preference changed");
  }

  @Override
  public void onResume() {
    super.onResume();
    getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

  }

  @Override
  public void onPause() {
    super.onPause();
    getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
  }
}