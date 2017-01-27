/**
 * Created by Yu-Cheng on 11/24/2016.
 */
package com.rahularodi.zephyr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class setting extends Activity {

    public String Sleep_Pattern = "Monophasic";
    public String Sleep_File = "Sleep_File";
    public static final int setting_tag = 1;
    public static Handler plottingHandler = null;
    public double[] sleep_time = convertTextToDouble(Sleep_Pattern);
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plot_setting);

        Intent intent = getIntent();
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.planets_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            protected Adapter initializedAdapter=null;
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
            {
                if(initializedAdapter !=parentView.getAdapter() ) {
                    initializedAdapter = parentView.getAdapter();
                    return;
                }
                String selected = parentView.getItemAtPosition(position).toString();
                Sleep_Pattern = selected;
                sleep_time = convertTextToDouble(Sleep_Pattern);

                Log.d("OnItemSelected", selected);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                Log.d("On Nothing Selected", "BAD");
            }
        });
        // Create an ArrayAdapter using the string array and a default spinner layout=
        List<File> files = getListFiles(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator));
        List<String> spinnerArray =  new ArrayList<String>();
        for(int i = 0; i < files.size(); i++){
            spinnerArray.add(files.get(i).getName().split("\\.")[0]);
        }
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerArray);

        // Specify the layout to use when the list of choices appears
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner2.setAdapter(adapter2);
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            protected Adapter initializedAdapter=null;
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
            {
                if(initializedAdapter !=parentView.getAdapter() ) {
                    initializedAdapter = parentView.getAdapter();
                    return;
                }
                String selected = parentView.getItemAtPosition(position).toString();
                Sleep_File = selected;
                Log.d("OnItemSelected", selected);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
                Log.d("On Nothing Selected", "BAD");
            }
        });

    }

    private List<File> getListFiles(File parentDir) {
        ArrayList<File> inFiles = new ArrayList<File>();
        File[] files = parentDir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                inFiles.addAll(getListFiles(file));
            } else {
                if(file.getName().endsWith(".csv")){
                    inFiles.add(file);
                }
            }
        }
        return inFiles;
    }

    /*Saves setting data*/
    protected void onResume(){
        super.onResume();
        Log.d("onResume", "Setting onResume");
        Bundle b = getIntent().getBundleExtra("Plot");
        if(b != null){
            Sleep_Pattern = b.getString("Sleep_Pattern");
            Sleep_File = b.getString("Sleep_File");
            Spinner spinner = (Spinner) findViewById(R.id.spinner);
            Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
            spinner.setSelection(getIndex(spinner, Sleep_Pattern));
            spinner2.setSelection(getIndex(spinner2, Sleep_File));
        }
    }

    /*helper class for the two spinner object in the setting UI*/
    private int getIndex(Spinner spinner, String myString){
        int index = 0;
        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).equals(myString)){
                index = i;
            }
        }
        return index;
    }

    /*Convert String of sleep pattern to time frame format*/
    public double[] convertTextToDouble(String input){
        ArrayList<Double> result = new ArrayList<>();
        String time = "";
        if(input.equals("Monophasic")){
            time = "00:00 am-8:00 am";
        }else if(input.equals("Segmented Sleep (Biphasic)")){
            time = "00:00 am-3:30 am,5:30 am-9:00 am";
        }else if(input.equals("Siesta Sleep (Biphasic)")){
            time = "00:00 am-5:00 am,12:00 pm-1:30 pm";
        }else if(input.equals("Triphasic Sleep")){
            time = "00:00 am-1:30 am,8:00 am-9:30 am,4:00 pm-5:30 pm";
        }else if(input.equals("Everyman Sleep")){
            time = "00:00 am-3:30 am,7:12 am-7:30 am,11:12 am-11:30 am,5:30 pm-5:48 pm";
        }else if(input.equals("Dual Core Sleep")){
            time = "00:00 am-3:30 am,7:30 am-9:00 am,3:00 pm-3:18 pm";
        }else{
            time = "12:00 am-8:00 am";
        }
        String[] sleepPeriods = time.split(",");
        for(int i = 0; i < sleepPeriods.length; i++){
            String[] sleepPeriod = sleepPeriods[i].split("-");
            String startTime = sleepPeriod[0];
            String endTime = sleepPeriod[1];
            double startTimeDouble = convertTimeToDouble(startTime)+0.0;
            double endTimeDouble = convertTimeToDouble(endTime)+0.0;
            result.add(startTimeDouble);
            result.add(endTimeDouble);
        }
        double[] results = new double[result.size()];
        for(int j = 0; j < result.size(); j++){
            results[j] = result.get(j);
        }

        return results;
    }

    /*Convert time frame format to double, this is used to compare with sleep state of the user
    * if the user is asleep in anytime NOT in these timeframes, the alarm should sound */
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

    //This is the onClick function that Plot button in the setting UI calls.
    public void goToPlot(View view)
    {
        Intent intent = new Intent(this, Plot.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        Bundle b = new Bundle();
        b.putString("Sleep_Pattern", Sleep_Pattern);
        b.putString("Sleep_File", Sleep_File);
        b.putDoubleArray("Sleep Time", sleep_time);
        Message msg = plottingHandler.obtainMessage();
        msg.what = setting_tag;
        msg.obj = b;
        plottingHandler.sendMessage(msg);
        startActivity(intent);
    }
}
