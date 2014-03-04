package com.example.glowybits;

import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MainActivity extends Activity {
  public BluetoothServiceConnection mConnection;
  private BluetoothReceiver mBluetoothReceiver;
  private Map<String, TextView> statusViews = new HashMap<String, TextView>();
  private OnSeekBarChangeListener brightnessListener;
  private OnSeekBarChangeListener speedListener;
  private OnSeekBarChangeListener widthListener;
  private OnSeekBarChangeListener colorSpeedListener;

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

    mConnection = new BluetoothServiceConnection();

    Intent intent = new Intent(this, BluetoothService.class);
    bindService(intent, mConnection, Context.BIND_AUTO_CREATE);    

    Log.i("MainActivity", String.format("Thread: %d", Thread.currentThread().getId()));

    final SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
    final SharedPreferences.Editor editor = sharedPref.edit();

    brightnessListener = new OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar arg0, int position, boolean arg2) {
        editor.putInt("brightness", position);
        editor.apply();

        double pos = (double)position / 1000;
        pos = Math.pow(pos, 2);
        changeBrightness((int) (pos * 255));
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

        double pos = (double)position / 1000;
        MainActivity.this.changeSpeed((float) pos);
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

        double pos = (double)position / 1000;
        MainActivity.this.changeColorSpeed((float) pos);
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
        double pos = (double)position / 1000;
        MainActivity.this.changeWidth((float) pos);
      }
      @Override
      public void onStartTrackingTouch(SeekBar arg0) { }
      @Override
      public void onStopTrackingTouch(SeekBar arg0) { }  
    };


    mBluetoothReceiver = new BluetoothReceiver() {

      @Override
      public void connecting(String addr) {
        super.connecting(addr);

        addDevice(addr);
        updateDevice(addr, addr + " - ");
      }

      @Override
      public void connected(String addr) {
        super.connected(addr);
        sendDefaults();
        updateDevice(addr, addr + " + ");
      }

      @Override
      public void disconnected(String addr) {
        super.disconnected(addr);
        removeDevice(addr);
        mConnection.getService().connect();
      }

      @Override
      public void ping(String addr, double ping, int fps, float g_load) {
        updateDevice(addr, String.format("%s (%3.2fms, % 3dfps, %3fg)", addr, ping, fps, g_load));
      }
    };
    LocalBroadcastManager.getInstance(this).registerReceiver(mBluetoothReceiver, new IntentFilter("message"));

    populateStatusViews();
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

  protected void updateDevice(String addr, String text) {
    statusViews.get(addr).setText(text);
  }

  public void removeDevice(String addr) {
    LinearLayout devices_view = (LinearLayout)findViewById(R.id.devices);
    View v = statusViews.remove(addr);
    devices_view.removeView(v);
    addDevice(addr);
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

  protected void restoreDefaults() {
    final SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
    getBrightnessControl().setProgress(sharedPref.getInt("brightness", 500));
    getSpeedControl().setProgress(sharedPref.getInt("speed", 500));
    getColorSpeedControl().setProgress(sharedPref.getInt("color_speed", 500));
    getWidthControl().setProgress(sharedPref.getInt("width", 500));
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


    LinearLayout devices_view = (LinearLayout)findViewById(R.id.devices);
    devices_view.removeAllViews();
    if(mConnection.getService() != null) {
      for(BluetoothConnection bc : mConnection.getService().getConnections().values()) {
        addDevice(bc.getDevice().getAddress());
      }
    }
  }

  public void changeMode(View v) {
    mConnection.getService().changeMode();
  }

  public void changeBrightness(int b) {
    mConnection.getService().changeBrightness(b);
  }

  public void changeSpeed(float rate) {
    mConnection.getService().changeSpeed(rate);
  }

  protected void changeColorSpeed(float rate) {
    mConnection.getService().changeColorSpeed(rate);
  }

  protected void changeWidth(float width) {
    mConnection.getService().changeWidth(width);    
  }


  public synchronized void onReconnect(View v) {
    mConnection.getService().reconnect();
  }

  public synchronized void onDisconnect(View v) {
    mConnection.getService().disconnect();
  }


  @Override
  protected void onResume() {
    super.onResume();    
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    LocalBroadcastManager.getInstance(this).unregisterReceiver(mBluetoothReceiver);

    // Unbind from the service
    if (mConnection.isBound()) {
      unbindService(mConnection);
    }
  }



  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    setContentView(R.layout.activity_main);
    populateStatusViews();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle item selection
    switch (item.getItemId()) {
    case R.id.action_reconnect:
      onDisconnect(null);
      return true;
    default:
      return super.onOptionsItemSelected(item);
    }
  }

}
