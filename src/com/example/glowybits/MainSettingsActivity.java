package com.example.glowybits;

import java.util.HashMap;
import java.util.Map;

import com.example.glowybits.rcp.ChangeSettings;
import com.example.glowybits.rcp.ChangeSettings.Mode;

import android.os.Bundle;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainSettingsActivity extends Fragment {
  private Map<String, TextView> statusViews = new HashMap<String, TextView>();
  private PoiSync poi_sync;
  private View rootView;

  public MainSettingsActivity() { }
  
  @Override
  public void onStart() {
    super.onStart();
    Log.i("MainActivity", "MainActivity::onStart()");
  }
  
  public final PoiSync getPoiSync() {
    return poi_sync;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    Log.i("MainActivity", "MainActivity::onCreate()");
    super.onCreate(savedInstanceState);
   
  }
  
  @Override 
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    rootView = inflater.inflate(R.layout.activity_main_settings, container, false);  
    
    Log.i("MainActivity", String.format("Thread: %d", Thread.currentThread().getId()));
    
    OnClickListener ocl = new OnClickListener() {
      public void onClick(View rb) { MainSettingsActivity.this.setMode(rb); }
    };
    
    RadioButton radio = (RadioButton)rootView.findViewById(R.id.radio_diamonds);
    radio.setOnClickListener(ocl);

    radio = (RadioButton)rootView.findViewById(R.id.radio_lines);
    radio.setOnClickListener(ocl);

    radio = (RadioButton)rootView.findViewById(R.id.radio_spiral);
    radio.setOnClickListener(ocl);
    
    getBrightnessControl().setOnSeekBarChangeListener(new ProgressChanger() {
      @Override
      public void onProgressChanged(SeekBar pbar, int position, boolean arg2) { 
        MainSettingsActivity.this.changeBrightness(pbar);
      }
    });

    getSpeedControl().setOnSeekBarChangeListener(new ProgressChanger() {
      @Override
      public void onProgressChanged(SeekBar pbar, int position, boolean arg2) {
        MainSettingsActivity.this.changeSpeed(pbar);
      }
    });

    getColorSpeedControl().setOnSeekBarChangeListener(new ProgressChanger() {
      @Override
      public void onProgressChanged(SeekBar pbar, int position, boolean arg2) {
        MainSettingsActivity.this.changeRainbowSpeed(pbar);
      }
    });

    getWidthControl().setOnSeekBarChangeListener(new ProgressChanger() {
      @Override
      public void onProgressChanged(SeekBar pbar, int position, boolean arg2) {
        MainSettingsActivity.this.changeWidth(pbar);
      }
    });
    // Inflate the layout for this fragment
    return rootView;
  }

  public void changeBrightness(SeekBar pbar) {
    int position = pbar.getProgress();
    int brightness = (int) (Math.pow((double)position / 1000, 2)*255);

    getPoiSync().setBrightness(brightness);
    
    SharedPreferences sharedPref = rootView.getContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPref.edit();
    editor.putInt("brightness", position);
    editor.apply();
  }
  
  public void changeSpeed(SeekBar pbar) {
    int position = pbar.getProgress();
    float speed = (float)(position) / 1000;
    
    getPoiSync().setSpeed(speed);

    SharedPreferences sharedPref = rootView.getContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPref.edit();
    editor.putInt("speed", position);
    editor.apply();
  }
  
  public void changeRainbowSpeed(SeekBar pbar) {
    int position = pbar.getProgress();   
    float rainbow_speed = (float)(position) / 1000;
    
    getPoiSync().setRainbowSpeed(rainbow_speed);

    SharedPreferences sharedPref = rootView.getContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPref.edit();
    editor.putInt("color_speed", position);
    editor.apply();
  }
  
  public void changeWidth(SeekBar pbar) {
    int position = pbar.getProgress();   
    float width = (float)(position) / 1000;
    MainSettingsActivity.this.getPoiSync().setWidth(width);

    SharedPreferences sharedPref = rootView.getContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPref.edit();
    editor.putInt("width", position);
    editor.apply();
  }
  
  public void setMode(View v) {
    Log.i("MainSettingsActivity", "MainSettingsActivity::setMode()");
    final SharedPreferences.Editor editor = rootView.getContext().getSharedPreferences("prefs", Context.MODE_PRIVATE).edit();

    poi_sync.setMode(getMode());
    editor.putInt("mode", getMode().ordinal());
    editor.apply();
  }
  
  protected void addDevice(String addr) {
    LinearLayout devices_view = (LinearLayout)rootView.findViewById(R.id.devices);
    TextView v = statusViews.get(addr);
    if (v == null) {
      v = new TextView(rootView.getContext());
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
    return (SeekBar)rootView.findViewById(R.id.brightness);
  }

  SeekBar getSpeedControl() {
    return (SeekBar)rootView.findViewById(R.id.speed);
  }

  SeekBar getColorSpeedControl() {
    return (SeekBar)rootView.findViewById(R.id.color_speed);
  }

  SeekBar getWidthControl() {
    return (SeekBar)rootView.findViewById(R.id.width);
  }
  
  Mode getMode() {
    RadioButton radio = (RadioButton)rootView.findViewById(R.id.radio_diamonds);
    if (radio.isChecked())
      return ChangeSettings.Mode.CHASE;

    radio = (RadioButton)rootView.findViewById(R.id.radio_lines);
    if (radio.isChecked())
      return ChangeSettings.Mode.LINES;
    
    radio = (RadioButton)rootView.findViewById(R.id.radio_spiral);
    if (radio.isChecked())
      return ChangeSettings.Mode.SPIRAL;
    
    return ChangeSettings.DEFAULT_MODE;
  }

  protected void restoreDefaults() {
    Log.i("MainSettingsActivity", "MainSetttingsActivity::restoreDefaults()");
    final SharedPreferences sharedPref = rootView.getContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);
    SeekBar sb;
    sb = getBrightnessControl();
    
    sb.setProgress(sharedPref.getInt("brightness", 500));
    changeBrightness(sb);
    
    sb = getSpeedControl();
    sb.setProgress(sharedPref.getInt("speed", 500));
    changeSpeed(sb);
    
    sb = getColorSpeedControl();
    sb.setProgress(sharedPref.getInt("color_speed", 500));
    changeRainbowSpeed(sb);
    
    sb = getWidthControl();
    sb.setProgress(sharedPref.getInt("width", 500));
    changeWidth(sb);

    RadioButton b = (RadioButton)(rootView.findViewById(R.id.radio_diamonds));
    int mode = sharedPref.getInt("mode", ChangeSettings.DEFAULT_MODE.ordinal());

    if (mode == ChangeSettings.Mode.CHASE.ordinal())
      b = (RadioButton)(rootView.findViewById(R.id.radio_diamonds));
    else if (mode == ChangeSettings.Mode.LINES.ordinal())
      b = (RadioButton)(rootView.findViewById(R.id.radio_lines));
    else if (mode == ChangeSettings.Mode.SPIRAL.ordinal())
      b = (RadioButton)(rootView.findViewById(R.id.radio_spiral));
    
    b.setChecked(true);
    setMode(b);
    
  }

  public void setPoiSync(PoiSync ps) {
    this.poi_sync = ps;
    restoreDefaults();

  }
}
