/**
 * Created by Yu-Cheng on 11/24/2016.
 */
package com.rahularodi.zephyr;
import android.util.Log;
import com.jjoe64.graphview.DefaultLabelFormatter;

//This class is used for to convert double values in the XY plot into
// readable Time Value and REM/NREM/Wake for X and Y axis respectively.
public class TimeFormatter extends DefaultLabelFormatter {

    @Override
    public String formatLabel(double value, boolean isValueX){
        if(!isValueX){
            //ValueY
            if(value == 0){
                return "Awake";
            }else if(value == 1){
                return "NREM";
            }else if( value == 2){
                return "REM";
            }else{
                return "";
            }
        }else{
            String valueText = "";
            String result = "";
            int startTime = -1;
            int startTimeHour = -1;
            int startTimeMin = -1;
            String startTimeHourText="";
            String startTimeMinText="";

            try {
                valueText = value + "";
                startTime = Integer.parseInt(valueText.split("\\.")[0]);
                startTimeHour = startTime / 60;
                startTimeMin = startTime % 60;
                startTimeHourText = startTimeHour+"";
                startTimeMinText = startTimeMin+"";
                if(startTimeHourText.length() != 2){
                    startTimeHourText = "0"+startTimeHourText;
                }
                if(startTimeMinText.length() != 2){
                    startTimeMinText = "0"+startTimeMinText;
                }
                result = startTimeHourText + ":" + startTimeMinText;
                return result;
            }catch(Exception e){
                Log.e("Exception", "TimeFormatter Exception", e);
            }
            return null;

        }
    }
}
