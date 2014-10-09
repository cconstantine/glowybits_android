package com.example.glowybits;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.example.glowybits.rcp.ChangeSettings;
import com.example.glowybits.rcp.RpcMessage;

class PoiSync extends Thread {
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
            .mode(mmMainActivity.getMode())
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
