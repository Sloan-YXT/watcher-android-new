package com.watcher.ffplayer;

import android.app.Activity;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.watcher.ffplayer.entity.Constant;
import com.watcher.ffplayer.entity.SocketStation;

public class DataClassifyActivity extends Activity {
    Button goData,goGraph,goFlowChart;
    TextView info;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_classify);
        goData = (Button)findViewById(R.id.normal_data);
        goGraph = (Button)findViewById(R.id.graph_data);
        goFlowChart = (Button)findViewById(R.id.flow_chart_out);
        info = (TextView)findViewById(R.id.data_show_info);
        info.setText(Constant.name+" "+"in "+Constant.position);
        goData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DataClassifyActivity.this,BoardDataActivity.class);
                intent.putExtra("board name",Constant.name);
                startActivity(intent);
            }
        });
        goGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DataClassifyActivity.this,GraphsActivity.class);
                startActivity(intent);
            }
        });
        goFlowChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DataClassifyActivity.this,FlowChart.class);
                intent.putExtra("board name",Constant.name);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        if(SocketStation.connfdOther.isClosed())
        {
            finish();
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}