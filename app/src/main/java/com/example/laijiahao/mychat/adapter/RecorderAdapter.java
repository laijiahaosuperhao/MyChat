package com.example.laijiahao.mychat.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.laijiahao.mychat.R;
import com.example.laijiahao.mychat.ui.MyRecorderActivity;

import java.util.List;

/**
 * Created by laijiahao on 16/10/10.
 */

public class RecorderAdapter extends ArrayAdapter<MyRecorderActivity.Recorder> {

    //最小宽度最大宽度根据屏幕宽度去定义
    private int mMinItemWidth;
    private int mMaxItemWidth;

    private LayoutInflater mInflater;


    //数据通过构造方法传入进来 ,布局文件ID当-1,这里不去使用它的,会自己指定
    public RecorderAdapter(Context context, List<MyRecorderActivity.Recorder> datas) {
        super(context, -1, datas);
        //完成LayoutInflater的初始化
        mInflater = LayoutInflater.from(context);

        //在构造方法里获取屏幕宽度
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);

        //对话框最大宽度
        mMaxItemWidth = (int) (outMetrics.widthPixels * 0.7f);
        //对话框最小宽度
        mMinItemWidth = (int) (outMetrics.widthPixels * 0.15f);
    }

    @NonNull
    @Override
    //在getView控制item的显示,view holder模式
    public View getView(int position, View convertView, ViewGroup parent) {

        MyRecorderActivity.Recorder recorder = getItem(position);

        ViewHolder holder = null;
        //完成holder的初始化
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_recorder, parent, false);
            holder = new ViewHolder();
            holder.leftLayout = (LinearLayout) convertView.findViewById(R.id.left_layout);
            holder.rightLayout = (RelativeLayout) convertView.findViewById(R.id.right_layout);
            holder.leftMsg = (TextView) convertView.findViewById(R.id.left_msg);

            holder.seconds = (TextView) convertView.findViewById(R.id.id_recorder_time);
            holder.length = convertView.findViewById(R.id.id_recorder_length);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //根据消息的类型来决定显示和隐藏哪种消息
        if (recorder.getType() == MyRecorderActivity.Recorder.TYPE_RECEIVED) {
            holder.leftLayout.setVisibility(View.VISIBLE);
            holder.rightLayout.setVisibility(View.GONE);
            holder.leftMsg.setText(recorder.getContent());
        } else if (recorder.getType() == MyRecorderActivity.Recorder.TYPE_SENT) {
            holder.rightLayout.setVisibility(View.VISIBLE);
            holder.leftLayout.setVisibility(View.GONE);
            holder.seconds.setText(Math.round(getItem(position).time) + "\"");
            ViewGroup.LayoutParams lp = holder.length.getLayoutParams();
            lp.width = (int) (mMinItemWidth + (mMaxItemWidth / 60f * getItem(position).time));
        }

        return convertView;
    }

    private class ViewHolder {
        TextView seconds;
        View length;
        LinearLayout leftLayout;
        TextView leftMsg;
        RelativeLayout rightLayout;
    }

}
