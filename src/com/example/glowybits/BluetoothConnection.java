package com.example.glowybits;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.example.glowybits.rcp.*;
import com.squareup.wire.Wire;

public class BluetoothConnection {
  private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

  final private Wire wire = new Wire();
  final private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
  private boolean is_connected = false;
  private BluetoothSocket mmSocket;
  
  private BluetoothDevice mmDevice;
  private long lastMillis;
  private Thread listener;

  @SuppressLint("UseSparseArrays")
  public BluetoothConnection(BluetoothDevice device)  {
    mmDevice = device;
    lastMillis = System.currentTimeMillis();

  }
  
  public void connect() throws InterruptedException, IOException {
    if (is_connected) {
      return;
    }
    Log.i("BluetoothConnection", String.format("%s: Connecting", mmDevice.getAddress()));

    mBluetoothAdapter.cancelDiscovery();
    mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
    mmSocket.connect();
    is_connected = true;
    
    listener = new Thread() {
      public void run() {
        try {
          InputStream is = mmSocket.getInputStream();
          while(is_connected) {
  
            int bytes = is.read() << 8;
            bytes += is.read();
        
            byte buffer[] = new byte[bytes];
            for(int offset = 0;offset < bytes;) {
              offset += is.read(buffer, offset, bytes - offset);
            }
            long now = System.currentTimeMillis();

            RpcMessage resp = wire.parseFrom(buffer,  RpcMessage.class);
            Log.i("BluetoothConnection", String.format("%s: -> (%dms) %s", mmDevice.getAddress(), now - lastMillis, resp.toString()));
          }
        } catch (IOException ioe) {
          ioe.printStackTrace();
          disconnect();
        }
      }
    };
    listener.start();
  }
  
  public void disconnect() {
    if(!is_connected) {
      return;
    }

    Log.i("BluetoothConnection", String.format("%s: Disconnecting", mmDevice.getAddress()));
    is_connected = false;
    try {
      listener.join();
    } catch (InterruptedException e1) {
      e1.printStackTrace();
    }
    
    try {
      mmSocket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public BluetoothDevice getDevice() {
    return mmDevice;
  }

  public boolean request(final RpcMessage.Builder in_rpc) {
    RpcMessage req = in_rpc.build();

    try {
      connect();

      OutputStream os = mmSocket.getOutputStream();
      
      int serializedSize = req.getSerializedSize();
  
      byte toSend[] = new byte[serializedSize + 2];
  
      //Write out the length of the message
      toSend[0] = (byte) (0xFF & (serializedSize >> 8));
      toSend[1] = (byte) (0xFF & serializedSize);
  
      req.writeTo(toSend, 2, toSend.length - 2);
      Log.i("BluetoothConnection", String.format("%s: <- %s", mmDevice.getAddress(), req.toString()));
      lastMillis = System.currentTimeMillis();

      os.write(toSend);
      os.flush(); 
    } catch (InterruptedException e) {
      e.printStackTrace();
      disconnect();
      return false;
    } catch (IOException e) {
      e.printStackTrace();
      disconnect();
      return false;
    }
    return true;
  }
}