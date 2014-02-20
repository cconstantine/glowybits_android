package com.example.glowybits;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import com.example.glowybits.rcp.*;
import com.squareup.wire.Wire;

public class BluetoothConnection extends Thread {
  private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
  public static final int CONNECTED    = 1;
  public static final int DISCONNECTED = 2;
  private  BluetoothSocket mmSocket;
  private  BluetoothDevice mmDevice;
  private  BluetoothAction mmAction;
  private  Handler         mmHandler;
  private  BluetoothAdapter mBluetoothAdapter;
  private Wire wire;

  public BluetoothConnection(BluetoothDevice device, BluetoothAction ba) {
    mmDevice = device;
    mmAction = ba;
    mmHandler = new Handler();
    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    wire = new Wire();
    start();
  }
  
  public BluetoothDevice getDevice() {
    return mmDevice;
  }

  public void run() {
    mBluetoothAdapter.cancelDiscovery();

    try {

      mmHandler.post(new Runnable() {
        @Override
        public void run() {
          BluetoothConnection.this.mmAction.connecting(BluetoothConnection.this);
        }
      });
      
      mmSocket = mmDevice.createInsecureRfcommSocketToServiceRecord(uuid);//.createRfcommSocketToServiceRecord(uuid);
      mmSocket.connect();

      mmHandler.post(new Runnable() {
        @Override
        public void run() {
          BluetoothConnection.this.mmAction.connected(BluetoothConnection.this);
        }
      });
      
      new Thread() {
        public void run() {
          try {
            InputStream is = mmSocket.getInputStream();
            
            while(true) {
              int bytes = is.read() << 8;
              bytes += is.read();

              Log.i("BluetoothConnection", "Reading " + bytes + " bytes");
              byte buffer[] = new byte[bytes];
              for(int offset = 0;offset < bytes;) {
                offset += is.read(buffer, offset, bytes - offset);
              }
              
              try {
                RpcMessage rpc = wire.parseFrom(buffer, RpcMessage.class);
                
                BluetoothConnection.this.mmAction.rcp_message(BluetoothConnection.this, rpc);
              } catch(Exception e) {
                Log.i("BluetoothConnection", e.getMessage());
                is.skip(is.available());
              }
            }
          } catch (IOException e) { }
        }
      }.start();
    } catch (IOException connectException) {
      mmHandler.post(new Runnable() {
        @Override
        public void run() {
          BluetoothConnection.this.mmAction.connectionFailed(BluetoothConnection.this);
        }
      });
      cancel();
    }
  }
  
  public void sendMessage(RpcMessage rpc) {
    try {
      OutputStream os = mmSocket.getOutputStream();
      
      byte buffer[] = rpc.toByteArray();
      byte toSend[] = new byte[rpc.toByteArray().length + 2];
      
      //Write out the length of the message
      toSend[0] = (byte) (0xFF & (buffer.length >> 8));
      toSend[1] = (byte) (0xFF & buffer.length);

      

      rpc.writeTo(toSend, 2, toSend.length - 2);
      
      Log.i("BluetoothConnection", "Writing " + buffer.length + " bytes");

      os.write(toSend);
      os.flush();
      
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
  }


  /** Will cancel an in-progress connection, and close the socket */
  public void cancel() {
    mmAction.disconnected(this);
    if(mmSocket == null)
      return;
    
    try {
      mmSocket.close();
    } catch (IOException e) { }
  }
}