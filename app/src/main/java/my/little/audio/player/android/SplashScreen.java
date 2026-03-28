package my.little.audio.player.android;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.ComponentActivity;

@SuppressLint("CustomSplashScreen")
public class SplashScreen extends ComponentActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_screen);
		
		new Handler(Looper.getMainLooper()).postDelayed(() -> {
			Global.getInstance().set_up();
			Intent intent = new Intent(SplashScreen.this, Main.class);
			startActivity(intent);
			finish();
		}, 500);
	}
}
