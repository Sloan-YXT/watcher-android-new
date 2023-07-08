package com.watcher.ffplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.watcher.ffplayer.R;
import com.watcher.ffplayer.entity.Alarm;
import com.watcher.ffplayer.entity.Board;
import com.watcher.ffplayer.entity.Graph;

import java.util.List;

public class GraphsAdapter extends BaseAdapter {
    private List<Graph> itemList;
    public List<Graph> getItemList() {
        return itemList;
    }
    public void setItemList(List<Graph> itemList) {
        this.itemList = itemList;
    }

    private Context context;
    private LayoutInflater inflater;
    public GraphsAdapter(Context context){
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
        ViewHolder vh = null;
        if(convertView == null){
            convertView  = inflater.inflate(R.layout.alarm_item,null);
            vh = new ViewHolder();
            vh.head = (TextView) convertView.findViewById(R.id.alarm_item_title);
            vh.time = (TextView) convertView.findViewById(R.id.alarm_item_type);
            convertView.setTag(vh);
        }else {
            vh = (ViewHolder) convertView.getTag();
        }
        Graph item = itemList.get(position);
        vh.head.setText("警告图片"+position);
        vh.time.setText(itemList.get(position).time);
        return convertView;
    }
    private class ViewHolder {
        TextView head,time;
    }
}
