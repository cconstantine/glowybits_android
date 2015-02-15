package com.example.glowybits;

import com.example.glowybits.ui_adaptors.ColorAdaptor;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.ColorPicker.OnColorChangedListener;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ColorSettingsFragment extends Fragment {
  private View rootView;
  
  public ColorSettingsFragment() { }
  
  @Override
  public void onStart() {
    super.onStart();
    Log.i("ColorSettingsFragment", "ColorSettingsFragment::onStart()");
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    Log.i("ColorSettingsFragment", "ColorSettingsFragment::onCreate()");
    super.onCreate(savedInstanceState);
  }
  
  @Override 
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    rootView = inflater.inflate(R.layout.color_settings, container, false); 
    
    ColorPicker picker = getPicker1();
    SaturationBar sb = (SaturationBar) rootView.findViewById(R.id.saturationBar1);
    ValueBar vb = (ValueBar) rootView.findViewById(R.id.valueBar1);
    picker.addSaturationBar(sb);
    picker.addValueBar(vb);
    picker.setShowOldCenterColor(false);
    picker.setOnColorChangedListener(PoiSyncService.getSelf().picker1);

    picker = getPicker2();
    sb =     (SaturationBar) rootView.findViewById(R.id.saturationBar2);
    vb =     (ValueBar)      rootView.findViewById(R.id.valueBar2);
    picker.addSaturationBar(sb);
    picker.addValueBar(vb);
    picker.setShowOldCenterColor(false);
    picker.setOnColorChangedListener(PoiSyncService.getSelf().picker2);
    
    picker = getPicker3();
    sb =     (SaturationBar) rootView.findViewById(R.id.saturationBar3);
    vb =     (ValueBar)      rootView.findViewById(R.id.valueBar3);
    picker.addSaturationBar(sb);
    picker.addValueBar(vb);
    picker.setShowOldCenterColor(false);
    picker.setOnColorChangedListener(PoiSyncService.getSelf().picker3);
    

    Log.i("ColorSettingsFragment", "ColorSettingsFragment::onCreateView()");
    
    restoreDefaults();
    
    return rootView;
  }
  
  public ColorPicker getPicker1() {
    return (ColorPicker)rootView.findViewById(R.id.colorPicker1);
  }
  
  public ColorPicker getPicker2() {
    return (ColorPicker)rootView.findViewById(R.id.colorPicker2);
  }
  
  public ColorPicker getPicker3() {
    return (ColorPicker)rootView.findViewById(R.id.colorPicker3);
  }

  protected void restoreDefaults() {
    Log.i("ColorSettingsFragment", "ColorSettingsFragment::restoreDefaults()");

    getPicker1().setColor(PoiSyncService.getSelf().picker1.getColor());
    getPicker2().setColor(PoiSyncService.getSelf().picker2.getColor());
    getPicker3().setColor(PoiSyncService.getSelf().picker3.getColor());
  }

}
