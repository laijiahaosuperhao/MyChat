package com.example.laijiahao.mychat.ui;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.laijiahao.mychat.R;
import com.example.laijiahao.mychat.adapter.ImageAdapter;
import com.example.laijiahao.mychat.domain.FolderBean;
import com.example.laijiahao.mychat.widget.ListImageDirPopupWindow;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ImageloaderActivity extends BaseActivity {

    private GridView mGridView;
    private List<String> mImgs;
    private ImageAdapter mImgAdapter;

    private RelativeLayout mBottomLy;
    private TextView mDirName;
    private TextView mDirCount;

    private File mCurrentDir;
    private int mMaxCount;

    //在扫描完成以后，mFolderBeans会完全赋上值
    private List<FolderBean> mFolderBeans = new ArrayList<FolderBean>();

    //显示进度的对话框
    private ProgressDialog mProgressDialog;

    private static final int DATA_LOADED = 0x110;

    private ListImageDirPopupWindow mDirPopupWindow;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == DATA_LOADED){
               mProgressDialog.dismiss();
                //绑定数据到view中，为GridView去创建数据
                data2View();

                initDirPopupWindow();
            }
        }
    };

    private void initDirPopupWindow() {
        mDirPopupWindow  =new ListImageDirPopupWindow(this,mFolderBeans);
        mDirPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                lightOn();
            }
        });

        mDirPopupWindow.setOnDirSelectedListener(new ListImageDirPopupWindow.OnDirSelectedListener() {
            @Override
            public void onSelected(FolderBean folderBean) {
                //更新Adapter
                mCurrentDir = new File(folderBean.getDir());

                mImgs = Arrays.asList(mCurrentDir.list(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String filename) {
                        if(filename.endsWith(".jpg")
                                ||filename.endsWith(".jpeg")
                                ||filename.endsWith(".png"))
                            return true;
                        return false;
                    }
                }));

                mImgAdapter = new ImageAdapter(ImageloaderActivity.this,mImgs,mCurrentDir.getAbsolutePath());

                mGridView.setAdapter(mImgAdapter);

                mDirCount.setText(mImgs.size()+"");
                mDirName.setText(folderBean.getName());
                mDirPopupWindow.dismiss();

            }
        });

    }

    //内容区域变亮，设置透明度给windowManager
    private void lightOn() {

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 1.0f;
        getWindow().setAttributes(lp);
    }

    private void data2View() {
        if (mCurrentDir == null)
        {
            Toast.makeText(getApplicationContext(), "未扫描到任何图片",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        mImgs = Arrays.asList(mCurrentDir.list());
        mImgAdapter = new ImageAdapter(this,mImgs,mCurrentDir.getAbsolutePath());
        mGridView.setAdapter(mImgAdapter);

        mDirCount.setText(mMaxCount + "");
        mDirName.setText(mCurrentDir.getName());

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imageloader);

        //初始化所有控件
        initView();

        //开启一个线程，遍历外部存储卡上所有的图片，
        // 得到图片以后，通过handler通知主线程去更新initView中初始化过的控件
        initDatas();

        //初始化事件，RelativeLayout中的点击事件，传出popwindow
        initEvent();

    }

    private void initEvent() {

        mBottomLy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDirPopupWindow.setAnimationStyle(R.style.dir_popupwindow_anim);
                /**
                 设置popwindow显示的位置
                 以触发弹出窗的view为基准，出现在view的正下方，弹出的pop_view左上角正对view的左下角  偏移量默认为0,0
                 有参数的话，就是一view的左下角进行偏移，xoff正的向左，负的向右. yoff没测，也应该是正的向下，负的向上
                 */
                mDirPopupWindow.showAsDropDown(mBottomLy,0,0);

                lightOff();

            }

        });
    }

    //内容区域变暗
    private void lightOff() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.3f;
        getWindow().setAttributes(lp);
    }

    //利用ContentProvider扫描手机中的所有图片
    private void initDatas() {
        //首先判断外部的存储卡
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "当前存储卡不可用！", Toast.LENGTH_SHORT).show();
            return;
        }

        mProgressDialog = ProgressDialog.show(this, null, "正在加载...");

        //开启一个线程扫描手机中的图片
        new Thread() {
            @Override
            public void run() {
                super.run();
                //在run方法中开始扫描
                Uri mImgUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                ContentResolver cr = ImageloaderActivity.this.getContentResolver();

                Cursor cursor = cr.query(mImgUri, null, MediaStore.Images.Media.MIME_TYPE + " = ? or "
                                + MediaStore.Images.Media.MIME_TYPE + "=?",
                        new String[]{"image/jpeg", "image/png"},
                        MediaStore.Images.Media.DATE_MODIFIED);

                //使用一个set去存储遍历过的parentFile的path,防止重复遍历
                Set<String> mDirPaths = new HashSet<String>();

                while (cursor.moveToNext()) {
                    //图片的路径
                    String path = cursor.getString(cursor
                            .getColumnIndex(MediaStore.Images.Media.DATA));
                    File parentFile = new File(path).getParentFile();
                    //如果等于null，结束当前循环，进行下一次循环
                    if (parentFile == null) {
                        continue;
                    }

                    String dirPath = parentFile.getAbsolutePath();

                    FolderBean folderBean = null;

                    //防止重复扫描
                    if (mDirPaths.contains(dirPath)) {
                        //如果包含，直接结束，代表当前的文件夹扫描过了
                        continue;
                    } else {
                        //如果不包含，把当前文件夹的路径加入到path中
                        mDirPaths.add(dirPath);
                        folderBean = new FolderBean();
                        //setDir以后，它的dir和name都有值了
                        folderBean.setDir(dirPath);
                        //直接设置为path，因为每个文件夹只会有一张图片去得到它，其它的都会走continue
                        folderBean.setFirstImagePath(path);
                    }

                    //通过ParentFile的list拿到它的图片个数，返回一个string的数组
                    //首先进行判空，
                    if (parentFile.list() == null) {
                        continue;
                    }

                    //需要过滤一下，因为我们需要的是图片的数量，有可能parentFile下包含一些其他的文件
                    int picSize = parentFile.list(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {
                            if (name.endsWith(".jpg")
                                    || name.endsWith(".jpeg") || name.endsWith(".png")) {
                                return true;
                            }
                            return false;
                        }
                    }).length;

                    folderBean.setCount(picSize);
                    //mFolderBeans完全是为了popwindow去进行初始化
                    mFolderBeans.add(folderBean);

                    if(picSize > mMaxCount){
                        mMaxCount = picSize; //显示当前文件夹图片的数量
                       mCurrentDir = parentFile; //显示当前文件夹
                    }

                }
                cursor.close();
//                //扫描完成，释放临时变量的内存,,mDirPaths声明在set方法里面，set方法结束以后它的内存是自动回收的。
//                mDirPaths = null;

                //通知handler扫描图片完成
                mHandler.sendEmptyMessage(DATA_LOADED);

            }
        }.start();
    }

    private void initView() {
        mGridView = (GridView) findViewById(R.id.id_gridView);
        mBottomLy = (RelativeLayout) findViewById(R.id.id_bottom_ly);
        mDirName = (TextView) findViewById(R.id.id_dir_name);
        mDirCount = (TextView) findViewById(R.id.id_dir_count);
    }



}
