package com.example.glowybits;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.example.glowybits.rcp.ChangeSettings;
import com.example.glowybits.rcp.ChangeSettings.Mode;
import com.example.glowybits.rcp.RpcMessage;

import android.os.Bundle;
import android.os.ParcelUuid;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
import android.widget.Toast;

public class MainActivity extends Activity {
  private Map<String, TextView> statusViews = new HashMap<String, TextView>();
  private OnSeekBarChangeListener brightnessListener;
  private OnSeekBarChangeListener speedListener;
  private OnSeekBarChangeListener widthListener;
  private OnSeekBarChangeListener colorSpeedListener;
  private PoiSync poi_sync;
  
  private class PoiSync extends Thread {
    Set<BluetoothConnection> connections = new HashSet<BluetoothConnection>();
    private BluetoothAdapter mBluetoothAdapter;
    private MainActivity mmMainActivity;
    public boolean running = true;
    
    public PoiSync(MainActivity ma) {
      mmMainActivity = ma;
      mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
      
      for(BluetoothDevice bd : mBluetoothAdapter.getBondedDevices()) {
        connections.add(new BluetoothConnection(bd));
      }
         
    }
    @Override
    public void run() {
      while(running) {
        double pos = (double)mmMainActivity.getBrightnessControl().getProgress() / 1000;
        int brightness = (int) (Math.pow(pos, 2)*255);
        float speed = (float)(mmMainActivity.getSpeedControl().getProgress()) / 1000;
        float rainbow_speed = (float)(mmMainActivity.getColorSpeedControl().getProgress()) / 1000;
        float width = (float)(mmMainActivity.getWidthControl().getProgress()) / 1000;
        
        
        RpcMessage.Builder msg = new RpcMessage.Builder().settings(
            new ChangeSettings.Builder()
              .mode(getMode())
              .brightness(brightness)
              .speed(speed)
              .rainbow_speed(rainbow_speed)
              .width(width)
              .build()
            ); 

        try {
          for(BluetoothConnection bc : connections) {
            try {
              bc.request(msg);
            } catch (InterruptedException e) {
              e.printStackTrace();
            } catch (IOException e) {
              e.printStackTrace();
              sleep(5000);
            }
          }
        
          sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      Log.i("MainActivity", "Stopping sync thread");
    }
  };

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

  public void setChaseMode(View v) {
    final SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();

    editor.putInt("mode", ChangeSettings.Mode.CHASE.ordinal());
    editor.apply();
  }
  
  public void setStarsMode(View v) {
    final SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();

    editor.putInt("mode", ChangeSettings.Mode.STARS.ordinal());
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
    
    return ChangeSettings.DEFAULT_MODE;
  }

  protected void restoreDefaults() {
    final SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
    getBrightnessControl().setProgress(sharedPref.getInt("brightness", 500));
    getSpeedControl().setProgress(sharedPref.getInt("speed", 500));
    getColorSpeedControl().setProgress(sharedPref.getInt("color_speed", 500));
    getWidthControl().setProgress(sharedPref.getInt("width", 500));

    RadioButton b;
    int mode = sharedPref.getInt("mode", ChangeSettings.DEFAULT_MODE.ordinal());
    if (mode == ChangeSettings.Mode.STARS.ordinal()) {
      b = (RadioButton)(this.findViewById(R.id.radio_diamonds));
      b.setChecked(false);
      
      b = (RadioButton)(this.findViewById(R.id.radio_stars));
      b.setChecked(true);
    } else {
      b = (RadioButton)(this.findViewById(R.id.radio_diamonds));
      b.setChecked(true);
      
      b = (RadioButton)(this.findViewById(R.id.radio_stars));
      b.setChecked(false);
    }
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
