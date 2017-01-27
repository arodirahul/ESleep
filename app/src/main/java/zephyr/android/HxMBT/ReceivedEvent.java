package zephyr.android.HxMBT;

import java.util.EventObject;

public class ReceivedEvent extends EventObject {
    private byte[] _bytes;

    public byte[] getBytes() {
        return this._bytes;
    }

    public ReceivedEvent(Object source, byte[] bytes) {
        super(source);
        this._bytes = bytes;
    }
}
