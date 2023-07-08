package com.watcher.ffplayer.adapter;




import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.app.Activity;
import com.watcher.ffplayer.BoardInfo;
import com.watcher.ffplayer.R;
import com.watcher.ffplayer.entity.Alarm;
import com.watcher.ffplayer.entity.Board;
import com.watcher.ffplayer.entity.Constant;
import com.watcher.ffplayer.entity.SocketStation;
import android.content.Intent;
import java.util.List;

public class AlarmsAdapter extends BaseAdapter {
    private List<Alarm> itemList;
    public List<Alarm> getItemList() {
        return itemList;
    }
    public void setItemList(List<Alarm> itemList) {
        this.itemList = itemList;
    }

    private Context context;
    private LayoutInflater inflater;
    public AlarmsAdapter(Context context){
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
            vh.type = (TextView) convertView.findViewById(R.id.alarm_item_type);
            convertView.setTag(vh);
        }else {
            vh = (ViewHolder) convertView.getTag();
        }
        Alarm item = itemList.get(position);
        vh.head.setText("alarm message "+position);
        Log.e("warn item",item.toString());
        if(item.type.equals("face")) {
            vh.type.setText("face detected " + item.time);
            vh.type.setBackgroundColor(0xFF0000);
        }
        else if(item.type.equals("data"))
        {
            vh.type.setText("abnormal sensor data detected " + item.time);
            vh.type.setBackgroundColor(0xFF4500);
        }
        return convertView;
    }
    private class ViewHolder {
        TextView head;
        TextView type;
    }
}
