package com.example.glowybits;

import com.example.glowybits.rcp.ChangeSettings;
import com.example.glowybits.rcp.ChangeSettings.Mode;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.SeekBar;

public class MainSettingsFragment extends Fragment {
  private View rootView;
  
  public MainSettingsFragment() { }
  
  @Override
  public void onStart() {
    super.onStart();
    Log.i("MainSettingsActivity", "MainSettingsActivity::onStart()");
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    Log.i("MainSettingsActivity", "MainSettingsActivity::onCreate()");
    super.onCreate(savedInstanceState);
  }
  
  @Override 
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    rootView = inflater.inflate(R.layout.activity_main_settings, container, false);  
    
    Log.i("MainSettingsActivity", "MainSettingsActivity::onCreateView()");
    
    OnClickListener ocl = new OnClickListener() {
      public void onClick(View rb) { MainSettingsFragment.this.setMode(rb); }
    };
    
    RadioButton radio = (RadioButton)rootView.findViewById(R.id.radio_diamonds);
    radio.setOnClickListener(ocl);

    radio = (RadioButton)rootView.findViewById(R.id.radio_stars);
    radio.setOnClickListener(ocl);

    radio = (RadioButton)rootView.findViewById(R.id.radio_spiral);
    radio.setOnClickListener(ocl);
    
    radio = (RadioButton)rootView.findViewById(R.id.radio_joint);
    radio.setOnClickListener(ocl);
    
    
    getBrightnessControl().setOnSeekBarChangeListener(new ProgressChanger() {
      @Override
      public void onProgressChanged(SeekBar pbar, int position, boolean arg2) { 
        PoiSyncService.getSelf().brightnessAdaptor.changeValue(pbar);
      }
    });

    getSpeedControl().setOnSeekBarChangeListener(new ProgressChanger() {
      @Override
      public void onProgressChanged(SeekBar pbar, int position, boolean arg2) {
        PoiSyncService.getSelf().speedAdaptor.changeValue(pbar);
      }
    });

    getColorSpeedControl().setOnSeekBarChangeListener(new ProgressChanger() {
      @Override
      public void onProgressChanged(SeekBar pbar, int position, boolean arg2) {
        PoiSyncService.getSelf().colorSpeedAdaptor.changeValue(pbar);
      }
    });

    getWidthControl().setOnSeekBarChangeListener(new ProgressChanger() {
      @Override
      public void onProgressChanged(SeekBar pbar, int position, boolean arg2) {
        PoiSyncService.getSelf().widthAdaptor.changeValue(pbar);
      }
    });
    
    restoreDefaults();
    
    // Inflate the layout for this fragment
    return rootView;
  }
  
  public void setMode(View v) {
    PoiSyncService.getSelf().setMode(getMode());
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

    radio = (RadioButton)rootView.findViewById(R.id.radio_stars);
    if (radio.isChecked())
      return ChangeSettings.Mode.STARS;
    
    radio = (RadioButton)rootView.findViewById(R.id.radio_spiral);
    if (radio.isChecked())
      return ChangeSettings.Mode.SPIRAL;
    
    radio = (RadioButton)rootView.findViewById(R.id.radio_joint);
    if (radio.isChecked())
      return ChangeSettings.Mode.JOINT;
    
    return ChangeSettings.DEFAULT_MODE;
  }

  protected void restoreDefaults() {
    Log.i("MainSettingsActivity", "MainSetttingsActivity::restoreDefaults()");

    getBrightnessControl().setProgress(PoiSyncService.getSelf().brightnessAdaptor.loadValue());
    getSpeedControl()     .setProgress(PoiSyncService.getSelf().speedAdaptor.loadValue());
    getColorSpeedControl().setProgress(PoiSyncService.getSelf().colorSpeedAdaptor.loadValue());
    getWidthControl()     .setProgress(PoiSyncService.getSelf().widthAdaptor.loadValue());
    
    RadioButton b = (RadioButton)(rootView.findViewById(R.id.radio_diamonds));
    int mode = PoiSyncService.getSelf().getMode();

    if (mode == ChangeSettings.Mode.CHASE.ordinal())
      b = (RadioButton)(rootView.findViewById(R.id.radio_diamonds));
    else if (mode == ChangeSettings.Mode.STARS.ordinal())
      b = (RadioButton)(rootView.findViewById(R.id.radio_stars));
    else if (mode == ChangeSettings.Mode.SPIRAL.ordinal())
      b = (RadioButton)(rootView.findViewById(R.id.radio_spiral));
    else if (mode == ChangeSettings.Mode.JOINT.ordinal())
      b = (RadioButton)(rootView.findViewById(R.id.radio_joint));
    
    b.setChecked(true);
    setMode(b);
  }

}
