package com.example.glowybits.ui_adaptors;

import com.example.glowybits.PoiSyncService;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.widget.SeekBar;

public abstract class SliderAdaptor<ResultType> {

  private Editor editor;
  private SharedPreferences sharedPref;
  private String _name;
  protected PoiSyncService poi_sync;

  public SliderAdaptor(Context c, String name) {
    sharedPref = c.getSharedPreferences("prefs", Context.MODE_PRIVATE);
    editor = sharedPref.edit();
    
    _name = name;
    loadValue();
  }
  
  public void changeValue(SeekBar pbar) {
    int position = pbar.getProgress();

    setValue(getValueFromPosition(position));
    
    editor.putInt(_name, position);
    editor.apply();
  }
  
  
  public int loadValue() {
    int position = sharedPref.getInt(_name, 500);
    setValue(getValueFromPosition(position));
    return position;
  }

  abstract protected ResultType getValueFromPosition(int position);
  abstract public void       setValue(ResultType value);
}
