package com.watcher.ffplayer;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.watcher.ffplayer.R;
import com.watcher.ffplayer.adapter.BoardDataAdapter;
import com.watcher.ffplayer.entity.Board;
import com.watcher.ffplayer.entity.BoardData;
import com.watcher.ffplayer.entity.SocketStation;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class BoardDataActivity extends Activity {
    private List<BoardData> boardData = new ArrayList<BoardData>();
    private BoardDataAdapter adapter;
    private LinearLayout footer;
    private ListView boardDataListView;
    private Button btn_back;

    private Button btn_del;
    String boardName;
    private Handler handler =  new Handler(Looper.myLooper())
    {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what)
            {
                case 200:
                    //修改List后再notify,否则实验发现notify 2次size为0，不正确--1.10
                    //当前版本的安卓api修改一次链表notify一次，notify后不能再修改链表
                    if(msg.obj.toString().equals("0"))
                    {
                        footer.setVisibility(View.GONE);
                    }
                    else
                    {
                        footer.setVisibility(View.VISIBLE);
                    }
                    adapter.notifyDataSetChanged();
                    if(boardData.size()==0)
                    {
                        Toast.makeText(BoardDataActivity.this, "No data for this month for the time being", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 400:
                    Toast.makeText(BoardDataActivity.this,"delete cloud sensor data success！",Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case 500:
                    Toast.makeText(BoardDataActivity.this,"server shutdown!",Toast.LENGTH_SHORT).show();
                    try {
                        Thread.sleep(3000);
                    }
                    catch (Exception e)
                    {
                        Log.e("BoardDataActivity",Log.getStackTraceString(e));
                    }
                    SocketStation.exit();
                    break;
                default:
                    Toast.makeText(BoardDataActivity.this,"数据请求失败！",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_data);
        Intent intent = getIntent();
        boardName = intent.getStringExtra("board name");
        boardDataListView = (ListView) findViewById(R.id.board_data_list_view);
        adapter = new BoardDataAdapter(this);
        boardDataListView.setAdapter(adapter);
        adapter.setItemList(boardData);
        footer = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.board_data_foot, null);
        boardDataListView.addFooterView(footer);
        boardDataListView.setFooterDividersEnabled(false);
        btn_back = footer.findViewById(R.id.board_data_back);
        btn_del = footer.findViewById(R.id.board_data_del);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        btn_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final View viewDialog = getLayoutInflater().inflate(R.layout.mydialog,
                        null, false);
                final Dialog dialog = new Dialog(BoardDataActivity.this,
                        R.style.myDialogTheme);
                dialog.setContentView(viewDialog);
                dialog.setCancelable(true);
                Button btn_ok = viewDialog.findViewById(R.id.dialog_ok);
                Button btn_notOk = viewDialog.findViewById(R.id.dialog_notOk);

                btn_notOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.e("BoardDataActivity","not ok button set");
                        dialog.dismiss();
                    }
                });
                btn_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        JSONObject data = new JSONObject();
                                        Log.e("BoardDataActivity","debug:prepare to send");
                                        data.put("type", "delete month data");
                                        data.put("board name",boardName);
                                        int len = data.toString().length();
                                        byte[] len_aft = SocketStation.intToByte(len);
                                        OutputStream outOther = SocketStation.connfdOther.getOutputStream();
                                        InputStream inOther = SocketStation.connfdOther.getInputStream();
                                        Log.e("BoardDataActivity",data.toString());
                                        for (int i = 0; i < 4; i++) {
                                            outOther.write(len_aft[i]);
                                        }
                                        byte[] sendData = data.toString().getBytes();
                                        for (int i = 0; i < len; i++) {
                                            outOther.write(sendData[i]);
                                        }

                                        handler.sendEmptyMessage(400);
                                    }
                                    catch (Exception e)
                                    {
                                        Log.e("BoardDataActivity",Log.getStackTraceString(e));
                                    }
                                }
                            }).start();
                            dialog.dismiss();
                        }
                        catch (Exception e)
                        {
                            Log.e("BoardDataActivity",Log.getStackTraceString(e));
                        }

                    }
                });
                dialog.show();
            }
        });


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject mondData = new JSONObject();
                    mondData.put("type", "month data");
                    mondData.put("board name",boardName);
                    int len = mondData.toString().length();
                    OutputStream outOther = SocketStation.connfdOther.getOutputStream();
                    InputStream inOther = SocketStation.connfdOther.getInputStream();
                    byte[] len_aft = SocketStation.intToByte(len);
                    for(int i=0;i<4;i++)
                    {
                        outOther.write(len_aft[i]);
                    }
                    byte[] sendData = mondData.toString().getBytes();
                    for(int i=0;i<sendData.length;i++)
                    {
                        outOther.write(sendData[i]);
                    }
                    int [] len_pre = new int[4];
                    for(int i=0;i<4;i++)
                    {
                        len_pre[i] = inOther.read();
                        if(len_pre[i]==-1)
                        {
                            handler.sendEmptyMessage(500);
                            return;
                        }
                    }
                    len = SocketStation.byteToInt(len_pre);
                    byte[] data = new byte[len];
                    for(int i=0;i<len;i++)
                    {
                        data[i] = (byte)inOther.read();
                        if(data[i]==-1)
                        {
                            handler.sendEmptyMessage(500);
                            return;
                        }
                    }
                    String recvData = new String(data,"utf8");
                    JSONObject jRecvData = new JSONObject(recvData);
                    int num = jRecvData.getInt("num");
                    JSONArray nodes = jRecvData.getJSONArray("data");
                    Log.e("BoardDataActivity",recvData);
                    for(int i=0;i<num;i++)
                    {
                        JSONObject j = nodes.getJSONObject(i);
                        BoardData node = new BoardData();
                        node.id = j.getString("id");
                        node.year = j.getString("year");
                        node.weekDay = j.getString("weekday");
                        node.time = j.getString("time");
                        node.date = j.getString("date");
                        node.month = j.getString("month");
                        node.boardName = j.getString("name");
                        node.boardLocation = j.getString("location");
                        node.temp = j.getString("temp");
                        node.humi = j.getString("humi");
                        node.light = j.getString("light");
                        node.smoke = j.getString("smoke");
                        node.action = j.getString("action");
                        boardData.add(node);
                    }
                    Message msg = handler.obtainMessage();
                    msg.what = 200;
                    msg.obj = num;
                    handler.sendMessage(msg);
                }
                catch (SocketException e)
                {
                    Log.e("BoardDataActivity",Log.getStackTraceString(e));
                    handler.sendEmptyMessage(500);
                }
                catch (Exception e)
                {
                    Log.e("BoardDataActivity",Log.getStackTraceString(e));
                }

            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}