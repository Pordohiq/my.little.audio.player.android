package my.little.audio.player.android;

// This file is part of 'my.little.audio.player.android'
// It is published on GitHub under the LGPLv3 License:
// https://github.com/Pordohiq/my.little.audio.player.android

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.DefaultMediaNotificationProvider;
import androidx.media3.session.LibraryResult;
import androidx.media3.session.MediaLibraryService;
import androidx.media3.session.MediaNotification;
import androidx.media3.session.MediaSession;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

@UnstableApi
public class AudioService extends MediaLibraryService {
	private MediaLibrarySession mediaLibrarySession;
	
	private final MediaLibrarySession.Callback callback = new MediaLibrarySession.Callback() {
		@NonNull @Override
		public ListenableFuture<LibraryResult<MediaItem>> onGetLibraryRoot(
				@NonNull MediaLibrarySession session, @NonNull MediaSession.ControllerInfo browser, @Nullable LibraryParams params) {
			MediaItem rootItem = new MediaItem.Builder()
					.setMediaId("root")
					.setMediaMetadata(new MediaMetadata.Builder()
							.setIsBrowsable(true)
							.setIsPlayable(false)
							.build())
					.build();
			return Futures.immediateFuture(LibraryResult.ofItem(rootItem, params));
		}
	};
	
	@Override @Nullable
	public MediaLibrarySession onGetSession(@NonNull MediaSession.ControllerInfo controllerInfo) {
		return mediaLibrarySession;
	}
	
	@Override
	public void onCreate() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			NotificationChannel channel = new NotificationChannel("channel_id", "Channel Name", NotificationManager.IMPORTANCE_LOW);
			notificationManager.createNotificationChannel(channel);
		}
		
		super.onCreate();
		ExoPlayer player = new ExoPlayer.Builder(this).build();
		mediaLibrarySession = new MediaLibrarySession.Builder(this, player, callback).build();
		
		MediaNotification.Provider mediaNotificationProvider = new DefaultMediaNotificationProvider.Builder(this)
				.setChannelId("channel_id")
				.setChannelName(R.string.global_app_name)
				.build();
		
		setMediaNotificationProvider(mediaNotificationProvider);
	}
	
	@Override
	public void onDestroy() {
		if (mediaLibrarySession != null) {
			mediaLibrarySession.getPlayer().release();
			mediaLibrarySession.release();
			mediaLibrarySession = null;
		}
		super.onDestroy();
	}
}
