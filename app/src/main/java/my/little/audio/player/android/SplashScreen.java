package my.little.audio.player.android;

// This file is part of 'my.little.audio.player.android'
// It is published on GitHub under the LGPLv3 Licence:
// https://github.com/Pordohiq/my.little.audio.player.android

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import my.little.audio.player.android.ResTree.ResTree;

@SuppressLint("CustomSplashScreen")
public class SplashScreen extends ComponentActivity {

	private final ActivityResultLauncher<Intent> introductionLauncher =
			registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
					result -> {
						startActivity(new Intent(SplashScreen.this, Main.class));
						finish();
					});
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_screen);
		
		new Handler(Looper.getMainLooper()).postDelayed(() -> {
			Global.getInstance().set_up();
			if (ResTree.library_root == null){
				Intent intent = new Intent(SplashScreen.this, Introduction.class);
				introductionLauncher.launch(intent);
			} else {
				Intent intent = new Intent(SplashScreen.this, Main.class);
				
				Intent incomingIntent = getIntent();
				if (Intent.ACTION_SEND.equals(incomingIntent.getAction()) && incomingIntent.getType() != null) {
					handleSharedFile(incomingIntent);
				}
				
				startActivity(intent);
				finish();
			}
		}, 500);
	}
	
	private void handleSharedFile(@NonNull Intent intent) {
		Uri fileUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
		if (fileUri != null) {
			try (InputStream ignored = getContentResolver().openInputStream(fileUri)) {
				ResTree.add_audio_file(fileUri, new ArrayList<>());
			} catch (IOException e) {
				Log.e(Global.APP_TAG, "Could not read the file!");
				finish();
			}
		}
	}
}