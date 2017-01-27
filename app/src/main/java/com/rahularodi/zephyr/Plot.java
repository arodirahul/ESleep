/**
 * Created by Yu-Cheng on 11/24/2016.
 */
package com.rahularodi.zephyr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import static com.rahularodi.zephyr.setting.plottingHandler;
import static com.rahularodi.zephyr.setting.setting_tag;

/*This is used for plotting. */
public class Plot extends Activity {
    int Fs = 4; //SAMPLING FREQUENCY = 4 HZ
    public final static String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    public ArrayList<String> timeFrame = new ArrayList<>();
    Handler mainHandler;
    String sleep_pattern;
    String sleep_file = "Sleep_File";
    double[] sleep_time = new double[]{0.0, 480.0};
    public static Handler plotHandler = null;
    public static Handler mainUIHandler = null;
    public static final int plot_tag = 1;
    private final int START_READ = 0x104;
    private ArrayList<Integer> realTimeSleepState = null;
    int temporaryCounter = 0;
    private ToggleButton togglebutton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plot_layout);
        //mainHandler = new Handler(this.getMainLooper());
        //MainActivity.mainUIHandler = mainHandler;
        Log.d("onCreate", "onCreate");
        togglebutton = (ToggleButton) findViewById(R.id.toggleButton);
    }
    public void toggleclick(View v) {
        if (togglebutton.isChecked()){
            //Toast.makeText(this, "ON", Toast.LENGTH_SHORT).show();
            NewConnectedListener.realTimeHandler = realTimeHandler;
            realTimeSleepState = new ArrayList<>();
        }else{
            //Toast.makeText(this, "OFF", Toast.LENGTH_SHORT).show();
            NewConnectedListener.realTimeHandler = null;
            realTimeSleepState = new ArrayList<>();
            timeFrame = new ArrayList<>();
        }
    }

    //Resume state on resume.
    protected void onResume(){
        super.onResume();
        Log.d("onResume", "Plot onResume");
    }

    //helper class for analyze real time sleep state.
    private void startAnalyze(int fileCount){
        //Total data points = 60 * 60 * 24
        //512 data points for analysis
        //169 analysis (files in total per day).

        String[] lines = readFile(fileCount);
        ArrayList<Integer>  values = parseLine(lines);
        int[] RRValues = arrayPadding(values);

        double[] fftValues = getFFT(RRValues);
        double[] frequency = getFrequency(fftValues);

        String fftString = "";
        for (int i = 0; i < fftValues.length - 1; i++) {
            fftString += fftValues[i] + ", ";
        }
        fftString += fftValues[fftValues.length - 1] + "]";
        String frequencyString = "";
        for (int i = 0; i < frequency.length - 1; i++) {
            frequencyString += frequency[i] + ", ";
        }
        frequencyString += frequency[frequency.length - 1] + "]";

        Log.d("FFTAmplitude", "[" + fftString);
        Log.d("Frequency", "[" + frequencyString);

        int sleepState = getSleepState(RRValues);
        realTimeSleepState.add(sleepState);
        double[] states = new double[realTimeSleepState.size()];
        for(int b = 0; b < timeFrame.size(); b++){
            states[b] = realTimeSleepState.get(b);
        }

        double[] number = new double[timeFrame.size()];
        for(int a = 0; a < timeFrame.size(); a++){
            String startTime = timeFrame.get(a).split("~")[0];
            int startTimeDouble = convertTimeToDouble(startTime);
            number[a] = startTimeDouble+30*a;
        }

        drawChart(number, states, this);
        Log.d("TimeFrame", "TimeFrame: "+ timeFrame);
        Log.d("SleepState", "Sleep State: " + states);
    }

    //This is the function that's called for Anaalyze button's oncreate.
    public void analyze(View view){
        String[] lines = readFile(-1);
        if(lines == null){
            Toast.makeText(this, "The Selected File Is Corrupted", Toast.LENGTH_SHORT);
            return;
        }
        timeFrame = new ArrayList<>();
        ArrayList<ArrayList<Integer>>  values = parseLines(lines, 512);
        if(values == null){
            Toast.makeText(this, "The Selected File Is Corrupted", Toast.LENGTH_SHORT);
            return;
        }
        ArrayList<Double> sleepState = new ArrayList<>();

        for(int j = 0; j < values.size(); j++) {
            int[] RRValues = arrayPadding(values.get(j));

            double[] fftValues = getFFT(RRValues);
            double[] frequency = getFrequency(fftValues);

            String fftString = "";
            for (int i = 0; i < fftValues.length - 1; i++) {
                fftString += fftValues[i] + ", ";
            }
            fftString += fftValues[fftValues.length - 1] + "]";


            String frequencyString = "";
            for (int i = 0; i < frequency.length - 1; i++) {
                frequencyString += frequency[i] + ", ";
            }
            frequencyString += frequency[frequency.length - 1] + "]";

            Log.d("FFTAmplitude", "[" + fftString);
            Log.d("Frequency", "[" + frequencyString);

            //Return 0 for wake, 1 for nREM, and 2 for REM
            int value = getSleepState(RRValues);
            sleepState.add((double)value);
            Log.d("TimeFrame", "TimeFrame: "+ timeFrame.get(j));
            Log.d("SleepState", "Sleep State: " + value);
        }
        double[] states = new double[sleepState.size()];
        for(int a = 0; a < sleepState.size(); a++){
            states[a] = sleepState.get(a);
        }

        double[] number = new double[timeFrame.size()];
        for(int b = 0; b < timeFrame.size(); b++){
            String frame = timeFrame.get(b);
            String startTime = frame.split("~")[0];
            int startTimeDouble = convertTimeToDouble(startTime);
            number[b] = startTimeDouble+0.0;
        }
        if(number.length > 0){
            drawChart(number, states, this);
        }
        timeFrame = new ArrayList<>();
        Log.d("TimeFrames", "TimeFrames: "+timeFrame);
        Log.d("SleepState", "SleepState: "+sleepState);
    }

    private int convertTimeToDouble(String time){
        int hour = 0;
        int minute = 0;
        String AMPM = "";
        String[] AMPMSplit = time.split(" ");
        String[] timeSplit = AMPMSplit[0].split(":");

        try {
            hour = Integer.parseInt(timeSplit[0]);
            minute = Integer.parseInt(timeSplit[1]);
            if(AMPMSplit.length == 2){
                AMPM = AMPMSplit[1];
            }
        }catch(Exception e){
            Log.e("Exception", "invalid x value");
        }
        int result = 0;

        if(AMPM.toLowerCase().equals("pm")){
            hour+=12;
        }
        result+=60*hour;
        result+=minute;
        return result;
    }

    //Parse lines from a single file.
    public ArrayList<Integer> parseLine(String[] lines){
        ArrayList<Integer> result = new ArrayList<>();
        String startTime = "";
        String value = "";
        String[] time1;
        String[] time2;
        String timeText;
        String[] line;
        String currentTime;
        for(int i = 0; i < lines.length; i++) {
            //TimeStamp, HeartRate, AvgRR, InstantSpeed, RRInterval[old-->new]
            line = lines[i].split(",");
            currentTime = line[0];
            if(i == 0){
                startTime = currentTime;
            }

            for(int j = 4; j < line.length; j++){
                value = line[j].split("\\.")[0];
                result.add(Integer.parseInt(value));
            }
            if(i == lines.length-1) {
                time1 = startTime.split(" ");
                time2 = currentTime.split(" ");
                timeText = "";
                if (time1.length == 3) {
                    timeText = time1[1] + " " + time1[2] + "~" + time2[1] + " " + time2[2];
                } else {
                    timeText = time1[1] + "~" + time2[1];
                }
                timeFrame.add(timeText);
            }
        }
        return result;
    }

    //Given lines of a file, plot a point every numberPerInterval value.
    //numberPerInterval is set to 512 currently.
    public ArrayList<ArrayList<Integer>> parseLines(String[] lines, int numbePerInterval){
        ArrayList<ArrayList<Integer>> result = new ArrayList<>();
        ArrayList<Integer> rrValues = new ArrayList<>();
        String startTime = "";
        int counter = 0;
        for(int i = 0; i < lines.length; i++) {
            //TimeStamp, HeartRate, AvgRR, InstantSpeed, RRInterval[old-->new]
            String[] line = lines[i].split(",");
            String currentTime = line[0];
            if(counter == 0){
                startTime = currentTime;
                rrValues = new ArrayList<>();
            }

            for(int j = 4; j < line.length; j++){
                try {
                    String value = line[j].split("\\.")[0];
                    rrValues.add(Integer.parseInt(value));
                }catch(Exception e){
                    Log.d("bad", "bad");
                }
                counter++;
                if(counter == numbePerInterval){
                    counter = 0;
                    String[] time1 = startTime.split(" ");
                    String[] time2 = currentTime.split(" ");
                    String timeText = "";
                    if(time1.length == 3){
                        timeText = time1[1]+" "+time1[2] + "~" + time2[1] + " " + time2[2];
                    }else{
                        timeText = time1[1] + "~" + time2[1];
                    }

                    timeFrame.add(timeText);
                    result.add(rrValues);
                }
            }
        }
        return result;
    }

    //Function to read a file
    //The index is used to access the correct file.
    //-1 is the default file.
    //0----> infinity are the file index for real time plotting.
    public String[] readFile(int index){
        //READ A FILE
        ArrayList<String> lines = new ArrayList<>();
        try {
            File f;

            if(index == -1){
                Log.d("file path", Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + sleep_file +".csv");
                f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + sleep_file+".csv");
            }else {
                Log.d("file path", Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Sleep_File" + index + ".csv");
                f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Sleep_File" + index + ".csv");
            }

            FileInputStream inFile = new FileInputStream(f);
            InputStreamReader isr = new InputStreamReader(inFile);
            BufferedReader reader = new BufferedReader(isr);
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            reader.close();
            isr.close();
            inFile.close();
        }catch(Exception e){
            //Log.e("FileNotExist", "FileNotExist", e);
            return null;
        }
        String[] result = new String[lines.size()];
        for(int i = 0; i < lines.size(); i++){
            result[i] = lines.get(i);
        }
        return result;
    }

    //padding for FFT in case there are not enough values.
    private int[] arrayPadding(ArrayList<Integer> input){
        int[] powerOf2 = new int[12];
        for(int i = 0; i <= 11; i++){
            powerOf2[i] = (int) Math.pow(2, i);
        }
        int largestPowerOf2 = -1;
        for(int j = 0; j < powerOf2.length; j++){
            if(powerOf2[j] >= input.size()){
                largestPowerOf2 = powerOf2[j];
                break;
            }
        }
        int[] result = new int[largestPowerOf2];
        int counter = 0;
        for(int k = 0; k < largestPowerOf2; k++){
            if(k < (2 * input.size()-largestPowerOf2)){
                result[k] = input.get(counter);
            }else{
                result[k] = input.get(counter);
                result[k+1] = input.get(counter);
                k++;
            }
            counter++;
        }
        return result;
    }

    //Returns true if the user shouldn't be asleep, and sound the alarm.
    private boolean checkInvalidSleep(double time, double sleepState){
        for(int i = 0; i < sleep_time.length-1; i+=2){
            if(!(sleep_time[i] < time && sleep_time[i+1] > time) && (int) sleepState != 0){
                //Sleep is invalid
                return true;
            }
        }
        //Sleep is valid
        return false;
    }

    //Helper function for drawing the graph.
    private void drawChart(double[] xvalues, double[] yvalues, final Context c){
        ArrayList<DataPoint> invalidSleep = new ArrayList<>();
        GraphView graph = (GraphView) findViewById(R.id.graph);
        graph.removeAllSeries();
        // first series is a line
        DataPoint[] points = new DataPoint[xvalues.length];

        for (int i = 0; i < points.length; i++) {
            points[i] = new DataPoint(xvalues[i], yvalues[i]);

            if(checkInvalidSleep(xvalues[i], yvalues[i])){
                DataPoint p = new DataPoint(xvalues[i], yvalues[i]);
                invalidSleep.add(p);
            }
        }
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(points);
        series.setDrawDataPoints(true);
        series.setDataPointsRadius((float) 20);
        series.setColor(Color.RED);

        //Tap Listener
        series.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                TimeFormatter x = new TimeFormatter();
                String formatXValue = x.formatLabel(dataPoint.getX(), true);
                String formatYValue = x.formatLabel(dataPoint.getY(), false);
                String pointText = "("+formatXValue+","+formatYValue+")";
                Toast.makeText(c, "Series1: On Data Point clicked: "+pointText, Toast.LENGTH_SHORT).show();
            }
        });

        // set manual X bounds
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(2);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(xvalues[0]-30);
        graph.getViewport().setMaxX(xvalues[0]+60);
        graph.getViewport().setScrollable(true); // enables horizontal scrolling
        graph.getViewport().setScalable(true); // enables horizontal zooming and scrolling

        //setLabelFormatter
        graph.getGridLabelRenderer().setLabelFormatter(new TimeFormatter());
        graph.getGridLabelRenderer().setNumHorizontalLabels(6); //only 4 because of the spacing
        graph.getGridLabelRenderer().setTextSize(20);//20 for phone, use 40 for emulator
        graph.getGridLabelRenderer().reloadStyles();
        graph.addSeries(series);


        DataPoint[] ps = new DataPoint[2];
        ps[0] = new DataPoint(xvalues[0]-30, 0);
        ps[1] = new DataPoint(xvalues[xvalues.length-1]+30, 0);
        LineGraphSeries<DataPoint> series2 = new LineGraphSeries<>(ps);
        series2.setDrawDataPoints(true);
        series2.setColor(Color.TRANSPARENT);
        graph.addSeries(series2);

//        DataPoint[] ps2 = new DataPoint[invalidSleep.size()];
//        for(int a = 0; a < ps2.length; a++){
//            ps2[a] = invalidSleep.get(a);
//        }
//        PointsGraphSeries<DataPoint> series3 = new PointsGraphSeries<>(ps2);
//        series3.setColor(Color.BLUE);
//        series3.setSize((float) 30);
//        graph.addSeries(series3);
        graph.invalidate();
    }

    //onClick function to go to setting UI.
    public void toSetting(View view)
    {
        Intent intent = new Intent(this, setting.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        Bundle b = getIntent().getBundleExtra("Setting");
        plotHandler =new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == setting_tag) {
                    Bundle settingReply = (Bundle) msg.obj;

                    String temp1 = settingReply.getString("Sleep_Pattern");
                    String temp2 = settingReply.getString("Sleep_File");
                    double[] temp3 = settingReply.getDoubleArray("Sleep_Time");
                    if(temp1 != null){
                        sleep_pattern = temp1;
                    }
                    if(temp2 != null){
                        sleep_file = temp2;
                    }
                    if(temp3 != null){
                        sleep_time = temp3;
                    }
                    Log.d("HandleMessage", sleep_pattern+"~"+sleep_file);
                }
            }
        };
        plottingHandler = plotHandler;
        if(b != null){
            intent.putExtra("Plot", b);
        }
        startActivity(intent);

    }

    //get frequency array from the given RR values.
    private double[] getFrequency(double[] RRValues){
        int length = RRValues.length;
        double[] frequency = new double[length];
        for(int i = 0; i < length; i++){
            frequency[i] = (i * Fs * 1.0) / length;
        }
        return frequency;
    }
    //Return 0 for wake, 1 for nREM, and 2 for REM
    private int getSleepState(int[] RRvalues){
        double[] fftValues = getFFT(RRvalues);
        double VLFPower = totalPower(fftValues, 0);
        double LFPower = totalPower(fftValues, 1);
        double HFPower = totalPower(fftValues, 2);
        double ratio = LFPower/HFPower;

        //((1.02+0.83) + (2.4-1.96))/2 = 1.145
        if(ratio > (1.145)){
            return 2;
        }

        //Wake & REM < (( 0.943 + 0.142) +   (1.035 - 0.157)) / 2  <   nREM
        //Wake & REM < 0.9815 < nREM
        double RR_average = averageRR(RRvalues);
        if(RR_average > 0.987 * 1000){
            return 1;
        }
        return 0;


//        Log.d("AverageRR", "AverageRR: "+RR_average);
//        Log.d("VLFPower", "VLFPower: "+VLFPower);
//        Log.d("LFPower", "LFPower: "+LFPower);
//        Log.d("HFPower", "HFPower: "+HFPower);
//        Log.d("LFPower/HFPower", "LFPower/HFPower: "+ratio);

    }

    //Calculate average RR
    public double averageRR(int[] RRValues){
        double result = 0;
        for(int i = 0; i < RRValues.length; i++){
            result += RRValues[i];
        }
        result = result / RRValues.length;
        return result;
    }

    //regions: 0 for VLF, 1 for LF, 2 for HF, 3 for all
    private double totalPower(double[] fftValues, int region){
        double totalPower = 0;
        int[] indexInFFT = getIndex(fftValues, region);
        if(region == 3){
            for(int j = 0; j < fftValues.length; j++){
                totalPower += fftValues[j];
            }
        }else {
            for (int i = 0; i < indexInFFT.length; i++) {
                totalPower += fftValues[indexInFFT[i]];
            }
        }
        return totalPower;
    }

    //regions: 0 for VLF, 1 for LF, 2 for HF
    private int[] getIndex(double[] fftValues, int region){
        ArrayList<Integer> temp = new ArrayList<Integer>();
        int length = fftValues.length;
        for(int i = 0; i < length; i++){
            if(region == 0 && 0.02 < (double) (Fs * i)/length && (double) (Fs * i)/length < 0.05){
                temp.add(i);
            }
            if(region == 1 && 0.05 <= (double) (Fs * i)/length && (double) (Fs * i)/length < 0.15){
                temp.add(i);
            }
            if(region == 2 && 0.15 <= (double) (Fs * i)/length && (double) (Fs * i)/length < 0.4){
                temp.add(i);
            }
        }
        int[]  result = new int[temp.size()];
        for(int j = 0; j < temp.size(); j++){
            result[j] = temp.get(j);
        }
        return result;
    }

    //length of RRValues needs to be even number
    private double[] getFFT(int[] RRValues){
        int length = RRValues.length;
        FFT f = new FFT(length);
        double[] real = new double[length];
        double[] imag = new double[length];
        for(int i = 0; i < length; i++){
            real[i] = RRValues[i];
            imag[i] = 0;
        }
        f.fft(real, imag);
        //double frequency = Fs*(0:(length/2))/length;
        double[] frequency = new double[(length/2)+1];
        for(int i = 0; i < (length/2)+1; i++){
            frequency[i] = (Fs * i)/length;
        }
        double[] power_2_side = new double[length];
        for(int i = 0; i < length; i++){
            power_2_side[i] = (Math.abs(real[i] + imag[i]))/length;
        }
        double[] power_1_side = new double[(length/2) + 1];
        for(int i = 0; i < (length/2) + 1; i++){
            if(i != 0 && i != (length/2)) {
                power_1_side[i] = 2 * power_2_side[i];
            }else{
                power_1_side[i] = power_2_side[i];
            }
        }
        return power_1_side;
    }

    //onClick function to go to mainUI.
    public void toMainUI(View v){
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        Bundle b = new Bundle();
        b.putString("Sleep_Pattern", sleep_pattern);
        b.putString("Sleep_File", sleep_file);

        Message msg = mainUIHandler.obtainMessage();
        msg.what = plot_tag;
        msg.obj = b;
        mainUIHandler.sendMessage(msg);
        startActivity(intent);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.activity_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    final Handler realTimeHandler = new Handler(){
        public void handleMessage(Message msg)
        {

            switch (msg.what)
            {
                case START_READ:
                    String fileCount = msg.getData().getString("START");
                    startAnalyze(Integer.parseInt(fileCount));
                    break;
            }
        }

    };
}
