package com.example.laijiahao.mychat.ui;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.laijiahao.mychat.R;
import com.example.laijiahao.mychat.adapter.FriendCircleAdapter;
import com.example.laijiahao.mychat.widget.Image;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by laijiahao on 16/10/13.
 */

public class FriendCircleActivity extends BaseActivity {
    private ListView listView;
    private List<List<Image>> imagesList;
    private ImageView iv_camera;
    private MediaPlayer mediaPlayer ;

    private String[][] images = new String[][]{{"http://d.hiphotos.baidu.com/image/h%3D200/sign=201258cbcd80653864eaa313a7dca115/ca1349540923dd54e54f7aedd609b3de9c824873.jpg", "640", "960"}
            , {"http://img3.fengniao.com/forum/attachpics/537/165/21472986.jpg", "640", "640"}
            , {"http://d.hiphotos.baidu.com/image/h%3D200/sign=ea218b2c5566d01661199928a729d498/a08b87d6277f9e2fd4f215e91830e924b999f308.jpg", "640", "640"}
            , {"http://img4.imgtn.bdimg.com/it/u=3445377427,2645691367&fm=21&gp=0.jpg", "640", "640"}
            , {"http://img4.imgtn.bdimg.com/it/u=2644422079,4250545639&fm=21&gp=0.jpg", "640", "640"}
            , {"http://img5.imgtn.bdimg.com/it/u=1444023808,3753293381&fm=21&gp=0.jpg", "640", "640"}
            , {"http://img4.imgtn.bdimg.com/it/u=882039601,2636712663&fm=21&gp=0.jpg", "640", "640"}
            , {"http://img4.imgtn.bdimg.com/it/u=4119861953,350096499&fm=21&gp=0.jpg", "640", "640"}
            , {"http://img5.imgtn.bdimg.com/it/u=2437456944,1135705439&fm=21&gp=0.jpg", "640", "640"}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friendcircle);

        iv_camera = (ImageView) findViewById(R.id.iv_camera);
        listView = (ListView) findViewById(R.id.lv_main);
        imagesList = new ArrayList<>();
        for (int i = 0; i < images.length; i++) {
            ArrayList<Image> itemList = new ArrayList<>();
            for (int j = 0; j <= i; j++) {
                itemList.add(new Image(images[j][0], Integer.parseInt(images[j][1]), Integer.parseInt(images[j][2])));
            }
            imagesList.add(itemList);
        }
        final FriendCircleAdapter adapter = new FriendCircleAdapter(FriendCircleActivity.this, imagesList);
        listView.setAdapter(adapter);

        /**创建一个MediaPlayer实例*/
        mediaPlayer = new MediaPlayer();
        /**初始化MediaPlayer对象*/
        initMediaPlayer();
        iv_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mediaPlayer.isPlaying()){
                    mediaPlayer.start(); //开始播放
                }else if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause(); //暂停播放
                }
            }
        });

    }

    /**初始化MediaPlayer对象*/
    private void initMediaPlayer() {
        //在SD卡的根目录下放置一个名为music.mp3的音频文件
        File file = new File(Environment.getExternalStorageDirectory(),"music.mp3");
        try {
            //调用这两个方法为MediaPlayer做好播放前的准备
            mediaPlayer.setDataSource(file.getPath());//通过创建一个File对象来指定音频文件的路径
            mediaPlayer.prepare(); //让MediaPlayer进入到准备状态
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void back(View view){
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mediaPlayer!=null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }
}