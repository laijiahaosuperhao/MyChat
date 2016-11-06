package com.example.laijiahao.mychat.widget;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.example.laijiahao.mychat.R;
import com.example.laijiahao.mychat.utils.AudioManager;
import com.example.laijiahao.mychat.utils.DialogManager;

import static com.example.laijiahao.mychat.ui.MyRecorderActivity.Recorder.TYPE_SENT;

/**
 * Created by laijiahao on 16/10/8.
 */

public class AudioRecorderButton extends Button implements AudioManager.AudioStateListener {

    private static final int DISTANCE_Y_CANCEL =50; //单位为px
    private static final int STATE_NORMAL = 1 ;  //默认状态
    private static final int STATE_RECORDING = 2 ;  //录音状态
    private static final int STATE_WANT_TO_CANCEL = 3 ;  //取消状态

    private int mCurState = STATE_NORMAL; //当前记录状态
    //已经开始录音
    private boolean isRecording = false;

    private DialogManager mDialogManager;

    private AudioManager mAudioManager;

    private float mTime;
    //是否触发long click的标志位,如果触发了longclick,理论上已经通过AudioManager进行prepare,
    //在up时需要release,cancel释放资源,如果没有触发longclick,就不用去释放资源
    private boolean mReady;

    public AudioRecorderButton(Context context) {
        this(context,null);
    }

    public AudioRecorderButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        //mDialogManager在Button构造的时候进行初始化
        mDialogManager = new DialogManager(getContext());

        String dir = Environment.getExternalStorageDirectory()+"/recorder_audios";//在存储卡的根目录下创建一个文件夹
        //在构造方法里初始化AudioManager
        mAudioManager = AudioManager.getInstance(dir);
        //注册mAudioManager的回调
        mAudioManager.setOnAudioStateListener(this);

        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mReady = true;
                mAudioManager.prepareAudio();
                return false;
            }
        });
    }

    /**
     * 录音完成后的回调
     */
    public interface AudioFinishRecorderListener{
        void onFinish(float seconds, String filePath, String content, int type);//seconds录音的时长
    }
    //成员变量
    private AudioFinishRecorderListener mListener;
    //添加一个方法,外部可以去set
    public void setAudioFinishRecorderListener(AudioFinishRecorderListener listener){
        mListener = listener;
    }

    /**
     * 获取音量大小和录音计时情况的Runnable
     */
    private Runnable mGetVoiceLevelRunnable = new Runnable() {
        @Override
        public void run() {
           //已经在录制
            while(isRecording){

                try {
                    Thread.sleep(100);//每隔0.1秒获取一次
                    mTime +=0.1f; //完成mTime的计时
                    mHandler.sendEmptyMessage(MSG_VOICE_CHANGED);//具体的获取
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private static final int MSG_AUDIO_PREPARED = 0X110;
    private static final int MSG_VOICE_CHANGED = 0X111;
    private static final int MSG_DIALOG_DIMISS = 0X112;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_AUDIO_PREPARED:
                    //显示应该在 audio end prepared 以后,就是在回调以后 ,理论上开始计时,记录录音时长,在Runnable里
                    mDialogManager.showRecordingDialog();
                    isRecording = true;
                    new Thread(mGetVoiceLevelRunnable).start();
                    break;
                case MSG_VOICE_CHANGED:
                    //获取Audio Voice
                    mDialogManager.updateVoiceLevel(mAudioManager.getVoiceLevel(7));
                    break;
                case MSG_DIALOG_DIMISS:
                    mDialogManager.dimissDialog();
                    break;

            }
        }
    };

    //Audio完全准备完毕以后的回调
    @Override
    public void wellPrepared() {
        mHandler.sendEmptyMessage(MSG_AUDIO_PREPARED);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (action) {

            case MotionEvent.ACTION_DOWN:
                changeState(STATE_RECORDING);
                break;

            case MotionEvent.ACTION_MOVE:

                //如果已经开始录音
                if(isRecording){
                    //根据x,y的坐标,判断是否想要取消
                    if(wantToCancel(x,y)){
                        changeState(STATE_WANT_TO_CANCEL);
                    }else{
                        changeState(STATE_RECORDING);
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                //连long click都没有触发
                if(!mReady){
                    reset();
                    return super.onTouchEvent(event);
                }
                //触发了long click,但还没prepare完毕,已经up 或 录音时间很短,小于我们预定的值
                if(!isRecording || mTime < 0.6f) {
                    mDialogManager.tooShort();
                    mAudioManager.cancel();//需要取消,因为已经prepare,迟早会start,start以后要stop
                    mHandler.sendEmptyMessageDelayed(MSG_DIALOG_DIMISS, 1300);//在1.3 秒以后关闭dialog

                }else if(mCurState ==STATE_RECORDING){ //正常录制结束
                    mDialogManager.dimissDialog();
                    //release
                    mAudioManager.release();
                    //callbackToActivity
                    if(mListener != null){
                        mListener.onFinish(mTime,mAudioManager.getCurrentFilePath(),null, TYPE_SENT);
                    }


                }else if(mCurState == STATE_WANT_TO_CANCEL){
                    mDialogManager.dimissDialog();
                    //cancel
                    mAudioManager.cancel();
                }

                reset();

                break;
        }

        return super.onTouchEvent(event);
    }

    /**
     * 恢复状态及标志位
     */
    private void reset() {
        isRecording = false;
        mReady = false;
        mTime = 0;
        changeState(STATE_NORMAL);
    }

    private boolean wantToCancel(int x, int y) {
        //判断手指的横坐标是否超出按钮范围
        if(x<0 || x > getWidth()){
            return true;
        }
        //判断手指的纵坐标是否超出(按钮范围再加上往上50px或按钮再往下50px的)范围
        if(y < -DISTANCE_Y_CANCEL || y > getHeight() + DISTANCE_Y_CANCEL){
            return true;
        }
        return false;
    }

    //改变Button显示文本和背景色 以及dialog的显示
    //dialog的状态和Button基本是一致的，除了tooshort
    private void changeState(int state) {
        if(mCurState != state){
            mCurState = state;
            switch (state) {
                case STATE_NORMAL:
                    //在up时dismiss掉dialog
                    setBackgroundResource(R.drawable.btn_recorder_nomal);
                    setText(R.string.str_recorder_normal);
                    break;
                case STATE_RECORDING:
                    setBackgroundResource(R.drawable.btn_recording);
                    setText(R.string.str_recorder_recording);
                    if(isRecording){
                        mDialogManager.recording();
                    }
                    break;
                case STATE_WANT_TO_CANCEL:
                    setBackgroundResource(R.drawable.btn_recording);
                    setText(R.string.str_recorder_want_cancel);
                    mDialogManager.wantToCancel();
                    break;
            }
        }
    }


}
