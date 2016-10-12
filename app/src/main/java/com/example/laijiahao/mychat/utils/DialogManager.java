package com.example.laijiahao.mychat.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.laijiahao.mychat.R;


/**
 * Created by laijiahao on 16/10/9.
 */

public class DialogManager {

    private Dialog mDialog;

    private ImageView mIcon;
    private ImageView mVoice;

    private TextView mLable;

    private Context mContext; //保持一个上下文的引用,来inflate一个布局文件

    public DialogManager(Context context) {
        mContext = context;
    }

    //Dialog要对外公布几个方法,以保证它不同的style的dialog的展现
    //第一个是默认的dialog,显示录音对话框
    public void showRecordingDialog() {
        mDialog = new Dialog(mContext, R.style.Theme_AudioDialog);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.dialog_recorder, null);
        mDialog.setContentView(view);

        mIcon = (ImageView) mDialog.findViewById(R.id.id_recorder_dialog_icon);
        mVoice = (ImageView) mDialog.findViewById(R.id.id_recorder_dialog_voice);

        mLable = (TextView) mDialog.findViewById(R.id.id_recorder_dialog_label);

        mDialog.show();

    }

    //正在录音时的ImageView的显示和TextView的显示
    public void recording() {
        if (mDialog != null && mDialog.isShowing()) {
            mIcon.setVisibility(View.VISIBLE);
            mVoice.setVisibility(View.VISIBLE);
            mLable.setVisibility(View.VISIBLE);

            mIcon.setImageResource(R.drawable.recorder);
            mLable.setText(R.string.dialog_recorder);
        }

    }

    //显示wantToCancel对话框
    public void wantToCancel() {
        if (mDialog != null && mDialog.isShowing()) {
            mIcon.setVisibility(View.VISIBLE);
            mVoice.setVisibility(View.GONE);
            mLable.setVisibility(View.VISIBLE);

            mIcon.setImageResource(R.drawable.cancel);
            mLable.setText(R.string.dialog_toCancel);
        }
    }

    //显示tooShort对话框
    public void tooShort() {
        if (mDialog != null && mDialog.isShowing()) {
            mIcon.setVisibility(View.VISIBLE);
            mVoice.setVisibility(View.GONE);
            mLable.setVisibility(View.VISIBLE);

            mIcon.setImageResource(R.drawable.voice_to_short);
            mLable.setText(R.string.dialog_tooShort);
        }
    }

    //隐藏对话框
    public void dimissDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    /**
     * 通过level去更新voice上的图片 v1到v7
     * @param level 1-7
     */
    //更新对话框上的音量的标志
    public void updateVoiceLevel(int level) {
        if (mDialog != null && mDialog.isShowing()) {
//            mIcon.setVisibility(View.VISIBLE);
//            mVoice.setVisibility(View.VISIBLE);
//            mLable.setVisibility(View.VISIBLE);

            //通过方法名找到资源,通过level整型找到resId的值
            int resId = mContext.getResources().getIdentifier("v" + level,
                    "drawable",mContext.getPackageName());
            mVoice.setImageResource(resId);
        }
    }
}
