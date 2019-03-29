package com.oliversabik.timer;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText editTextInput;
    private TextView countdownText;

    private Button pauseButton;
    private Button stopButton;
    private Button startButton;

    private CountDownTimer countdownTimer;
    private long endTime;
    private long pausedTimeLeftInMillisecond;

    private TimerState timerState;

    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "personal_notification";
    private static final String END_TIME = "endTime";
    private static final String PAUSED_TIME = "countdownText";
    private static final String TIMER_STATE = "timerState";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timerState = TimerState.STOPPED;

        countdownText = findViewById(R.id.countdown_text);
        editTextInput = findViewById(R.id.edit_text_input);

        pauseButton = findViewById(R.id.pauseButton);
        startButton = findViewById(R.id.statButton);
        stopButton = findViewById(R.id.stopButton);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimerInputValidationState timerInputValidationState = getTimerInputState ();
                if (timerInputValidationState == TimerInputValidationState.VALID){
                    Long timeSetInMilliseconds = parseTimerInputToMilliseconds();
                    setTimerDuration(timeSetInMilliseconds);
                    endTime = System.currentTimeMillis() + timeSetInMilliseconds;
                    startTimer(timeSetInMilliseconds);
                }
                else{
                    displayToastMessage(timerInputValidationState);
                }
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timerState == TimerState.STARTED){
                    pauseTimer();
                    pausedTimeLeftInMillisecond = endTime - System.currentTimeMillis();
                }
                else{
                    endTime = System.currentTimeMillis() + pausedTimeLeftInMillisecond;
                    startTimer(endTime - System.currentTimeMillis());
                }
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCountdownText(0);
                stopTimer();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI(timerState);

        if (timerState == TimerState.STARTED) {
            Long timeLeftInMilliseconds = endTime - System.currentTimeMillis();
            startTimer(timeLeftInMilliseconds);
        }
        else {
            setCountdownText(pausedTimeLeftInMillisecond);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (timerState == TimerState.STARTED){
            stopTimer();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLong(END_TIME, endTime);
        outState.putLong(PAUSED_TIME, pausedTimeLeftInMillisecond);
        outState.putSerializable(TIMER_STATE, timerState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        endTime = savedInstanceState.getLong(END_TIME);
        pausedTimeLeftInMillisecond = savedInstanceState.getLong(PAUSED_TIME);
        timerState = (TimerState) savedInstanceState.getSerializable(TIMER_STATE);
    }

    private enum TimerState {
        STOPPED,
        STARTED,
        PAUSED
    }

    private void updateUI(TimerState timerState) {
        switch (timerState){
            case PAUSED:
                startButton.setVisibility(View.INVISIBLE);
                editTextInput.setVisibility(View.INVISIBLE);
                pauseButton.setText(R.string.buttonStartText);
            break;
            case STARTED:
                startButton.setVisibility(View.INVISIBLE);
                editTextInput.setVisibility(View.INVISIBLE);
                pauseButton.setText(R.string.buttonPauseText);
                break;
            case STOPPED:
                startButton.setVisibility(View.VISIBLE);
                editTextInput.setVisibility(View.VISIBLE);
                break;
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
        setCountdownText(milliseconds);
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
        if (countdownTimer != null && timerState == TimerState.STARTED)  {
            countdownTimer.cancel();
        }
        timerState = TimerState.STOPPED;
        updateUI(timerState);
    }

    private void pauseTimer() {
        timerState = TimerState.PAUSED;
        countdownTimer.cancel();
        updateUI(timerState);
    }

    private void startTimer(long timeDurationInMilliseconds) {

        countdownTimer = new CountDownTimer(timeDurationInMilliseconds, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerState = TimerState.STARTED;
                setCountdownText(millisUntilFinished);
                updateUI(timerState);
            }

            @Override
            public void onFinish() {
                timerState = TimerState.STOPPED;
                displayTimeRunOutNotification();
                Toast.makeText(MainActivity.this, R.string.toastMessageTimeRunOut, Toast.LENGTH_SHORT).show();
                updateUI(timerState);
                vibrate();
            }
        }.start();
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
