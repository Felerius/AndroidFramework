package de.hpi.hci.bachelorproject2016.bluetoothlib;

public abstract class PrinterConnection {
    public interface OnConnectionCallBack{
        void connectionEstablished();
        void connectionLost();
        void connectionRefused();
        void newCharsAvailable(byte[] c, int byteCount);

    }

    abstract public void setOnConnectionCallBack(OnConnectionCallBack onConnectionCallBack);
    abstract public void write(String data);
    abstract public void connect();
    abstract public boolean isConnected();
    abstract public void tearDown();
}
