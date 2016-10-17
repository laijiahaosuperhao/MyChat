package com.example.laijiahao.mychat.ui;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.laijiahao.mychat.R;
import com.example.laijiahao.mychat.constant.Constant;
import com.example.laijiahao.mychat.widget.AlertDialog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by laijiahao on 16/9/23.
 */

public class ProfileActivity extends BaseActivity implements View.OnClickListener{

    private static final int TAKE_PHOTO = 1;
    private static final int CROP_PHOTO = 2;
    private static final int CHOOSE_PHOTO = 3;
    private RelativeLayout re_avatar;
    private ImageView iv_avatar;
    private Uri imageUri;
    private Bitmap bitmap;
    private String imagePath;

    private boolean mode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myinfo);
        re_avatar = (RelativeLayout)this.findViewById(R.id.re_avatar);
        iv_avatar = (ImageView) this.findViewById(R.id.iv_avatar);
        re_avatar.setOnClickListener(this);
        mode = getSharedPreferences(Constant.USERINFO_FILENAME,
                Context.MODE_PRIVATE).getBoolean("mode", false);
        if(!mode){
            String imagePathOK = getSharedPreferences(Constant.USERINFO_FILENAME,
                    Context.MODE_PRIVATE).getString("imagePath", "laijiahaosuperhao");
            displayImage(imagePathOK);
            imagePath=imagePathOK;
        }else{
            imageUri = Uri.parse(getSharedPreferences(Constant.USERINFO_FILENAME,
                    Context.MODE_PRIVATE).getString("imageUri", "laijiahaosuperhao"));
            try {
                bitmap = BitmapFactory.decodeStream
                        (getContentResolver().openInputStream(imageUri));
                iv_avatar.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.re_avatar:
                showPhotoDialog();
                break;
        }
    }

    private void showPhotoDialog() {

        List<String> items = new ArrayList<String>();
        items.add("拍照");
        items.add("相册");
        final AlertDialog fxAlertDialog = new AlertDialog(ProfileActivity.this, null, items);
        fxAlertDialog.init(new AlertDialog.OnItemClickListner() {
            @Override
            public void onClick(int position) {
                switch (position) {
                    case 0:
                        mode =true;
                        //创建File对象,用于存储拍照后的图片
                        File outputImage = new File(Environment.getExternalStorageDirectory(),"output_image.jpg");
                        try{
                            if(outputImage.exists()){
                                outputImage.delete();
                            }
                            outputImage.createNewFile();
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                        imageUri = Uri.fromFile(outputImage);
                        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
                        startActivityForResult(intent,TAKE_PHOTO);//启动相机程序
                        break;
                    case 1:
                        mode =false;
                        Intent intent1 = new Intent("android.intent.action.GET_CONTENT");
                        intent1.setType("image/*");
                        startActivityForResult(intent1,CHOOSE_PHOTO); //打开相册
                        break;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case TAKE_PHOTO:
                if(resultCode == RESULT_OK){
                    Intent intent = new Intent("com.android.camera.action.CROP");
                    intent.setDataAndType(imageUri,"image/*");
                    intent.putExtra("scale",true);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
                    startActivityForResult(intent,CROP_PHOTO); //启动裁剪程序
                }
                break;
            case CROP_PHOTO:
                if(resultCode == RESULT_OK){
                    try {
                        bitmap = BitmapFactory.decodeStream
                                (getContentResolver().openInputStream(imageUri));
                        iv_avatar.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case CHOOSE_PHOTO:
                if(resultCode == RESULT_OK){
                    //判断手机系统版本号
                    if(Build.VERSION.SDK_INT>=19){
                        //4.4及以上系统使用这个方法处理图片
                        handleImageOnKitKat(data);
                    }else {
                        //4.4以下系统使用这个方法处理图片
                        handleImageBeforeKitKat(data);
                    }
                }
                break;
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void handleImageOnKitKat(Intent data) {
        Uri uri = data.getData();
        if(DocumentsContract.isDocumentUri(this,uri)){
            //如果是document类型的Uri,则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())){
                String id = docId.split(":")[1]; //解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" +id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection);
            }else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri,null);
            }

        }else if ("content".equalsIgnoreCase(uri.getScheme())){
            //如果不是document类型的Uri,则使用普通方式处理
            imagePath = getImagePath(uri,null);
        }
        displayImage(imagePath); //根据图片路径显示图片
    }

    private void displayImage(String imagePath) {
        if(imagePath != null){
            bitmap = BitmapFactory.decodeFile(imagePath);
            iv_avatar.setImageBitmap(bitmap);
        }else{
            Toast.makeText(this,"failed to get image",Toast.LENGTH_SHORT).show();
        }
    }


    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        imagePath = getImagePath(uri,null);
        displayImage(imagePath);
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        //通过Uri和selection来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri,null,selection,null,null);
        if(cursor!=null){
            if(cursor.moveToFirst()){
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mode){
            getSharedPreferences(Constant.USERINFO_FILENAME, MODE_PRIVATE).edit().putString("imageUri", String.valueOf(imageUri)).commit();

        }else{
            getSharedPreferences(Constant.USERINFO_FILENAME, MODE_PRIVATE).edit().putString("imagePath", imagePath).commit();

        }
        getSharedPreferences(Constant.USERINFO_FILENAME, MODE_PRIVATE).edit().putBoolean("mode", mode).commit();

    }

    public void back(View view) {
        finish();
    }
}
