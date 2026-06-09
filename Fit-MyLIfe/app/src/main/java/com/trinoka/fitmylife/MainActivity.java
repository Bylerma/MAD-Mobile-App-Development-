package com.trinoka.fitmylife;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private TextView tvStepCount, tvCalories, tvActivityTime;
    private ProgressBar pbSteps;
    private Button btnLogout, btnPlay, btnPause, btnStop;
    private MediaPlayer mediaPlayer;
    private boolean isPrepared = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        // Handle window insets for Edge-to-Edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI components
        tvStepCount = findViewById(R.id.tvStepCount);
        tvCalories = findViewById(R.id.tvCalories);
        tvActivityTime = findViewById(R.id.tvActivityTime);
        pbSteps = findViewById(R.id.pbSteps);
        btnLogout = findViewById(R.id.btnLogout);
        btnPlay = findViewById(R.id.btnPlay);
        btnPause = findViewById(R.id.btnPause);
        btnStop = findViewById(R.id.btnStop);

        // Initialize MediaPlayer for streaming
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );
        try {
            mediaPlayer.setDataSource("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3");
            mediaPlayer.setOnPreparedListener(mp -> {
                isPrepared = true;
                Toast.makeText(this, "Music ready", Toast.LENGTH_SHORT).show();
            });
            mediaPlayer.prepareAsync();
            Toast.makeText(this, "Loading music...", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Set simulated data
        updateDashboard(8542, 520, 60);

        // Set click listeners
        btnLogout.setOnClickListener(v -> {
            stopMusic();
            
            // Clear login session
            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isLoggedIn", false);
            editor.apply();

            Toast.makeText(MainActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        btnPlay.setOnClickListener(v -> {
            if (mediaPlayer != null && isPrepared && !mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                Toast.makeText(this, "Playing Music", Toast.LENGTH_SHORT).show();
            } else if (!isPrepared) {
                Toast.makeText(this, "Music is still loading...", Toast.LENGTH_SHORT).show();
            }
        });

        btnPause.setOnClickListener(v -> {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                Toast.makeText(this, "Paused Music", Toast.LENGTH_SHORT).show();
            }
        });

        btnStop.setOnClickListener(v -> {
            stopMusic();
        });
    }

    private void stopMusic() {
        if (mediaPlayer != null && isPrepared) {
            mediaPlayer.stop();
            // Need to prepare again if we want to play after stop
            try {
                isPrepared = false;
                mediaPlayer.prepareAsync();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Toast.makeText(this, "Stopped Music", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateDashboard(int steps, int calories, int minutes) {
        tvStepCount.setText(String.format("%,d", steps));
        tvCalories.setText(String.format("%d kcal", calories));
        tvActivityTime.setText(String.format("%d mins", minutes));
        pbSteps.setProgress(steps);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}