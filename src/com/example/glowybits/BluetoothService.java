package com.example.glowybits;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

class BluetoothServiceConnection implements ServiceConnection {
  private BluetoothService mService;
  private boolean mBound;

  public boolean isBound() {
    return mBound;
  }

  public BluetoothService getService() {
    return mService;
  }

  // Called when the connection with the service is established
  public void onServiceConnected(ComponentName className, IBinder service) {
    Log.i("ServiceConnection", "onServiceConnected");
    // Because we have bound to an explicit
    // service that is running in our own process, we can
    // cast its IBinder to a concrete class and directly access it.
    LocalBinder binder = (LocalBinder) service;
    mService = binder.getService();
    mBound = true;
  }

  // Called when the connection with the service disconnects unexpectedly
  public void onServiceDisconnected(ComponentName className) {
    Log.i("ServiceConnection", "onServiceDisconnected");
    mBound = false;
  }
}

class LocalBinder extends Binder {
  private BluetoothService mBs;
  public LocalBinder(BluetoothService bs) {
    mBs = bs;
  }
  BluetoothService getService() {
    return mBs;
  }
}


public class BluetoothService extends Service {
  
  private void info(String s) {
    Log.i("BluetoothService", s);
  }

  // Binder given to clients
  private final IBinder mBinder = new LocalBinder(BluetoothService.this);
  private BluetoothAdapter mBluetoothAdapter;
  private Set<BluetoothDevice> pairedDevices;
  private Map<String, BluetoothConnection> connections;
  private ScheduledThreadPoolExecutor threads;

  // Handler that receives messages from the thread
  private final class ServiceHandler extends Handler {
    public ServiceHandler(Looper looper) {
      super(looper);
    }
    @Override
    public void handleMessage(Message msg) {
      LocalBroadcastManager  lbm =  LocalBroadcastManager.getInstance(BluetoothService.this);
      Intent i = new Intent("message");

      Bundle b = msg.getData();
      b.putInt("what", msg.what);

      i.replaceExtras(b);

      lbm.sendBroadcast(i);
    }
  }

  @Override
  public void onCreate() {
    info("BluetoothService::onCreate(" + this.hashCode() + ")");
    
    threads = new ScheduledThreadPoolExecutor(10);

    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    pairedDevices = mBluetoothAdapter.getBondedDevices();
    connections = new HashMap<String, BluetoothConnection>();
    Log.i("BluetoothService", "Found " + pairedDevices.size() + " devices.");


    // Start up the thread running the service.
    HandlerThread thread = new HandlerThread("ServiceStartArguments");
    thread.start();
    
    for(BluetoothDevice device : pairedDevices) {
      connections.put(device.getAddress(), new BluetoothConnection(device, new ServiceHandler(thread.getLooper())));
    }

    connect();
  }

  public void changeMode() {
    threads.execute(new Runnable() {
      @Override
      public void run() {
        
        for(final BluetoothConnection c : connections.values()) {
          try {
            c.changeMode();
          } catch (IOException e) {
            e.printStackTrace();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }   
      }
    });
  }

  public void changeBrightness(final int b) {
    threads.execute(new Runnable() {
      @Override
      public void run() {

        for(BluetoothConnection c : connections.values()) {
          try { 
            c.changeBrightness(b);
          } catch (IOException e) {
            e.printStackTrace();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
    });
  }

  public void changeSpeed(final float rate) {
    threads.execute(new Runnable() {
      @Override
      public void run() {

        for(BluetoothConnection c : connections.values()) {
          try { 
            c.changeSpeed(rate);
          } catch (IOException e) {
            e.printStackTrace();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
    });
  }
  
  public void changeColorSpeed(final float rate) {
    threads.execute(new Runnable() {
      @Override
      public void run() {

        for(BluetoothConnection c : connections.values()) {
          try { 
            c.changeColorSpeed(rate);
          } catch (IOException e) {
            e.printStackTrace();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
    });
  }


  public void changeWidth(final float width) {
    threads.execute(new Runnable() {
      @Override
      public void run() {

        for(BluetoothConnection c : connections.values()) {
          try { 
            c.changeWidth(width);
          } catch (IOException e) {
            e.printStackTrace();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
    });
  }

  public void connect() {
    for(BluetoothConnection device : connections.values()) {
      device.connect();
    }
  }

  public void reconnect() {
    threads.execute(new Runnable() {
      @Override
      public void run() {
        for(BluetoothConnection th : connections.values()) {
          th.disconnect();
        }
        connect();
      }
    });
  }

  public void disconnect() {
    threads.execute(new Runnable() {
      @Override
      public void run() {
        for(BluetoothConnection th : connections.values()) {
          th.disconnect();
        }
      }
    });
  }

  public synchronized BluetoothConnection getConnection(String addr) {
    return connections.get(addr);
  }
  
  public Map<String, BluetoothConnection> getConnections() {
    return connections;
  }

  @Override
  public synchronized int onStartCommand(Intent intent, int flags, int startId) {
    info("BluetoothService::onStartCommand(" + this.hashCode() + ")");

    return START_STICKY;
  }

  @Override
  public IBinder onBind(Intent intent) {
    info("BluetoothService::onBind");
    return mBinder;
  }

  @Override
  public void onDestroy() {
    disconnect();

    info("BluetoothService::onDestroy");
    // Tell the user we stopped.
    Toast.makeText(this, "Stopping glowybits", Toast.LENGTH_SHORT).show();
    super.onDestroy();
  }
}