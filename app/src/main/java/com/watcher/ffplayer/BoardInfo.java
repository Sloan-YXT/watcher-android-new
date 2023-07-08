package com.watcher.ffplayer;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.watcher.ffplayer.R;
import com.watcher.ffplayer.entity.Alarm;
import com.watcher.ffplayer.entity.Board;
import com.watcher.ffplayer.entity.BoardData;
import com.watcher.ffplayer.entity.Constant;
import com.watcher.ffplayer.entity.DataAlarm;
import com.watcher.ffplayer.entity.GraphAlarm;
import com.watcher.ffplayer.entity.SocketStation;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class BoardInfo extends Activity {
    private TextView action,temp,humi,light,smoke,name,location;
    private Button alarm, video, monthData;
    private int needToShut =1;
    private Board board;
    private SharedPreferences nodeDataNum;
    private SharedPreferences.Editor editor;
    private File fileRoot;
    private ImageView alarm_sign;
    private ImageView backGroud;
    private TextView noGood;
    private Thread pdata,pwarn;
    private boolean prePareToGo = false;
    private boolean closeAct = false;
    //Button btn = findViewById(R.id.gr)
    private Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 200:
                    try {
                        SocketStation.connfdOther.close();
                        SocketStation.connfdWarn.close();
                        SocketStation.connfdData.close();
                        Toast.makeText(getApplicationContext(), "Node connection break down", Toast.LENGTH_SHORT).show();
//                        Intent intent = new Intent(BoardInfo.this, MainActivity.class);
//                        startActivity(intent);
                        //very important!!

                        needToShut = 0;
                        //一串上下文不是一个简单的进程，否则这句不能做到并发执行，应该是类似线程和管程的东西
                        finish();
                    } catch (Exception e) {
                        Log.e("debug", Log.getStackTraceString(e));
                        SocketStation.exit();
                    }
                    break;
                case 250:
                    try {

                        String data = msg.obj.toString();
                        JSONObject jdata = new JSONObject(data);
                        String temp = jdata.getString("temp");
                        String humi = jdata.getString("humi");
                        String light = jdata.getString("light");
                        String smoke = jdata.getString("smoke");
                        String location = jdata.getString("position");
                        String action = jdata.getString("action");
                        BoardInfo.this.action.setText("Action:"+action);
                        BoardInfo.this.temp.setText("Temperature:" + temp + "℃");
                        BoardInfo.this.humi.setText("Humidity:" + humi + "%RH");
                        Log.e("BoardInfo","light=="+light);
                        if(light.equals(Constant.NA))
                        {
                            Log.e("BoardInfo","light==NA");
                            BoardInfo.this.light.setText("No light sensor available");
                        }
                        else if(light.equals(Constant.wrongLight)==false) {
                            Log.e("BoardInfo:!!!!!!",Constant.wrongLight);
                            BoardInfo.this.light.setText("Light:normal");
                        } else {

                            BoardInfo.this.light.setText("Light:abnormal");
                        }
                        if(smoke.equals(Constant.NA))
                        {
                            BoardInfo.this.smoke.setText("No smoke sensor available");
                        }
                        else if (smoke.equals(Constant.wrongSmoke)==false) {
                            Log.e("BoardInfo:!!!!!!!!!",Constant.wrongSmoke);
                            BoardInfo.this.smoke.setText("Smoke:normal");
                        } else {
                            BoardInfo.this.smoke.setText("Smoke:abnormal");
                        }
                        BoardInfo.this.location.setText("Location:" + location);
                    }
                    catch (Exception e)
                    {
                        Log.e("debug",Log.getStackTraceString(e));
                        SocketStation.exit();
                    }
                    break;
                case 300:
                        Log.e("BoardInfo","Alarm message:"+msg.obj.toString());
                        if(msg.obj.toString().equals("0"))
                        {
                            noGood.setText("No event detected");
                            alarm_sign.setVisibility(View.INVISIBLE);
                            backGroud.setImageResource(R.drawable.ok);
                        }
                        else{
                            Log.e("warn data",msg.obj.toString());
                            noGood.setText("Sensitive events detected！");
                            alarm_sign.setVisibility(View.VISIBLE);
                            backGroud.setImageResource(R.drawable.not_ok);
                        }
                        break;
                case 500:
                    Toast.makeText(BoardInfo.this,"Connection with server has been shutdown",Toast.LENGTH_SHORT).show();
                    Log.e("BoardInfo","Server has been shutdown");
                    SocketStation.exit();
                    break;
                default:
                    Toast.makeText(getApplicationContext(), "数据请求失败！", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        board = (Board) intent.getSerializableExtra("node");
        setContentView(R.layout.board_watch);
        action = findViewById(R.id.action_data);
        temp = findViewById(R.id.temp_data);
        humi = findViewById(R.id.humi_data);
        light = findViewById(R.id.light_data);
        smoke = findViewById(R.id.smoke_data);
        name = findViewById(R.id.board_name);
        humi.setText("No data yet");
        temp.setText("No data yet");
        light.setText("No data yet");
        smoke.setText("No data yet");
        location = findViewById(R.id.board_location);
        Constant.name = board.boardName;
        Constant.position = board.boardLocation;
        name.setText("Name:" + board.boardName);
        location.setTextColor(Color.RED);
        location.setText("Location:" + board.boardLocation);
        location.setTextColor(Color.BLUE);
        alarm = findViewById(R.id.alarm_btn);
        video = findViewById(R.id.tv_btn);
        noGood = findViewById(R.id.noGoodText);
        backGroud = findViewById(R.id.imageSmile);
        backGroud.setImageResource(R.drawable.ok);
        monthData = findViewById(R.id.mdata_btn);
        Constant.rootDir = Constant.rootBase+"/"+board.boardName+"/";
        fileRoot = new File(Constant.rootDir);
        alarm_sign = findViewById(R.id.alarm_img);
        if(!fileRoot.exists())
        {
            if(!fileRoot.mkdirs())
            {
                Log.e("main","dir create failed");
                SocketStation.exit();
            }
        }
        video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent intent = new Intent(BoardInfo.this, VideoPlayerActivity.class);
                String vaddr = "rtmp://"+Constant.ip+"/livevideo0/"+SocketStation.vCode;
//                intent.putExtra("extra_url",vaddr);
                //startActivity(intent);
                if(Constant.boardType.equals("raspi")) {
                    Intent callIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(vaddr));
                    try {
                        startActivity(callIntent);
                    } catch (Exception e) {
                        //Toast.makeText(getApplicationContext(),"请安装一个实时播放器，推荐使用一起发布的版本!",Toast.LENGTH_LONG);
                        Toast.makeText(getApplicationContext(), "请安装一个实时播放器，推荐使用一起发布的版本!", Toast.LENGTH_LONG).show();
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(), Constant.boardType+" currently doesn't support real time video", Toast.LENGTH_LONG).show();
                }
            }
        });
        monthData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(BoardInfo.this, DataClassifyActivity.class);
                startActivity(intent1);
            }
        });

        Thread p = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                       try {
                            OutputStream outOther = SocketStation.connfdOther.getOutputStream();
                            JSONObject connData = new JSONObject();
                            connData.put("type", "connect");
                            connData.put("board name", board.boardName);
                            connData.put("client name", SocketStation.client_name);
                            int len = connData.toString().getBytes().length;
                            byte[] lenSend = SocketStation.intToByte(len);
                            //exception位置1，说明服务器主动断开或网络异常，退出
                            outOther.write(lenSend);
                            outOther.write(connData.toString().getBytes());
                            InputStream inOther = SocketStation.connfdOther.getInputStream();
                            int [] vcode_dir = new int[4];
                            //不直接往byte传是因为高字节会被java解读为符号，而java没有unsigned
                            for(int i=0;i<4;i++)
                            {
                                vcode_dir[i] = inOther.read();
                            }
                            SocketStation.vCode = SocketStation.byteToInt(vcode_dir);
                            Log.e("BoardInfo",String.valueOf(SocketStation.vCode));
                            String highTemp,highHumi,wrongLight,wrongSmoke;
                            int[] lenRecv = new int[4];
                            for(int i=0;i<4;i++)
                            {
                                lenRecv[i] = inOther.read();
                            }
                            //退出点2，说明再连接时板子已经断开，这时warn是没消息的
                            len = SocketStation.byteToInt(lenRecv);
                            if(len==-1)
                            {
                                Log.e("null connect","len==-1");
                                closeAct = true;
                                handler.sendEmptyMessage(200);
                                Thread.sleep(2000);
                                return;
                            }
                            Log.e("debug","len="+len);
                            byte [] highTempIn = new byte[len];;
                            inOther.read(highTempIn);
                            highTemp = new String(highTempIn,"utf8");
                            for(int i=0;i<4;i++)
                            {
                                lenRecv[i] = inOther.read();
                            }
                            len = SocketStation.byteToInt(lenRecv);
                            byte [] highHumiIn = new byte[len];
                            inOther.read(highHumiIn);
                            highHumi = new String(highHumiIn,"utf8");
                            for(int i=0;i<4;i++)
                            {
                                lenRecv[i] = inOther.read();
                            }
                            len = SocketStation.byteToInt(lenRecv);
                            byte [] wrongLightIn = new byte[len];
                            inOther.read(wrongLightIn);
                            wrongLight = new String(wrongLightIn,"utf8");
                            for(int i=0;i<4;i++)
                            {
                                lenRecv[i] = inOther.read();
                            }
                            len = SocketStation.byteToInt(lenRecv);
                            byte [] wrongSmokeIn = new byte[len];
                            inOther.read(wrongSmokeIn);
                            wrongSmoke = new String(wrongSmokeIn,"utf8");
                            for(int i=0;i<4;i++)
                            {
                                lenRecv[i] = inOther.read();
                            }
                            len = SocketStation.byteToInt(lenRecv);
                            byte [] boardType = new byte[len];
                            inOther.read(boardType);
                            Constant.boardType = new String(boardType,"utf8");
                            Constant.highTemp = Double.parseDouble(highTemp);
                            Constant.highHumi = Double.parseDouble(highHumi);
                            String tmp = wrongLight;
                            len = wrongLight.lastIndexOf('.');
                            Constant.wrongLight = wrongLight.substring(0,len);
                            len = wrongSmoke.lastIndexOf('.');
                            Constant.wrongSmoke = wrongSmoke.substring(0,len);
                        }
                        catch (SocketException e)
                        {
                                Log.e("BoardInfo",Log.getStackTraceString(e));
                                handler.sendEmptyMessage(500);
                                Thread.currentThread().interrupt();
                        }
                        catch (Exception e) {
                            Log.e("BoardInfo",Log.getStackTraceString(e));
                            //SocketStation.exit();
                        }
                    }
                }
        );
        p.start();
        try {
            p.join();
        }
        catch (Exception e)
        {
            Log.e("debug","connect thread corrupt");
            closeAct = true;
            handler.sendEmptyMessage(500);
        }
        if(closeAct == true)
        {
            try {
                SocketStation.connfdOther.close();
                SocketStation.connfdWarn.close();
                SocketStation.connfdData.close();
                Toast.makeText(getApplicationContext(), "节点连接断开！", Toast.LENGTH_SHORT).show();
                needToShut = 0;
                finish();
                //注意finish会在onCreate完毕后才执行，千万别傻傻的在这里堵个循环
                return;
            } catch (Exception e) {
                Log.e("debug", Log.getStackTraceString(e));
                SocketStation.exit();
            }
        }
        pwarn = new Thread(new Runnable() {
            @Override
            public void run() {
                while ((true)) {
                    try {
                        Socket sock = SocketStation.connfdWarn;
                        int [] lenRecv = new int[4];
                        int len;
                        InputStream inWarn = sock.getInputStream();
                        for(int i=0;i<4;i++)
                        {
                            //这个地方还是不会出exception的
                            lenRecv[i] = inWarn.read();
                        }
                        len = SocketStation.byteToInt(lenRecv);
                        if(len==-1)
                        {
                            throw new SocketException();
                        }
                        byte [] warnMessageIn = new byte[len];
                        inWarn.read(warnMessageIn);
                        String warnMessage = new String(warnMessageIn,"utf8");
                        JSONObject warn = new JSONObject(warnMessage);
                        String type = warn.getString("type");
                        Log.e("warn data",warnMessage);
                        Log.e("warn data",type);
                        if(type.equals("data"))
                        {
                            Log.e("warn data","in type data");
                            String time = warn.getString("time");
                            DataAlarm alarm = new DataAlarm();
                            alarm.setType(type);
                            alarm.warnActivity = DataShow.class;
                            alarm.time = time;
                            alarm.temp = warn.getString("temp");
                            alarm.humi = warn.getString("humi");
                            alarm.light = warn.getString("light");
                            alarm.smoke = warn.getString("smoke");
                            Log.e("warn data","before synchronize");
                            synchronized (SocketStation.alarms) {
                                SocketStation.alarms.add(alarm);
                                Log.e("warn data","in synchronize");
                            }
                        }
                        else if(type.equals("face")) {
                            Log.e("warn_data","face detected");
                            String time = warn.getString("time");
                            GraphAlarm alarm = new GraphAlarm();
                            alarm.setType(type);
                            alarm.warnActivity = GraphShowAlarm.class;
                            alarm.time = time;
                            synchronized (SocketStation.alarms) {
                                SocketStation.alarms.add(alarm);
                            }
                        }
                        else if(type.equals("cmd"))
                        {
                            Message msg = handler.obtainMessage();
                            msg.what = 200;
                            handler.sendMessage(msg);
                            return;
                        }
                        Message msg = handler.obtainMessage();
                        msg.what = 300;
                        synchronized (SocketStation.alarms) {
                            msg.obj = String.valueOf(SocketStation.alarms.size());
                            Log.e("warn data",String.valueOf(SocketStation.alarms.size()));
                        }
                        handler.sendMessage(msg);
                        Thread.sleep(500);
                    }
                    catch (InterruptedException e)
                    {
                        return;
                    }
                    catch (SocketException e)
                    {
                        if(prePareToGo)
                        {
                            //这类阻塞打断的exception会清掉interrupt位，交给接口用户处理
                            return;
                            //Log.e("Main","prePare to Go");
                        }
                        handler.sendEmptyMessage(500);
                        Log.e("BoardInfo",Log.getStackTraceString(e));

                    }
                    catch (Exception e)
                    {
                        Log.e("BoardInfo",Log.getStackTraceString(e));
                        SocketStation.exit();
                    }
                }
            }
        });
        pwarn.start();
        pdata = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            while (true) {
                                OutputStream outData = SocketStation.connfdData.getOutputStream();
                                JSONObject j = new JSONObject();
                                j.put("type","pull data");
                                j.put("board name",board.boardName);
                                String pullMessage = j.toString();
                                int len = pullMessage.getBytes().length;
                                byte[] lenSend = SocketStation.intToByte(len);
                                //write发的是野生数据，但|会考虑符号
                                outData.write(lenSend);
                                outData.write(pullMessage.getBytes());
                                InputStream dataIn = SocketStation.connfdData.getInputStream();
                                int[] lenPre = new int[4];



                                //Thread.sleep(1000);
                                Log.e("BoardInfo", "before recv len");
                                for (int i = 0; i < 4; i++) {
                                    lenPre[i] = dataIn.read();
                                }
                                Log.e("BoardInfo", "After recv len:" + lenPre);
                                len = SocketStation.byteToInt(lenPre);
                                if(len==-1)
                                {
                                    throw new SocketException();
                                }
                                Log.e("BoardInfo", "After transform len:" + len);
                                byte[] data = new byte[len];
                                for (int i = 0; i < len; i++) {
                                    data[i] = (byte) dataIn.read();
                                    if (data[i] == -1) {
                                        Log.e("BoardInfo", String.valueOf(SocketStation.connfdWarn.isClosed()));
                                        handler.sendEmptyMessage(500);
                                        while(true);
                                    }
                                }
                                Message msg = handler.obtainMessage();
                                msg.obj = new String(data, "utf8");
                                msg.what = 250;;
                                handler.sendMessage(msg);
                                Thread.sleep(Constant.pullDuration*1000);
                            }
                        }
                        catch (SocketException e)
                        {
                            if(prePareToGo)
                            {
                                Log.e("Main","prePareToGo");
                                return;
                            }
                            //1.只发一条message,由warn一并负责;2.如果不按1做看似可以，可是breset时会关掉connData引发message(500),导致计划外的退出
                            //handler.sendEmptyMessage(500);
                            Log.e("BoardInfo",Log.getStackTraceString(e));

                        }
                        catch (InterruptedException e)
                        {
                            return;
                        }
                        catch (Exception e)
                        {
                            Log.e("BoardInfo",Log.getStackTraceString(e));
                            SocketStation.exit();
                        }
                    }
                }
        );
        pdata.start();
        alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle data = new Bundle();
                Intent intent1 = new Intent(BoardInfo.this,AlarmActivity.class);
                intent1.putExtra("board_name",board.boardName);
                intent1.putExtra("board_position",board.boardLocation);
                startActivity(intent1);
            }
        });
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        try {
            prePareToGo = true;
            if(needToShut==1) {
                SocketStation.connfdOther.close();
                SocketStation.connfdWarn.close();
                SocketStation.connfdData.close();
                needToShut = 0;
                pdata.interrupt();
                pwarn.interrupt();
                Log.e("Main","back to Main");
            }
        }
        catch (Exception e)
        {
            Log.e("BoardInfo",Log.getStackTraceString(e));
        }
        //finish();
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Message msg = handler.obtainMessage();
        msg.what = 300;
        synchronized (SocketStation.alarms) {
            msg.obj = String.valueOf(SocketStation.alarms.size());
            Log.e("Resume BoardData",String.valueOf(SocketStation.alarms.size()));
        }
        handler.sendMessage(msg);
    }

    @Override
    protected void onDestroy() {
        try {
            Log.e("Main","next activity destroyed");
            if(needToShut==1) {
                SocketStation.connfdOther.close();
                SocketStation.connfdWarn.close();
                SocketStation.connfdData.close();
            }
            synchronized (SocketStation.alarms)
            {
                SocketStation.alarms.clear();
            }
        }
        catch (Exception e)
        {
            Log.e("BoardInfo",Log.getStackTraceString(e));
        }
        super.onDestroy();
    }
}