package vcmsa.projects.drive;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {

    SharedPreferences sp;
    RelativeLayout layoutJoystick;
    TextView textViewX, textViewY, textViewAngle, textViewDistance, textViewDirection;
    ObjectAnimator animateBallY, animateBallX;
    JoyStickClass joystick;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sp = getSharedPreferences("MyUserPrefs", Context.MODE_PRIVATE);
        ImageView ball = findViewById(R.id.ball);

        // Initialize TextViews
        textViewX = findViewById(R.id.textView1);
        textViewY = findViewById(R.id.textView2);
        textViewAngle = findViewById(R.id.textView3);
        textViewDistance = findViewById(R.id.textView4);
        textViewDirection = findViewById(R.id.textView5);

        layoutJoystick = findViewById(R.id.layout_joystick);
        joystick = new JoyStickClass(getApplicationContext(), layoutJoystick, R.drawable.image_button);

        // Configure joystick
        joystick.setStickSize(150, 150);
        joystick.setLayoutSize(500, 500);
        joystick.setLayoutAlpha(150);
        joystick.setStickAlpha(100);
        joystick.setOffset(90);
        joystick.setMinimumDistance(50);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Define a constant for speed and duration
                final float slowSpeed = 3f;
                final long animationDuration = 50;

                // Retrieve current joystick direction from shared preferences
                String joystickDirection = sp.getString("joystickdirection", "stop");

                // Perform action based on joystick direction
                performJoystickAction(ball, joystickDirection, slowSpeed, animationDuration);

                // Continue executing every 100ms
                handler.postDelayed(this, 100);
            }
        }, 10);

        layoutJoystick.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                joystick.drawStick(event);

                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                    // Update joystick info on screen
                    textViewX.setText("X : " + joystick.getX());
                    textViewY.setText("Y : " + joystick.getY());
                    textViewAngle.setText("Angle : " + joystick.getAngle());
                    textViewDistance.setText("Distance : " + joystick.getDistance());

                    // Get the joystick direction
                    String joystickDirection = getJoystickDirection(joystick.get8Direction());

                    // Update the joystick direction in SharedPreferences
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("joystickdirection", joystickDirection);
                    editor.apply();

                    textViewDirection.setText("Direction : " + joystickDirection);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    resetJoystick();
                }

                // Send HTTP request based on joystick direction
                sendJoystickDirectionRequest();

                return true;
            }
        });
    }

    private void resetJoystick() {
        // Reset joystick data displayed on screen
        textViewX.setText("X :");
        textViewY.setText("Y :");
        textViewAngle.setText("Angle :");
        textViewDistance.setText("Distance :");
        textViewDirection.setText("Direction :");

        // Reset joystick direction in SharedPreferences
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("joystickdirection", "stop");
        editor.apply();
    }

    private String getJoystickDirection(int direction) {
        switch (direction) {
            case JoyStickClass.STICK_UP:
                return "up";
            case JoyStickClass.STICK_UPRIGHT:
                return "upright";
            case JoyStickClass.STICK_RIGHT:
                return "right";
            case JoyStickClass.STICK_DOWNRIGHT:
                return "downright";
            case JoyStickClass.STICK_DOWN:
                return "down";
            case JoyStickClass.STICK_DOWNLEFT:
                return "downleft";
            case JoyStickClass.STICK_LEFT:
                return "left";
            case JoyStickClass.STICK_UPLEFT:
                return "upleft";
            case JoyStickClass.STICK_NONE:
            default:
                return "stop";
        }
    }

    private void performJoystickAction(ImageView ball, String joystickDirection, float slowSpeed, long animationDuration) {
        // Perform action based on joystick direction using ObjectAnimator
        if (joystickDirection.equals("left")) {
            animateBallX = createBallAnimation(ball, "x", ball.getX(), ball.getX() - ball.getWidth() / slowSpeed, animationDuration);
        } else if (joystickDirection.equals("right")) {
            animateBallX = createBallAnimation(ball, "x", ball.getX(), ball.getX() + ball.getWidth() / slowSpeed, animationDuration);
        } else if (joystickDirection.equals("up")) {
            animateBallY = createBallAnimation(ball, "Y", ball.getY(), ball.getY() - ball.getHeight(), animationDuration);
        } else if (joystickDirection.equals("down")) {
            animateBallY = createBallAnimation(ball, "Y", ball.getY(), ball.getY() + ball.getHeight(), animationDuration);
        } else if (joystickDirection.equals("upright")) {
            animateBallY = createBallAnimation(ball, "Y", ball.getY(), ball.getY() - ball.getHeight(), animationDuration);
            animateBallX = createBallAnimation(ball, "x", ball.getX(), ball.getX() + ball.getWidth() / slowSpeed, animationDuration);
        } else if (joystickDirection.equals("downright")) {
            animateBallY = createBallAnimation(ball, "Y", ball.getY(), ball.getY() + ball.getHeight() / slowSpeed, animationDuration);
            animateBallX = createBallAnimation(ball, "x", ball.getX(), ball.getX() + ball.getWidth() / slowSpeed, animationDuration);
        } else if (joystickDirection.equals("downleft")) {
            animateBallY = createBallAnimation(ball, "Y", ball.getY(), ball.getY() + ball.getHeight() / slowSpeed, animationDuration);
            animateBallX = createBallAnimation(ball, "x", ball.getX(), ball.getX() - ball.getWidth() / slowSpeed, animationDuration);
        } else if (joystickDirection.equals("upleft")) {
            animateBallY = createBallAnimation(ball, "Y", ball.getY(), ball.getY() - ball.getHeight(), animationDuration);
            animateBallX = createBallAnimation(ball, "x", ball.getX(), ball.getX() - ball.getWidth() / slowSpeed, animationDuration);
        }
    }

    private ObjectAnimator createBallAnimation(ImageView ball, String property, float startValue, float endValue, long duration) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(ball, property, startValue, endValue);
        animator.setDuration(duration);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
        return animator;
    }

    private void sendJoystickDirectionRequest() {
        String joystickDirection = sp.getString("joystickdirection", "stop");
        String baseUrl = "http://192.168.4.1/";  // IP of ESP8266

        // Send HTTP request based on joystick direction
        if (joystickDirection != null) {
            if (joystickDirection.equals("up")) {
                sendHttpRequest(baseUrl + "forward");
            } else if (joystickDirection.equals("down")) {
                sendHttpRequest(baseUrl + "backward");
            } else if (joystickDirection.equals("left")) {
                sendHttpRequest(baseUrl + "left");
            } else if (joystickDirection.equals("right")) {
                sendHttpRequest(baseUrl + "right");
            } else if (joystickDirection.equals("stop")) {
                sendHttpRequest(baseUrl + "stop");
            }
        }
    }

    private void sendHttpRequest(final String urlString) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // Handle successful response if needed
                    }
                    connection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
