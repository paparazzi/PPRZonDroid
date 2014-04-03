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

public class Model {

  private Bitmap icon;
  private String AcName;
  private String AcBattery;

  public Model(Bitmap icon, String AcName, String AcBattery) {
    super();
    this.icon = icon;
    this.AcName = AcName;
    this.AcBattery = AcBattery;
  }

  public Bitmap getIcon() {
    return icon;
  }

  public void setIcon(Bitmap icon) {
    this.icon = icon;
  }

  public String getTitle() {
    return AcName;
  }

  public void setTitle(String AcName) {
    this.AcName = AcName;
  }

  public String getCounter() {
    return AcBattery;
  }

  public void setCounter(String AcBattery) {
    this.AcBattery = AcBattery;
  }


}