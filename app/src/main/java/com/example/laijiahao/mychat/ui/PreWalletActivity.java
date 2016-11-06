package com.example.laijiahao.mychat.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import com.example.laijiahao.mychat.fragment.PasswordFragment;

/**
 * Created by laijiahao on 16/9/26.
 */
public class PreWalletActivity extends BaseActivity{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sp = getSharedPreferences("sp", Context.MODE_PRIVATE);
                String passwordStr = sp.getString("password","");
                //没有设置密码
                if(TextUtils.isEmpty(passwordStr)){
                    startActivity(new Intent(PreWalletActivity.this,PatternActivity.class));
                    finish();
                }else{
                    //密码检查
                    getSupportFragmentManager().beginTransaction().replace(android.R.id.content, PasswordFragment.newInstance(PasswordFragment.TYPE_CHECK)).commit();

                }
            }
        },500);
    }


}
