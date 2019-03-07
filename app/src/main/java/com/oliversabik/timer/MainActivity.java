package com.oliversabik.timer;

import android.content.Context;
import android.os.CountDownTimer;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Vibrator;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String CHANNEL_ID = "personal_notification";
    private final int NOTIFICATION_ID = 1;

    private EditText editTextInput;
    private TextView countdownText;
    private String timeLeftText;

    private Button setButton;
    private Button countdownButton;

    private CountDownTimer countdownTimer;
    private long timeLeftInMilliseconds;
    private boolean timerRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        countdownText = findViewById(R.id.countdown_text);
        editTextInput = findViewById(R.id.edit_text_input);

        setButton = findViewById(R.id.button_set);
        countdownButton = findViewById(R.id.countdown_button);

        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = editTextInput.getText().toString();

                if (input.length() == 0){
                    Toast.makeText(MainActivity.this, R.string.toastMessageEmpty,Toast.LENGTH_SHORT).show();
                    return;
                }

                long millisInput = Long.parseLong(input) * 60000;

                if (millisInput == 0) {
                    Toast.makeText(MainActivity.this, R.string.toastMessagePositiveNumber,Toast.LENGTH_SHORT).show();
                    return;
                }
                setTime(millisInput);
                editTextInput.setText("");
            }
        });

        countdownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startStop();
            }
        });

        updateTimer();
    }

    public void startStop() {
        if (timerRunning) {
            stopTimer();
        }
        else {
            startTimer();
        }
    }

    public void stopTimer() {
        countdownTimer.cancel();
        countdownButton.setText(R.string.buttonStartText);
        timerRunning = false;
        updateButtons();
    }

    public void setTime(long milliseconds) {
        timeLeftInMilliseconds = milliseconds;
        updateTimer();
        closeKeyboard();
    }

    public void startTimer() {
        countdownTimer = new CountDownTimer(timeLeftInMilliseconds, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMilliseconds = millisUntilFinished;
                updateTimer();
                updateButtons();
            }

            @Override
            public void onFinish() {
                displayNotification();
                Toast.makeText(MainActivity.this, R.string.toastMessageTimeRunOut, Toast.LENGTH_SHORT).show();
                countdownButton.setText(R.string.buttonStartText);
                timerRunning = false;
                getVibration();
                updateButtons();
            }
        }.start();
        countdownButton.setText(R.string.buttonPauseText);
        timerRunning = true;
    }

    public void updateTimer(){
        int minutes = (int) timeLeftInMilliseconds / 1000 / 60;
        int seconds = (int) timeLeftInMilliseconds / 1000 % 60;

        timeLeftText = String.format(Locale.getDefault(), "%02d:%02d",minutes, seconds);

        countdownText.setText(timeLeftText);
    }

    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }

    private void updateButtons() {
        if (timerRunning) {
            editTextInput.setVisibility(View.INVISIBLE);
            setButton.setVisibility(View.INVISIBLE);
        }
        else {
            editTextInput.setVisibility(View.VISIBLE);
            setButton.setVisibility(View.VISIBLE);
        }
    }

    private void displayNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_launcher_background);
        builder.setContentTitle(getString(R.string.notificationTitle));
        builder.setContentText(getString( R.string.notificationContentMessage));
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(NOTIFICATION_ID, builder.build());
    }

    private void getVibration() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(500);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLong("timeLeftInMilliseconds", timeLeftInMilliseconds);
        outState.putBoolean("timerRunning",timerRunning);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        timeLeftInMilliseconds = savedInstanceState.getLong("timeLeftInMilliseconds");
        timerRunning = savedInstanceState.getBoolean("timerRunning");
        updateTimer();
        updateButtons();

        if (timerRunning){
            startTimer();
        }
    }
}
