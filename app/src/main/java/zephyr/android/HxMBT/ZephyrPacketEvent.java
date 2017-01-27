package zephyr.android.HxMBT;

import java.util.EventObject;

public class ZephyrPacketEvent extends EventObject {
    private ZephyrPacketArgs _packet;

    public ZephyrPacketArgs getPacket() {
        return this._packet;
    }

    public ZephyrPacketEvent(Object source, ZephyrPacketArgs packet) {
        super(source);
        this._packet = packet;
    }
}
