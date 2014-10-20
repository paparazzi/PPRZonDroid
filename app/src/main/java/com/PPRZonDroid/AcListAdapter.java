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

/**
 * Aircraft list adapter
 * Created by savas on 1/26/14.
 */

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class AcListAdapter extends ArrayAdapter<Model> {

  private final Context context;
  private final ArrayList<Model> modelsArrayList;
  public int SelectedInd = -1;

  public AcListAdapter(Context context, ArrayList<Model> modelsArrayList) {

    super(context, R.layout.aclist, modelsArrayList);


    this.context = context;
    this.modelsArrayList = modelsArrayList;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {

    // 1. Create inflater
    LayoutInflater inflater = (LayoutInflater) context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    // 2. Get rowView from inflater

    View rowView = null;


    rowView = inflater.inflate(R.layout.aclist, parent, false);
      TextView AC_Name=(TextView) rowView.findViewById(R.id.ac_name);

    if (position == SelectedInd) {
      rowView.setBackgroundColor(Color.parseColor("#479cd5"));
      AC_Name.setTextColor(Color.WHITE);
    }
    // 3. Get icon,ac_name & ac_battery views from the rowView
    ImageView imgView = (ImageView) rowView.findViewById(R.id.item_icon);
    TextView titleView = (TextView) rowView.findViewById(R.id.ac_name);
    TextView counterView = (TextView) rowView.findViewById(R.id.ac_battery);


    // 4. Set the text for textView
    imgView.setImageBitmap(modelsArrayList.get(position).getIcon());
    titleView.setText(modelsArrayList.get(position).getTitle());
    counterView.setText(modelsArrayList.get(position).getCounter());

    return rowView;
  }
}