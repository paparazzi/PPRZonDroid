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
 * TCP comm handling
 */

package com.PPRZonDroid;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class TCPClient {

  public String SERVERIP;
  public int SERVERPORT;
  public boolean DEBUG;
  public boolean TCPConnected = false; //indicates whether
  PrintWriter out;
  BufferedReader in;
  String ReceivedMsg = null;
  private InetAddress serverAddr = null;
  private Socket socket;

  /**
   * Sends the message entered by client to the server
   *
   * @param message text entered by client
   */
  public void sendMessage(String message) {

    if (!TCPConnected) {
      setup_tcp();
    }

    if (out != null && !out.checkError()) {
      out.println(message);
      out.flush();
    }
  }

  /**
   * Create TCP socket
   */
  public void setup_tcp() {

    try {
      serverAddr = InetAddress.getByName(SERVERIP);
      socket = new Socket(serverAddr, SERVERPORT);
      //send the message to the server
      out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
      //receive the message which the server sends back
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      TCPConnected = true;
    } catch (Exception e) {
      e.printStackTrace();
      TCPConnected = false;
    }

  }

  public String readMessage() {


    //TODO neeed to reconnect on connection loss!!
    if (!TCPConnected) {
      setup_tcp();
      return null;
    }
    try {

      if (in.ready()) {
        ReceivedMsg = in.readLine();
        //Log.d("PPRZ_info", "received new data:"+ReceivedMsg);
      } else {
        ReceivedMsg = null;
      }

    } catch (IOException e2) {
      TCPConnected = false;
        if (DEBUG) Log.d("PPRZ_info", "TCP comm problem");
    }
    return ReceivedMsg;
  }


}
