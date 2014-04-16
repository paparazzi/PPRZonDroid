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
 * PFD image creation method is Kevin Hester from arduleader project. Modified by Savas Sen.
 * https://github.com/geeksville/arduleader
 */


package com.PPRZonDroid;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;

public class uiGraphics {


  double roll = Double.NaN, pitch = 0, yaw = 0;
  Paint grid_paint = new Paint();
  Paint ground = new Paint();
  Paint sky = new Paint();
  Paint white = new Paint();
  Paint whiteCenter = new Paint();
  Paint whitebar = new Paint();
  Paint whiteStroke = new Paint();
  Paint statusText = new Paint();
  Paint plane = new Paint();
  Paint redSolid = new Paint();
  GradientDrawable groundGradient = new GradientDrawable(
          GradientDrawable.Orientation.TOP_BOTTOM, new int[]{0xc0d4a017, 0xc0835c3b}); // Caramel to Brown Bear
  /*create pdf background
  Thanks for Kevin Hester from arduleader project. Most codes are are used from HUD class of his project.
  https://github.com/geeksville/arduleader
 */
  private int width;
  private Integer height;
  private String altitude = "";
  private String remainBatt = "";
  private String battVolt = "";
  private String gpsFix = "";

  /**
   * Second attempt to parse color
   *
   * @param ColorStr
   * @return
   */
  private int get_long_color(String ColorStr) {

    //get red, green , blue values from incoming str (remove transparency)
    try {
      if (ColorStr.length() > 9) {
        String ncolor;
        ncolor = "#ff" + ColorStr.substring(1, 3) + ColorStr.substring(5, 7) + ColorStr.substring(9, 11);

        return Color.parseColor(ncolor);
      }
      return android.R.color.white;

    } catch (Exception ex) {
      //Stg wrong with color parsing return
      return android.R.color.white;
    }

  }

  /**
   * First attempt to parse color
   *
   * @param ColorStr
   * @return
   */
  public int get_color(String ColorStr) {

    int mColor;
    try {
      mColor = Color.parseColor(ColorStr);
      return mColor;
    } catch (Exception ex) {
      return (get_long_color(ColorStr));
    }

  }

  /**
   * Create AC carrot
   *
   * @param AcColor
   * @param GraphicsScaleFactor
   * @return
   */
  public Bitmap create_ac_carrot(String AcColor, float GraphicsScaleFactor) {


    int w = (int) (15 * GraphicsScaleFactor);
    int h = (int) (10 * GraphicsScaleFactor);
    Bitmap.Config conf = Bitmap.Config.ARGB_4444; // see other conf types
    Bitmap bmp = Bitmap.createBitmap(w, h, conf); // this creates a MUTABLE bitmapAircraftData[IndexOfAc].AC_Color
    Canvas canvas = new Canvas(bmp);


    Paint p = new Paint();

    p.setColor(Color.parseColor("#E36A19"));


    p.setStyle(Paint.Style.FILL);
    p.setAntiAlias(true);

    Path CApath = new Path();
    CApath.moveTo(2, 2);   //1
    CApath.lineTo((w - 2), 2);    //2
    CApath.lineTo((w / 2), (h - 2));   //3

    //ACpath.lineTo((w/8),(7*h/8));
    CApath.close();
    //border

    canvas.drawPath(CApath, p);

    Paint cStroke = new Paint();


    cStroke.setColor(get_color(AcColor));


    cStroke.setStyle(Paint.Style.STROKE);
    cStroke.setStrokeWidth(2f);
    cStroke.setAntiAlias(true);
    canvas.drawPath(CApath, cStroke);

    return bmp;
  }

  private Canvas create_selected_canvas(Canvas CanvIn, int AcColor, float GraphicsScaleFactor) {

    int w = CanvIn.getWidth();
    int h = CanvIn.getHeight();

    float SelLineLeng = 4 * GraphicsScaleFactor;

    Path SelPath = new Path();
    SelPath.moveTo(0, 0); //1
    SelPath.lineTo(SelLineLeng, 0);
    SelPath.moveTo(0, 0);
    SelPath.lineTo(0, SelLineLeng);
    SelPath.moveTo(w, 0); //2
    SelPath.lineTo(w - SelLineLeng, 0);
    SelPath.moveTo(w, 0);
    SelPath.lineTo(w, SelLineLeng);
    SelPath.moveTo(w, h);   //3
    SelPath.lineTo(w, h - SelLineLeng);
    SelPath.moveTo(w, h);
    SelPath.lineTo(w - SelLineLeng, h);
    SelPath.moveTo(0, h);
    SelPath.lineTo(0, h - SelLineLeng);
    SelPath.moveTo(0, h);
    SelPath.lineTo(SelLineLeng, h);

    Paint p = new Paint();
    //p.setColor(AcColor);
    p.setColor(Color.YELLOW);
    p.setAntiAlias(true);
    p.setStyle(Paint.Style.STROKE);
    p.setStrokeWidth(3 * GraphicsScaleFactor);
    CanvIn.drawPath(SelPath, p);
    return CanvIn;
  }

  /**
   * Method create AC Logo
   *
   * @param AcType      AC Type (fixedwing,rotorcraft or flyingwing. default :fixedwing ...
   * @param AcColorStrX Ac Color
   * @param GraphicsScaleFactor  Logo Scale factor
   * @return Bitmap
   */
  public Bitmap create_ac_icon(String AcType, String AcColorStrX, float GraphicsScaleFactor, boolean AcSelected) {

    //validate color string
    int AcColor = get_color(AcColorStrX);


    if (AcType.equals("rotorcraft")) {


      int w = (int) (34 * GraphicsScaleFactor);
      int h = (int) (34 * GraphicsScaleFactor);
      Bitmap.Config conf = Bitmap.Config.ARGB_4444; // see other conf types
      Bitmap bmp = Bitmap.createBitmap(w, h, conf); // this creates a MUTABLE bitmapAircraftData[IndexOfAc].AC_Color
      Canvas canvas = new Canvas(bmp);

      if (AcSelected) {
        canvas = create_selected_canvas(canvas, AcColor, GraphicsScaleFactor);
      }

      //Create rotorcraft logo
      Paint p = new Paint();

      p.setColor(AcColor);


      p.setStyle(Paint.Style.STROKE);
      //p.setStrokeWidth(2f);
      p.setAntiAlias(true);

      Path ACpath = new Path();
      ACpath.moveTo((3 * w / 16), (h / 2));
      ACpath.addCircle(((3 * w / 16) + 1), (h / 2), ((3 * w / 16) - 2), Path.Direction.CW);
      ACpath.moveTo((3 * w / 16), (h / 2));
      ACpath.lineTo((13 * w / 16), (h / 2));
      ACpath.addCircle((13 * w / 16), (h / 2), ((3 * w / 16) - 2), Path.Direction.CW);
      ACpath.addCircle((w / 2), (13 * h / 16), ((3 * w / 16) - 2), Path.Direction.CW);
      ACpath.moveTo((w / 2), (13 * h / 16));
      ACpath.lineTo((w / 2), (5 * h / 16));
      ACpath.lineTo((6 * w / 16), (5 * h / 16));
      ACpath.lineTo((w / 2), (2 * h / 16));
      ACpath.lineTo((10 * w / 16), (5 * h / 16));
      ACpath.lineTo((w / 2), (5 * h / 16));

      canvas.drawPath(ACpath, p);

      Paint black = new Paint();
      black.setColor(Color.BLACK);
      black.setStyle(Paint.Style.STROKE);
      black.setStrokeWidth(6f);
      black.setAntiAlias(true);

      canvas.drawPath(ACpath, black);
      p.setStrokeWidth(3.5f);
      canvas.drawPath(ACpath, p);
      return bmp;
    } else if (AcType.equals("flyingwing")) {

      int w = (int) (30 * GraphicsScaleFactor);
      int h = (int) (15 * GraphicsScaleFactor);
      Bitmap.Config conf = Bitmap.Config.ARGB_4444; // see other conf types
      Bitmap bmp = Bitmap.createBitmap(w, h, conf); // this creates a MUTABLE bitmapAircraftData[IndexOfAc].AC_Color
      Canvas canvas = new Canvas(bmp);


      if (AcSelected) {
        canvas = create_selected_canvas(canvas, AcColor, GraphicsScaleFactor);
      }

      //Create flyingwing logo
      Paint p = new Paint();

      p.setColor(AcColor);

      p.setStyle(Paint.Style.FILL);
      p.setAntiAlias(true);

      Path ACpath = new Path();
      ACpath.moveTo(2, (h - 2));   //1
      ACpath.lineTo(2, (3 * h / 4));    //2
      ACpath.lineTo((w / 2), 2);   //3
      ACpath.lineTo((w - 2), (3 * h / 4));   //4
      ACpath.lineTo((w - 2), (h - 2));  //5
      ACpath.lineTo((w / 2), (5 * h / 8));  //6
      ACpath.lineTo(2, (h - 2));
      //ACpath.lineTo((w/8),(7*h/8));
      ACpath.close();
      //border

      canvas.drawPath(ACpath, p);

      Paint black = new Paint();
      black.setColor(Color.BLACK);
      black.setStyle(Paint.Style.STROKE);
      black.setStrokeWidth(2f);
      black.setAntiAlias(true);
      canvas.drawPath(ACpath, black);


      return bmp;
    } else {

      //Create fixedwing logo
      int w = (int) (28 * GraphicsScaleFactor);
      int h = (int) (28 * GraphicsScaleFactor);
      Bitmap.Config conf = Bitmap.Config.ARGB_4444; // see other conf types
      Bitmap bmp = Bitmap.createBitmap(w, h, conf); // this creates a MUTABLE bitmapAircraftData[IndexOfAc].AC_Color
      Canvas canvas = new Canvas(bmp);

      if (AcSelected) {
        canvas = create_selected_canvas(canvas, AcColor, GraphicsScaleFactor);
      }

      Paint p = new Paint();

      p.setColor(AcColor);


      p.setStyle(Paint.Style.FILL);
      p.setAntiAlias(true);

      Path ACpath = new Path();
      ACpath.moveTo(2, (3 * h / 8));      //1
      ACpath.lineTo((13 * w / 32), (5 * h / 16));     //2
      ACpath.lineTo((13 * w / 32), (h / 8));     //3
      ACpath.lineTo((w / 2), 2);     //4
      ACpath.lineTo((19 * w / 32), (h / 8));     //5
      ACpath.lineTo((19 * w / 32), (5 * h / 16));     //6
      ACpath.lineTo((w - 2), (3 * h / 8));     //7
      ACpath.lineTo((w - 2), (h / 2));    //8
      ACpath.lineTo((19 * w / 32), (h / 2));   //9
      ACpath.lineTo((9 * w / 16), (6 * h / 8));  //10
      ACpath.lineTo((3 * w / 4), (13 * h / 16));    //11
      ACpath.lineTo((3 * w / 4), (14 * h / 16));    //12
      ACpath.lineTo((17 * w / 32), (14 * h / 16));    //13
      ACpath.lineTo((17 * w / 32), (h - 2));     //14
      ACpath.lineTo((15 * w / 32), (h - 2));     //15
      ACpath.lineTo((15 * w / 32), (14 * h / 16));    //16
      ACpath.lineTo((w / 4), (14 * h / 16));   //17
      ACpath.lineTo((w / 4), (13 * h / 16));   //18
      ACpath.lineTo((7 * w / 16), (6 * h / 8));  //19
      ACpath.lineTo((13 * w / 32), (h / 2));   //20
      ACpath.lineTo((2), (h / 2));      //21

      ACpath.close();
      //border

      canvas.drawPath(ACpath, p);

      Paint black = new Paint();
      black.setColor(Color.BLACK);
      black.setStyle(Paint.Style.STROKE);
      black.setStrokeWidth(2f);
      black.setAntiAlias(true);
      canvas.drawPath(ACpath, black);

      return bmp;
    }

  }

  //Creates icons for markers
  public Bitmap create_marker_icon(String AcColorX, String MarkerName, float GraphicsScaleFactor) {


    int w = (int) (34 * GraphicsScaleFactor); //24
    int h = (int) (54 * GraphicsScaleFactor);
    Bitmap.Config conf = Bitmap.Config.ARGB_4444; // see other conf types
    Bitmap bmp = Bitmap.createBitmap(w, h, conf); // this creates a MUTABLE bitmapAircraftData[IndexOfAc].AC_Color
    Canvas canvas = new Canvas(bmp);

    Paint p = new Paint();
    p.setColor(get_color(AcColorX));
    p.setStyle(Paint.Style.FILL);
    p.setAntiAlias(true);

    Path MarkerPath = new Path();
    MarkerPath.moveTo((w / 2), (6*h/16));  //point 1
    MarkerPath.lineTo((7*w/16), (5*h/16)); //point 2
    MarkerPath.lineTo((2*w/8), (5*h/16));    //point 3
    MarkerPath.lineTo((2*w/8), (2));    //point 4  (2*h/8)
    MarkerPath.lineTo((6*w/8), (2));   //point 5
    MarkerPath.lineTo((6*w/8), (5*h/16));   //point 6
    MarkerPath.lineTo((9*w/16), (5*h/16)); //point 7

    MarkerPath.close();

    //border
    canvas.drawPath(MarkerPath, p);
    Paint black = new Paint();
    black.setColor(Color.BLACK);
    black.setStyle(Paint.Style.STROKE);
    black.setStrokeWidth(2f);
    black.setAntiAlias(true);
    canvas.drawPath(MarkerPath, black);

    //Text

    black.setTextAlign(Paint.Align.CENTER);
    black.setStrokeWidth(3f * GraphicsScaleFactor);
    black.setTextSize(8f * GraphicsScaleFactor);

    //for DEBUG
    //canvas.drawRect(0,0,w,h,black);


    p.setStyle(Paint.Style.STROKE);
    p.setTextAlign(Paint.Align.CENTER);
    p.setTextSize(8f * GraphicsScaleFactor);
    p.setStrokeWidth(1f * GraphicsScaleFactor);

    canvas.drawText(MarkerName, (w / 2),(7*h/32), black);
    canvas.drawText(MarkerName, (w / 2),(7*h/32), p);

    return bmp;
  }

  public void create_pfd2(Bitmap PfdBit, double rollx, double pitchx, double yawx, String alt, String bat, String GpsStat, float GraphicsScaleFactor) {

    width = (int) (240 * GraphicsScaleFactor);
    height = (int) (140 * GraphicsScaleFactor);

    roll = rollx;
    pitch = pitchx;
    yaw = yawx;
    altitude = alt + "m";
    battVolt = bat + "v";
    gpsFix = GpsStat;

    grid_paint.setColor(Color.rgb(100, 100, 100));
    grid_paint.setAntiAlias(true);

    groundGradient.setCornerRadius(0);
    groundGradient.setStroke(0, 0);

    // Per http://www.computerhope.com/htmcolor.htm
    // Copper
    ground.setARGB(220, 185, 103, 17);
    ground.setAntiAlias(true);
    sky.setARGB(220, 55, 165, 239);
    sky.setAntiAlias(true);
    whitebar.setARGB(25, 255, 255, 255);
    whitebar.setAntiAlias(true);

    white.setColor(Color.WHITE);
    white.setTextSize(9.0f * GraphicsScaleFactor);
    white.setAntiAlias(true);

    whiteCenter.setColor(Color.WHITE);
    whiteCenter.setTextSize(9.0f * GraphicsScaleFactor);
    whiteCenter.setTextAlign(Paint.Align.CENTER);
    whiteCenter.setAntiAlias(true);

    statusText.setColor(Color.WHITE);
    statusText.setTextSize(15.0f * GraphicsScaleFactor);
    statusText.setAntiAlias(true);

    whiteStroke.setColor(Color.WHITE);
    whiteStroke.setStyle(Paint.Style.STROKE);
    whiteStroke.setStrokeWidth(3);
    whiteStroke.setAntiAlias(true);

    plane.setColor(Color.RED);
    plane.setStyle(Paint.Style.STROKE);
    plane.setStrokeWidth(3);
    plane.setAntiAlias(true);

    redSolid.setColor(Color.RED);
    redSolid.setAntiAlias(true);


    Canvas canvas = new Canvas(PfdBit);

    // clear screen
    canvas.drawColor(Color.rgb(20, 20, 20));

    canvas.translate(width / 2, height / 2);

    canvas.save();
    drawPitch(canvas);
    canvas.restore();
    canvas.save();
    drawRoll(canvas);
    canvas.restore();
    canvas.save();
    drawYaw(canvas);
    canvas.restore();
    canvas.save();
    drawText(canvas);
    canvas.restore();
    canvas.save();
    drawPlane(canvas);
    canvas.restore();

   // return bmp;

  }

  private void drawPlane(Canvas canvas) {
    canvas.drawCircle(0, 0, 15, plane);

    canvas.drawLine(-15, 0, -25, 0, plane);
    canvas.drawLine(15, 0, 25, 0, plane);
    canvas.drawLine(0, -15, 0, -25, plane);

  }

  private void drawText(Canvas canvas, int i, String text, Paint p, boolean left) {
    Rect bounds = new Rect();
    p.getTextBounds(text, 0, text.length(), bounds);

    float y = (float) (height / 2.0 - i * bounds.height() * 1.2)
            - (float) bounds.height() * 0.3f;

    if (left)
      canvas.drawText(text,
              (float) (-width / 2.0 + bounds.height() * .2f), y, p);
    else
      canvas.drawText(
              text,
              (float) (width / 2.0 - bounds.width() - bounds.height() * .2f),
              y, p);

  }

  private void drawText(Canvas canvas) {
    drawText(canvas, 1, gpsFix, statusText, true);
    drawText(canvas, 0, altitude, statusText, true);

    //drawText(canvas, 1, remainBatt, statusText, false);
    drawText(canvas, 0, battVolt, statusText, false);

  }

  private void drawYaw(Canvas canvas) {
    canvas.drawRect(-width, -height / 2, width, -height / 2 + 30, sky);
    canvas.drawLine(-width, -height / 2 + 30, width, -height / 2 + 30,
            white);

    // width / 2 == yawPosition
    // then round to nearest 5 degrees, and draw it.

    double centerDegrees = yaw;
    double numDegreesToShow = 50;
    double degreesPerPixel = (double) width / numDegreesToShow;

    double mod = yaw % 5;
    for (double angle = (centerDegrees - mod) - numDegreesToShow / 2.0; angle <= (centerDegrees - mod)
            + numDegreesToShow / 2.0; angle += 5) {

      // protect from wraparound
      double workAngle = (angle + 360.0);
      while (workAngle >= 360)
        workAngle -= 360.0;

      // need to draw "angle"
      // How many pixels from center should it be?
      int distanceToCenter = (int) ((angle - centerDegrees) * degreesPerPixel);

      canvas.drawLine(distanceToCenter, -height / 2 + 20,
              distanceToCenter, -height / 2 + 40, white);

      if (workAngle % 90 == 0) {
        String compass[] = {"N", "E", "S", "W"};
        int index = (int) workAngle / 90;
        canvas.drawText(compass[index], distanceToCenter,
                -height / 2 + 25, whiteCenter);

      } else
        canvas.drawText((int) (workAngle) + "", distanceToCenter,
                -height / 2 + 25, whiteCenter);

    }

    // Draw the center line
    canvas.drawLine(0, -height / 2, 0, -height / 2 + 40, plane);

  }

  private void drawRoll(Canvas canvas) {

    int r = (int) ((double) width * 0.35); // 250;
    RectF rec = new RectF(-r, -height / 2 + 60, r, -height / 2 + 60 + 2 * r);

    // Draw the arc
    canvas.drawArc(rec, -180 + 45, 90, false, whiteStroke);

    // draw the ticks
    // The center of the circle is at:
    // 0, -height/2 + 60 + r

    float centerY = -height / 2 + 65 + r;
    for (int i = -45; i <= 45; i += 15) {
      // Draw ticks
      float dx = (float) Math.sin(i * Math.PI / 180) * r;
      float dy = (float) Math.cos(i * Math.PI / 180) * r;
      canvas.drawLine(dx, centerY - dy, (dx + (dx / 25)), centerY
              - (dy + dy / 25), whiteStroke);

      // Draw the labels
      if (i != 0) {
        dx = (float) Math.sin(i * Math.PI / 180) * (r - 30);
        dy = (float) Math.cos(i * Math.PI / 180) * (r - 30);
        canvas.drawText(Math.abs(i) + "", dx, centerY - dy, whiteCenter);

      }
    }

    float dx = (float) Math.sin(-roll * Math.PI / 180) * r;
    float dy = (float) Math.cos(-roll * Math.PI / 180) * r;
    canvas.drawCircle(dx, centerY - dy, 10, redSolid);

  }

  private void drawPitch(Canvas canvas) {

    int step = 40; // Pixels per 5 degree step

    canvas.translate(0, (int) (pitch * (step / 5)));
    canvas.rotate(-(int) roll);

    // Draw the background box - FIXME, doesn't yet work - WTF
    // groundGradient.setBounds(-width, 0, width, 2 * height);
    // groundGradient.draw(canvas);

    canvas.drawRect(-width, 0, width, 5 * height /* Go plenty low */, ground);
    canvas.drawRect(-width, -5 * height /* Go plenty high */, width, 0, sky);
    canvas.drawRect(-width, -20, width, 20, whitebar);

    // Draw the vertical grid
    canvas.drawLine(-width, 0, width, 0, white);
    // canvas.f

    for (int i = -step * 20; i < step * 20; i += step) {
      if (i != 0) {
        if (i % (2 * step) == 0) {
          canvas.drawLine(-50, i, 50, i, white);
          canvas.drawText((5 * i / -step) + "", -90, i + 5, white);

        } else
          canvas.drawLine(-20, i, 20, i, white);
      }
    }
  }


}
