package zephyr.android.HxMBT;

import java.util.Iterator;
import java.util.Vector;

public class ZephyrProtocol implements ReceivedListener {
    private BTComms _comms;
    private ZephyrPacket _packet;
    private Vector<ZephyrPacketListener> eventSubscribers;

    /* renamed from: zephyr.android.HxMBT.ZephyrProtocol.1 */
    class C00101 extends Thread {
        C00101() {
        }

        public void run() {
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ZephyrProtocol.this.GetSerialNumber(true);
            ZephyrProtocol.this.SendLifeSign();
        }
    }

    /* renamed from: zephyr.android.HxMBT.ZephyrProtocol.2 */
    class C00112 extends Thread {
        C00112() {
        }

        public void run() {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ZephyrProtocol.this.SendLifeSign();
        }
    }

    private void OnZephyrPacket(ZephyrPacketArgs packet) {
        Iterator<ZephyrPacketListener> iter = ((Vector) this.eventSubscribers.clone()).iterator();
        while (iter.hasNext()) {
            ((ZephyrPacketListener) iter.next()).ReceivedPacket(new ZephyrPacketEvent(this, packet));
        }
    }

    public void addZephyrPacketEventListener(ZephyrPacketListener listener) {
        this.eventSubscribers.add(listener);
    }

    public void removeZephyrPacketEventListener(ZephyrPacketListener listener) {
        this.eventSubscribers.remove(listener);
    }

    public ZephyrProtocol(BTComms comms) {
        this.eventSubscribers = new Vector();
        this._packet = new ZephyrPacket();
        this._comms = comms;
        this._comms.addReceivedEventListener(this);
        new C00101().start();
    }

    public void SendLifeSign() {
        if (this._comms.canWrite()) {
            System.out.println("Sending life sign packet.");
            this._comms.write(this._packet.getLifeSignMessage());
        }
        if (this._comms.canWrite()) {
            new C00112().start();
        }
    }

    public void GetSerialNumber(boolean activate) {
        if (this._comms.canWrite()) {
            this._comms.write(this._packet.getSetSerialNumberMessage(activate));
        }
    }

    public void Received(ReceivedEvent eventArgs) {
        Iterator<byte[]> iter = this._packet.Serialize(eventArgs.getBytes()).iterator();
        while (iter.hasNext()) {
            ZephyrPacketArgs msg = null;
            try {
                msg = this._packet.Parse((byte[]) iter.next());
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (msg != null) {
                OnZephyrPacket(msg);
            }
        }
    }
}
