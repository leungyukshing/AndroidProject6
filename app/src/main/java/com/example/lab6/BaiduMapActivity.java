package com.example.lab6;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.baidu.location.LocationClient;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;

public class BaiduMapActivity extends AppCompatActivity {
    private Sensor accelerometer;
    private Sensor magnetic;
    private SensorManager sensorManager;
    private LocationManager locationManager;
    private SensorEventListener sensorEventListener;
    private LocationListener locationListener;
    private BaiduMap.OnMapStatusChangeListener onMapStatusChangeListener;
    private Location location;
    private float degree;

    private MapView mMapView = null;
    private ImageButton locate;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_map1);

        // Get current location
        LocationClient locationClient = new LocationClient(getApplicationContext());
        locationClient.start();
        locationClient.start();
        locationClient.requestLocation();

        initView();
        initEvent();
        initData();
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorEventListener, magnetic, SensorManager.SENSOR_DELAY_NORMAL);

        // Get Location Manager
        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        // Use GPS location
        final String provider = LocationManager.GPS_PROVIDER;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(provider, 2000, 10, locationListener);
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        // Convert Coordinates
        BaiduMap baiduMap = mMapView.getMap();
        // Add Listener
        baiduMap.setOnMapStatusChangeListener(onMapStatusChangeListener);
        // normal map
        baiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        baiduMap.setMyLocationEnabled(true);

        CoordinateConverter converter = new CoordinateConverter();
        converter.from(CoordinateConverter.CoordType.GPS);
        converter.coord(new LatLng(location.getLatitude(), location.getLongitude()));
        LatLng destinationLatLng = converter.convert();

        // Set current location
        degree = 90.0f;
        mMapView.getMap().setMyLocationEnabled(true);
        MyLocationData data = new MyLocationData.Builder()
                .latitude(destinationLatLng.latitude)
                .longitude(destinationLatLng.longitude)
                .direction(degree).build();
        mMapView.getMap().setMyLocationData(data);

        // Center the map
        MapStatus mapStatus = new MapStatus.Builder().target(destinationLatLng).build();
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
        mMapView.getMap().setMapStatus(mapStatusUpdate);
    }

    private void initView() {
        mMapView = findViewById(R.id.bmapView);
        locate = findViewById(R.id.locate);
    }

    private void initEvent() {
        // Sensor Event Listener
        sensorEventListener = new SensorEventListener() {
            float[] acceleromterValues = new float[3];
            float[] magneticValues = new float[3];
            private float lastDegree;

            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    acceleromterValues = sensorEvent.values.clone();
                }
                else if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                    magneticValues = sensorEvent.values.clone();
                }

                float[] R = new float[9];
                float[] values = new float[3];
                SensorManager.getRotationMatrix(R, null, acceleromterValues, magneticValues);
                SensorManager.getOrientation(R, values);
                float rotateDegree = -(float)Math.toDegrees(values[0]);
                if (Math.abs(rotateDegree - lastDegree) > 1) {
                    lastDegree = rotateDegree;
                    degree = rotateDegree;
                    if (location != null) {
                        setMarker(location, degree);
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) { }
        };

        // Location Event Listener
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location loca) {
                location = loca;
                setMarker(location, degree);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) { }

            @Override
            public void onProviderEnabled(String s) { }

            @Override
            public void onProviderDisabled(String s) { }
        };

        // Button Click Listener
        locate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Convert Coordinate
                CoordinateConverter converter = new CoordinateConverter();
                converter.from(CoordinateConverter.CoordType.GPS);
                converter.coord(new LatLng(location.getLatitude(), location.getLongitude()));
                LatLng destinationLatLng = converter.convert();

                // Set Middle
                MapStatus mapStatus = new MapStatus.Builder().target(destinationLatLng).build();
                MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
                mMapView.getMap().setMapStatus(mapStatusUpdate);

                // Change Image
                locate.setImageResource(R.drawable.center);
            }
        });

        // Map Change Event Listener
        onMapStatusChangeListener = new BaiduMap.OnMapStatusChangeListener() {
            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus) {

            }

            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus, int i) {

            }

            @Override
            public void onMapStatusChange(MapStatus mapStatus) {

            }

            @Override
            public void onMapStatusChangeFinish(MapStatus status) {
                // Convert Coordinate
                CoordinateConverter converter = new CoordinateConverter();
                converter.from(CoordinateConverter.CoordType.GPS);
                converter.coord(new LatLng(location.getLatitude(), location.getLongitude()));
                LatLng destinationLatLng = converter.convert();

                MapStatus mapStatus = new MapStatus.Builder().target(destinationLatLng).build();
                if (!status.equals(mapStatus)) {
                    locate.setImageResource(R.drawable.define);
                }
            }
        };
    }
    private void initData() {
        // Get Sensor Manager
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetic = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // Get Location Manager
        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        final String provider = LocationManager.GPS_PROVIDER;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(provider, 2000, 10, locationListener);
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            setMarker(location, degree);
        }
    }
    private void setMarker(Location location, float degree) {
        // Convert Coordinates
        CoordinateConverter converter = new CoordinateConverter();
        converter.from(CoordinateConverter.CoordType.GPS);
        converter.coord(new LatLng(location.getLatitude(), location.getLongitude()));
        LatLng destinationLatLng = converter.convert();

        mMapView.getMap().setMyLocationEnabled(true);
        MyLocationData data = new MyLocationData.Builder()
                .latitude(destinationLatLng.latitude)
                .longitude(destinationLatLng.longitude)
                .direction(degree).build();
        mMapView.getMap().setMyLocationData(data);

        // Set Marker icon
        Bitmap bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),  R.drawable.timg), 100, 100, true);
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);
        MyLocationConfiguration configuration = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, bitmapDescriptor);
        mMapView.getMap().setMyLocationConfiguration(configuration);
    }

    @Override
    protected void onResume() {
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorEventListener, magnetic, SensorManager.SENSOR_DELAY_NORMAL);
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        sensorManager.unregisterListener(sensorEventListener);
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        sensorManager.unregisterListener(sensorEventListener);
    }
}
