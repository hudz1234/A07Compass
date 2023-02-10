package com.example.a07facing;

import static java.util.Collections.rotate;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
    View rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rootView = getWindow().getDecorView();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);
        FloatingActionButton fab = findViewById(R.id.fab);
        Button button2 = findViewById(R.id.button2);
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
            public void onClick(View view) {
                rotate((ImageView) fab);
                textView.setText(rotateFrom + " Degrees");
                speakOut();
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rootView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.teal_700));
            }
        });


    }
        private void rotate(ImageView caller) {
        // remove the animation
        caller.clearAnimation();

        // set the rotation of the image based on the currentDegree variable
        caller.setRotation(-currentDegree);

        // set the text view to show the current degree
        textView.setText(String.format(Locale.US, "%.2f Degrees", currentDegree));

        // speak out the direction
        speakOut();
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
            SensorManager.getOrientation(rotationMatrix, orientationValues);
            currentDegree = (float) Math.toDegrees(orientationValues[0]);
            currentDegree = (currentDegree + 360) % 360;
            textView.setText("Heading: " + Float.toString(currentDegree) + " degrees");

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
