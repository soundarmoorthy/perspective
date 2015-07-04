package com.flicq.tennis.external;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;

public class DrawableAwesome extends Drawable {
  
  private static final float PADDING_RATIO = 0.88f;

  private final Context context;
  private final int icon;
  private final Paint paint;
  private final int width;
  private final int height;
  private final float size;

  private DrawableAwesome(int icon, int sizeDpi, int color,
                          boolean antiAliased, boolean fakeBold, float shadowRadius,
                          float shadowDx, float shadowDy, int shadowColor, Context context) {
    super();
    this.context = context;
    this.icon = icon;
    this.size = dpToPx(sizeDpi) * PADDING_RATIO;
    this.height = dpToPx(sizeDpi);
    this.width = dpToPx(sizeDpi);
    int color1 = color;
    boolean antiAliased1 = antiAliased;
    boolean fakeBold1 = fakeBold;
    float shadowRadius1 = shadowRadius;
    float shadowDx1 = shadowDx;
    float shadowDy1 = shadowDy;
    int shadowColor1 = shadowColor;
    this.paint = new Paint();
    
    paint.setStyle(Paint.Style.FILL);
    paint.setTextAlign(Paint.Align.CENTER);
    this.paint.setColor(color1);
    this.paint.setTextSize(this.size);
    Typeface font = Typeface.createFromAsset(context.getAssets(), "fontawesome-webfont.ttf");
    this.paint.setTypeface(font);
    this.paint.setAntiAlias(antiAliased1);
    this.paint.setFakeBoldText(fakeBold1);
    this.paint.setShadowLayer(shadowRadius1, shadowDx1, shadowDy1, shadowColor1);
  }
  
  @Override
  public int getIntrinsicHeight() {
    return height;
  }
  
  @Override
  public int getIntrinsicWidth() {
    return  width;
  }

  @Override
  public void draw(Canvas canvas) {
    float xDiff = (width/2.0f);
    String stringIcon = this.context.getResources().getString(icon);
    canvas.drawText(stringIcon, xDiff, size, paint);
  }

  @Override
  public int getOpacity() {
    return PixelFormat.TRANSLUCENT;
  }

  @Override
  public void setAlpha(int alpha) {
    paint.setAlpha(alpha);
  }

  @Override
  public void setColorFilter(ColorFilter cf) {
    paint.setColorFilter(cf);
  }

  private int dpToPx(int dp) {
    DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
    return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
  }
  
  public static class DrawableAwesomeBuilder {
    private final Context context;
    private final int icon;
    private int sizeDpi = 32;
    private int color = Color.GRAY;
    private boolean antiAliased = true;
    private boolean fakeBold = true;
    private float shadowRadius = 0;
    private float shadowDx = 0;
    private float shadowDy = 0;
    private int shadowColor = Color.WHITE;
    
    public DrawableAwesomeBuilder(Context context, int icon) {
      this.context = context;
      this.icon = icon;
    }

    public void setSize(int size) {
      this.sizeDpi = size;
    }

    public void setColor(int color) {
      this.color = color;
    }

    public void setAntiAliased(boolean antiAliased) {
      this.antiAliased = antiAliased;
    }

    public void setFakeBold(boolean fakeBold) {
      this.fakeBold = fakeBold;
    }
    
    public void setShadow(float radius, float dx, float dy, int color) {
      this.shadowRadius = radius;
      this.shadowDx = dx;
      this.shadowDy = dy;
      this.shadowColor = color;
    }
    
    public DrawableAwesome build() {
      return new DrawableAwesome(icon, sizeDpi, color, antiAliased, fakeBold,
          shadowRadius, shadowDx, shadowDy, shadowColor, context);
    }
  }
}
