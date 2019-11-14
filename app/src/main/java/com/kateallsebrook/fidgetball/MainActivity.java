package com.kateallsebrook.fidgetball;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.TimeAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    //Global Objects
    ImageView ball;
    Point size;
    Vibrator vibrator;
    SensorManager sensorManager;
    Sensor sensor;
    SensorEventListener sensorEventListener;
    Timer tmr;
    TimerTask tmrTask;
    Timer tmrTxt;
    TimerTask tmrTxtTask;
    TextView sensText;
    TextView focusText;
    TextView instructionsText;
    TextView mindlessText;

    //Gyroscope Variables
    private static final float NS2S = 1.0f / 1000000000.0f;
    private final float[] deltaRotationVector = new float[4];

    //Global Variables
    float ballSpeedX, ballSpeedY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Default onCreate stuff
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Remove Status Bar
        removeStatusBar();

        //Initialize objects
        Display display = getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        //Tilt Control Sensors
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorEventListener = new SensorEventListener() {
           float timestamp;
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                Log.d("GYROLISTENER", Float.toString(sensorEvent.values[0]));
               if(timestamp!=0) {
                   ballSpeedY = 15 * sensorEvent.values[0];
                   ballSpeedX = 10 * sensorEvent.values[1];
               }
                timestamp = sensorEvent.timestamp;
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };

        sensorManager.registerListener(sensorEventListener, sensor, 1);

        //Ball + Animation
        ball = (ImageView) findViewById(R.id.ball);
        ball.setX(size.x / 2 - ball.getWidth()/2 -85);
        ball.setY(size.y / 2 - ball.getHeight()/2 + 35);

        //Initialize TextViews
        sensText = (TextView) findViewById(R.id.sensText);
        focusText = (TextView) findViewById(R.id.focusText);
        instructionsText = (TextView) findViewById(R.id.instructionsText);
        mindlessText = (TextView) findViewById(R.id.mindlessText);
    }

    //Touch to calibrate ball
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        ball.setX(event.getX() + ball.getWidth() - 250);
        ball.setY(event.getY() + ball.getHeight() - 250);
        return super.onTouchEvent(event);
    }


    //Volume Buttons
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        //Local Variables
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            //Up
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    if(ball.getY() > 0) {
                        ball.setY(ball.getY() - 50);
                        vibrator.vibrate(5);
                    }
                    else {
                        vibrator.vibrate(30);
                    }
                }
                return true;

            //Down
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    //Log.d("HTN", "IT WORKS!!! (down)");
                    if(ball.getY() + ball.getHeight() <= size.y - 30) {
                        ball.setY(ball.getY() + 50);
                        vibrator.vibrate(5);
                    }
                    else {
                        vibrator.vibrate(30);
                    }
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }

    //Override Methods
    /*@Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeStatusBar();
        tmrTask.cancel();

        makeTextviewsInvisibile();
    }

    @Override
    protected void onStop() {
        super.onStop();
        removeStatusBar();
        tmrTask.cancel();

        makeTextviewsInvisibile();
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeStatusBar();
        tmrTask.cancel();

        makeTextviewsInvisibile();
    }

    @Override
    protected void onResume() {
        super.onResume();
        removeStatusBar();

        //Gyro Ball Moving
        tmr = new Timer();
        tmrTask = new TimerTask() {
            public void run() {
                Log.d("TICKER", Float.toString(ballSpeedX));
                if(ball.getWidth() + ball.getX() + 10 < size.x && ball.getX() > 0) {
                    ball.setX(ball.getX() + ballSpeedX);
                }
                else {
                    if(ball.getX() < 0) {
                        ball.setX(ball.getX() + 40);
                    }
                    else {
                        ball.setX(ball.getX() - 40);
                    }
                    vibrator.vibrate(30);
                }
                if(ball.getHeight() + ball.getY() + 10 < size.y && ball.getY() > 0) {
                    ball.setY(ball.getY() + ballSpeedY);
                }
                else {
                    if(ball.getY() < 0) {
                        ball.setY(ball.getY() + 40);
                    }
                    else {
                        ball.setY(ball.getY() - 40);
                    }
                    vibrator.vibrate(30);
                }
            }
        };

        tmr.schedule(tmrTask,10,10);

        //Text Stuff
        tmrTxt = new Timer();

    final Animation out = new AlphaAnimation(1.0f,0.0f);
    out.setStartOffset(10000);
    out.setDuration(2000);
    out.setAnimationListener(new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            makeTextviewsInvisibile();
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }

    });

        if(sensText.getVisibility() == View.VISIBLE) {
            sensText.startAnimation(out);
            focusText.startAnimation(out);
            instructionsText.startAnimation(out);
            mindlessText.startAnimation(out);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        removeStatusBar();
    }

    public void removeStatusBar() {
        //Hide the status bar.
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    public void makeTextviewsInvisibile() {
        sensText.setVisibility(View.INVISIBLE);
        focusText.setVisibility(View.INVISIBLE);
        instructionsText.setVisibility(View.INVISIBLE);
        mindlessText.setVisibility(View.INVISIBLE);
    }
}



