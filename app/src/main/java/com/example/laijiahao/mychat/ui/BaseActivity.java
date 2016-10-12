package com.example.laijiahao.mychat.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.laijiahao.mychat.utils.ActivityCollector;

/**
 * Created by laijiahao on 16/9/7.
 */
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
}
