package com.prashant.exoplayer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import java.util.Formatter;
import java.util.Locale;

/**
 * Created by BytesBee.
 *
 * @author BytesBee Infotech (Prashant Adesara)
 * @link <a href="https://bytesbee.com">BytesBee</a>
 */
public class CustomExoActivity extends AppCompatActivity {
    private static final int REWIND_TIME_MS = 5000; // 5 seconds
    private static final int FORWARD_TIME_MS = 15000; // 15 seconds
    private static final int CONTROLS_HIDE_TIMEOUT_MS = 5000; // 5 seconds

    private PlayerView playerView;
    private ExoPlayer player;

    private ImageButton btnPlayPause;
    private ImageButton btnRewind;
    private ImageButton btnForward;
    private SeekBar seekBar;
    private TextView currentPosition;
    private TextView totalDuration;
    private TextView tvLive;
    private ImageView imgSettings;
    private View controlsContainer;
    private Handler handler;
    private boolean isPlaying = false;
    private StringBuilder formatBuilder;
    private Formatter formatter;
    private final Runnable hideControlsRunnable = this::hideControls;
    private PopupWindow speedPopup;
    private float currentSpeed = 1.0f;
    private String videoUrl = Constants.DEFAULT_URL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_exo);

        initializeViews();
        getData();
        initializePlayer();
        setupListeners();
    }

    private void initializeViews() {
        playerView = findViewById(R.id.player_view);

        btnPlayPause = findViewById(R.id.btn_play_pause);
        btnRewind = findViewById(R.id.btn_rewind);
        btnForward = findViewById(R.id.btn_forward);
        seekBar = findViewById(R.id.seek_bar);
        currentPosition = findViewById(R.id.current_position);
        totalDuration = findViewById(R.id.total_duration);
        tvLive = findViewById(R.id.tv_live);
        imgSettings = findViewById(R.id.imgSettings);
        controlsContainer = findViewById(R.id.controls_container);

        formatBuilder = new StringBuilder();
        formatter = new Formatter(formatBuilder, Locale.getDefault());
        handler = new Handler(Looper.getMainLooper());
    }

    private void getData() {
        try {
            Intent intent = getIntent();
            videoUrl = intent.getStringExtra(Constants.KEY_URL);
        } catch (Exception ignored) {
        }
    }

    private void initializePlayer() {
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        player.addListener(new Player.Listener() {
            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                Toast.makeText(CustomExoActivity.this, getString(R.string.error_playback), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_READY) {
                    updateTotalDuration();
                    updatePlayPauseButton();
                }
            }
        });

        loadVideo();
    }

    private void setupListeners() {

        btnPlayPause.setOnClickListener(v -> {
            if (player != null) {
                if (isPlaying) {
                    player.pause();
                } else {
                    player.play();
                }
                updatePlayPauseButton();
                resetHideControlsTimer();
            }
        });

        btnRewind.setOnClickListener(v -> {
            if (player != null) {
                player.seekTo(Math.max(0, player.getCurrentPosition() - REWIND_TIME_MS));
                resetHideControlsTimer();
            }
        });

        btnForward.setOnClickListener(v -> {
            if (player != null) {
                player.seekTo(Math.min(player.getDuration(), player.getCurrentPosition() + FORWARD_TIME_MS));
                resetHideControlsTimer();
            }
        });

        imgSettings.setOnClickListener(v -> showSpeedPopup());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && player != null) {
                    player.seekTo(progress);
                    updateCurrentPosition();
                    resetHideControlsTimer();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeCallbacks(hideControlsRunnable);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                resetHideControlsTimer();
            }
        });

        // Set up touch listener for showing controls
        playerView.setOnTouchListener(this::onTouch);

        startPositionUpdateTimer();
    }

    private void toggleControls() {
        if (controlsContainer.getVisibility() == View.VISIBLE) {
            hideControls();
        } else {
            showControls();
        }
    }

    private void showControls() {
        controlsContainer.setVisibility(View.VISIBLE);
        resetHideControlsTimer();
    }

    private void hideControls() {
        controlsContainer.setVisibility(View.GONE);
    }

    private void resetHideControlsTimer() {
        handler.removeCallbacks(hideControlsRunnable);
        handler.postDelayed(hideControlsRunnable, CONTROLS_HIDE_TIMEOUT_MS);
    }

    private void startPositionUpdateTimer() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateCurrentPosition();
                updateSeekBar();
                handler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    private void updateCurrentPosition() {
        if (player != null) {
            currentPosition.setText(stringForTime(player.getCurrentPosition()));
        }
    }

    private void updateTotalDuration() {
        if (player != null) {
            totalDuration.setText(stringForTime(player.getDuration()));
            seekBar.setMax((int) player.getDuration());
        }
    }

    private void updateSeekBar() {
        if (player != null) {
            seekBar.setProgress((int) player.getCurrentPosition());
        }
    }

    private void updatePlayPauseButton() {
        isPlaying = player != null && player.isPlaying();
        btnPlayPause.setImageResource(isPlaying ?
                R.drawable.ic_exo_pause :
                R.drawable.ic_exo_play);
    }

    private String stringForTime(long timeMs) {
        if (timeMs < 0) {
            timeMs = 0;
        }
        long totalSeconds = timeMs / 1000;
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        formatBuilder.setLength(0);
        return formatter.format("%02d:%02d", minutes, seconds).toString();
    }

    private void loadVideo() {

        if (TextUtils.isEmpty(videoUrl)) {
            Toast.makeText(this, getString(R.string.error_invalid_url),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the URL is a live stream
        boolean isLiveStream = isLiveStreamUrl(videoUrl);
        tvLive.setVisibility(isLiveStream ? View.VISIBLE : View.GONE);

        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(videoUrl));
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();
        updatePlayPauseButton();
        showControls();
    }

    private boolean isLiveStreamUrl(String url) {
        // Check for common live streaming URL patterns
        String lowerUrl = url.toLowerCase();
        return lowerUrl.contains(".m3u8") || // HLS
                lowerUrl.contains(".m3u") ||  // HLS
                lowerUrl.contains(".ism") ||  // Smooth Streaming
                lowerUrl.contains(".mpd") ||  // DASH
                lowerUrl.contains("rtmp://") || // RTMP
                lowerUrl.contains("rtsp://");   // RTSP
    }

    private void showSpeedPopup() {
        // Inflate the popup layout
        @SuppressLint("InflateParams") View popupView = LayoutInflater.from(this).inflate(R.layout.popup_speed_menu, null);

        // Create the popup window with proper width and height
        speedPopup = new PopupWindow(
                popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
        );

        // Enable popup window to receive touch events
        speedPopup.setTouchable(true);
        speedPopup.setFocusable(true);
        speedPopup.setOutsideTouchable(true);

        // Set background to make touch events work
        speedPopup.setBackgroundDrawable(ContextCompat.getDrawable(this, android.R.color.transparent));
        // Set up click listeners for speed options
        int[] speedIds = {
                R.id.speed_025, R.id.speed_05, R.id.speed_075,
                R.id.speed_normal, R.id.speed_125, R.id.speed_15, R.id.speed_2
        };
        float[] speedValues = {0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f};

        for (int i = 0; i < speedIds.length; i++) {
            TextView speedOption = popupView.findViewById(speedIds[i]);
            final float speed = speedValues[i];

            // Highlight current speed
            if (speed == currentSpeed) {
                speedOption.setTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_light));
            }

            speedOption.setOnClickListener(v -> {
                setPlaybackSpeed(speed);
                speedPopup.dismiss();
            });
        }

        // Calculate position for popup
        int[] location = new int[2];
        imgSettings.getLocationInWindow(location);

        // Measure the popup content to get actual dimensions
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

//        // Show popup above the settings icon
        int xOffset = -popupView.getMeasuredWidth() + imgSettings.getWidth();
        int yOffset = -popupView.getMeasuredHeight() - imgSettings.getHeight();

        // Show the popup with calculated offsets
        speedPopup.showAsDropDown(imgSettings, xOffset, yOffset);
        resetHideControlsTimer();
    }

    private void setPlaybackSpeed(float speed) {
        if (player != null) {
            currentSpeed = speed;
            PlaybackParameters params = new PlaybackParameters(speed);
            player.setPlaybackParameters(params);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player == null) {
            initializePlayer();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(hideControlsRunnable);
        if (player != null) {
            player.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (player != null) {
            player.release();
            player = null;
        }
    }

    private boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            toggleControls();
            return true;
        }
        return false;
    }
}