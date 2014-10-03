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

  private  BluetoothDevice mmDevice;

  @SuppressLint("UseSparseArrays")
  public BluetoothConnection(BluetoothDevice device) {
    mmDevice = device;
  }

  public BluetoothDevice getDevice() {
    return mmDevice;
  }

  public RpcMessage request(final RpcMessage.Builder in_rpc) throws InterruptedException, IOException {
    RpcMessage req = in_rpc.build();
    RpcMessage resp = null;
    mBluetoothAdapter.cancelDiscovery();

    BluetoothSocket mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
    try {
      mmSocket.connect();
        
      OutputStream os = mmSocket.getOutputStream();
      InputStream is = mmSocket.getInputStream();
      is.skip(is.available()); // Make sure we don't have any wonky data
      
      int serializedSize = req.getSerializedSize();
  
      byte toSend[] = new byte[serializedSize + 2];
  
      //Write out the length of the message
      toSend[0] = (byte) (0xFF & (serializedSize >> 8));
      toSend[1] = (byte) (0xFF & serializedSize);
  
      req.writeTo(toSend, 2, toSend.length - 2);
      Log.i("BluetoothConnection", String.format("%s: <- %s", mmDevice.getAddress(), req.toString()));
  
      os.write(toSend);
      os.flush(); 
  
      int bytes = is.read() << 8;
      bytes += is.read();
  
      byte buffer[] = new byte[bytes];
      for(int offset = 0;offset < bytes;) {
        offset += is.read(buffer, offset, bytes - offset);
      }
      resp = wire.parseFrom(buffer,  RpcMessage.class);
      Log.i("BluetoothConnection", String.format("%s: -> %s", mmDevice.getAddress(), resp.toString()));

      resp = wire.parseFrom(buffer, RpcMessage.class);
  
    } finally  {
      mmSocket.close();
    }
    return resp;
  }
}