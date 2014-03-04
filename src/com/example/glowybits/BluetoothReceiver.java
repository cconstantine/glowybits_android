package com.example.glowybits;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

class BluetoothReceiver extends BroadcastReceiver {
  
  @Override
  final public void onReceive(Context c, Intent i) {
    int what    = i.getIntExtra(   "what", 0);
    String addr = i.getStringExtra("bt_address");

    switch(what){
    case BluetoothConnection.CONNECTING:
      connecting(addr);
      break;
    case BluetoothConnection.CONNECTED:
      connected(addr);
      break;
    case BluetoothConnection.DISCONNECTED:
      disconnected(addr);
      break;
    case BluetoothConnection.PING:
      ping(addr, i.getDoubleExtra("ping", 0), i.getIntExtra("fps", 0), i.getFloatExtra("g", 0));
      break;
    }
  }
  
  public void connecting(String addr) {
    Log.i("BluetoothReceiver",String.format("%s: connecting", addr));
  }
  
  public void connected(String addr) {
    Log.i("BluetoothReceiver",String.format("%s: connected", addr)); 
  }
  
  public void disconnected(String addr) {
    Log.i("BluetoothReceiver",String.format("%s: disconnected", addr));
  }
  
  public void ping(String addr, double ping, int fps, float g) {
    Log.i("BluetoothReceiver", String.format("%s: ping time %f (%d)", addr, ping, fps));

  }
}