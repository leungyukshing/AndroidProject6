package com.example.lab6;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

public class MapActivity extends AppCompatActivity {
    // View
    private ImageView arrow;
    private TextView rotate;
    private TextView longtitude;
    private TextView latitude;

    private SensorManager sensorManager;
    private Sensor magnetic;
    private Sensor accelerometer;
    private SensorEventListener sensorEventListener;

    private LocationManager locationManager;
    private LocationListener locationListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map1);

        initView();
        initEvent();
        initData();
    }
    private void initData() {
        // Get Sensor Manager
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        // Get Sensors
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetic = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // Get Location Manager
        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        // Get location using network
        final String provider = LocationManager.GPS_PROVIDER;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        locationManager.requestLocationUpdates(provider, 2000, 10, locationListener);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        // Initialize primary longtitude and latitude
        updateLocation(location);
    }

    private void updateLocation(Location location) {
        if (location != null) {
            double lat = location.getLatitude();
            double lon = location.getLongitude();
            latitude.setText("经度：" + Double.toString((lat)));
            longtitude.setText("纬度：" + Double.toString(lon));
        }
    }

    private void initView(){
        arrow = findViewById(R.id.arrow);
        rotate = findViewById(R.id.rotate);
        longtitude = findViewById(R.id.longtitude);
        latitude = findViewById(R.id.latitude);
    }

    private void initEvent() {
        // Sensor Event Listener
        sensorEventListener = new SensorEventListener() {
            float[] accelerometerValue = new float[3];
            float[] magneticValue = new float[3];
            private float lastDegree;

            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    accelerometerValue = sensorEvent.values.clone();
                }
                else {
                    magneticValue = sensorEvent.values.clone();
                }
                // Animation
                float[] R = new float[9];
                float[] values = new float[3];
                SensorManager.getRotationMatrix(R, null, accelerometerValue, magneticValue);
                SensorManager.getOrientation(R, values);
                float rotateDegree = -(float)Math.toDegrees(values[0]);
                rotate.setText("旋转角度：" + Float.toString(rotateDegree));
                if (Math.abs(rotateDegree - lastDegree) > 1) {
                    RotateAnimation animation = new RotateAnimation(lastDegree, rotateDegree, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    animation.setFillAfter(true);
                    arrow.startAnimation(animation);
                    lastDegree = rotateDegree;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) { }
        };

        // Location Event Listener
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateLocation(location);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) { }

            @Override
            public void onProviderEnabled(String s) { }

            @Override
            public void onProviderDisabled(String s) { }
        };
    }

    @Override
    protected void onResume() {
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorEventListener, magnetic, SensorManager.SENSOR_DELAY_NORMAL);
        super.onResume();
    }

    @Override
    protected void onPause() {
        sensorManager.unregisterListener(sensorEventListener);
        super.onPause();
    }
}
