package com.watcher.ffplayer;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.watcher.ffplayer.R;
import com.watcher.ffplayer.adapter.AlarmsAdapter;
import com.watcher.ffplayer.entity.Alarm;
import com.watcher.ffplayer.entity.Board;
import com.watcher.ffplayer.entity.Constant;
import com.watcher.ffplayer.entity.SocketStation;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class AlarmActivity extends Activity {
    private ListView alarms;
    private TextView name,position;
    private AlarmsAdapter alarmsAdapter;
//    SharedPreferences sdata;
//    SharedPreferences.Editor editor;
    private LinearLayout footer;
    private TextView footerNotify;
    private File rootDir;
    private Button back,del;
    private List<Alarm> alarmList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        name = (TextView)findViewById(R.id.alarm_name);
        position = (TextView)findViewById(R.id.alarm_position);
        alarms = (ListView)findViewById(R.id.alarms);
        alarmsAdapter = new AlarmsAdapter(this);
        alarms.setAdapter(alarmsAdapter);
        footer = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.alarm_foot, null);
        back = (Button)footer.findViewById(R.id.alarms_back);
        del = (Button)footer.findViewById(R.id.alarms_del);
        alarms.addFooterView(footer);
        alarms.setFooterDividersEnabled(false);
        alarms.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    synchronized (SocketStation.alarms) {
                        alarmList = new ArrayList<Alarm>(SocketStation.alarms);
                    }
                    alarmsAdapter.setItemList(alarmList);
                    alarmsAdapter.notifyDataSetChanged();
                } else if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {

                }

            }
            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {

            }
        });
        Intent intent = getIntent();
        String sname = intent.getStringExtra("board_name");
        name.setText(sname);
        String sposition = intent.getStringExtra("board_position");
        position.setText(sposition);
        synchronized (SocketStation.alarms) {
            alarmList = new ArrayList<Alarm>(SocketStation.alarms);
        }
        alarmsAdapter.setItemList(alarmList);
        alarmsAdapter.notifyDataSetChanged();
        synchronized (SocketStation.alarms) {
            if (SocketStation.alarms.size() == 0) {
                Toast.makeText(AlarmActivity.this, "currently no alarm dectected", Toast.LENGTH_SHORT).show();
                try {
                    Thread.sleep(1);
                } catch (Exception e) {
                    Log.e("AlarmActivity", Log.getStackTraceString(e));
                }
                //AlarmActivity.this.finish();
            }
        }
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlarmActivity.this.finish();
            }
        });
        del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View viewDialog = getLayoutInflater().inflate(R.layout.mydialog,
                        null, false);
                final Dialog dialog = new Dialog(AlarmActivity.this,
                        R.style.myDialogTheme);
                dialog.setContentView(viewDialog);
                dialog.setCancelable(true);
                Button btn_ok = viewDialog.findViewById(R.id.dialog_ok);
                Button btn_notOk = viewDialog.findViewById(R.id.dialog_notOk);
                btn_notOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                btn_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        synchronized (SocketStation.alarms)
                        {
                            SocketStation.alarms.clear();
                            Toast.makeText(AlarmActivity.this,"alarm information delete success",Toast.LENGTH_LONG).show();
                            alarmList = new ArrayList<Alarm>(SocketStation.alarms);
                            alarmsAdapter.setItemList(alarmList);
                            alarmsAdapter.notifyDataSetChanged();
                            dialog.dismiss();
                        }
                    }
                });
                dialog.show();
            }
        });
        alarms.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                Alarm node = alarmList.get(position);
                Bundle data = new Bundle();
                data.putSerializable("alarm",node);
                Intent intent = new Intent(AlarmActivity.this, node.warnActivity);
                intent.putExtras(data);
                Log.e("warn data","item clicked");
                AlarmActivity.this.startActivity(intent);

                //AlarmActivity.this.finish();
            }
        });
    }

    @Override
    protected void onResume() {
        if(SocketStation.connfdOther.isClosed())
        {
            finish();
        }
        synchronized (SocketStation.alarms) {
            alarmList = new ArrayList<Alarm>(SocketStation.alarms);
        }
        alarmsAdapter.setItemList(alarmList);
        alarmsAdapter.notifyDataSetChanged();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}