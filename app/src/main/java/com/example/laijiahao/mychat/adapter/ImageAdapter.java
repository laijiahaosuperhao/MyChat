package com.example.laijiahao.mychat.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.example.laijiahao.mychat.R;
import com.example.laijiahao.mychat.utils.ImageLoader;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ImageAdapter extends BaseAdapter {
    //图片所在文件夹的路径
    private String mDirPath;
    //图片的名称的List的集合
    private List<String> mImgPaths;

    private LayoutInflater mInflater;

    private static Set<String> mSelectedImg = new HashSet<String>();

    private int mScreenWidth;

    //参数：上下文、图片的名称的List的集合、图片所在文件夹的路径
    public ImageAdapter(Context context, List<String> mDatas, String dirPath) {
        this.mDirPath = dirPath;
        this.mImgPaths = mDatas;
        mInflater = LayoutInflater.from(context);

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        mScreenWidth = outMetrics.widthPixels;
    }

    @Override
    public int getCount() {
        return mImgPaths.size();
    }

    @Override
    public Object getItem(int position) {
        return mImgPaths.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder ;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_gridview, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.mImg = (ImageView) convertView.findViewById(R.id.id_item_image);
            viewHolder.mSelect = (ImageButton) convertView.findViewById(R.id.id_item_select);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //重置状态

        //显示picture_no的图片，因为图片会进行回调去加载图片，切换到第二屏，没切换过去的时候，
        // 都显示picture_no的图片，而不是上一屏的图片，然后再变成下一屏的图片
        viewHolder.mImg.setImageResource(R.drawable.pictures_no);
        viewHolder.mSelect.setImageResource(R.drawable.picture_unselected);
        viewHolder.mImg.setColorFilter(null);

        //此时imageView弄不到的，因为imageView的宽度写的是matchparent，而且当前imageView还没被显示，它的宽度还没被计算
        viewHolder.mImg.setMaxWidth(mScreenWidth/3);

        //实现图片加载，ImageLoader会把bitmap设置到imageview上
        ImageLoader.getInstance(3, ImageLoader.Type.LIFO).loadImage(mDirPath + "/" + mImgPaths.get(position), viewHolder.mImg);

        final String filePath = mDirPath + "/" +mImgPaths.get(position);
        viewHolder.mImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //已经被选择
                if(mSelectedImg.contains(filePath)){
                    mSelectedImg.remove(filePath);
                    viewHolder.mImg.setColorFilter(null);
                    viewHolder.mSelect.setImageResource(R.drawable.picture_unselected);
                }else {
                    //未被选择
                    mSelectedImg.add(filePath);
                    viewHolder.mImg.setColorFilter(Color.parseColor("#77000000"));
                    viewHolder.mSelect.setImageResource(R.drawable.pictures_selected);
                }
     //           notifyDataSetChanged();//会发生闪屏
            }
        });

        if(mSelectedImg.contains(filePath)){
            viewHolder.mImg.setColorFilter(Color.parseColor("#77000000"));
            viewHolder.mSelect.setImageResource(R.drawable.pictures_selected);
        }

        return convertView;
    }

    //用于对应Item布局里面的控件，减少findViewById这样的操作
    private class ViewHolder {
        ImageView mImg;
        ImageButton mSelect;
    }

}

