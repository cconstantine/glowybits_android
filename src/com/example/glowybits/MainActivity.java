package com.example.glowybits;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;
import com.example.glowybits.rcp.RpcMessage;
import com.example.glowybits.rcp.RpcMessage.Action;

public class MainActivity extends Activity {
  Set<BluetoothDevice> pairedDevices;
  Map<String, BluetoothConnection> connections;
  BluetoothAction mActions;

  BluetoothAdapter mBluetoothAdapter;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    
    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    pairedDevices = mBluetoothAdapter.getBondedDevices();
    Log.i("BluetoothService", "Found " + pairedDevices.size() + " devices.");

    
    mActions = new BluetoothAction() {
      @Override
      public void connecting(BluetoothConnection bc) {
        Log.i("BluetoothService", "Connecting to: " + bc.getDevice().getName() + " " +  bc.getDevice().getAddress());
//        Toast.makeText(MainActivity.this, "Connecting to: " + bc.getDevice().getName(), Toast.LENGTH_SHORT);
      }
      
      @Override
      public void connectionFailed(BluetoothConnection bc) {
        Log.i("BluetoothService", "Connection failed to: " + bc.getDevice().getName() + " " +  bc.getDevice().getAddress());
//        Toast.makeText(MainActivity.this, "Connection failed to: " + bc.getDevice().getName(), Toast.LENGTH_SHORT);
      }      
      
      @Override
      public void connected(BluetoothConnection bc) {
        Log.i("BluetoothService", "Connected to: " + bc.getDevice().getName() + " " +  bc.getDevice().getAddress());
//        Toast.makeText(MainActivity.this, "Connected to: " + bc.getDevice().getName(), Toast.LENGTH_SHORT);
      }

      @Override
      public void disconnected(BluetoothConnection bc) {
        Log.i("BluetoothService", "Disonnected from: " + bc.getDevice().getName() + " " +  bc.getDevice().getAddress());
//        Toast.makeText(MainActivity.this, "Disonnected from: " + bc.getDevice().getName(), Toast.LENGTH_SHORT);
      }

      @Override
      public void rcp_message(BluetoothConnection bc, RpcMessage rpc) {
        Log.i("BluetoothService", bc.getDevice().getName() + " " +  bc.getDevice().getAddress() + ": " + rpc.toString());

      }
      
    };
    
    SeekBar sb = (SeekBar)this.findViewById(R.id.seek1);
    sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

      @Override
      public void onProgressChanged(SeekBar arg0, int position, boolean arg2) {
          Log.i("MainActivity", String.format("Seeking: %d, %s", position, arg2 ? "true" : "false"));
          changeBrightness(position);
      }

      @Override
      public void onStartTrackingTouch(SeekBar arg0) {
        // TODO Auto-generated method stub
        
      }

      @Override
      public void onStopTrackingTouch(SeekBar arg0) {
        // TODO Auto-generated method stub
        
      }
      
    });

    reconnect();
  }
  
  public synchronized void reconnect() {
    disconnect();
    
    for(BluetoothDevice device : pairedDevices) {
      connections.put(device.getAddress(), new BluetoothConnection(device, mActions));
    }
  }
  
  public void reconnect(View v) {
    reconnect();
  }
  
  public void changeMode(View v) {
    for(BluetoothConnection c : connections.values()) {
      RpcMessage msg = new RpcMessage.Builder()
          .action(Action.CHANGE_MODE)
          .build();
      c.sendMessage(msg);
    }
  }
  
  public void changeBrightness(int b) {

    for(BluetoothConnection c : connections.values()) {
      RpcMessage msg = new RpcMessage.Builder()
          .action(Action.CHANGE_BRIGHTNESS)
          .arg1(b)
          .build();
      c.sendMessage(msg);
    }
  }
  

  public synchronized void disconnect() {
    Map<String, BluetoothConnection> oldConnections = connections;
    connections = Collections.synchronizedMap(new HashMap<String, BluetoothConnection>()); 

    if (oldConnections != null) {
      for(BluetoothConnection th : oldConnections.values()) {
        th.cancel();
      }
    }
  }
  

  @Override
  protected void onResume() {
    super.onResume();
    
    
  }
  
  @Override
  protected void onDestroy() {
    disconnect();
    super.onDestroy();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

}
