package com.example.laijiahao.mychat.utils;

import android.media.AudioManager;
import android.media.MediaPlayer;

import java.io.IOException;

/**
 * Created by laijiahao on 16/10/10.
 */
public class MediaManager {

    private static MediaPlayer mMediaPlayer;

    private static boolean isPause;

    public static void playSound(String filePath, MediaPlayer.OnCompletionListener onCompletionListener) {
        if(mMediaPlayer == null){
            mMediaPlayer = new MediaPlayer(); //Idle状态
            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    mMediaPlayer.reset(); //发现error,让 mMediaPlayer.reset()
                    return false;
                }
            });
        }else{
            mMediaPlayer.reset(); //Idle状态
        }

        try {
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnCompletionListener(onCompletionListener);
            mMediaPlayer.setDataSource(filePath);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //若音频比较长有30秒,用户在播放,此时用户回到主界面,或者此时来了一个电话,造成activity pause状态,此时就该将音频进行停止
    public static void pause(){
        if(mMediaPlayer!=null && mMediaPlayer.isPlaying()){
            mMediaPlayer.pause();
            isPause = true;
        }
    }

    public static void resume(){
        if(mMediaPlayer!=null && isPause){
            mMediaPlayer.start();
            isPause = false;
        }
    }

    public static void release(){
        if(mMediaPlayer!=null){
            mMediaPlayer.release();
            mMediaPlayer=null;
        }
    }

}
