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
import android.content.res.Configuration;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MainActivity extends Activity {
  public BluetoothServiceConnection mConnection;
  private BluetoothReceiver mBluetoothReceiver;
  private Map<String, TextView> statusViews = new HashMap<String, TextView>();

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
        updateDevice(addr, addr + " + ");
      }
      
      @Override
      public void disconnected(String addr) {
        super.disconnected(addr);
        removeDevice(addr);
      }

      @Override
      public void ping(String addr, double ping, int fps) {
        updateDevice(addr, String.format("%s (%3.2fms, % 3dfps)", addr, ping, fps));
      }
    };
    LocalBroadcastManager.getInstance(this).registerReceiver(mBluetoothReceiver, new IntentFilter("message"));
  }
  

  protected void addDevice(String addr) {
    LinearLayout devices_view = (LinearLayout)findViewById(R.id.devices);
    TextView v = new TextView(MainActivity.this);
    v.setText(addr);
    statusViews.put(addr, v);
    
    devices_view.addView(v);
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
  
  protected void populateStatusViews() {
    SeekBar sb = (SeekBar)this.findViewById(R.id.brightness);
    sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar arg0, int position, boolean arg2) {
        double pos = (double)position / 1000;
        pos = Math.pow(pos, 2);
        changeBrightness((int) (pos * 255));
      }
      @Override
      public void onStartTrackingTouch(SeekBar arg0) { }
      @Override
      public void onStopTrackingTouch(SeekBar arg0) { }  
    });
    
    sb = (SeekBar)this.findViewById(R.id.speed);
    sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar arg0, int position, boolean arg2) {
        double pos = (double)position / 1000;
        MainActivity.this.changeSpeed((float) pos);
      }
      @Override
      public void onStartTrackingTouch(SeekBar arg0) { }
      @Override
      public void onStopTrackingTouch(SeekBar arg0) { }  
    });
    

    sb = (SeekBar)this.findViewById(R.id.color_speed);
    sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar arg0, int position, boolean arg2) {
        double pos = (double)position / 1000;
        MainActivity.this.changeColorSpeed((float) pos);
      }
      @Override
      public void onStartTrackingTouch(SeekBar arg0) { }
      @Override
      public void onStopTrackingTouch(SeekBar arg0) { }  
    });

    sb = (SeekBar)this.findViewById(R.id.width);
    sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar arg0, int position, boolean arg2) {
        double pos = (double)position / 1000;
        MainActivity.this.changeWidth((float) pos);
      }
      @Override
      public void onStartTrackingTouch(SeekBar arg0) { }
      @Override
      public void onStopTrackingTouch(SeekBar arg0) { }  
    });
    
    
    LinearLayout devices_view = (LinearLayout)findViewById(R.id.devices);
    devices_view.removeAllViews();
    for(BluetoothConnection bc : mConnection.getService().getConnections().values()) {
      addDevice(bc.getDevice().getAddress());
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

}
