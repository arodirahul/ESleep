package zephyr.android.HxMBT;

import java.io.ByteArrayOutputStream;
import java.util.Vector;

public class ZephyrPacket {
    private byte ACK;
    private int CRC8_POLY;
    private byte ETX;
    private byte FALSE;
    private int MINIMUM_LENGTH;
    private byte NAK;
    private int POS_DLC;
    private int POS_MSG;
    private int POS_PAYLOAD;
    private int POS_STX;
    private byte STX;
    private byte TRUE;
    private ByteArrayOutputStream _buffer;
    private CRC8 _crc8;
    private int _length;

    public ZephyrPacket() {
        this.STX = (byte) 2;
        this.ETX = (byte) 3;
        this.ACK = (byte) 6;
        this.NAK = (byte) 21;
        this.CRC8_POLY = 140;
        this.MINIMUM_LENGTH = 5;
        this.POS_STX = 0;
        this.POS_MSG = this.POS_STX + 1;
        this.POS_DLC = this.POS_MSG + 1;
        this.POS_PAYLOAD = this.POS_DLC + 1;
        this.FALSE = (byte) 0;
        this.TRUE = (byte) 1;
        this._length = -1;
        this._crc8 = new CRC8(this.CRC8_POLY);
        this._buffer = new ByteArrayOutputStream();
    }

    public Vector<byte[]> Serialize(byte[] inputStream) {
        Vector<byte[]> packets = new Vector();
        int index = 0;
        while (index < inputStream.length) {
            if (this._length < 0 && inputStream[index] == this.STX) {
                this._buffer.reset();
                this._length = this.MINIMUM_LENGTH;
            }
            if (this._length >= 0) {
                if (this._buffer.size() <= this._length) {
                    this._buffer.write(inputStream[index]);
                }
                if (this._buffer.size() == this.POS_DLC + 1) {
                    this._length += inputStream[index];
                }
                if (this._buffer.size() >= this._length) {
                    packets.add(this._buffer.toByteArray());
                    this._buffer.reset();
                    this._length = -1;
                }
            }
            index++;
        }
        return packets;
    }

    public ZephyrPacketArgs Parse(byte[] packet) throws Exception {
        byte crcFailStatus = this.FALSE;
        if (packet.length <= 0) {
            throw new Exception("Empty packet.");
        } else if (packet[this.POS_STX] != this.STX) {
            throw new Exception("Not a STX.");
        } else if (packet.length < this.MINIMUM_LENGTH) {
            throw new Exception("Too short.");
        } else {
            byte dlc = packet[this.POS_DLC];
            if (packet.length < this.MINIMUM_LENGTH + dlc) {
                throw new Exception("Wrong length.");
            }
            byte crc = packet[this.POS_PAYLOAD + dlc];
            byte[] payload = new byte[dlc];
            System.arraycopy(packet, this.POS_PAYLOAD, payload, 0, dlc);
            if (this._crc8.Calculate(payload) != crc) {
                crcFailStatus = this.TRUE;
            }
            byte end = packet[(this.POS_PAYLOAD + dlc) + 1];
            if (end == this.ACK || end == this.NAK || end == this.ETX) {
                return new ZephyrPacketArgs(packet[this.POS_MSG], payload, end, dlc, crcFailStatus);
            }
            throw new Exception("Wrong end.");
        }
    }

    public byte[] getLifeSignMessage() {
        byte[] bArr = new byte[5];
        bArr[0] = (byte) 2;
        bArr[1] = (byte) 35;
        bArr[4] = (byte) 3;
        return bArr;
    }

    public byte[] getSetSerialNumberMessage(boolean activate) {
        byte[] message = new byte[6];
        message[0] = (byte) 2;
        message[1] = (byte) 11;
        message[2] = (byte) 1;
        message[5] = (byte) 3;
        if (activate) {
            message[3] = (byte) 1;
        }
        byte[] crcCalc = new byte[1];
        System.arraycopy(message, 3, crcCalc, 0, 1);
        message[4] = this._crc8.Calculate(crcCalc);
        return message;
    }
}
