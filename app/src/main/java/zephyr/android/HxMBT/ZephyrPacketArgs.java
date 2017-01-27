package zephyr.android.HxMBT;

public class ZephyrPacketArgs {
    private byte _CrcStatus;
    private byte _NumRcvdBytes;
    private byte[] _bytes;
    private int _msgID;
    private byte _status;

    public byte[] getBytes() {
        return this._bytes;
    }

    public int getMsgID() {
        return this._msgID;
    }

    public byte getStatus() {
        return this._status;
    }

    public byte getNumRvcdBytes() {
        return this._NumRcvdBytes;
    }

    public byte getCRCStatus() {
        return this._CrcStatus;
    }

    public ZephyrPacketArgs(int msgID, byte[] data, byte status, byte NumRcvdBytes, byte CrcFailStatus) {
        this._msgID = msgID;
        this._bytes = data;
        this._status = status;
        this._NumRcvdBytes = NumRcvdBytes;
        this._CrcStatus = CrcFailStatus;
    }
}
