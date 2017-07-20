package com.tongli.imu;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    FileOutputStream mFout;
    OutputStreamWriter mOutWriter;
    private SensorManager mSensorManager;
    private Sensor sensor;
    private Sensor sensorM;
    private Sensor sensorG;

    private final float[] mAccelerometerReading = new float[3];
    private final float[] mMagnetometerReading = new float[3];
    private final float[] mGyroReading = new float[3];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {


            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

            return;
        }

        try {
            File path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS);
            File myFile = new File(path, "straightLine_CalInertialAndMag.csv");
            mFout = new FileOutputStream(myFile,true);
            mOutWriter = new OutputStreamWriter(mFout);
            mOutWriter.append("Packet number,Gyroscope X (deg/s),Gyroscope Y (deg/s),Gyroscope Z (deg/s),Accelerometer X (g),Accelerometer Y (g),Accelerometer Z (g),Magnetometer X (G),Magnetometer Y (G),Magnetometer Z (G)\n");
            mOutWriter.close();
            mFout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mSensorManager = (SensorManager) getSystemService(this.SENSOR_SERVICE);
        sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorM = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensorManager.registerListener(this, sensor,
                SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, sensorM,
                SensorManager.SENSOR_DELAY_FASTEST);
        sensorG = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorManager.registerListener(this, sensorG,
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        try {
            mOutWriter.close();
            mFout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mHandler.removeCallbacksAndMessages(null);
        super.onPause();
    }
    Handler mHandler;
    int mDelay;
    long mTime;
    int mFreq = 350;
    int mCount = 0;
    @Override
    protected void onResume() {
        super.onResume();
        try {
            File path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS);
            File myFile = new File(path, "straightLine_CalInertialAndMag.csv");
            mFout = new FileOutputStream(myFile,true);
            mOutWriter = new OutputStreamWriter(mFout);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mHandler = new Handler();
        mDelay = 1000/mFreq; //milliseconds

        mTime = System.currentTimeMillis();
        mHandler.postDelayed(new Runnable(){
            public void run(){
                //do something
                long cur = System.currentTimeMillis();
                int times = (int)(cur - mTime) * mFreq / 1000;
                if(times > 1) {
                    for(int i = 0; i < times; i++) {
                        writeToFile();
                    }
                    mTime = cur;
                }
                mHandler.postDelayed(this, mDelay);
            }
        }, mDelay);
    }



    private void writeToFile() {
        String contents = String.valueOf(mCount) + "," +
                String.valueOf(Math.toDegrees(mGyroReading[0])) + "," +
                String.valueOf(Math.toDegrees(mGyroReading[1])) + "," +
                String.valueOf(Math.toDegrees(mGyroReading[2])) + "," +
                String.valueOf(mAccelerometerReading[0]/9.81) + "," +
                String.valueOf(mAccelerometerReading[1]/9.81) + "," +
                String.valueOf(mAccelerometerReading[2]/9.81) + "," +
                String.valueOf(mMagnetometerReading[0]) + "," +
                String.valueOf(mMagnetometerReading[1]) + "," +
                String.valueOf(mMagnetometerReading[2]) + "\n";
        mCount += 1;
        try {
            mOutWriter.append(contents);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
        // You must implement this callback in your code.
    }

    // Get readings from accelerometer and magnetometer. To simplify calculations,
    // consider storing these readings as unit vectors.
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, mAccelerometerReading,
                    0, mAccelerometerReading.length);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, mMagnetometerReading,
                    0, mMagnetometerReading.length);
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            System.arraycopy(event.values, 0, mGyroReading,
                    0, mGyroReading.length);
        }
    }
}
