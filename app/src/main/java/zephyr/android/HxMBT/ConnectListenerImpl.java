package zephyr.android.HxMBT;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

//For getting bluetooth packets.
public class ConnectListenerImpl implements ConnectedListener<BTClient> {
    protected static final int HR_SPD_DIST_PACKET = 1200;
    private HRSpeedDistPacketInfo HRSpdDistInfoPacket;
    private byte[] Payload;
    public int SERIAL_NUMBER;
    public String SerialNumber;
    private Handler _handler;

    /* renamed from: zephyr.android.HxMBT.ConnectListenerImpl.1 */


    public class HRSpeedDistPacketInfo {
        private byte BatteryChargeInd;
        private double Cadence;
        private double Distance;
        private int PrevHBNum = -1; //by Rahul
        private int CurrentHBNum = -1; //by Rahul
        private int HBNum; //by Rahul
        private String FirmwareID;
        private String FirmwareVersion;
        private String HardwareID;
        private String HardwareVersion;
        private byte HeartBeatNum;
        private byte[] HeartBeatTS;
        private double[] RRInterval = new double[14]; //by Rahul
        private double[] HeartBeatTime = new double[15]; //by Rahul
        //private double[] HeartBeatTime ;  //by Rahul
        private double AvgRRInterval; // by Rahul
        private byte HeartRate;
        private double InstantSpeed;
        private byte Strides;

        public String GetFirmwareID(byte[] DataPacket) {
            this.FirmwareID = String.valueOf((short) (((DataPacket[1] & 255) << 8) | ((short) (DataPacket[0] & 255))));
            return this.FirmwareID;
        }

        public String GetFirmwareVersion(byte[] DataPacket) {
            short FirmwareVersion_Temp = (short) (DataPacket[2] & 255);
            char e = (char) DataPacket[3];
            this.FirmwareVersion = String.valueOf(((char) DataPacket[2]) + e);
            return this.FirmwareVersion;
        }

        public String GetHardwareID(byte[] DataPacket) {
            this.HardwareID = String.valueOf(((short) (((DataPacket[5] & 255) << 8) | ((short) (DataPacket[4] & 255)))) & 255);
            return this.HardwareID;
        }

        public String GetHardwareVersion(byte[] DataPacket) {
            char e = (char) DataPacket[7];
            this.HardwareVersion = String.valueOf(((char) DataPacket[6]) + e);
            return this.HardwareVersion;
        }

        public byte GetBatteryChargeInd(byte[] DataPacket) {
            this.BatteryChargeInd = (byte) (DataPacket[8] & 255);
            return this.BatteryChargeInd;
        }

        public byte GetHeartRate(byte[] DataPacket) {
            this.HeartRate = (byte) (DataPacket[9] & 255);
            return this.HeartRate;
        }

        public byte GetHeartBeatNum(byte[] DataPacket) {
            this.HeartBeatNum = (byte) (DataPacket[10] & 255);
            return this.HeartBeatNum;
        }

        public byte[] GetHeartBeatTS(byte[] DataPacket) {
            System.arraycopy(DataPacket, 7, this.HeartBeatTS, 0, 30);
            return this.HeartBeatTS;
        }

        public int GetCurrentHBNumber(byte[] DataPacket){  // By Rahul
            this.HeartBeatNum = (byte) (DataPacket[10] & 255);
            CurrentHBNum = (int) HeartBeatNum;

            if(PrevHBNum < 0) {
                HBNum = 1; //This is the first packet.
            } else if(PrevHBNum < CurrentHBNum){
                HBNum = CurrentHBNum - PrevHBNum; //This is for packs that HBN didn't get wrapped around.
            } else if(CurrentHBNum < PrevHBNum){
                HBNum = 256 - PrevHBNum + CurrentHBNum; //wrap around has occurred.
            }
            PrevHBNum = CurrentHBNum;
            return HBNum;
        }


       /*private int getBit(int position, byte ID)
        {
            return (ID >> position) & 1;
        }

        private int convertByteToUnsigned(byte input){
            int result = 0;
            for(int i = 0; i < 8; i++){
                result += getBit(i, input) * (int) Math.pow(2, i);
            }
            return result;
        }*/

        public double GetAvgRRInterval(byte[] DataPacket) { // By Rahul
            this.AvgRRInterval = 0.0;
            double avgRRInt = 0.0;
            //double[] RRInterval = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
            //double[] HeartBeatTime = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};

            //******************************Getting the number of heart beats in one packet*******************
            this.HeartBeatNum = (byte) (DataPacket[10] & 255);
            //CurrentHBNum = (int) HeartBeatNum;
            CurrentHBNum = (int)HeartBeatNum & 0xFF;
            //CurrentHBNum = convertByteToUnsigned(HeartBeatNum);
            //CurrentHBNum = Byte.toUnsignedInt(HeartBeatNum);

            System.out.println("CurrentHBNum is "+ CurrentHBNum);

            if(PrevHBNum < 0) {
                HBNum = 1; //This is the first packet.
            } else if(PrevHBNum < CurrentHBNum){
                HBNum = CurrentHBNum - PrevHBNum; //This is for packs that HBN didn't get wrapped around.
            } else if(CurrentHBNum < PrevHBNum){
                HBNum = 256 - PrevHBNum + CurrentHBNum; //wrap around has occurred.
            }

            if(CurrentHBNum-PrevHBNum > 4)
                HBNum = 1;

            PrevHBNum = CurrentHBNum;

            System.out.println("HBNum is "+ HBNum);
            //********************************************************************************************

            HeartBeatTime[0] = (double) (((short) (((DataPacket[12] & 255) << 8) | ((short) (DataPacket[11] & 255))))&0xFFFF);
            System.out.println("HeartBeatTime[0] is "+ HeartBeatTime[0]);
            HeartBeatTime[1] = (double) (((short) (((DataPacket[14] & 255) << 8) | ((short) (DataPacket[13] & 255))))&0xFFFF);
            System.out.println("HeartBeatTime[1] is "+ HeartBeatTime[1]);
            HeartBeatTime[2] = (double) (((short) (((DataPacket[16] & 255) << 8) | ((short) (DataPacket[15] & 255))))&0xFFFF);
            HeartBeatTime[3] = (double) (((short) (((DataPacket[18] & 255) << 8) | ((short) (DataPacket[17] & 255))))&0xFFFF);
            HeartBeatTime[4] = (double) (((short) (((DataPacket[20] & 255) << 8) | ((short) (DataPacket[19] & 255))))&0xFFFF);
            HeartBeatTime[5] = (double) (((short) (((DataPacket[22] & 255) << 8) | ((short) (DataPacket[21] & 255))))&0xFFFF);
            HeartBeatTime[6] = (double) (((short) (((DataPacket[24] & 255) << 8) | ((short) (DataPacket[23] & 255))))&0xFFFF);
            HeartBeatTime[7] = (double) (((short) (((DataPacket[26] & 255) << 8) | ((short) (DataPacket[25] & 255))))&0xFFFF);
            HeartBeatTime[8] = (double) (((short) (((DataPacket[28] & 255) << 8) | ((short) (DataPacket[27] & 255))))&0xFFFF);
            HeartBeatTime[9] = (double) (((short) (((DataPacket[30] & 255) << 8) | ((short) (DataPacket[29] & 255))))&0xFFFF);
            HeartBeatTime[10] = (double) (((short) (((DataPacket[32] & 255) << 8) | ((short) (DataPacket[31] & 255))))&0xFFFF);
            HeartBeatTime[11] = (double) (((short) (((DataPacket[34] & 255) << 8) | ((short) (DataPacket[33] & 255))))&0xFFFF);
            HeartBeatTime[12] = (double) (((short) (((DataPacket[36] & 255) << 8) | ((short) (DataPacket[35] & 255))))&0xFFFF);
            HeartBeatTime[13] = (double) (((short) (((DataPacket[38] & 255) << 8) | ((short) (DataPacket[37] & 255))))&0xFFFF);
            HeartBeatTime[14] = (double) (((short) (((DataPacket[40] & 255) << 8) | ((short) (DataPacket[39] & 255))))&0xFFFF);

            for(int i = 0; i <= 13; i++){
                if(HeartBeatTime[i] > HeartBeatTime[i+1]){
                    RRInterval[i] = HeartBeatTime[i] - HeartBeatTime[i+1];
                }else if(HeartBeatTime[i] < HeartBeatTime[i+1]){
                    RRInterval[i] = 65536 + HeartBeatTime[i] - HeartBeatTime[i+1];
                }
                //System.out.println("RRInterval" + i +" is " + RRInterval[i]);
            }

            for (int i = 0; i < HBNum; i++){
                avgRRInt = avgRRInt + RRInterval[i];
            }
            this.AvgRRInterval = avgRRInt / HBNum;
            return this.AvgRRInterval;
        }

        public double[] GetRRInterval(byte[] DataPacket) { // By Rahul

            this.HeartBeatNum = (byte) (DataPacket[10] & 255);
            CurrentHBNum = (int)HeartBeatNum & 0xFF;
            if(PrevHBNum < 0) {
                HBNum = 1; //This is the first packet.
            } else if(PrevHBNum < CurrentHBNum){
                HBNum = CurrentHBNum - PrevHBNum; //This is for packs that HBN didn't get wrapped around.
            } else if(CurrentHBNum < PrevHBNum){
                HBNum = 256 - PrevHBNum + CurrentHBNum; //wrap around has occurred.
            }

            if(CurrentHBNum-PrevHBNum > 4)
                HBNum = 1;

            PrevHBNum = CurrentHBNum;

            //********************************************************************************************

            HeartBeatTime[0] = (double) (((short) (((DataPacket[12] & 255) << 8) | ((short) (DataPacket[11] & 255))))&0xFFFF);
            HeartBeatTime[1] = (double) (((short) (((DataPacket[14] & 255) << 8) | ((short) (DataPacket[13] & 255))))&0xFFFF);
            HeartBeatTime[2] = (double) (((short) (((DataPacket[16] & 255) << 8) | ((short) (DataPacket[15] & 255))))&0xFFFF);
            HeartBeatTime[3] = (double) (((short) (((DataPacket[18] & 255) << 8) | ((short) (DataPacket[17] & 255))))&0xFFFF);
            HeartBeatTime[4] = (double) (((short) (((DataPacket[20] & 255) << 8) | ((short) (DataPacket[19] & 255))))&0xFFFF);
            HeartBeatTime[5] = (double) (((short) (((DataPacket[22] & 255) << 8) | ((short) (DataPacket[21] & 255))))&0xFFFF);
            HeartBeatTime[6] = (double) (((short) (((DataPacket[24] & 255) << 8) | ((short) (DataPacket[23] & 255))))&0xFFFF);
            HeartBeatTime[7] = (double) (((short) (((DataPacket[26] & 255) << 8) | ((short) (DataPacket[25] & 255))))&0xFFFF);
            HeartBeatTime[8] = (double) (((short) (((DataPacket[28] & 255) << 8) | ((short) (DataPacket[27] & 255))))&0xFFFF);
            HeartBeatTime[9] = (double) (((short) (((DataPacket[30] & 255) << 8) | ((short) (DataPacket[29] & 255))))&0xFFFF);
            HeartBeatTime[10] = (double) (((short) (((DataPacket[32] & 255) << 8) | ((short) (DataPacket[31] & 255))))&0xFFFF);
            HeartBeatTime[11] = (double) (((short) (((DataPacket[34] & 255) << 8) | ((short) (DataPacket[33] & 255))))&0xFFFF);
            HeartBeatTime[12] = (double) (((short) (((DataPacket[36] & 255) << 8) | ((short) (DataPacket[35] & 255))))&0xFFFF);
            HeartBeatTime[13] = (double) (((short) (((DataPacket[38] & 255) << 8) | ((short) (DataPacket[37] & 255))))&0xFFFF);
            HeartBeatTime[14] = (double) (((short) (((DataPacket[40] & 255) << 8) | ((short) (DataPacket[39] & 255))))&0xFFFF);

            for(int i = 0; i <= 13; i++){
                if(HeartBeatTime[i] > HeartBeatTime[i+1]){
                    RRInterval[i] = HeartBeatTime[i] - HeartBeatTime[i+1];
                }else if(HeartBeatTime[i] < HeartBeatTime[i+1]){
                    RRInterval[i] = 65536 + HeartBeatTime[i] - HeartBeatTime[i+1];
                }
                //System.out.println("RRInterval" + i +" is " + RRInterval[i]);
            }
            double[] result = new double[HBNum];
            int count = 0;
            for(int j = HBNum - 1; j >= 0; j--){
                result[j] = RRInterval[count];
                count++;
            }
            return result;
        }

        public double GetDistance(byte[] DataPacket) {
            this.Distance = ((double) ((short) (((DataPacket[48] & 255) << 8) | ((short) (DataPacket[47] & 255))))) / 16.0d;
            return this.Distance;
        }

        public double GetInstantSpeed(byte[] DataPacket) {
            this.InstantSpeed = ((double) ((short) (((DataPacket[50] & 255) << 8) | ((short) (DataPacket[49] & 255))))) / 256.0d;
            return this.InstantSpeed;
        }

        public byte GetStrides(byte[] DataPacket) {
            this.Strides = (byte) (DataPacket[51] & 255);
            return this.Strides;
        }

        public double GetCadence(byte[] DataPacket) {
            this.Cadence = ((double) ((short) (((DataPacket[54] & 255) << 8) | ((short) (DataPacket[53] & 255))))) / 16.0d;
            return this.Cadence;
        }
    }

    class C00061 implements ZephyrPacketListener {
        C00061() {
        }

        public void ReceivedPacket(ZephyrPacketEvent eventArgs) {
            ZephyrPacketArgs msg = eventArgs.getPacket();
            byte CRCFailStatus = msg.getCRCStatus();
            byte RcvdBytes = msg.getNumRvcdBytes();
            Message text1;
            Bundle b1;
            if (38 == msg.getMsgID()) {
                String genText1 = String.format("Received HR, Speed and Distance Packet", new Object[0]);
                text1 = ConnectListenerImpl.this._handler.obtainMessage(ConnectListenerImpl.HR_SPD_DIST_PACKET);
                b1 = new Bundle();
                b1.putString("genText", genText1);
                //Log.d("Zephyr HR Speed Dist Packet Parsed", genText1);
                text1.setData(b1);
                ConnectListenerImpl.this._handler.sendMessage(text1);
                System.out.println("Battry Charge Value is " + ConnectListenerImpl.this.HRSpdDistInfoPacket.GetBatteryChargeInd(msg.getBytes()));
                System.out.println("Distance is " + ConnectListenerImpl.this.HRSpdDistInfoPacket.GetDistance(msg.getBytes()));
                System.out.println("Strides is " + ConnectListenerImpl.this.HRSpdDistInfoPacket.GetStrides(msg.getBytes()));
                System.out.println("Firmware Version is 9500." + ConnectListenerImpl.this.HRSpdDistInfoPacket.GetFirmwareID(msg.getBytes()) + ".V" + ConnectListenerImpl.this.HRSpdDistInfoPacket.GetFirmwareVersion(msg.getBytes()));
                System.out.println("Hardware Version is 9800." + ConnectListenerImpl.this.HRSpdDistInfoPacket.GetHardwareID(msg.getBytes()) + ".V" + ConnectListenerImpl.this.HRSpdDistInfoPacket.GetHardwareVersion(msg.getBytes()));
            }
            if (ConnectListenerImpl.this.SERIAL_NUMBER == msg.getMsgID()) {
                System.out.println("Received Serial Number");
                String Snum = ConnectListenerImpl.this.SerialNumber;
                text1 = ConnectListenerImpl.this._handler.obtainMessage(1210);
                b1 = new Bundle();
                b1.putString("SerialNumtxt", Snum);
                //Log.d("Zephyr Serial Number PacketParsed", Snum);
                text1.setData(b1);
                ConnectListenerImpl.this._handler.sendMessage(text1);
            }
        }
    }

    public ConnectListenerImpl(Handler handler, byte[] dataBytes) {
        this.SERIAL_NUMBER = 11;
        this.HRSpdDistInfoPacket = new HRSpeedDistPacketInfo();
        this._handler = handler;
        this.Payload = dataBytes;
    }

    public void Connected(ConnectedEvent<BTClient> eventArgs) {
        System.out.println(String.format("Connected to HxM %s.", new Object[]{((BTClient) eventArgs.getSource()).getDevice().getName()}));
        this.SerialNumber = ((BTClient) eventArgs.getSource()).getDevice().getName();
        new ZephyrProtocol(((BTClient) eventArgs.getSource()).getComms()).addZephyrPacketEventListener(new C00061());
    }
}
