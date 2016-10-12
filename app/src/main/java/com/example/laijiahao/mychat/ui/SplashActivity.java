package com.example.laijiahao.mychat.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

import com.example.laijiahao.mychat.DemoHelper;
import com.example.laijiahao.mychat.R;
import com.example.laijiahao.mychat.utils.ImageUtils;
import com.example.laijiahao.mychat.utils.PrefUtils;
import com.hyphenate.chat.EMClient;

/**
 * Created by laijiahao on 16/9/15.
 */
public class SplashActivity extends BaseActivity {

    private static final int sleepTime = 2000;
    private ImageView rlRoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                startActivity(new Intent(getApplicationContext(),
//                          LoginActivity.class));
//                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
//              finish();
//            }
//        },2000);
        rlRoot = (ImageView) findViewById(R.id.iv_login);
        // 旋转, 缩放, 渐变
        // 旋转
        RotateAnimation animRotate = new RotateAnimation(0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        animRotate.setDuration(1000);
        animRotate.setFillAfter(true);

        // 缩放
        ScaleAnimation animScale = new ScaleAnimation(0, 1, 0, 1,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        animScale.setDuration(1000);
        animScale.setFillAfter(true);

        // 渐变
        AlphaAnimation animAlpha = new AlphaAnimation(0, 1);
        animAlpha.setDuration(2000);
        animAlpha.setFillAfter(true);

        // 动画集合
        AnimationSet animSet = new AnimationSet(true);
        animSet.addAnimation(animRotate);
        animSet.addAnimation(animScale);
        animSet.addAnimation(animAlpha);

        rlRoot.startAnimation(animSet);

        animSet.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // 判断是否需要跳到新手引导
                boolean isGuideShow = PrefUtils.getBoolean("is_guide_show",
                        false, getApplicationContext());

                if (isGuideShow) {
                    // 动画结束后跳登录页面
                    startActivity(new Intent(getApplicationContext(),
                            LoginActivity.class));
                } else {
                    // 跳到新手引导
                    startActivity(new Intent(getApplicationContext(),
                            GuideActivity.class));
                }

                finish();
            }
        });

        initBackGround();
    }

    private void initBackGround() {
        Bitmap bitMap = ImageUtils.decodeSampledBitmapFromResource(getResources(), R.drawable.fx_bg_welcome, 200, 200);
        BitmapDrawable bd = new BitmapDrawable(getResources(), bitMap);
        rlRoot.setBackground(bd);
    }

    @Override
    protected void onDestroy() {
        BitmapDrawable bd = (BitmapDrawable)rlRoot.getBackground();

        rlRoot.setBackgroundResource(0);//别忘了把背景设为null，避免onDraw刷新背景时候出现used a recycled bitmap错误

        bd.setCallback(null);
        bd.getBitmap().recycle();
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();

        new Thread(new Runnable() {
            public void run() {
                if (DemoHelper.getInstance().isLoggedIn()) {
                    // auto login mode, make sure all group and conversation is loaed before enter the main screen
                    long start = System.currentTimeMillis();
                    EMClient.getInstance().groupManager().loadAllGroups();
                    EMClient.getInstance().chatManager().loadAllConversations();
                    long costTime = System.currentTimeMillis() - start;
                    //wait
                    if (sleepTime - costTime > 0) {
                        try {
                            Thread.sleep(sleepTime - costTime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    //enter main screen
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                }else {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                    }
//                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
//                    finish();
                }
            }
        }).start();

    }

}

