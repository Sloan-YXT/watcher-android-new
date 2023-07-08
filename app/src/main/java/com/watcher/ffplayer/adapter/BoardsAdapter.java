package com.watcher.ffplayer.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.watcher.ffplayer.R;
import com.watcher.ffplayer.entity.Board;

public class BoardsAdapter extends BaseAdapter{
    private List<Board> itemList;
    public List<Board> getItemList() {
        return itemList;
    }
    public void setItemList(List<Board> itemList) {
        this.itemList = itemList;
    }

    private Context context;
    private LayoutInflater inflater;
    public BoardsAdapter(Context context){
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
            convertView  = inflater.inflate(R.layout.board_item,null);
            vh = new ViewHolder();
            vh.boardTitle = (TextView) convertView.findViewById(R.id.board_item_title);
            vh.boardName = (TextView) convertView.findViewById(R.id.board_item_name);
            vh.boardLocation = (TextView) convertView.findViewById(R.id.board_item_location);
            convertView.setTag(vh);
        }else {
            vh = (ViewHolder) convertView.getTag();
        }
        Board item = itemList.get(position);
        vh.boardTitle.setText("Node"+position);
        vh.boardName.setText("Name:"+item.boardName);
        vh.boardLocation.setText("Location:"+item.boardLocation);
        return convertView;
    }
    private class ViewHolder {
        TextView boardTitle,boardName,boardLocation;
    }
}
