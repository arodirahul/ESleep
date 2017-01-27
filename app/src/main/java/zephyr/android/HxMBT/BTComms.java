package zephyr.android.HxMBT;

import android.bluetooth.BluetoothSocket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;

public class BTComms extends Thread {
    private BluetoothSocket _btStream;
    private InputStream _istream;
    private OutputStream _ostream;
    private LinkedBlockingQueue<byte[]> _queue;
    private Vector<ReceivedListener> eventSubscribers;

    /* renamed from: zephyr.android.HxMBT.BTComms.1 */
    class C00051 extends Thread {
        C00051() {
        }

        public void run() {
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            BTComms.this.CallingReceivers();
        }
    }

    public boolean canWrite() {
        return this._ostream != null;
    }

    public boolean canRead() {
        return this._istream != null;
    }

    private void OnReceived(byte[] bytes) {
        Iterator<ReceivedListener> iter = ((Vector) this.eventSubscribers.clone()).iterator();
        while (iter.hasNext()) {
            ((ReceivedListener) iter.next()).Received(new ReceivedEvent(this, bytes));
        }
    }

    public void addReceivedEventListener(ReceivedListener listener) {
        this.eventSubscribers.add(listener);
    }

    public void removeReceivedEventListener(ReceivedListener listener) {
        this.eventSubscribers.remove(listener);
    }

    private void CallingReceivers() {
        while (true) {
            try {
                OnReceived((byte[]) this._queue.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    public BTComms(BluetoothSocket btStream) {
        this.eventSubscribers = new Vector();
        this._queue = new LinkedBlockingQueue();
        this._btStream = btStream;
        try {
            this._ostream = this._btStream.getOutputStream();
            this._istream = this._btStream.getInputStream();
            new C00051().start();
        } catch (IOException e) {
            System.out.println("Can't create input/output streams.");
        }
    }

    public void run() {
        byte[] buffer = new byte[512];
        if (this._istream != null) {
            while (this._istream != null) {
                try {
                    int nbRead = this._istream.read(buffer);
                    if (nbRead > 0) {
                        try {
                            byte[] data = new byte[nbRead];
                            System.arraycopy(buffer, 0, data, 0, nbRead);
                            this._queue.put(data);
                        } catch (InterruptedException e) {
                        }
                    }
                } catch (IOException e2) {
                    e2.printStackTrace();
                    return;
                }
            }
        }
    }

    public void write(byte[] bytes) {
        if (this._ostream != null) {
            try {
                this._ostream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void Close() {
        if (this._istream != null) {
            try {
                this._istream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this._istream = null;
        }
        if (this._ostream != null) {
            try {
                this._ostream.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            this._ostream = null;
        }
    }
}
