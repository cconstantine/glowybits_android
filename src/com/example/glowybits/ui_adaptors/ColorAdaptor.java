package com.example.glowybits.ui_adaptors;

import com.example.glowybits.PoiSyncService;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.ColorPicker.OnColorChangedListener;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public abstract class ColorAdaptor implements OnColorChangedListener {

  private Editor editor;
  private SharedPreferences sharedPref;
  private String _name;
  protected PoiSyncService poi_sync;

  public ColorAdaptor(Context c, String name) {
    sharedPref = c.getSharedPreferences("prefs", Context.MODE_PRIVATE);
    editor = sharedPref.edit();
    
    _name = name;
    loadValue();
  }
  
  @Override
  public void onColorChanged(int color) {
    setValue(color);
    
    editor.putInt(_name, color);
    editor.apply();
  }
  
  public int getColor() {
    return sharedPref.getInt(_name, 500);
  }
  
  public int loadValue() {
    int color = getColor();
    setValue(color);
    return color;
  }

  abstract public void   setValue(int color);
}
