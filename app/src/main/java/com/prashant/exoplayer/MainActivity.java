package com.prashant.exoplayer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

/**
 * Created by BytesBee.
 *
 * @author BytesBee Infotech (Prashant Adesara)
 * @link <a href="https://bytesbee.com">BytesBee</a>
 */
public class MainActivity extends AppCompatActivity {
    private PlayerView playerView;
    private ExoPlayer player;
    private String videoUrl;

    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        playerView = findViewById(R.id.player_view);
        playerView.setShowNextButton(false);
        playerView.setShowPreviousButton(false);
        getData();

        // Initialize player
        initializePlayer();
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

        // Add a listener to handle playback errors
        player.addListener(new Player.Listener() {
            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                Toast.makeText(MainActivity.this,
                        getString(R.string.error_playback),
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Load the default URL
        loadVideo();
    }

    private void loadVideo() {
        if (TextUtils.isEmpty(videoUrl)) {
            Toast.makeText(this, getString(R.string.error_invalid_url), Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a MediaItem
        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(videoUrl));

        // Set the media item to be played
        player.setMediaItem(mediaItem);

        // Prepare the player
        player.prepare();

        // Start playing automatically
        player.play();
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
        if (player != null) {
            player.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
    }
} 