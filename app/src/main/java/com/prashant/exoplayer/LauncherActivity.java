package com.prashant.exoplayer;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

/**
 * Created by BytesBee.
 *
 * @author BytesBee Infotech (Prashant Adesara)
 * @link <a href="https://bytesbee.com">BytesBee</a>
 */
public class LauncherActivity extends AppCompatActivity {
    private TextInputEditText urlInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        urlInput = findViewById(R.id.url_input);
        urlInput.setText(Constants.DEFAULT_URL);
        MaterialButton btnExoPlay = findViewById(R.id.btnExoPlayer);
        MaterialButton btnCustomExo = findViewById(R.id.btnCustomExoPlayer);

        btnExoPlay.setOnClickListener(v -> openNextScreen(MainActivity.class));

        btnCustomExo.setOnClickListener(v -> openNextScreen(CustomExoActivity.class));
    }

    private void openNextScreen(Class<?> cls) {
        String videoUrl = Objects.requireNonNull(urlInput.getText()).toString().trim();
        Intent intent = new Intent(LauncherActivity.this, cls);
        intent.putExtra(Constants.KEY_URL, TextUtils.isEmpty(videoUrl) ? Constants.DEFAULT_URL : videoUrl);
        startActivity(intent);
    }
} 