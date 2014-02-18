package com.example.glowybits;

import com.example.glowybits.rcp.RpcMessage;

public abstract class BluetoothAction {

  public abstract void connecting(BluetoothConnection bc);
  public abstract void connectionFailed(BluetoothConnection bc);
  public abstract void connected(BluetoothConnection bc);
  public abstract void disconnected(BluetoothConnection bc);
  public abstract void rcp_message(BluetoothConnection bluetoothConnection, RpcMessage rpc);
}
