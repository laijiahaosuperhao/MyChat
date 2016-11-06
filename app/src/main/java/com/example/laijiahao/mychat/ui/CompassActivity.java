package com.example.laijiahao.mychat.ui;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.example.laijiahao.mychat.R;

/**
 * Created by laijiahao on 16/9/23.
 */
public class CompassActivity extends BaseActivity{

    private SensorManager sensorManager;
    private ImageView compassImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);
        compassImg = (ImageView) findViewById(R.id.compass_img);
        //获取SensorManager的实例
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //获取地磁传感器的实例
        Sensor magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        //获取加速度传感器的实例
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //注册监听器
        sensorManager.registerListener(listener,magneticSensor,SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(listener,accelerometerSensor,SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(sensorManager!=null){
            sensorManager.unregisterListener(listener);
        }
    }

    private SensorEventListener listener = new SensorEventListener() {

        float[] accelerometerValues = new float[3];
        float[] magneticValues = new float[3];
        private float lastRotateDegree;

        @Override
        public void onSensorChanged(SensorEvent event) {
           //判断当前是加速度传感器还是地磁传感器
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                //注意赋值时要调用clone()方法
                accelerometerValues = event.values.clone();
            }else if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
                //注意赋值时要调用clone()方法
                magneticValues = event.values.clone();
            }

            float[] R = new float[9];
            float[] values = new float[3];
            SensorManager.getRotationMatrix(R,null,accelerometerValues,magneticValues);
            SensorManager.getOrientation(R,values);
            //value[0]表示手机围绕z轴旋转的弧度
            Log.d("CompassActivity","value[0] is" + Math.toDegrees(values[0]));
            //将计算出的旋转角度取反，用于旋转指南针背景图
            float rotateDegree = -(float)Math.toDegrees(values[0]);
            if(Math.abs(rotateDegree - lastRotateDegree)>1){
                RotateAnimation animation = new RotateAnimation
                        (lastRotateDegree, rotateDegree, Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
                animation.setFillAfter(true);
                compassImg.startAnimation(animation);
                lastRotateDegree = rotateDegree;
            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
}
