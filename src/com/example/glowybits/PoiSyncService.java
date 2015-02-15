package com.example.glowybits;

import com.example.glowybits.rcp.ChangeSettings;
import com.example.glowybits.rcp.ChangeSettings.Mode;
import com.example.glowybits.ui_adaptors.ColorAdaptor;
import com.example.glowybits.ui_adaptors.SliderAdaptor;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.IBinder;
import android.util.Log;

public class PoiSyncService extends Service {

  private static PoiSyncService self;
  private PoiSync poi_sync;
  private Editor editor;
  private SharedPreferences sharedPref;
  
  protected SliderAdaptor<Integer> brightnessAdaptor;
  protected SliderAdaptor<Float>   speedAdaptor;
  protected SliderAdaptor<Float>   colorSpeedAdaptor;
  protected SliderAdaptor<Float>   widthAdaptor;
  protected ColorAdaptor           picker1;
  protected ColorAdaptor           picker2;
  protected ColorAdaptor           picker3;

  public static PoiSyncService getSelf() {
    return self;
  }
  
  @Override
  public void onCreate() {
    sharedPref = getBaseContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);
    editor = sharedPref.edit();
    
    PoiSyncService.self = this;
    Log.i("PoiSyncService", "PoiSyncService::onCreate()");
    poi_sync = new PoiSync();
    
    getPoiSync().setMode(Mode.values()[getMode()]);
     
    brightnessAdaptor = new SliderAdaptor<Integer>(getBaseContext(), "brightness") {
      @Override
      protected Integer getValueFromPosition(int position) {
        return (int) (Math.pow((double)position / 1000, 2)*255);
      }
      @Override
      public void setValue(Integer value) {
        getPoiSync().setBrightness(value);        
      }
    };
    brightnessAdaptor.loadValue();
    
    speedAdaptor = new SliderAdaptor<Float>(getBaseContext(), "speed") {
      @Override
      protected Float getValueFromPosition(int position) {
        return (float)(position) / 1000;
      }
      @Override
      public void setValue(Float value) {
        getPoiSync().setSpeed(value);        
      }
    };
    
    colorSpeedAdaptor = new SliderAdaptor<Float>(getBaseContext(), "color_speed") {
      @Override
      protected Float getValueFromPosition(int position) {
        return (float)(position) / 1000;
      }
      @Override
      public void setValue(Float value) {
        getPoiSync().setRainbowSpeed(value);        
      }
    };
    
    widthAdaptor = new SliderAdaptor<Float>(getBaseContext(), "width") {
      @Override
      protected Float getValueFromPosition(int position) {
        return (float)(position) / 1000;
      }
      @Override
      public void setValue(Float value) {
        getPoiSync().setWidth(value);        
      }
    };
    
    picker1 = new ColorAdaptor(getBaseContext(), "color1") {
      @Override
      public void setValue(int color) {
        Log.i("PoiSyncService", String.format("PoiSyncService::color1: %x", color));
        PoiSyncService.this.getPoiSync().setColor1(color);
      }
    };
    picker2 = new ColorAdaptor(getBaseContext(), "color2") {
      @Override
      public void setValue(int color) {
        PoiSyncService.this.getPoiSync().setColor2(color);
      }
    };

    picker3 = new ColorAdaptor(getBaseContext(), "color3") {
      @Override
      public void setValue(int color) {
        PoiSyncService.this.getPoiSync().setColor3(color);
      }
    };


  }
  
  @Override
  public int onStartCommand (Intent intent, int flags, int startId) {
    Log.i("PoiSyncService", "PoiSyncService::onStartCommand()");

    getPoiSync().start();
    return START_STICKY;
  }
  
  @Override
  public void onDestroy() {
    Log.i("PoiSyncService", "PoiSyncService::onDestroy()");

    poi_sync.running = false;
    poi_sync.interrupt();
    try {
      poi_sync.join(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    super.onDestroy();
  }

  @Override
  public IBinder onBind(Intent arg0) {
    return null;
  }
  
  protected PoiSync getPoiSync() {
    return poi_sync;
  }
  
  public void setMode(Mode mode) {
    getPoiSync().setMode(mode);
    editor.putInt("mode", mode.ordinal());
    editor.apply();
  }
  
  public int getMode() {
    return sharedPref.getInt("mode", ChangeSettings.DEFAULT_MODE.ordinal());
  }
  
}
