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

    private EditText editTextInput;
    private TextView countdownText;
    private String timeLeftText;

    private Button setButton;
    private Button startStopButton;

    private CountDownTimer countdownTimer;
    private long timeLeftInMilliseconds;
    private long timeSetInMilliseconds;
    private long endTime;
    private boolean timerRunning;

    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "personal_notification";
    private static final String TIME_LEFT_IN_MILLISECONDS = "timeLeftInMilliseconds";
    private static final String TIMER_RUNNING = "timerRunning";
    private static final String END_TIME = "endTime";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        countdownText = findViewById(R.id.countdown_text);
        editTextInput = findViewById(R.id.edit_text_input);

        setButton = findViewById(R.id.button_set);
        startStopButton = findViewById(R.id.countdown_button);

        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimerInputValidationStates timerInputValidationStates = getTimerInputState ();

                if (timerInputValidationStates == TimerInputValidationStates.VALID){
                    setTimerDuration(timeSetInMilliseconds);
                }
                else{
                    displayToastMessage(timerInputValidationStates);
                }
            }
        });

        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!timerRunning) {
                    startTimer();
                }
                else {
                    stopTimer();
                }
            }
        });

        setCountdownText();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (timerRunning){
            stopTimer();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLong(TIME_LEFT_IN_MILLISECONDS , timeLeftInMilliseconds);
        outState.putBoolean(TIMER_RUNNING,timerRunning);
        outState.putLong(END_TIME,endTime );

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        timeLeftInMilliseconds = savedInstanceState.getLong(TIME_LEFT_IN_MILLISECONDS);
        timerRunning = savedInstanceState.getBoolean(TIMER_RUNNING);
        setCountdownText();
        updateVisibilityOfButtons();

        if (timerRunning){
            endTime = savedInstanceState.getLong(END_TIME);
            timeLeftInMilliseconds = endTime - System.currentTimeMillis();
            startTimer();
        }
    }

    public enum TimerInputValidationStates{
        VALID,
        EMPTY,
        NOT_POSITIVE_NUMBER
    }

    private void displayToastMessage(TimerInputValidationStates state){
        if (state == TimerInputValidationStates.EMPTY){
            Toast.makeText(MainActivity.this, R.string.toastMessageEmpty,Toast.LENGTH_SHORT).show();
        }
        if (state == TimerInputValidationStates.NOT_POSITIVE_NUMBER){
            Toast.makeText(MainActivity.this, R.string.toastMessagePositiveNumber,Toast.LENGTH_SHORT).show();
        }
    }

    private TimerInputValidationStates getTimerInputState(){

        String timerInput = editTextInput.getText().toString();
        TimerInputValidationStates inputValidationStates = TimerInputValidationStates.VALID;

        if (timerInput.length() == 0){
            inputValidationStates = TimerInputValidationStates.EMPTY;
        }

        timeSetInMilliseconds = timerInput.isEmpty() ? -1 : Long.parseLong(timerInput) * 60000;

        if (timeSetInMilliseconds == 0) {
            inputValidationStates = TimerInputValidationStates.NOT_POSITIVE_NUMBER;
        }
        return inputValidationStates;
    }

    private void setTimerDuration(long milliseconds) {
        timeLeftInMilliseconds = milliseconds;
        setCountdownText();
        closeKeyboardAfterSetInput();
        cleanTimerInput();
    }

    private void cleanTimerInput() {
        editTextInput.setText("");
    }

    private void setCountdownText(){
        int minutes = (int) timeLeftInMilliseconds / 1000 / 60;
        int seconds = (int) timeLeftInMilliseconds / 1000 % 60;

        timeLeftText = String.format(Locale.getDefault(), "%02d:%02d",minutes, seconds);

        countdownText.setText(timeLeftText);
    }

    private void stopTimer() {
        countdownTimer.cancel();
        startStopButton.setText(R.string.buttonStartText);
        timerRunning = false;
        updateVisibilityOfButtons();
    }

    private void startTimer() {
        endTime = System.currentTimeMillis() + timeLeftInMilliseconds;
        countdownTimer = new CountDownTimer(timeLeftInMilliseconds, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMilliseconds = millisUntilFinished;
                setCountdownText();
                updateVisibilityOfButtons();
            }

            @Override
            public void onFinish() {
                displayTimeRunOutNotification();
                Toast.makeText(MainActivity.this, R.string.toastMessageTimeRunOut, Toast.LENGTH_SHORT).show();
                startStopButton.setText(R.string.buttonStartText);
                timerRunning = false;
                vibrate();
                updateVisibilityOfButtons();
            }
        }.start();
        startStopButton.setText(R.string.buttonPauseText);
        timerRunning = true;
    }

    private void updateVisibilityOfButtons() {
        if (timerRunning) {
            editTextInput.setVisibility(View.INVISIBLE);
            setButton.setVisibility(View.INVISIBLE);
        }
        else {
            editTextInput.setVisibility(View.VISIBLE);
            setButton.setVisibility(View.VISIBLE);
        }
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(500);
    }

    private void displayTimeRunOutNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_launcher_background);
        builder.setContentTitle(getString(R.string.notificationTimerTitle));
        builder.setContentText(getString( R.string.notificationTimerContentMessage));
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(NOTIFICATION_ID, builder.build());
    }

    private void closeKeyboardAfterSetInput() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }


}
