package com.rahularodi.zephyr;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;

import zephyr.android.HxMBT.BTClient;
import zephyr.android.HxMBT.ConnectListenerImpl;
import zephyr.android.HxMBT.ConnectedEvent;
import zephyr.android.HxMBT.ZephyrPacketArgs;
import zephyr.android.HxMBT.ZephyrPacketEvent;
import zephyr.android.HxMBT.ZephyrPacketListener;
import zephyr.android.HxMBT.ZephyrProtocol;

//Uses ConnectListenerImpl.java and handles the bluetooth packet. 
public class NewConnectedListener extends ConnectListenerImpl
{
	private Handler _OldHandler;
	private Handler _aNewHandler; 
	private int GP_MSG_ID = 0x20;
	private int GP_HANDLER_ID = 0x20;
	private int HR_SPD_DIST_PACKET =0x26;
	
	private final int HEART_RATE = 0x100;
	private final int INSTANT_SPEED = 0x101;
	private final int HEART_BEAT_TS = 0x102;
	private final int BATTERY_CHARGE = 0x103;
	private final int START_READ = 0x104;
	private HRSpeedDistPacketInfo HRSpeedDistPacket = new HRSpeedDistPacketInfo();

	private FileWriter writer;
	private int elementCount = 0;
	public static int fileCount = 0;
	public static String Write_Sleep_File = "Sleep_File";
	public static Handler realTimeHandler = null;

	public NewConnectedListener(Handler handler,Handler _NewHandler) {
		super(handler, null);
		_OldHandler= handler;
		_aNewHandler = _NewHandler;

		// TODO Auto-generated constructor stub
		try{
			writer = new FileWriter(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Write_Sleep_File + fileCount+".csv"), false);
			System.out.println("!!!!!!!!!!!!!!!!!!!!!!Opened file!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		} catch(Exception e){
			e.printStackTrace();
		}

	}

	public void isConnected(int flag){
		try {
			if(flag == 0)
				writer.write("\n");
				writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void storeValuesToCSV(String heartR, String avgrrInt, String speed, String rrArrayText){
		//String timeStmp = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
		String date=DateFormat.getDateInstance(DateFormat.SHORT).format(Calendar.getInstance().getTime());
		String time=DateFormat.getTimeInstance(DateFormat.SHORT).format(Calendar.getInstance().getTime());
		String timeStmp = date+" "+time;
		try{
			writer.write(timeStmp + "," + heartR + "," + avgrrInt + "," + speed + "," + rrArrayText + "\n");
			System.out.println("*************************Wrote to "+Write_Sleep_File + fileCount+".csv"+"*******************************");
		} catch(Exception e){
			System.out.println("#####################Failed to write to file#########################");
			e.printStackTrace();
		}
	}

	public void Connected(ConnectedEvent<BTClient> eventArgs) {
		System.out.println(String.format("Connected to BioHarness %s.", eventArgs.getSource().getDevice().getName()));
		
		//Creates a new ZephyrProtocol object and passes it the BTComms object
		ZephyrProtocol _protocol = new ZephyrProtocol(eventArgs.getSource().getComms());
		//ZephyrProtocol _protocol = new ZephyrProtocol(eventArgs.getSource().getComms(), );
		_protocol.addZephyrPacketEventListener(new ZephyrPacketListener() {
			public void ReceivedPacket(ZephyrPacketEvent eventArgs) {
				ZephyrPacketArgs msg = eventArgs.getPacket();
				byte CRCFailStatus;
				byte RcvdBytes;

				CRCFailStatus = msg.getCRCStatus();
				RcvdBytes = msg.getNumRvcdBytes() ;
				if (HR_SPD_DIST_PACKET==msg.getMsgID())
				{
					byte [] DataArray = msg.getBytes();
					//***************Displaying Battery charge********************************
					double BatteryCharge = HRSpeedDistPacket.GetBatteryChargeInd(DataArray);
					Message text1 =  _aNewHandler.obtainMessage(BATTERY_CHARGE);
					Bundle b1 = new Bundle();
					b1.putString("BatteryCharge", String.valueOf(BatteryCharge));
					text1.setData(b1);
					_aNewHandler.sendMessage(text1);
					System.out.println("Battery Charge is " + BatteryCharge);
					
					//***************Displaying the Heart Rate********************************
					int HRate =  HRSpeedDistPacket.GetHeartRate(DataArray) & 0xFF;
					text1 = _aNewHandler.obtainMessage(HEART_RATE);

					b1.putString("HeartRate", String.valueOf(HRate));
					text1.setData(b1);
					_aNewHandler.sendMessage(text1);
					System.out.println("Heart Rate is "+ HRate);

					//***************Displaying the Instant Speed********************************
					double InstantSpeed = HRSpeedDistPacket.GetInstantSpeed(DataArray);
					
					text1 = _aNewHandler.obtainMessage(INSTANT_SPEED);
					b1.putString("InstantSpeed", String.valueOf(InstantSpeed));
					text1.setData(b1);
					_aNewHandler.sendMessage(text1);
					System.out.println("Instant Speed is "+ InstantSpeed);

					//***************Displaying the Heart Beat TS********************************
					double AvgRRInterval = HRSpeedDistPacket.GetAvgRRInterval(DataArray);

					text1 = _aNewHandler.obtainMessage(HEART_BEAT_TS);
					b1.putString("AvgRRInterval", String.valueOf(AvgRRInterval));
					text1.setData(b1);
					_aNewHandler.sendMessage(text1);
					System.out.println("AvgRRInterval TS is " + AvgRRInterval);

					//***************Get RRIntervals********************************
					double[] RRInterval = HRSpeedDistPacket.GetRRInterval(DataArray);
					String RRIntervalText = "";
					for(int i = 0; i < RRInterval.length-1; i++){
						RRIntervalText+=RRInterval[i]+",";  //oldest one appended first
					}
					RRIntervalText+=RRInterval[RRInterval.length-1];
					System.out.println("RRInterval is "+ RRIntervalText);

					storeValuesToCSV(String.valueOf(HRate), String.valueOf(AvgRRInterval), String.valueOf(InstantSpeed), RRIntervalText);
					if(realTimeHandler != null) {
						elementCount++;
						if (elementCount >= 512) {
							elementCount = 0;
							Message m = realTimeHandler.obtainMessage(START_READ);
							Bundle b = new Bundle();
							b.putString("START", fileCount + "");
							m.setData(b);
							realTimeHandler.sendMessage(m);
							try {
								writer.close();
								fileCount++;
								writer = new FileWriter(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Write_Sleep_File + fileCount + ".csv"), false);
							} catch (Exception e) {
							}
						}
					}
				}
			}
		});
	}
	
}