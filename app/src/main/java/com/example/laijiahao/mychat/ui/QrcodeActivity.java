package com.example.laijiahao.mychat.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.laijiahao.mychat.R;
import com.example.laijiahao.mychat.constant.Constant;
import com.xys.libzxing.zxing.encoding.EncodingUtils;

import java.io.FileNotFoundException;

/**
 * Created by laijiahao on 16/10/17.
 */
public class QrcodeActivity extends BaseActivity {

    private ImageView iv_qr_image;
    private boolean mode;
    private Uri imageUri;
    private Bitmap bitmap;
    private String imagePathOK;
    private Bitmap qrCodeBitmap;
    private String tel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);

        mode = getSharedPreferences(Constant.USERINFO_FILENAME,
                Context.MODE_PRIVATE).getBoolean("mode", false);
        if (!mode) {
            imagePathOK = getSharedPreferences(Constant.USERINFO_FILENAME,
                    Context.MODE_PRIVATE).getString("imagePath", "");

        } else {
            imageUri = Uri.parse(getSharedPreferences(Constant.USERINFO_FILENAME,
                    Context.MODE_PRIVATE).getString("imageUri", ""));
            try {
                bitmap = BitmapFactory.decodeStream
                        (getContentResolver().openInputStream(imageUri));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        tel = getSharedPreferences(Constant.USERINFO_FILENAME, Context.MODE_PRIVATE).getString("tel", "laijiahaosuperhao");
        iv_qr_image = (ImageView) this.findViewById(R.id.iv_qr_image);
        if (!TextUtils.isEmpty(tel)) {
            if (!TextUtils.isEmpty(imagePathOK)) {
                //根据字符串生成二维码图片并显示在界面上，第二个参数为图片的大小（666*666）
                qrCodeBitmap = EncodingUtils.createQRCode(tel, 666, 666,
                        BitmapFactory.decodeFile(imagePathOK));
            } else if (imageUri != null) {
                if (TextUtils.isEmpty(imageUri.toString())) {
                    try {
                        qrCodeBitmap = EncodingUtils.createQRCode(tel, 666, 666,
                                BitmapFactory.decodeStream
                                        (getContentResolver().openInputStream(imageUri)));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (qrCodeBitmap == null) {
                //根据字符串生成二维码图片并显示在界面上，第二个参数为图片的大小（350*350）
                qrCodeBitmap = EncodingUtils.createQRCode(tel, 600, 600,
                        BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
            }
            iv_qr_image.setImageBitmap(qrCodeBitmap);
        } else {
            Toast.makeText(QrcodeActivity.this, "Tel can not be empty", Toast.LENGTH_SHORT).show();
        }


    }

}
