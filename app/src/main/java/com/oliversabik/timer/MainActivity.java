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

    private Button setButton;
    private Button startStopButton;

    private CountDownTimer countdownTimer;
    private long timeLeftInMilliseconds;
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
                TimerInputValidationState timerInputValidationState = getTimerInputState ();

                if (timerInputValidationState == TimerInputValidationState.VALID){
                    Long timeSetInMilliseconds = parseTimerInputToMilliseconds();
                    setTimerDuration(timeSetInMilliseconds);
                }
                else{
                    displayToastMessage(timerInputValidationState);
                }
            }
        });

        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!timerRunning) {
                    startTimer();
                    timerRunning = true;
                }
                else {
                    timerRunning = false;
                    stopTimer();
                }
            }
        });

        setCountdownText(0);
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

        setCountdownText(timeLeftInMilliseconds);
        updateVisibilityOfButtons();

        if (timerRunning){
            endTime = savedInstanceState.getLong(END_TIME);
            timeLeftInMilliseconds = endTime - System.currentTimeMillis();
            startTimer();
        }
    }

    private enum TimerInputValidationState {
        VALID(null),
        EMPTY(R.string.toastMessageEmpty),
        NOT_POSITIVE_NUMBER(R.string.toastMessagePositiveNumber);

        private Integer stringResult;

        TimerInputValidationState(Integer stringRes) {
            this.stringResult =  stringRes;
        }

        private int getStringResult() {
            return stringResult;
        }
    }

    private void displayToastMessage(TimerInputValidationState state){
        Toast.makeText(this, state.getStringResult(), Toast.LENGTH_SHORT).show();
    }

    private long parseTimerInputToMilliseconds() {
        long timeSetInMilliseconds;
        String timerInput = editTextInput.getText().toString();
        timeSetInMilliseconds = Long.parseLong(timerInput)* 60000;
        return timeSetInMilliseconds;
    }

    private TimerInputValidationState getTimerInputState(){

        String timerInput = editTextInput.getText().toString();
        TimerInputValidationState inputValidationState;

        if (timerInput.length() == 0){
            inputValidationState = TimerInputValidationState.EMPTY;
        }
        else if (timerInput.equals("0")){
            inputValidationState = TimerInputValidationState.NOT_POSITIVE_NUMBER;
        } else {
            inputValidationState = TimerInputValidationState.VALID;
        }
        return inputValidationState;
    }

    private void setTimerDuration(long milliseconds) {
        timeLeftInMilliseconds = milliseconds;
        setCountdownText(timeLeftInMilliseconds);
        closeKeyboardAfterSetInput();
        cleanTimerInput();
    }

    private void cleanTimerInput() {
        editTextInput.setText("");
    }

    private void setCountdownText(long timeLeftInMilliseconds){
        int hours = (int) (timeLeftInMilliseconds / 1000) / 3600;
        int minutes = (int) ((timeLeftInMilliseconds / 1000) % 3600) / 60;
        int seconds = (int) (timeLeftInMilliseconds / 1000) % 60;

        String timeLeftText;
        if (hours > 0) {
            timeLeftText = String.format(Locale.getDefault(),
                    "%d:%02d:%02d", hours, minutes, seconds);
        } else{
            timeLeftText = String.format(Locale.getDefault(), "%02d:%02d",minutes, seconds);
        }

        countdownText.setText(timeLeftText);
    }

    private void stopTimer() {
        countdownTimer.cancel();
        startStopButton.setText(R.string.buttonStartText);
        updateVisibilityOfButtons();
    }

    private void startTimer() {
        endTime = System.currentTimeMillis() + timeLeftInMilliseconds;
        countdownTimer = new CountDownTimer(timeLeftInMilliseconds, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMilliseconds = millisUntilFinished;
                setCountdownText(timeLeftInMilliseconds);
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
