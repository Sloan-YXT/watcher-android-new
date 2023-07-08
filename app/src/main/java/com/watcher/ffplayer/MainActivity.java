package com.watcher.ffplayer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.watcher.ffplayer.R;
import com.watcher.ffplayer.adapter.BoardsAdapter;
import com.watcher.ffplayer.entity.Alarm;
import com.watcher.ffplayer.entity.Board;
import com.watcher.ffplayer.entity.Constant;
import com.watcher.ffplayer.entity.SocketStation;
//synchronized、虚拟机和垃圾回收可以看到java在内存分配、野指针调用和死锁上做出的努力,
//服务器已经发现它们是多么棘手的问题，程序员都是人不可能不犯错
public class MainActivity extends Activity {
	private ListView lv;
	private List<Board> listData,listDataTmp;
	private BoardsAdapter adapter;
	private int hasConnected = 0,fromNext = 0;
	private boolean connect_sync=false;
	LinearLayout footer;
	TextView footerNotify;
	private Handler handler =  new Handler(Looper.myLooper())
	{
		@Override
		public void handleMessage(@NonNull Message msg) {
			switch (msg.what)
			{
				case 200:
					//修改List后再notify,否则实验发现notify 2次size为0，不正确--1.10
					//当前版本的安卓api修改一次链表notify一次，notify后不能再修改链表

					Log.e("Main","message recved");
					MainActivity.this.footerNotify.setVisibility(View.INVISIBLE);
					synchronized (this) {
						//和UI clicker的数据同步
						listData = new ArrayList<Board>(listDataTmp);
						adapter.setItemList(listData);
						adapter.notifyDataSetChanged();
					}
					Log.e("Main","aft message recved");
					if(listData.size()==0)
					{
						Toast.makeText(MainActivity.this, "No onlie node currently", Toast.LENGTH_SHORT).show();
					}
					break;
				case 201:
					Toast.makeText(getApplicationContext(),"already has a "+SocketStation.client_name+"on server "+Constant.ip+",please contact system manager"
					,Toast.LENGTH_LONG).show();
					try {
						Thread.sleep(1000);
					}
					catch (Exception e)
					{
						Log.e("201 sleep duration",Log.getStackTraceString(e));
						System.exit(1);
					}
					finish();
				case 300:
					Toast.makeText(getApplicationContext(),"server has been shut down,please relogin!",Toast.LENGTH_LONG).show();
					finish();
					break;
				default:
					Toast.makeText(MainActivity.this,"数据请求失败！",Toast.LENGTH_SHORT).show();
					break;
			}
		}
	};
	void connect()
	{
		try {
			synchronized (MainActivity.this) {
				connect_sync = false;
			}
			listDataTmp = new ArrayList<Board>();
			SocketStation.connfdOther = new Socket();
			SocketStation.connfdOther.connect(new InetSocketAddress(Constant.ip, Constant.portOther), 1400);
			int len = SocketStation.client_name.length();
			OutputStream outOther = SocketStation.connfdOther.getOutputStream();
			outOther.write(SocketStation.intToByte(len));
			outOther.write(SocketStation.client_name.getBytes());
			InputStream in_boards = SocketStation.connfdOther.getInputStream();
			int[] status_box = new int[4];
			for (int i = 0; i < 4; i++) {
				status_box[i] = in_boards.read();
			}
			int status = SocketStation.byteToInt(status_box);
			if(status!=SocketStation.LOGIN_SUCCESS)
			{
				handler.sendEmptyMessage(201);
				while(true);
			}

			SocketStation.connfdWarn = new Socket();
			SocketStation.connfdData = new Socket();
			Log.e("Socket", "85");

			len = SocketStation.client_name.length();
			int[] len_pre = new int[4];
			for (int i = 0; i < 4; i++) {
				len_pre[i] = in_boards.read();
			}
			len = SocketStation.byteToInt(len_pre);
			Log.e("Socket", "len:" + len);
			byte[] boardData = new byte[len];
			for (int i = 0; i < len; i++) {
				boardData[i] = (byte) in_boards.read();
			}
			String data = new String(boardData, "utf8");
			Log.e("MainActivity", "recv:" + data);
			JSONObject data_handler = new JSONObject(data);
			int numer = data_handler.getInt("num");
			JSONArray nodes;
			if (!String.valueOf(data_handler.get("nodes")).equals("null")) {
				Log.e("MainActivity", String.valueOf(data_handler.get("nodes")));
				nodes = data_handler.getJSONArray("nodes");
			} else {
				nodes = new JSONArray();
			}
			for (int i = 0; i < numer; i++) {
				Board node = new Board();
				JSONObject j = nodes.getJSONObject(i);
				node.boardName = j.getString("name");
				node.boardLocation = j.getString("position");
				listDataTmp.add(node);
			}
			Log.e("Socket", data);
			handler.sendEmptyMessage(200);
			SocketStation.connfdWarn.connect(new InetSocketAddress(Constant.ip, Constant.portWarn), 1400);
			OutputStream outWarn = SocketStation.connfdWarn.getOutputStream();
			len = SocketStation.client_name.length();
			outWarn.write(SocketStation.intToByte(len));
			outWarn.write(SocketStation.client_name.getBytes());
			InputStream inWarn = SocketStation.connfdWarn.getInputStream();

			int[] in_box = new int[4];
			for (int i = 0; i < 4; i++) {
				in_box[i] = inWarn.read();
			}
			len = SocketStation.byteToInt(in_box);
			byte[] warn_rep = new byte[len];
			for (int i = 0; i < len; i++) {
				warn_rep[i] = (byte) inWarn.read();
			}
			String warn_reply = new String(warn_rep, "utf8");
			Log.e("warn connect reply", warn_reply);
			SocketStation.connfdData.connect(new InetSocketAddress(Constant.ip, Constant.portData), 1400);
			OutputStream outData = SocketStation.connfdData.getOutputStream();
			len = SocketStation.client_name.length();
			outData.write(SocketStation.intToByte(len));
			outData.write(SocketStation.client_name.getBytes());
			InputStream inData = SocketStation.connfdData.getInputStream();
			in_box = new int[4];
			for (int i = 0; i < 4; i++) {
				in_box[i] = inData.read();
			}
			len = SocketStation.byteToInt(in_box);
			byte[] data_rep = new byte[len];
			for (int i = 0; i < len; i++) {
				data_rep[i] = (byte) inData.read();
			}
			String data_reply = new String(data_rep, "utf8");
			Log.e("data connect reply", data_reply);
			synchronized (MainActivity.this) {
				connect_sync = true;
			}
		}
		catch (SocketException e)
		{
			Log.e("Socket", Log.getStackTraceString(e));
			handler.sendEmptyMessage(300);
		}
		catch(Exception e)
		{
			Log.e("Socket", Log.getStackTraceString(e));
			SocketStation.exit();
		}
	}
	void reconnect()
	{
		try {
			boolean connect_status;
			synchronized (MainActivity.this) {
				connect_status = connect_sync;
			}
			if(connect_status==false)
				return;
			SocketStation.connfdOther.close();
			SocketStation.connfdData.close();
			SocketStation.connfdWarn.close();
			connect();
		}
		catch (SocketException e)
		{
			Log.e("Socket", Log.getStackTraceString(e));
			handler.sendEmptyMessage(300);
		}
		catch(Exception e)
		{
			Log.e("Socket", Log.getStackTraceString(e));
			SocketStation.exit();
		}
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {

		Log.e("Main","create");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.boards);
		PackageManager pm = getPackageManager();
		boolean permission;
		Constant.rootBase = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath()+"/";
		Log.e("main",Constant.rootBase);
		File file = new File(Constant.rootBase);
		if(!file.mkdirs())
		{
			Log.e("MainActivity","mk root dir failed");
		}
		adapter = new BoardsAdapter(this);
		lv = (ListView) findViewById(R.id.lv);

		lv.setAdapter(adapter);

		footer = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.main_foot, null);
		footerNotify = (TextView) footer.findViewById(R.id.main_footer_text);
		footer = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.graph_foot, null);
		footerNotify = (TextView) footer.findViewById(R.id.graph_footer_text);
		//footer.setVisibility(View.INVISIBLE);
		lv.addFooterView(footer);
		lv.setFooterDividersEnabled(true);
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> parent, View view,
		int position, long id) {
				synchronized (MainActivity.this) {
					Log.e("Main", "item clicked");
					if(connect_sync==false)
					{
						return;
					}
					if(position == listData.size())
					{
						//尼玛页脚也算
						return;
					}
					Board node = listData.get(position);
					Bundle data = new Bundle();
					data.putSerializable("node", node);

					Intent intent = new Intent(MainActivity.this, BoardInfo.class);
					intent.putExtras(data);
					Log.e("Main", "before start");
					MainActivity.this.startActivity(intent);
					hasConnected = 1;
					//MainActivity.this.finish();
				}
		}
		});
		lv.setOnScrollListener(new AbsListView.OnScrollListener() {
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
				{
						Log.e("MainActivity","ListView被刷新！");
						new Thread(new Runnable() {
							@Override
							public void run(){
								reconnect();
							}
						} ).start();
					MainActivity.this.footer.setVisibility(View.VISIBLE);
					MainActivity.this.footerNotify.setVisibility(View.VISIBLE);
				}
				else if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE)
				{
					MainActivity.this.footer.setVisibility(View.INVISIBLE);
				}

			}

			@Override
			public void onScroll(AbsListView absListView, int i, int i1, int i2) {

			}
		});
		Thread pCon = new Thread(new Runnable() {
			@Override
			public void run() {
				connect();
			}
		} );
		//不能复用
		pCon.start();
	}

	@Override
	protected void onResume() {
			Log.e("Main","From Next");
			if (hasConnected == 1) {
				hasConnected = 0;
				new Thread(new Runnable() {
					@Override
					public void run() {
						reconnect();
					}
				}).start();
			}
		    super.onResume();
	}

	@Override
	protected void onDestroy() {
//		try {
//			if(hasConnected==0) {
//				//这里不是退出逻辑，是先退出然后再进入下个act，下个act返回再的新建Main的逻辑
//				SocketStation.connfdOther.close();
//				SocketStation.connfdData.close();
//				SocketStation.connfdWarn.close();
//				Log.e("MainActivity","socket closed");
//			}
//			else
//			{
//				Log.e("MainActivity","socket not closed");
//			}
//		}
//		catch (Exception e)
//		{
//			Log.e("BoardInfo",Log.getStackTraceString(e));
//		}
		super.onDestroy();
	}
}
