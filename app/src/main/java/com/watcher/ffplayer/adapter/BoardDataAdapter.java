package com.watcher.ffplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.watcher.ffplayer.R;
import com.watcher.ffplayer.entity.Board;
import com.watcher.ffplayer.entity.BoardData;
import com.watcher.ffplayer.entity.Constant;

import java.util.List;

public class BoardDataAdapter extends BaseAdapter {
    private List<BoardData> itemList;
    public List<BoardData> getItemList() {
        return itemList;
    }
    public void setItemList(List<BoardData> itemList) {
        this.itemList = itemList;
    }

    private Context context;
    private LayoutInflater inflater;
    public BoardDataAdapter(Context context){
        this.context = context;
        inflater = LayoutInflater.from(context);
    }
    @Override
    public int getCount() {
        return itemList == null ? 0 : itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BoardDataAdapter.ViewHolder vh = null;
        if(convertView == null){
            convertView  = inflater.inflate(R.layout.board_data_item,null);
            vh = new BoardDataAdapter.ViewHolder();
            vh.boardDataTitle = (TextView) convertView.findViewById(R.id.board_data_item_title);
            vh.boardDataLocation = (TextView) convertView.findViewById(R.id.board_data_item_location);
            vh.boardDataTime = (TextView) convertView.findViewById(R.id.board_data_item_time);
            vh.boardDataAction = (TextView) convertView.findViewById(R.id.board_data_item_action);
            vh.boardDataTemp = (TextView) convertView.findViewById(R.id.board_data_item_temp);
            vh.boardDataHumi = (TextView) convertView.findViewById(R.id.board_data_item_humi);
            vh.boardDataLight = (TextView) convertView.findViewById(R.id.board_data_item_light);
            vh.boardDataSmoke = (TextView) convertView.findViewById(R.id.board_data_item_smoke);
            convertView.setTag(vh);
        }else {
            vh = (BoardDataAdapter.ViewHolder) convertView.getTag();
        }
        BoardData item = itemList.get(position);
        vh.boardDataTitle.setText("Node"+item.boardName+":"+position);
        vh.boardDataLocation.setText("location:"+item.boardLocation);
        vh.boardDataTime.setText("captured at:"+item.month+"//"+item.date+"//"+item.year+"//"+","+item.weekDay+" "+item.time);
        if(item.action.equals("NA"))
        {
            vh.boardDataAction.setText("No action sensor available");
        }
        else
        {
            vh.boardDataAction.setText("action:"+item.action);
        }
        if (item.temp.equals(Constant.NA))
        {
            vh.boardDataLight.setText("temperature:no temperature sensor");
        }
        else {
            vh.boardDataTemp.setText("temperature(Celsius):" + item.temp);
        }
        if (item.humi.equals(Constant.NA))
        {
            vh.boardDataHumi.setText("humidity:no humidity sensor");
        }
        else {
            vh.boardDataHumi.setText("humidity(RH):" + item.humi);
        }
        if(item.light.equals(Constant.NA)==true)
        {
            vh.boardDataLight.setText("light:no light sensor");
        }
        else if(item.light.equals(Constant.wrongLight)==false)
        {
            vh.boardDataLight.setText("light:normal");
        }
        else
        {
            vh.boardDataLight.setText("light:too dark");
        }
        if(item.smoke.equals(Constant.NA)==true)
        {
            vh.boardDataSmoke.setText("smoke:no smoke sensor");
        }
        else if(item.smoke.equals(Constant.wrongSmoke)==false)
        {
            vh.boardDataSmoke.setText("smoke:sensitive smoke detected");
        }
        else
        {
            vh.boardDataSmoke.setText("smoke:normal");
        }
        return convertView;
    }
    private class ViewHolder {
        TextView boardDataTitle,boardDataTime,boardDataLocation,boardDataAction,boardDataTemp,boardDataHumi,boardDataLight,boardDataSmoke;
    }
}