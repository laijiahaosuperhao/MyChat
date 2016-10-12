package com.example.laijiahao.mychat.ui;

import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.example.laijiahao.mychat.R;
import com.example.laijiahao.mychat.runtimepermissions.PermissionsManager;
import com.example.laijiahao.mychat.runtimepermissions.PermissionsResultAction;
import com.example.laijiahao.mychat.utils.MediaManager;
import com.example.laijiahao.mychat.adapter.RecorderAdapter;
import com.example.laijiahao.mychat.widget.AudioRecorderButton;

import java.util.ArrayList;
import java.util.List;

import static com.example.laijiahao.mychat.ui.MyRecorderActivity.Recorder.TYPE_RECEIVED;

public class MyRecorderActivity extends BaseActivity implements View.OnClickListener {

    private ListView mListView;
    private ArrayAdapter<Recorder> mAdapter;
    private List<Recorder> mDatas = new ArrayList<Recorder>();

    private AudioRecorderButton mAudioRecorderButton;

    private View mAnimView;

    private Button btn_set_mode_keyboard;
    private Button btn_set_mode_voice;
    private EditText input_text;
    private Button send;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_myrecorder);
        runtimepermissions();

        input_text = (EditText) findViewById(R.id.input_text);
        send = (Button) findViewById(R.id.send);
        btn_set_mode_keyboard = (Button) findViewById(R.id.btn_set_mode_keyboard);
        btn_set_mode_voice = (Button) findViewById(R.id.btn_set_mode_voice);
        btn_set_mode_keyboard.setOnClickListener(this);
        btn_set_mode_voice.setOnClickListener(this);
        send.setOnClickListener(this);

        mListView = (ListView) findViewById(R.id.id_listview);
        mAudioRecorderButton = (AudioRecorderButton) findViewById(R.id.id_record_button);
        mAudioRecorderButton.setAudioFinishRecorderListener(new AudioRecorderButton.AudioFinishRecorderListener() {
            @Override
            public void onFinish(float seconds, String filePath, String content, int type) {
                Recorder recorder = new Recorder(seconds, filePath, content, type);
                mDatas.add(recorder);
                mAdapter.notifyDataSetChanged();
                mListView.setSelection(mDatas.size() - 1);
            }
        });

        mAdapter = new RecorderAdapter(this, mDatas);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                    TextView leftmsg = (TextView) view.findViewById(R.id.left_msg);
//                    if(leftmsg.getText()!=null){
//                        return;
//                    }

                //第一个对话框在播放,我们点击第二个的时候,首先会把第一个的动画停止,且让mAnimView = null,
                // 然后再去赋值第二个的mAnimView,开启它的动画
                if (mAnimView != null) {
                    mAnimView.setBackgroundResource(R.drawable.adj);
                    mAnimView = null;
                }
                //播放动画
                mAnimView = view.findViewById(R.id.id_record_anim);
                mAnimView.setBackgroundResource(R.drawable.play_anim);
                AnimationDrawable anim = (AnimationDrawable) mAnimView.getBackground();
                anim.start();
                if (mDatas.get(position).filePath != null) {

                    //用MediaManager类来播放音频  参数二 回调接口,音频播放完成后,需要在结束的回调方法里面将animView的动画取消
                    MediaManager.playSound(mDatas.get(position).filePath, new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mAnimView.setBackgroundResource(R.drawable.adj);
                        }
                    });
                }
            }

        });

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_set_mode_keyboard:
                btn_set_mode_keyboard.setVisibility(View.GONE);
                mAudioRecorderButton.setVisibility(View.GONE);
                btn_set_mode_voice.setVisibility(View.VISIBLE);
                input_text.setVisibility(View.VISIBLE);
                send.setVisibility(View.VISIBLE);
                if (mAudioRecorderButton.getVisibility()== View.VISIBLE) {
                    // TODO hide keyboard
                }
                break;
            case R.id.btn_set_mode_voice:
                btn_set_mode_keyboard.setVisibility(View.VISIBLE);
                mAudioRecorderButton.setVisibility(View.VISIBLE);
                btn_set_mode_voice.setVisibility(View.GONE);
                input_text.setVisibility(View.GONE);
                send.setVisibility(View.GONE);
                break;
            case R.id.send:
                String content = input_text.getText().toString();
                if (!TextUtils.isEmpty(content)) {
                    Recorder recorder = new Recorder(0, null, content, TYPE_RECEIVED);
                    mDatas.add(recorder);
                    mAdapter.notifyDataSetChanged();
                    mListView.setSelection(mDatas.size() - 1);
                    input_text.setText(""); //清空输入框的内容
                }
                break;
        }
    }


    public class Recorder {
        public float time;
        String filePath;
        public static final int TYPE_RECEIVED = 0; //这是一条收到的消息
        public static final int TYPE_SENT = 1; //这是一条发出的消息

        String content; //消息的内容
        int type; //消息的类型

        public Recorder(float time, String filePath, String content, int type) {
            this.time = time;
            this.filePath = filePath;
            this.content = content;
            this.type = type;
        }

        public float getTime() {
            return time;
        }

        public void setTime(float time) {
            this.time = time;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public String getContent() {
            return content;
        }

        public int getType() {
            return type;
        }
    }

    private void runtimepermissions() {
        /**
         * 请求所有必要的权限----
         */
        PermissionsManager.getInstance().requestAllManifestPermissionsIfNecessary(this, new PermissionsResultAction() {
            @Override
            public void onGranted() {
//				Toast.makeText(MainActivity.this, "All permissions have been granted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDenied(String permission) {
                //Toast.makeText(MainActivity.this, "Permission " + permission + " has been denied", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        MediaManager.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MediaManager.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MediaManager.release();
    }

    public void back(View view) {
        finish();
    }
}
