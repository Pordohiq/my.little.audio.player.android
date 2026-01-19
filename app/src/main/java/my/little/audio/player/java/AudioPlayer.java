package my.little.audio.player.java;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.media3.exoplayer.ExoPlayer;

import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSessionService;

public class AudioPlayer extends MediaSessionService {
	private MediaSession session;
	private ExoPlayer player;
	
	@Nullable @Override
	public MediaSession onGetSession(@NonNull MediaSession.ControllerInfo controllerInfo) {
		return session;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		player = new ExoPlayer.Builder(this).build();
		session = new MediaSession.Builder(this, player).build();
	}
	
	@Override
	public void onDestroy() {
		if (session != null) {
			player.release();
			session.release();
		}
		super.onDestroy();
	}
}
