package kuchinke.com.airbar_tracking_app;

/**
 * Created by Adrian on 03.01.2017.
 */

abstract class PrinterConnection {
    public interface OnConnectionCallBack{
        void connectionEstablished();
        void connectionLost();
        void connectionRefused();
        void newCharsAvailable(byte[] c, int byteCount);

    }

    abstract public void setOnConnectionCallBack(OnConnectionCallBack onConnectionCallBack);
    abstract void write(String data);
    abstract void connect();
    abstract boolean isConnected();
    abstract void tearDown();
}
