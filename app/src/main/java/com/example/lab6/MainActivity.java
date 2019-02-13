package com.example.lab6;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;

public class MainActivity extends AppCompatActivity {
    private SensorManager sensorManager;
    private SensorEventListener sensorEventListener;
    private Sensor sensor;
    private ImageView imageUp;
    private ImageView imageDown;
    private AnimationSet downAnimationSet;
    private AnimationSet upAnimationSet;

    private boolean flag = true;
    private Vibrator vibrator;
    private SoundPool soundPool;
    private int soundId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initData();
        initEvent();
    }

    @Override
    protected void onPause() {
        // Unregister Shaking Event
        sensorManager.unregisterListener(sensorEventListener);
        super.onPause();
    }

    @Override
    protected void onResume() {
        // Register Shaking Event
        sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        super.onResume();
    }

    private void initView() {
        imageUp = (ImageView) findViewById(R.id.image_up);
        imageDown = (ImageView)findViewById(R.id.image_down);
    }
    private void initData() {
        // Get Sensor Manager
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        // Get Accelerometer
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // Initialize SoundPool
        soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
        // Load Music
        soundId = soundPool.load(this, R.raw.shake, 1);
        // Get Vibrate Service
        vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);

        upAnimationSet = new AnimationSet(true);
        // Up Image
        // Move up
        TranslateAnimation upUptranslateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, -0.5f);
        // Set time
        upUptranslateAnimation.setDuration(300);
        // Move down
        TranslateAnimation upDownTranslateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, -0.5f, Animation.RELATIVE_TO_SELF, 0);
        // Set time
        upDownTranslateAnimation.setDuration(300);

        // Set Start Delay
        upDownTranslateAnimation.setStartOffset(300);
        // construct animation set
        upAnimationSet.addAnimation(upUptranslateAnimation);
        upAnimationSet.addAnimation(upDownTranslateAnimation);
        upAnimationSet.setDuration(800);
        upAnimationSet.setStartOffset(200);

        downAnimationSet = new AnimationSet(true);

        // Down Image
        // Move up
        TranslateAnimation downUptranslateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0);
        downUptranslateAnimation.setDuration(300);
        downUptranslateAnimation.setStartOffset(300);
        // Move down
        TranslateAnimation downDowntranslateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0.5f);
        downDowntranslateAnimation.setDuration(300);
        downAnimationSet.addAnimation(downDowntranslateAnimation);
        downAnimationSet.addAnimation(downUptranslateAnimation);
        downAnimationSet.setDuration(800);
        downAnimationSet.setStartOffset(200);
    }
    private void initEvent() {
        // Sensor Event Listener
        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                float[] values = sensorEvent.values;
                float x = values[0];
                float y = values[1];
                float z = values[2];
                // Whether a shaking action
                if (x > 9) {
                    if (flag) {
                        System.out.println("Shaking!!");
                        // Play Animation
                        imageUp.startAnimation(upAnimationSet);
                        imageDown.startAnimation(downAnimationSet);
                        // Play music
                        soundPool.play(soundId, 1.0f, 1.0f, 1, 1,1.0f);
                        // Vibrate
                        vibrator.vibrate(new long[]{0, 300}, -1);
                        // Toast
                        Toast.makeText(MainActivity.this, "摇一摇", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) { }
        };

        // Animation Listener
        upAnimationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                flag = false;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                flag = true;
                //Intent intent = new Intent(MainActivity.this, MapActivity.class);
                Intent intent = new Intent(MainActivity.this, BaiduMapActivity.class);
                startActivity(intent);
            }

            @Override
            public void onAnimationRepeat(Animation animation) { }
        });
    }
}
