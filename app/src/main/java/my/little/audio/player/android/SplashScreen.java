package my.little.audio.player.android;

// This file is part of 'my.little.audio.player.android'
// It is published on GitHub under the LGPLv3 License:
// https://github.com/lomjek/my.little.audio.player.android

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

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
				startActivity(intent);
				finish();
			}
		}, 500);
	}
}
