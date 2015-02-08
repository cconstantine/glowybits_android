package com.example.glowybits;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

import com.example.glowybits.rcp.ChangeSettings;
import com.example.glowybits.rcp.ChangeSettings.Mode;
import com.example.glowybits.rcp.RpcMessage;

class PoiSync extends Thread {
  Set<BluetoothConnection> connections = new HashSet<BluetoothConnection>();
  private BluetoothAdapter mBluetoothAdapter;
  public boolean running = true;

  public PoiSync(Context ma) {
    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    
    for(BluetoothDevice bd : mBluetoothAdapter.getBondedDevices()) {
      connections.add(new BluetoothConnection(bd));
    }
  }
  
  
  private Mode _mode;
  private Mode getMode()       { return _mode; }
  public  void setMode(Mode m) { _mode = m; }
  
  private Integer _brightness;
  private Integer getBrightness()          { return _brightness; }
  public  void    setBrightness(Integer b) { _brightness = b; }
  
  private Float _speed;
  private Float getSpeed()        { return _speed; }
  public  void  setSpeed(Float s) { _speed = s; }

  private Float _rainbow_speed;
  private Float getRainbowSpeed()        { return _rainbow_speed; }
  public  void  setRainbowSpeed(Float rs) { _rainbow_speed = rs; }

  private Float _width;
  private Float getWidth()        { return _width; }
  public  void  setWidth(Float w) { _width = w; }
  
  @Override
  public void run() {
    Log.i("PoiSync", "PoiSync::run()");
    while(running) {
      RpcMessage.Builder msg = new RpcMessage.Builder().settings(
          new ChangeSettings.Builder()
            .mode(getMode())
            .brightness(getBrightness())
            .speed(getSpeed())
            .rainbow_speed(getRainbowSpeed())
            .width(getWidth())
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
    Log.i("PoiSync", "PoiSync::run() exit");
  }
};
