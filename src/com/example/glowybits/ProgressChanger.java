package com.example.glowybits;

import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

abstract class ProgressChanger implements OnSeekBarChangeListener {
  @Override
  public void onStartTrackingTouch(SeekBar arg0) { }
  @Override
  public void onStopTrackingTouch(SeekBar arg0) { }  
}