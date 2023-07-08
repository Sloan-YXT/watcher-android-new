package com.watcher.ffplayer;

import android.app.Activity;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.watcher.ffplayer.entity.Constant;
import com.watcher.ffplayer.entity.DataAlarm;
import com.watcher.ffplayer.entity.SocketStation;

public class DataShow extends Activity {
    private TextView time,temp,humi,light,smoke;
    private Button back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("DataShow","in DataShow before set ContentView");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_show);
        Log.e("DataShow","in DataShow");
        back = (Button) findViewById(R.id.data_back);
        time = (TextView)findViewById(R.id.data_time);
        temp  = (TextView)findViewById(R.id.data_temp);
        humi = (TextView)findViewById(R.id.data_humi);
        light = (TextView)findViewById(R.id.data_light);
        smoke = (TextView)findViewById(R.id.data_smoke);
        final Intent intent = getIntent();
        Log.e("warn data","DataShow 1");
        DataAlarm alarm = (DataAlarm) intent.getSerializableExtra("alarm");
        Log.e("warn data","DataShow 2");
        time.setText("time:"+alarm.time);
        String tail;
        if( Double.parseDouble(alarm.temp)> Constant.highTemp) {
            tail = "(too high)";
        }
        else
        {
            tail = "(normal)";
        }
        if(alarm.temp == Constant.NA)
        {
            temp.setText("Temperature:No temperature sensor");
        }
        else {
            temp.setText("Temperature:" + alarm.temp + tail);
        }
        if( Double.parseDouble(alarm.humi)> Constant.highHumi) {
            tail = "(too high)";
        }
        else
        {
            tail = "(normal)";
        }
        if(alarm.temp == Constant.NA)
        {
            temp.setText("Humidity:No humidity sensor");
        }
        else {
            humi.setText("Humidity:" + alarm.humi + tail);
        }
        if(alarm.light.equals(Constant.NA))
        {
            tail = "No light Sensor";
        }
        else if(alarm.light.equals(Constant.wrongLight)) {

            tail = "Too dark";
        }
        else
        {
            tail = "Normal";
        }
        light.setText("Light:"+tail);
        if(alarm.light.equals(Constant.NA))
        {
            tail = "No light Sensor";
        }
        else if(alarm.smoke.equals(Constant.wrongSmoke))
        {
            tail = "Sensitive gas detected";
        }
        else
        {
            tail = "Normal";
        }
        smoke.setText("Smoke:"+tail);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}