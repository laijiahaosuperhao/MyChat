package com.example.laijiahao.mychat.utils;

import android.media.MediaRecorder;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by laijiahao on 16/10/9.
 */

public class AudioManager {

    private MediaRecorder mMediaRecorder;
    private String mDir; //文件夹名称,放置录制的音频
    private String mCurrentFilePath; //把录音保存到文件夹以后,需要把文件夹的path回传给button,然后Button回传给activity

    //这个类使用单例
    private static AudioManager mInstance;

    private boolean isPrepared;

    //私有化它的构造方法
    private AudioManager(String dir) {
        mDir = dir;
    }


    /**
     * 回调准备完毕,然后Button才会显示录音框,然后去计时
     */
    /**
     * prepare();--end prepare->callback, end prepare提供callback告诉Button prepare完成了,所以需要一个接口
     */
    public interface AudioStateListener {
        //告诉Button prepare完毕的方法
        void wellPrepared();
    }

    public AudioStateListener mListener;

    public void setOnAudioStateListener(AudioStateListener listener) {
        mListener = listener;
    }

    //公布一个static的工厂类,工厂方法
    public static AudioManager getInstance(String dir) {
        if (mInstance == null) {
            //若=null,同步一下,同步的对象是AudioManager.class, static级别的
            synchronized (AudioManager.class) {
                //在同步区再次判断 mInstance 是否等于null, 否则就return mInstance
                if (mInstance == null) {
                    mInstance = new AudioManager(dir);
                }
            }
        }

        return mInstance;
    }

    //去准备 以及开始
    public void prepareAudio() {
        try {
            isPrepared = false;
            //创建文件夹,文件夹的路径是mDir
            File dir = new File(mDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String fileName = generateFileName();

            File file = new File(dir, fileName); //根据filename创建一个file,文件的路径是dir,名称是fileName

            mCurrentFilePath = file.getAbsolutePath();
            mMediaRecorder = new MediaRecorder();
            //设置输出文件
            mMediaRecorder.setOutputFile(file.getAbsolutePath());//文件完整路径
            //设置MediaRecorder的音频源为麦克风
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            //设置音频格式
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            //设置音频的编码为amr
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            //准备结束
            isPrepared = true;

            if(mListener!=null){
                mListener.wellPrepared();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 随机生成文件的名称
     *
     * @return
     */
    private String generateFileName() {
        return UUID.randomUUID().toString() + ".amr";
    }

    //获得当前音量等级,默认return 1
    public int getVoiceLevel(int maxLevel) {
        if(isPrepared){

            try {
                //若得不到最大振幅的值,会出现异常,所以捕获,然后return 1,dialog能正常显示,忽略掉这个错误
                //获得它最大振幅 mMediaRecorder.getMaxAmplitude() 1-32767
                return maxLevel * mMediaRecorder.getMaxAmplitude()/32768+1;//得到0-1之间的值*7得到0-7的值,这个值取整,最小是0,最大是6(6.9取整也是6),再加1,是1-7
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return 1;
    }

    //释放
    public void release() {
        mMediaRecorder.stop();
        mMediaRecorder.release();
        mMediaRecorder = null;

    }

    //取消
    public void cancel() {

        release();
        //prepare时产生一个文件,cancel就应该删除这个文件
        if(mCurrentFilePath!=null){
            File file = new File(mCurrentFilePath);
            file.delete();
            mCurrentFilePath = null;
        }
    }

    public String getCurrentFilePath() {
        return mCurrentFilePath;
    }
}
