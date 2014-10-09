package com.example.glowybits;

import java.util.HashMap;
import java.util.Map;

import com.example.glowybits.rcp.ChangeSettings;
import com.example.glowybits.rcp.ChangeSettings.Mode;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MainActivity extends Activity {
  private Map<String, TextView> statusViews = new HashMap<String, TextView>();
  private OnSeekBarChangeListener brightnessListener;
  private OnSeekBarChangeListener speedListener;
  private OnSeekBarChangeListener widthListener;
  private OnSeekBarChangeListener colorSpeedListener;
  private PoiSync poi_sync;
  
  @Override
  public void onStart() {
    super.onStart();
    Log.i("MainActivity", "MainActivity::onStart()");
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    Log.i("MainActivity", "MainActivity::onCreate()");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);  

    Log.i("MainActivity", String.format("Thread: %d", Thread.currentThread().getId()));

    final SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
    final SharedPreferences.Editor editor = sharedPref.edit();

    
    brightnessListener = new OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar arg0, int position, boolean arg2) {
        editor.putInt("brightness", position);
        editor.apply();
      }
      @Override
      public void onStartTrackingTouch(SeekBar arg0) { }
      @Override
      public void onStopTrackingTouch(SeekBar arg0) { }  
    };

    speedListener = new OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar arg0, int position, boolean arg2) {
        editor.putInt("speed", position);
        editor.apply();
      }
      @Override
      public void onStartTrackingTouch(SeekBar arg0) { }
      @Override
      public void onStopTrackingTouch(SeekBar arg0) { }  
    };

    colorSpeedListener = new OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar arg0, int position, boolean arg2) {

        editor.putInt("color_speed", position);
        editor.apply();
      }
      @Override
      public void onStartTrackingTouch(SeekBar arg0) { }
      @Override
      public void onStopTrackingTouch(SeekBar arg0) { }  
    };

    widthListener = new OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar arg0, int position, boolean arg2) {
        editor.putInt("width", position);
        editor.apply();
      }
      @Override
      public void onStartTrackingTouch(SeekBar arg0) { }
      @Override
      public void onStopTrackingTouch(SeekBar arg0) { }  
    };

    populateStatusViews();

    poi_sync = new PoiSync(this);
    poi_sync.start();
  }

  public void setMode(View v) {
    final SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();

    editor.putInt("mode", getMode().ordinal());
    editor.apply();
  }
  
  protected void addDevice(String addr) {
    LinearLayout devices_view = (LinearLayout)findViewById(R.id.devices);
    TextView v = statusViews.get(addr);
    if (v == null) {
      v = new TextView(MainActivity.this);
    }
    v.setText(addr);

    if (v.getParent()!= null) {
      ((ViewGroup) v.getParent()).removeView(v);
    }
    if(v.getParent() != devices_view)
      devices_view.addView(v);
    statusViews.put(addr, v);
  }

  SeekBar getBrightnessControl() {
    return (SeekBar)this.findViewById(R.id.brightness);
  }

  SeekBar getSpeedControl() {
    return (SeekBar)this.findViewById(R.id.speed);
  }

  SeekBar getColorSpeedControl() {
    return (SeekBar)this.findViewById(R.id.color_speed);
  }

  SeekBar getWidthControl() {
    return (SeekBar)this.findViewById(R.id.width);
  }
  
  Mode getMode() {
    RadioButton radio = (RadioButton)this.findViewById(R.id.radio_diamonds);
    if (radio.isChecked())
      return ChangeSettings.Mode.CHASE;
    
    radio = (RadioButton)this.findViewById(R.id.radio_stars);
    if (radio.isChecked())
      return ChangeSettings.Mode.STARS;

    radio = (RadioButton)this.findViewById(R.id.radio_lines);
    if (radio.isChecked())
      return ChangeSettings.Mode.LINES;
    
    radio = (RadioButton)this.findViewById(R.id.radio_spiral);
    if (radio.isChecked())
      return ChangeSettings.Mode.SPIRAL;
    
    return ChangeSettings.DEFAULT_MODE;
  }

  protected void restoreDefaults() {
    final SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
    getBrightnessControl().setProgress(sharedPref.getInt("brightness", 500));
    getSpeedControl().setProgress(sharedPref.getInt("speed", 500));
    getColorSpeedControl().setProgress(sharedPref.getInt("color_speed", 500));
    getWidthControl().setProgress(sharedPref.getInt("width", 500));

    RadioButton b = (RadioButton)(this.findViewById(R.id.radio_diamonds));
    int mode = sharedPref.getInt("mode", ChangeSettings.DEFAULT_MODE.ordinal());

    if(mode == ChangeSettings.Mode.STARS.ordinal())
      b = (RadioButton)(this.findViewById(R.id.radio_stars));
    else if (mode == ChangeSettings.Mode.CHASE.ordinal())
      b = (RadioButton)(this.findViewById(R.id.radio_diamonds));
    else if (mode == ChangeSettings.Mode.LINES.ordinal())
      b = (RadioButton)(this.findViewById(R.id.radio_lines));
    else if (mode == ChangeSettings.Mode.SPIRAL.ordinal())
      b = (RadioButton)(this.findViewById(R.id.radio_spiral));
    
    b.setChecked(true);
    
  }

  protected void sendDefaults() {
    final SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
    brightnessListener.onProgressChanged(null,  sharedPref.getInt("brightness",  500), false);
    speedListener.onProgressChanged(null,  sharedPref.getInt("speed",  500), false);
    colorSpeedListener.onProgressChanged(null,  sharedPref.getInt("color_speed",  500), false);
    widthListener.onProgressChanged(null,  sharedPref.getInt("width",  500), false);
    brightnessListener.onProgressChanged(null,  sharedPref.getInt("brightness",  500), false);
  }

  protected void populateStatusViews() {
    restoreDefaults();
    getBrightnessControl().setOnSeekBarChangeListener(brightnessListener);
    getSpeedControl(     ).setOnSeekBarChangeListener(speedListener);
    getColorSpeedControl().setOnSeekBarChangeListener(colorSpeedListener);
    getWidthControl(     ).setOnSeekBarChangeListener(widthListener);    
  }

  @Override
  protected void onResume() {
    super.onResume();    
  }

  @Override
  protected void onDestroy() {
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
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    //getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    setContentView(R.layout.activity_main);
    populateStatusViews();
  }

}
