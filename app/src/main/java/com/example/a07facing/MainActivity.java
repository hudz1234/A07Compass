package com.example.a07facing;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private String direction;
    private String dir[] = {"North", "Northeast", "East", "Southeast", "South", "Southwest", "West", "Northwest", "North"};
    private TextToSpeech tts;
    private ImageView imageView;
    private TextView textView;
    private float rotateFrom = 0;
    private float rotateTo = 45;
    private float currentDegree = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);
        FloatingActionButton fab = findViewById(R.id.fab);
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.US);

                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        // language not supported
                    } else {
                        // language supported
                    }

                } else {
                    // initialization failed
                }
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View imageView) {
                rotate((ImageView) imageView);
                textView.setText(rotateFrom + " Degrees");
                speakOut();
            }
        });
    }

    //@Override
    private void rotate(ImageView caller) {
        RotateAnimation r = new RotateAnimation(rotateFrom, rotateTo, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        r.setDuration((long) 2 * 500);
        r.setRepeatCount(0);
        r.setFillAfter(true);
        caller.startAnimation(r);
        rotateFrom = rotateTo;
        if (rotateTo != 360)
            rotateTo += 45;
        else
            rotateTo = 45;
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);

    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    float[] gravity = new float[3];
    float[] magnetic = new float[3];
    float[] rotationMatrix = new float[9];
    float[] orientationValues = new float[3];

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravity = event.values.clone();
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magnetic = event.values.clone();
        }

        boolean success = SensorManager.getRotationMatrix(rotationMatrix, null, gravity, magnetic);
        if (success) {
            SensorManager.getOrientation(rotationMatrix, orientationValues);
            float azimuthInRadians = orientationValues[0];
            float azimuthInDegrees = (float) (Math.toDegrees(azimuthInRadians) + 360) % 360;

            textView.setText("Heading: " + Float.toString(azimuthInDegrees) + " degrees");

            // create a rotation animation (reverse turn degree degrees)
            RotateAnimation ra = new RotateAnimation(
                    currentDegree,
                    -azimuthInDegrees,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f);

            // how long the animation will take place
            ra.setDuration(210);

            // set the animation after the end of the reservation status
            ra.setFillAfter(true);

            // Start the animation
            imageView.startAnimation(ra);
            currentDegree = -azimuthInDegrees;
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    // my function for image rotation
// use a fixed value (45 degrees) here for later expansion
    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
    //@Override
    private void speakOut() {
        int degree = Math.round(currentDegree);
        String direction = "";
        int directionIndex = (int) ((currentDegree + 22.5) / 45);
        direction = dir[directionIndex];
        if (degree >= 337.5 || degree < 22.5) {
            direction = "North";
        } else if (degree >= 22.5 && degree < 67.5) {
            direction = "Northeast";
        } else if (degree >= 67.5 && degree < 112.5) {
            direction = "East";
        } else if (degree >= 112.5 && degree < 157.5) {
            direction = "Southeast";
        } else if (degree >= 157.5 && degree < 202.5) {
            direction = "South";
        } else if (degree >= 202.5 && degree < 247.5) {
            direction = "Southwest";
        } else if (degree >= 247.5 && degree < 292.5) {
            direction = "West";
        } else if (degree >= 292.5 && degree < 337.5) {
            direction = "Northwest";
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(
                    "You are facing " + direction,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    null
            );
        } else {
            HashMap<String, String> map = new HashMap<>();
            map.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_NOTIFICATION));
            tts.speak("You are facing " + direction, TextToSpeech.QUEUE_FLUSH, map);
        }
    }


}
