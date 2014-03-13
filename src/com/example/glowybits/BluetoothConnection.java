package com.example.glowybits;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.example.glowybits.rcp.*;
import com.example.glowybits.rcp.RpcMessage.Action;
import com.squareup.wire.Wire;

public class BluetoothConnection {
  private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

  final private Wire wire = new Wire();
  final private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
  final private Lock lock = new ReentrantLock();

  public static final int CONNECTING    = 1;
  public static final int CONNECTED    = 2;
  public static final int DISCONNECTED = 3;
  public static final int MESSAGE_RECEIVED = 4;
  public static final int PING = 5;

  private int              mLastRid = 1;
  private  BluetoothSocket mmSocket;
  private  BluetoothDevice mmDevice;
  private  Handler         mmHandler;
  private  Handler         mmOutputHandler;
  private class AwaitingResponse {
    public AwaitingResponse(Condition t) {
      cond = t;
    }
    public Condition cond;
    public RpcMessage response = null;
  }
  private Map<Integer,AwaitingResponse> responses;

  private synchronized Integer nextRid() {
    return mLastRid++;
  }

  @SuppressLint("UseSparseArrays")
  public BluetoothConnection(BluetoothDevice device, Handler h) {
    mmDevice = device;
    mmHandler = h;
    responses = Collections.synchronizedMap(new HashMap<Integer, AwaitingResponse>());
    HandlerThread outputThread = new HandlerThread(device.getAddress() + " output");
    outputThread.start();
    mmOutputHandler = new Handler(outputThread.getLooper());

    new Thread() {
      @Override
      public void run() {
        try {
          while(true) {
            if (isConnected()) {
              long startNano = System.nanoTime();

              try {
                RpcMessage rpc = getFps();
                double delta = (double)(System.nanoTime() - startNano) / 1000000.0;

                Message m = mmHandler.obtainMessage(PING);

                Bundle b = new Bundle();
                b.putString("bt_address", mmDevice.getAddress());
                b.putDouble("ping", delta);
                b.putInt("fps", rpc.arg1);
                b.putFloat("g", rpc.arg2);
                m.setData(b);

                mmHandler.sendMessage(m);

              } catch (InterruptedException e) {
                BluetoothConnection.this.disconnect();
                e.printStackTrace();
              }
            }

            sleep(1000);
          }

        } catch (IOException e) {
          e.printStackTrace();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

      }
    }.start();
  }

  public BluetoothDevice getDevice() {
    return mmDevice;
  }

  public synchronized void connect() {
    disconnect();

    mBluetoothAdapter.cancelDiscovery();

    Message m = mmHandler.obtainMessage(CONNECTING);

    Bundle b = new Bundle();
    b.putString("bt_address", mmDevice.getAddress());
    m.setData(b);

    mmHandler.sendMessage(m);
    try {
      mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
      mmSocket.connect();
    } catch (IOException e) {
      disconnect();
      e.printStackTrace();      
      return;
    }

    m = mmHandler.obtainMessage(CONNECTED);
    b = new Bundle();
    b.putString("bt_address", mmDevice.getAddress());
    m.setData(b);

    mmHandler.sendMessage(m);
    new Thread() {
      @Override
      public void run() {
        try {
          InputStream is = mmSocket.getInputStream();

          while(true) {
            int bytes = is.read() << 8;
            bytes += is.read();

            byte buffer[] = new byte[bytes];
            for(int offset = 0;offset < bytes;) {
              offset += is.read(buffer, offset, bytes - offset);
            }

            RpcMessage msg = wire.parseFrom(buffer, RpcMessage.class);

            Log.i("BluetoothConnection", String.format("%s: -> %s", mmDevice.getAddress(), msg.toString()));
            AwaitingResponse ar = responses.get(msg.rid);
            if (ar != null) {
              try {
                lock.lock();
                ar.response = msg;
                ar.cond.signalAll();
              } finally {
                lock.unlock();
              }
            }
          }
        } catch (IOException e) { }
      }
    }.start();
  }

  protected boolean isConnected() {
    return mmSocket != null && mmSocket.isConnected();
  }

  private void sendMessage(final RpcMessage outgoing_rpc) throws IOException {

    OutputStream os = mmSocket.getOutputStream();
    int serializedSize = outgoing_rpc.getSerializedSize();

    byte toSend[] = new byte[outgoing_rpc.getSerializedSize() + 2];

    //Write out the length of the message
    toSend[0] = (byte) (0xFF & (serializedSize >> 8));
    toSend[1] = (byte) (0xFF & serializedSize);

    outgoing_rpc.writeTo(toSend, 2, toSend.length - 2);
    Log.i("BluetoothConnection", String.format("%s: <- %s", mmDevice.getAddress(), outgoing_rpc.toString()));

    os.write(toSend);
    os.flush();    
  }
  
  private RpcMessage request(final RpcMessage.Builder in_rpc) throws InterruptedException, IOException  {
    return request(in_rpc, false);
  }

  private RpcMessage request(final RpcMessage.Builder in_rpc, boolean has_response) throws InterruptedException, IOException {
    if(!isConnected()) {
      throw new IOException();
    }
    final RpcMessage req = in_rpc.rid(nextRid()).build();

    if (!has_response) {
      sendMessage(req);
      return null;
    }
    
    final int rid = req.rid;
    RpcMessage resp = null;
    Condition cond = lock.newCondition();
    responses.put(rid, new AwaitingResponse(cond));

    sendMessage(req);


    try {
      lock.lock();
      if (!cond.await(2, TimeUnit.SECONDS)) {
        throw new InterruptedException();
      }
    } finally {
      lock.unlock();
      resp = responses.remove(rid).response;
    }

    return resp;
  }


  /** Will cancel an in-progress connection, and close the socket */
  public void disconnect() {
    if(mmSocket != null) {

      Message m = mmHandler.obtainMessage(DISCONNECTED);
      Bundle b = new Bundle();
      b.putString("bt_address", mmDevice.getAddress());
      m.setData(b);
      mmHandler.sendMessage(m);

      try {
        mmSocket.close();
        mmSocket = null;
        Thread.sleep(1000);
      } catch (IOException e) {
        e.printStackTrace();      
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  public void changeMode() throws IOException, InterruptedException {
    this.request(new RpcMessage.Builder().action(Action.CHANGE_MODE));
  }

  public RpcMessage getFps() throws IOException, InterruptedException {
    RpcMessage rsp = this.request(new RpcMessage.Builder().action(Action.FRAMES_PER_SECOND), true);
    return rsp;
  }

  public void changeBrightness(int b) throws IOException, InterruptedException {
    this.request(new RpcMessage.Builder().action(Action.CHANGE_BRIGHTNESS).arg1(b));
  }

  public void changeSpeed(float rate) throws IOException, InterruptedException {
    RpcMessage.Builder msg = new RpcMessage.Builder().action(Action.CHANGE_SPEED).arg2(rate);
    this.request(msg);
  }

  public void changeColorSpeed(float rate) throws IOException, InterruptedException {
    RpcMessage.Builder msg = new RpcMessage.Builder().action(Action.CHANGE_RAINBOW_SPD).arg2(rate);
    this.request(msg);
  }

  public void changeWidth(float width) throws IOException, InterruptedException {
    RpcMessage.Builder msg = new RpcMessage.Builder().action(Action.CHANGE_WIDTH).arg2(width);
    this.request(msg);
  }



}