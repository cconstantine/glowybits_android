package com.example.glowybits;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.example.glowybits.rcp.ChangeSettings;
import com.example.glowybits.rcp.ChangeSettings.Mode;
import com.example.glowybits.rcp.RpcMessage;

class PoiSync extends Thread {
  Set<BluetoothConnection> connections = new HashSet<BluetoothConnection>();
  private BluetoothAdapter mBluetoothAdapter;
  public boolean running = true;

  public PoiSync() {
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

  private int  _color1;
  private int  getColor1()        { return _color1; }
  public  void setColor1(int c) { _color1 = c; }
  
  private int  _color2;
  private int  getColor2()        { return _color2; }
  public  void setColor2(int c) { _color2 = c; }

  private int   _color3;
  private int  getColor3()        { return _color3; }
  public  void setColor3(int c) { _color3 = c; }
  
  @Override
  public void run() {
    BluetoothConnection lastUsed = null;
    Log.i("PoiSync", "PoiSync::run()");
    while(running) {
      Log.i("PoiSyncService", String.format("PoiSyncService::color1: %x", getColor1()));

      RpcMessage.Builder msg = new RpcMessage.Builder().settings(
          new ChangeSettings.Builder()
            .mode(getMode())
            .brightness(getBrightness())
            .speed(getSpeed())
            .rainbow_speed(getRainbowSpeed())
            .width(getWidth())
            .color1(getColor1())
            .color2(getColor2())
            .color3(getColor3())
            .build()
          ); 

      try {
        if (lastUsed != null) {
          lastUsed.request(msg);
          } else {
          for(BluetoothConnection bc : connections) {
            if (bc.request(msg)) {
              lastUsed = bc;
              continue;
            }
            sleep(1000);
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
