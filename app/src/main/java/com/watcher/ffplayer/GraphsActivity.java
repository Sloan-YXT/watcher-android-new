package com.watcher.ffplayer;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.watcher.ffplayer.adapter.GraphsAdapter;
import com.watcher.ffplayer.entity.Graph;
import com.watcher.ffplayer.entity.SocketStation;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class GraphsActivity extends Activity {
    private ListView graphs;
    private List<Graph> graphList = new ArrayList<Graph>(),graphListTmp;
    private GraphsAdapter adapter;
    private Button back,del;
    private LinearLayout footer;
    private String cacheDel;
    private Handler handler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what)
            {
                case 200:
                    synchronized (graphList) {
                        graphList = new ArrayList<Graph>(graphListTmp);
                    }
                    adapter.setItemList(graphList);
                    adapter.notifyDataSetChanged();
                    break;
                case 300:
                    Toast.makeText(getApplicationContext(),"服务器主动断开连接！请重新登陆",Toast.LENGTH_LONG).show();
                    SocketStation.exit();
                    break;
                case 400:
                    Toast.makeText(GraphsActivity.this,"delete cloud face data success！",Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                default:
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graphs);
        graphs = (ListView) findViewById(R.id.graphs_list_view) ;
        footer = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.alarm_foot, null);
        back = (Button)footer.findViewById(R.id.alarms_back);
        del = (Button)footer.findViewById(R.id.alarms_del);
        graphs.addFooterView(footer);
        graphs.setFooterDividersEnabled(true);
        adapter = new GraphsAdapter(this);
        graphs.setAdapter(adapter);
        graphs.addFooterView(footer);
        graphs.setFooterDividersEnabled(true);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View viewDialog = getLayoutInflater().inflate(R.layout.mydialog,
                        null, false);
                final Dialog dialog = new Dialog(GraphsActivity.this,
                        R.style.myDialogTheme);
                dialog.setContentView(viewDialog);
                dialog.setCancelable(true);
                Button btn_ok = viewDialog.findViewById(R.id.dialog_ok);
                Button btn_notOk = viewDialog.findViewById(R.id.dialog_notOk);
                btn_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    JSONObject j = new JSONObject();
                                    j.put("type", "delete faces");
                                    OutputStream outOther = SocketStation.connfdOther.getOutputStream();
                                    String msg = j.toString();
                                    int len = msg.length();
                                    byte []lenSend = SocketStation.intToByte(len);
                                    outOther.write(lenSend);
                                    outOther.write(msg.getBytes("utf8"));

                                }
                                catch (SocketException e)
                                {
                                    handler.sendEmptyMessage(300);
                                }
                                catch (IOException e)
                                {
                                    handler.sendEmptyMessage(300);
                                }
                                catch (Exception e)
                                {
                                    Log.e("Graphs Activity",Log.getStackTraceString(e));
                                    SocketStation.exit();
                                }

                            }
                        }).start();
                        graphListTmp = new ArrayList<Graph>();
                        handler.sendEmptyMessage(200);
                        dialog.dismiss();
                        handler.sendEmptyMessage(400);
                    }
                });
                btn_notOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });
        graphs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Graph graph = null;
                synchronized (graphList) {
                    graph = graphList.get(position);
                }
                cacheDel = graph.time;
                Intent intent = new Intent(GraphsActivity.this,GraphShow.class);
                intent.putExtra("path",graph.time);
                startActivity(intent);
            }
        });
        Thread p = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject j = new JSONObject();
                    j.put("type", "all faces");
                    String message = j.toString();
                    int len = message.length();
                    byte[] sendLen = SocketStation.intToByte(len);
                    OutputStream outOther = SocketStation.connfdOther.getOutputStream();
                    InputStream inOther = SocketStation.connfdOther.getInputStream();
                    outOther.write(sendLen);
                    outOther.write(message.getBytes("utf8"));
                    int[] recvLen = new int[4];
                    for(int i=0;i<4;i++)
                    {
                        recvLen[i] = inOther.read();
                    }
                    len = SocketStation.byteToInt(recvLen);
                    Log.e("GraphsActivity","len="+len);
                    byte[] messageBox = new byte[len];
                    for(int i=0;i<len;i++)
                    {
                        messageBox[i] = (byte)inOther.read();
                    }
                    Log.e("GraphsActivity",new String(messageBox,"utf8"));
                    JSONObject k = new JSONObject(new String(messageBox,"utf8"));
                    int num = k.getInt("num");
                    JSONArray arr = new JSONArray();
                    if(num != 0)
                    {
                        arr = k.getJSONArray("data");
                    }
                    graphListTmp = new ArrayList<Graph>();
                    for(int i= 0;i<num;i++)
                    {
                        String time = arr.getString(i);
                        Graph node = new Graph();
                        node.time = time;
                        graphListTmp.add(node);
                    }
                    handler.sendEmptyMessage(200);
                }
                catch (SocketException e)
                {
                    handler.sendEmptyMessage(300);
                }
                catch (IOException e)
                {
                    handler.sendEmptyMessage(300);
                }
                catch (Exception e)
                {
                    Log.e("Graphs Activity", Log.getStackTraceString(e));
                    SocketStation.exit();
                }
            }
        });
        p.start();
        try {
            p.join();
        }
        catch (Exception e)
        {
            Log.e("Graphs Activity",Log.getStackTraceString(e));
            SocketStation.exit();
        }
    }

    @Override
    protected void onResume() {
        if(SocketStation.connfdOther.isClosed())
        {
            finish();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject j = new JSONObject();
                    j.put("type", "all faces");
                    String message = j.toString();
                    int len = message.length();
                    byte[] sendLen = SocketStation.intToByte(len);
                    OutputStream outOther = SocketStation.connfdOther.getOutputStream();
                    InputStream inOther = SocketStation.connfdOther.getInputStream();
                    outOther.write(sendLen);
                    outOther.write(message.getBytes("utf8"));
                    int[] recvLen = new int[4];
                    for(int i=0;i<4;i++)
                    {
                        recvLen[i] = inOther.read();
                    }
                    len = SocketStation.byteToInt(recvLen);
                    Log.e("GraphsActivity","len="+len);
                    byte[] messageBox = new byte[len];
                    for(int i=0;i<len;i++)
                    {
                        messageBox[i] = (byte)inOther.read();
                    }
                    Log.e("GraphsActivity",new String(messageBox,"utf8"));
                    JSONObject k = new JSONObject(new String(messageBox,"utf8"));
                    int num = k.getInt("num");
                    JSONArray arr = new JSONArray();
                    if(num != 0)
                    {
                        arr = k.getJSONArray("data");
                    }
                    graphListTmp = new ArrayList<Graph>();
                    for(int i= 0;i<num;i++)
                    {
                        String time = arr.getString(i);
                        Graph node = new Graph();
                        node.time = time;
                        if(time.equals(cacheDel)==false)
                            graphListTmp.add(node);
                    }
                    handler.sendEmptyMessage(200);
                }
                catch (SocketException e)
                {
                    handler.sendEmptyMessage(300);
                }
                catch (IOException e)
                {
                    handler.sendEmptyMessage(300);
                }
                catch (Exception e)
                {
                    Log.e("Graphs Activity", Log.getStackTraceString(e));
                    SocketStation.exit();
                }
            }
        }).start();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}