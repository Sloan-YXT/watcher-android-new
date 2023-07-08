package com.watcher.ffplayer;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.watcher.ffplayer.entity.Alarm;
import com.watcher.ffplayer.entity.Constant;
import com.watcher.ffplayer.entity.GraphAlarm;
import com.watcher.ffplayer.entity.SocketStation;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;

public class GraphShow extends Activity {
    ImageView graph ;
    Button btn_bck,btn_del ;
    Bitmap gsource;
    Handler handler = new Handler(Looper.myLooper())
    {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 200:
                    graph.setImageBitmap(gsource);
                    Log.e("graph","bitmap found");
                    break;
                case 300:
                    Toast.makeText(getApplicationContext(),"服务器主动断开连接！请重新登陆",Toast.LENGTH_LONG).show();
                    SocketStation.exit();
                    break;
                case 350:
                    String tip = msg.obj.toString();
                    Toast.makeText(GraphShow.this, tip, Toast.LENGTH_LONG).show();
                    finish();
                case 400:
                    Toast.makeText(getApplicationContext(),"图片删除成功",Toast.LENGTH_LONG).show();
                    finish();
                    break;
                default:
                    Log.e("graph","shouldn't be here");
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_show);
        graph = (ImageView)findViewById(R.id.graph_data);
        btn_bck = (Button) findViewById(R.id.graph_back);
        btn_del = (Button) findViewById(R.id.graph_delete);
        final Intent intent = getIntent();
        String time = intent.getStringExtra("path");
        try {
            OutputStream outOther = SocketStation.connfdOther.getOutputStream();
            InputStream inOther = SocketStation.connfdOther.getInputStream();
            JSONObject j = new JSONObject();
            j.put("type","face");
            j.put("time",time);
            String path = Constant.rootDir+"/"+ time;
            //把空格换成_
            //path = path.replace(" ","_");
            File gCache = new File(path);
            if(gCache.exists())
            {
                Log.e("graph","graph already exists");
                gsource = BitmapFactory.decodeFile(path);
                handler.sendEmptyMessage(200);
            }
            else {
                gCache.createNewFile();
                //思考：线程很特殊，在外部函数返回后它生命周期得以延续，那么当然调用thread初始化函数的栈也需要延长生命周期
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String msg = j.toString();
                            int len = msg.length();
                            byte[] lenToSend = SocketStation.intToByte(len);
                            outOther.write(lenToSend);
                            outOther.write(msg.getBytes("utf8"));
                            OutputStream out = new FileOutputStream(gCache);
                            int[] readLen = new int[4];
                            for (int i = 0; i < 4; i++) {
                                readLen[i] = inOther.read();
                            }
                            len = SocketStation.byteToInt(readLen);
                            Log.e("graph", "len=" + String.valueOf(len));
                            if (len < Constant.wrongGLen) {
                                byte[] gBox = new byte[len];
                                inOther.read(gBox);
                                Log.e("graph", "len_too_short=" + String.valueOf(len));
                                Message msgS = handler.obtainMessage();
                                msgS.what = 350;
                                msgS.obj = new String(gBox,"utf8");
                                handler.sendMessage(msgS);
                            }
                            else {
                                int gBox;
                                //注意read不会阻塞到读完len(gbox)
                                for (int i = 0; i < len; i++) {
                                    gBox = inOther.read();
                                    out.write(gBox);
                                }
                                out.close();
                                Log.e("graph","path="+gCache.getAbsolutePath());
                                gsource = BitmapFactory.decodeFile(path);
                                handler.sendEmptyMessage(200);
                            }
                        }
                        catch (SocketException e)
                        {
                            Log.e("graph",Log.getStackTraceString(e));
                            SocketStation.exit();
                        }
                        catch (Exception e)
                        {
                            Log.e("graph",Log.getStackTraceString(e));
                            SocketStation.exit();
                        }
                    }
                }).start();
            }
        }
        catch (Exception e)
        {
            Log.e("graph",Log.getStackTraceString(e));
            SocketStation.exit();
        }
        btn_bck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btn_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View viewDialog = getLayoutInflater().inflate(R.layout.mydialog,
                        null, false);
                final Dialog dialog = new Dialog(GraphShow.this,
                        R.style.myDialogTheme);
                dialog.setContentView(viewDialog);
                dialog.setCancelable(true);
                Button btn_ok = viewDialog.findViewById(R.id.dialog_ok);
                Button btn_notOk = viewDialog.findViewById(R.id.dialog_notOk);
                btn_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Thread p  = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    JSONObject j = new JSONObject();
                                    j.put("type","delete face");
                                    j.put("time",time);
                                    String msg = j.toString();
                                    int len = msg.length();
                                    byte [] sendLen;
                                    sendLen = SocketStation.intToByte(len);

                                    OutputStream outOther = SocketStation.connfdOther.getOutputStream();
                                    outOther.write(sendLen);
                                    outOther.write(msg.getBytes("utf8"));
                                    dialog.dismiss();
                                    handler.sendEmptyMessage(400);
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
                                    Log.e("graphshow",Log.getStackTraceString(e));
                                    SocketStation.exit();
                                }
                            }
                        });
                        p.start();
                        finish();
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
    }
    //Bitmap bitmap = BitmapFactory.decodeFile(path,options);

    protected void onDestroy() {
        super.onDestroy();
    }
}