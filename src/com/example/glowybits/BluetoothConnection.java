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

    new Thread() {
      @Override
      public void run() {
        try {
          while(true) {
            if (isConnected()) {
              long startNano = System.nanoTime();

              try {
                int fps = getFps();
                double delta = (double)(System.nanoTime() - startNano) / 1000000.0;

                Message m = mmHandler.obtainMessage(PING);

                Bundle b = new Bundle();
                b.putString("bt_address", mmDevice.getAddress());
                b.putDouble("ping", delta);
                b.putInt("fps", fps);
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

  public void connect() {
    disconnect();
    new Thread() {
      @Override
      public void run() {
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

  private RpcMessage request(final RpcMessage.Builder in_rpc) throws IOException, InterruptedException {
    RpcMessage req = in_rpc.rid(nextRid()).build();
    final int rid = req.rid;
    RpcMessage resp = null;
    Condition cond = lock.newCondition();
    responses.put(rid, new AwaitingResponse(cond));

    sendMessage(req);

    try {
      lock.lock();
      if (!cond.await(1, TimeUnit.SECONDS)) {
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

  public int changeMode() throws IOException, InterruptedException {
    RpcMessage rsp = this.request(new RpcMessage.Builder().action(Action.CHANGE_MODE));
    return rsp.arg1;
  }

  public int getFps() throws IOException, InterruptedException {
    RpcMessage rsp = this.request(new RpcMessage.Builder().action(Action.FRAMES_PER_SECOND));
    return rsp.arg1;
  }

  public int changeBrightness(int b) throws IOException, InterruptedException {
    RpcMessage rsp = this.request(new RpcMessage.Builder().action(Action.CHANGE_BRIGHTNESS).arg1(b));
    return rsp.arg1;
  }

  public float changeSpeed(float rate) throws IOException, InterruptedException {
    RpcMessage.Builder msg = new RpcMessage.Builder().action(Action.CHANGE_SPEED).arg2(rate);
    RpcMessage rsp = this.request(msg);
    return rsp.arg2;  
  }
  
  public float changeColorSpeed(float rate) throws IOException, InterruptedException {
    RpcMessage.Builder msg = new RpcMessage.Builder().action(Action.CHANGE_RAINBOW_SPD).arg2(rate);
    RpcMessage rsp = this.request(msg);
    return rsp.arg2;  
  }

  public float changeWidth(float width) throws IOException, InterruptedException {
    RpcMessage.Builder msg = new RpcMessage.Builder().action(Action.CHANGE_WIDTH).arg2(width);
    RpcMessage rsp = this.request(msg);
    return rsp.arg2;  
  }



}