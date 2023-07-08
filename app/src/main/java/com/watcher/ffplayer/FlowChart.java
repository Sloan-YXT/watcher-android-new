package com.watcher.ffplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.watcher.ffplayer.entity.BoardData;
import com.watcher.ffplayer.entity.Constant;
import com.watcher.ffplayer.entity.SocketStation;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlowChart extends Activity {
    private LineChart temp_chart,humi_chart;
    private BarChart action_chart_bar;
    private PieChart action_chart_pie;
    private List<BoardData> boardData = new ArrayList<BoardData>();
    String boardName;
    private Handler handler =  new Handler(Looper.myLooper())
    {
        @Override
        public void handleMessage(@NonNull Message msg) {
            Log.e("FlowChart","in looper");
            Log.e("FlowChart", "what="+String.valueOf(msg.what));
            switch (msg.what)
            {
                case 200:
                    Log.e("FlowChart", "what="+String.valueOf(msg.what));
                    temp_chart.setNoDataTextColor(Color.parseColor("#00bcef"));
                    List entries_temp = new ArrayList<>();
                    List entries_humi = new ArrayList<>();
                    String month = Constant.month_dict[Integer.parseInt(boardData.get(0).month)-1];
                    HashMap<String,Float> action_info = new HashMap<String,Float>();
                    action_info.put("stationary",0f);
                    action_info.put("walking",0f);
                    action_info.put("running",0f);
                    for(int i=0;i<boardData.size();i++)
                    {
                        String action = boardData.get(i).action;
                        if(action.equals("idle")==false&&action.equals("NA")==false)
                        {
                            Log.e("FlowChart",action);
                            action_info.put(action,action_info.get(action)+1);
                        }
                        entries_temp.add(new Entry(i,Float.parseFloat(boardData.get(i).temp)));
                        entries_humi.add(new Entry(i,Float.parseFloat(boardData.get(i).humi)));
                    }
                    temp_chart.getDescription().setEnabled(false);
                    humi_chart.getDescription().setEnabled(false);
                    temp_chart.getLegend().setForm(Legend.LegendForm.LINE);
                    humi_chart.getLegend().setForm(Legend.LegendForm.LINE);
                    //xAxis.setDrawGridLines(false);
                    XAxis xAxis = temp_chart.getXAxis();
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxis = humi_chart.getXAxis();
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    YAxis rightYAxis = temp_chart.getAxisRight();
                    rightYAxis.setEnabled(false);
                    humi_chart.getAxisRight().setEnabled(false);
                    LineDataSet lineDataSet = new LineDataSet(entries_temp,"temperature in "+month);
                    LineData lineData = new LineData(lineDataSet);
                    temp_chart.setData(lineData);
                    lineDataSet = new LineDataSet(entries_humi,"humidity in "+month);
                    lineData = new LineData(lineDataSet);
                    humi_chart.setData(lineData);
                    temp_chart.invalidate();
                    humi_chart.invalidate();
                    List<BarEntry> action_bar_data = new ArrayList<>();
                    action_bar_data.add(new BarEntry(0,action_info.get("stationary")));
                    action_bar_data.add(new BarEntry(1,action_info.get("walking")));
                    action_bar_data.add(new BarEntry(2,action_info.get("running")));
                    xAxis  = action_chart_bar.getXAxis();
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"stationary","walking","running"}));
                    action_chart_bar.getDescription().setEnabled(false);
                    BarDataSet barDataSet=new BarDataSet(action_bar_data,"action chart in month "+month);
                    BarData actionBarData=new BarData(barDataSet);
                    action_chart_bar.setData(actionBarData);
                    action_chart_bar.invalidate();
                    List<PieEntry> action_pie_Data = new ArrayList<>();
                    int []colors = new int[3];
                    int pos = 0;
                    if(action_info.get("stationary")>0) {
                        action_pie_Data.add(new PieEntry(action_info.get("stationary"), "stationary"));
                        colors[pos++] = ColorTemplate.VORDIPLOM_COLORS[0];
                    }
                    if(action_info.get("walking")>0)
                    {
                        action_pie_Data.add(new PieEntry(action_info.get("walking"),"walking"));
                        colors[pos++] = ColorTemplate.JOYFUL_COLORS[0];
                    }
                    if(action_info.get("running")>0)
                    {
                        action_pie_Data.add(new PieEntry(action_info.get("running"),"running"));
                        colors[pos++] = ColorTemplate.COLORFUL_COLORS[0];
                    }
                    PieDataSet actionPieSet = new PieDataSet(action_pie_Data, "");
                    actionPieSet.setColors(colors);
                    action_chart_pie.getDescription().setEnabled(false);
                    PieData actionPieData = new PieData(actionPieSet);
                    actionPieData.setDrawValues(true);
                    actionPieData.setValueFormatter(new PercentFormatter());
                    action_chart_pie.setUsePercentValues(true);
                    action_chart_pie.setExtraOffsets(5, 5, 5, 5);
                    action_chart_pie.setDrawHoleEnabled(true);
                    action_chart_pie.setTransparentCircleColor(Color.WHITE);//设置PieChart内部透明圆与内部圆间距(31f-28f)填充颜色
                    action_chart_pie.setTransparentCircleAlpha(0);//设置PieChart内部透明圆与内部圆间距(31f-28f)透明度[0~255]数值越小越透明
                    action_chart_pie.setHoleRadius(0f);//设置PieChart内部圆的半径(这里设置0f,即不要内部圆)
                    action_chart_pie.setTransparentCircleRadius(31f);//设置PieChart内部透明圆的半径(这里设置31.0f)

                    action_chart_pie.setDrawCenterText(true);//是否绘制PieChart内部中心文本（true：下面属性才有意义）

                    action_chart_pie.setRotationAngle(0);//设置pieChart图表起始角度

                    action_chart_pie.setRotationEnabled(true);//设置pieChart图表是否可以手动旋转
                    action_chart_pie.setHighlightPerTapEnabled(true);//设置piecahrt图表点击Item高亮是否可用

                    action_chart_pie.animateY(1400, Easing.EaseInOutQuad);// 设置pieChart图表展示动画效果



                    Legend l = action_chart_pie.getLegend();
                    l.setForm(Legend.LegendForm.LINE);
                    l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
                    l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
                    l.setOrientation(Legend.LegendOrientation.VERTICAL);
                    l.setDrawInside(false);
                    l.setXEntrySpace(7f);
                    l.setYEntrySpace(0f);
                    l.setYOffset(0f);
                    action_chart_pie.setData(actionPieData);
                    action_chart_pie.invalidate();
                    break;

                case 500:
                    Toast.makeText(FlowChart.this,"server shutdown!",Toast.LENGTH_SHORT).show();
                    try {
                        Thread.sleep(3000);
                    }
                    catch (Exception e)
                    {
                        Log.e("FlowChart",Log.getStackTraceString(e));
                    }
                    SocketStation.exit();
                    break;
                default:
                    Toast.makeText(FlowChart.this,"数据请求失败！",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flow_chart);
        temp_chart = (LineChart) findViewById(R.id.temp_chart);
        temp_chart.setNoDataText("loading...");
        humi_chart = (LineChart) findViewById(R.id.humi_chart);
        humi_chart.setNoDataText("loading");
        action_chart_bar = (BarChart) findViewById(R.id.action_chart_bar);
        action_chart_bar.setNoDataText("loading");
        action_chart_pie = (PieChart) findViewById(R.id.action_chart_pie);
        action_chart_pie.setNoDataText("loading");
        Intent intent = getIntent();
        boardName = intent.getStringExtra("board name");
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
                    Log.e("FlowChart",recvData);
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
                    Log.e("FlowChart","before send msg");
                    Message msg = handler.obtainMessage();
                    msg.what = 200;
                    msg.obj = num;
                    handler.sendMessage(msg);
                    Log.e("FlowChart","after send msg");
                }
                catch (SocketException e)
                {
                    Log.e("FlowChart",Log.getStackTraceString(e));
                    handler.sendEmptyMessage(500);
                }
                catch (Exception e)
                {
                    Log.e("FlowChart",Log.getStackTraceString(e));
                }

            }
        }).start();
    }
}