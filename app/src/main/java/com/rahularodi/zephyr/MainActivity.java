package com.rahularodi.zephyr;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Set;

import zephyr.android.HxMBT.BTClient;
import zephyr.android.HxMBT.ZephyrProtocol;

public class MainActivity extends Activity {
    /** Called when the activity is first created. */
	BluetoothAdapter adapter = null;
	BTClient _bt;
	int BTConnected = 0;
	ZephyrProtocol _protocol;
	NewConnectedListener _NConnListener;
	private final int HEART_RATE = 0x100;
	private final int INSTANT_SPEED = 0x101;
	private final int HEART_BEAT_TS = 0x102;
	private final int BATTERY_CHARGE = 0x103;

	Context context = this;
	UserDBHelper userDBHelper;
	SQLiteDatabase sqLiteDatabase;
	TextView heartRate, instantSpeed, rrInterval, batteryCharge;
	public final static String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
	String sleep_pattern;
	String sleep_file = "Sleep_File";

	public static Handler mainUIHandler;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        /*Sending a message to android that we are going to initiate a pairing request*/
        IntentFilter filter = new IntentFilter("android.bluetooth.device.action.PAIRING_REQUEST");
        /*Registering a new BTBroadcast receiver from the Main Activity context with pairing request event*/
       this.getApplicationContext().registerReceiver(new BTBroadcastReceiver(), filter);
        // Registering the BTBondReceiver in the application that the status of the receiver has changed to Paired
        IntentFilter filter2 = new IntentFilter("android.bluetooth.device.action.BOND_STATE_CHANGED");
       this.getApplicationContext().registerReceiver(new BTBondReceiver(), filter2);
        
      //Obtaining the handle to act on the CONNECT button
        TextView tv = (TextView) findViewById(R.id.labelStatusMsg);
		String ErrorText  = "Not Connected to HxM ! !";
		tv.setText(ErrorText);

		heartRate = (EditText)findViewById(R.id.labelHeartRate);
		heartRate.setText("000");

		batteryCharge = (EditText)findViewById(R.id.labelBatteryCharge);
		heartRate.setText("000");

		instantSpeed = (EditText)findViewById(R.id.labelInstantSpeed);
		instantSpeed.setText("0.0");

		rrInterval = (EditText)findViewById(R.id.labelAvgRRInterval);
		rrInterval.setText("0.0");

		Button btnConnect = (Button) findViewById(R.id.ButtonConnect);
        if (btnConnect != null)
        {
        	btnConnect.setOnClickListener(new OnClickListener() {
        		public void onClick(View v) {
        			String BhMacID = "00:07:80:9D:8A:E8";
        			//String BhMacID = "00:07:80:88:F6:BF";
        			adapter = BluetoothAdapter.getDefaultAdapter();
        			Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
        			if (pairedDevices.size() > 0) 
        			{
                        for (BluetoothDevice device : pairedDevices) 
                        {
                        	if (device.getName().startsWith("HXM")) 
                        	{
                        		BluetoothDevice btDevice = device;
                        		BhMacID = btDevice.getAddress();
                                break;
                        	}
                        }
        			}
        			//BhMacID = btDevice.getAddress();
        			BluetoothDevice Device = adapter.getRemoteDevice(BhMacID);
        			String DeviceName = Device.getName();
        			_bt = new BTClient(adapter, BhMacID);
        			_NConnListener = new NewConnectedListener(Newhandler,Newhandler);
        			_bt.addConnectedEventListener(_NConnListener);
        			if(_bt.IsConnected())
        			{
        				_bt.start();
        				TextView tv = (TextView) findViewById(R.id.labelStatusMsg);
        				String ErrorText  = "Connected to HxM "+DeviceName;
						tv.setText(ErrorText);
						BTConnected = 1;
						 //Reset all the values to 0s
        			}
        			else
        			{
        				TextView tv = (TextView) findViewById(R.id.labelStatusMsg);
        				String ErrorText  = "Unable to Connect !";
						tv.setText(ErrorText);
						BTConnected = 0;
        			}
        		}
        	});
        }
        /*Obtaining the handle to act on the DISCONNECT button*/
        Button btnDisconnect = (Button) findViewById(R.id.ButtonDisconnect);
        if (btnDisconnect != null)
        {
        	btnDisconnect.setOnClickListener(new OnClickListener() {
				@Override
				/*Functionality to act if the button DISCONNECT is touched*/
				public void onClick(View v) {
					// TODO Auto-generated method stub
					/*Reset the global variables*/
					TextView tv = (TextView) findViewById(R.id.labelStatusMsg);
    				String ErrorText  = "Disconnected from HxM!";
					 tv.setText(ErrorText);

					/*This disconnects listener from acting on received messages*/	
					_bt.removeConnectedEventListener(_NConnListener);
					/*Close the communication with the device & throw an exception if failure*/
					_bt.Close();
				
				}
        	});
        }
		final Button btnStore = (Button) findViewById(R.id.StartLogging);
		btnStore.setTag(1);
		btnStore.setText("Stop");

		btnStore.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(BTConnected == 1) {
					final int status = (Integer) v.getTag();
					if (status == 1) {
						_NConnListener.isConnected(0);
						btnStore.setText("Start");
						v.setTag(0); //pause
					} else {
						btnStore.setText("Stop");
						v.setTag(1); //pause
					}
				}
				else {
					Context context = getApplicationContext();
					CharSequence text = "Not connected to Zephyr HxM!";
					int duration = Toast.LENGTH_SHORT;
					Toast toast = Toast.makeText(context, text, duration);
					toast.show();
				}
			}
		});
    }

	/*Test function for asynchronous writing data to file. (This is not used anymore)*/
	public void TestWrite(View v){
		//NewConnectedListener test = new NewConnectedListener(Newhandler,Newhandler);
		System.out.println(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "writeTest" + ".csv");
		AsyncTask.execute(new Runnable() {
			FileWriter writer;
			int counter = 0;
			int fileCounter = 0;
			public void run() {
				//TODO your background code
				for(int j = 0; j < 5; j++) {
					try {
						System.out.println("****************************Write FILE*******************************************");
						writer = new FileWriter(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "writeTest" + fileCounter + ".csv"), false);
						for (int i = 0; i < 5; i++) {
							System.out.println("Write File: Counter: " + counter + " in fileCounter: " + fileCounter+"\n");
							writer.write(counter+"\n");
							Thread.sleep(1000);
							counter++;
						}
						fileCounter++;
						writer.close();
						System.out.println("**********************CLOSE WRITE FILE***********************************");

					} catch (Exception e) {
						e.printStackTrace();
					}
					if(mainUIHandler != null){
						Runnable myRunnable = new Runnable() {
							@Override
							public void run() {
								File f;
								int fileCounter = 0;
								//public void run() {
								//TODO your background code
								try {
									String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "writeTest" + fileCounter + ".csv";
									Log.d("file path", path);
									System.out.println("**********************READ FILE********************************");
									f = new File(path);
									FileInputStream inFile = new FileInputStream(f);
									InputStreamReader isr = new InputStreamReader(inFile);
									BufferedReader reader = new BufferedReader(isr);
									String line;
									while ((line = reader.readLine()) != null) {
										System.out.println(line);
										Thread.sleep(1000);
									}
									fileCounter++;
									System.out.println("**********************CLOSE READ FILE********************************");
									reader.close();
									isr.close();
									inFile.close();
								} catch (Exception e) {
									Log.e("FileNotExist", "FileNotExist", e);
								}
							}
						};
						mainUIHandler.post(myRunnable);
					}
				}
			}
		});
	}

	/* Handler function for receiving bluetooth packets */
    private class BTBondReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle b = intent.getExtras();
			BluetoothDevice device = adapter.getRemoteDevice(b.get("android.bluetooth.device.extra.DEVICE").toString());
			Log.d("Bond state", "BOND_STATED = " + device.getBondState());
		}
    }

	/* Handler function for receiving bluetooth packets */
    private class BTBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("BTIntent", intent.getAction());
			Bundle b = intent.getExtras();
			Log.d("BTIntent", b.get("android.bluetooth.device.extra.DEVICE").toString());
			Log.d("BTIntent", b.get("android.bluetooth.device.extra.PAIRING_VARIANT").toString());
			try {
				BluetoothDevice device = adapter.getRemoteDevice(b.get("android.bluetooth.device.extra.DEVICE").toString());
				Method m = BluetoothDevice.class.getMethod("convertPinToBytes", new Class[] {String.class} );
				byte[] pin = (byte[])m.invoke(device, "1234");
				m = device.getClass().getMethod("setPin", new Class [] {pin.getClass()});
				Object result = m.invoke(device, pin);
				Log.d("BTTest", result.toString());
			} catch (SecurityException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (NoSuchMethodException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }

	/*Test function for writing result to a file (This is not used anymore) */
	public void storeInfo(View view)
	{
		String timeStmp = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
		String heartR = heartRate.getText().toString();
		String rrInt = rrInterval.getText().toString();
		String speed = instantSpeed.getText().toString();

		userDBHelper = new UserDBHelper(context);
		sqLiteDatabase = userDBHelper.getWritableDatabase();
		userDBHelper.addInformation(timeStmp, heartR, rrInt, speed, sqLiteDatabase);
		Toast.makeText(getBaseContext(), "Data Being Saved", Toast.LENGTH_SHORT).show();
		userDBHelper.close();

	}

	/*Test function for writing result to a file (This is not used anymore) */
	public void storeValuesToCSVOnClick(View view){
		String timeStmp = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
		String heartR = heartRate.getText().toString();
		String rrInt = rrInterval.getText().toString();
		String speed = instantSpeed.getText().toString();

		try{
			FileWriter writer = new FileWriter(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Sleep_File.csv"), true);
			writer.write(timeStmp + "," + heartR + "," + rrInt + "," + speed + "\n");
			writer.close(); //close it only once when stop store button is pressed. Maybe use a global flag.
			Toast.makeText(getBaseContext(), "Data Saved", Toast.LENGTH_SHORT).show();
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	/*Test function for writing result to a file (This is not used anymore) */
	public  static void writeToFile(){

		try {
			FileWriter writer = new FileWriter(new File(Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"LogFile.txt"), true);
			String timeStmp = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
			writer.write(timeStmp + "\n");
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* This is the onClick function for MainUI's Plot button*/
    public void goToPlot(View view)
    {
        Intent intent = new Intent(this, Plot.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		mainUIHandler =new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == Plot.plot_tag) {
					Bundle plotReply = (Bundle) msg.obj;
					sleep_pattern = plotReply.getString("Sleep_Pattern");
					sleep_file = plotReply.getString("Sleep_File");
					Log.d("HandleMessage", sleep_pattern+"~"+sleep_file);
				}
			}
		};
		Plot.mainUIHandler = mainUIHandler;
        startActivity(intent);
    }

	/*Handler for receiving bluetooth data*/
	final Handler Newhandler = new Handler(){
    	public void handleMessage(Message msg)
    	{
    		TextView tv;
    		switch (msg.what)
    		{
				case HEART_RATE:
					String HeartRatetext = msg.getData().getString("HeartRate");
					tv = (EditText)findViewById(R.id.labelHeartRate);
					System.out.println("Heart Rate Info is "+ HeartRatetext);
					if (tv != null)tv.setText(HeartRatetext);
				break;
    		
				case INSTANT_SPEED:
					String InstantSpeedtext = msg.getData().getString("InstantSpeed");
					tv = (EditText)findViewById(R.id.labelInstantSpeed);
					if (tv != null)tv.setText(InstantSpeedtext);

				break;

				case HEART_BEAT_TS:
					String AvgRRInterval = msg.getData().getString("AvgRRInterval");
					tv = (EditText)findViewById(R.id.labelAvgRRInterval);
					if (tv != null)tv.setText(AvgRRInterval);

					break;

				case BATTERY_CHARGE:
					String BatteryCharge = msg.getData().getString("BatteryCharge");
					tv = (EditText)findViewById(R.id.labelBatteryCharge);
					if (tv != null)tv.setText(BatteryCharge);

					break;
    		}
    	}

    };
    
}


