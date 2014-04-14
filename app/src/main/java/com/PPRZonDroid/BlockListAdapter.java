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

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

public class BlockListAdapter extends ArrayAdapter<BlockModel> {

  private final Context context;
  private final ArrayList<BlockModel> BlockModelArrayList;
  public int SelectedInd = -1;
  public int ClickedInd = -1;
  public ProgressBar BlProgress;
  public int BlColor;

  public BlockListAdapter(Context context, ArrayList<BlockModel> BlockModelArrayList) {

    super(context, R.layout.acblock, BlockModelArrayList);


    this.context = context;
    this.BlockModelArrayList = BlockModelArrayList;

  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {

    // 1. Create inflater
    LayoutInflater inflater = (LayoutInflater) context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    // 2. Get rowView from inflater
    View rowView = null;
    rowView = inflater.inflate(R.layout.acblock, parent, false);
    // 3. Get icon,bl names rowView

    TextView titleView = (TextView) rowView.findViewById(R.id.bl_name);
    //Highlight selected block
    if (position == SelectedInd) {
      rowView.setBackgroundColor(Color.parseColor("#479cd5"));
        titleView.setTextColor(Color.WHITE);

    }

    // 4. Set the textView for block name
    titleView.setText(BlockModelArrayList.get(position).getTitle());

    // 5. Set block color
    View BlColorBar = (View)  rowView.findViewById(R.id.bl_color);
    BlColorBar.setBackgroundColor(BlColor);

    // 6. If item is clicked show clicked item template
      if (position == ClickedInd) {
          //Inflate bl_clicked template
          rowView = inflater.inflate(R.layout.bl_clicked, parent, false);

          //Set animation
          Animation mAnim;
          mAnim= AnimationUtils.loadAnimation(context,R.anim.bl_selected_anim);
          rowView.startAnimation(mAnim);

          //Set the titleView_bl for block name
          TextView titleView_bl = (TextView) rowView.findViewById(R.id.bl_name_clicked);
          titleView_bl.setText(BlockModelArrayList.get(position).getTitle());

          //Set progressbar value
          BlProgress = (ProgressBar) rowView.findViewById(R.id.BLC_Progress);
          //BlProgress.setProgress(SetProgress);

      }

    // 7. return rowView

    return rowView;
  }
}