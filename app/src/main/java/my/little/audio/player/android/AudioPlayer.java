package my.little.audio.player.android;

// This file is part of 'my.little.audio.player.android'
// It is published on GitHub under the LGPLv3 License:
// https://github.com/Pordohiq/my.little.audio.player.android

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.annotation.OptIn;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;

import androidx.media3.session.CommandButton;
import androidx.media3.session.DefaultMediaNotificationProvider;
import androidx.media3.session.MediaNotification;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSessionService;

import com.google.common.collect.ImmutableList;

import my.little.audio.player.android.Action.Action;

public class AudioPlayer extends MediaSessionService {
	private MediaSession session;
	private ExoPlayer player;
	
	@Nullable @Override
	public MediaSession onGetSession(@NonNull MediaSession.ControllerInfo controllerInfo) {
		return session;
	}
    
    private void updateMetadata() {
        Object artist = null;
        if (Action.get_audio_info() != null) {
            artist = Action.get_audio_info().get("artist");
        }
        String artistName = artist != null ? artist.toString() : "";
        Uri artworkUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.global_app_icon);
        
		String title = Global.current_audio == null ? "" : Global.current_audio.getName() + "_test";
		
        MediaMetadata metadata = new MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artistName)
                .setArtworkUri(artworkUri)
                .build();
        
        player.setPlaylistMetadata(metadata);
        
        if (session != null) {
            session.setCustomLayout(ImmutableList.of());
        }
    }
	
	@OptIn(markerClass = UnstableApi.class) @Override
	public void onCreate() {
		super.onCreate();
		player = new ExoPlayer.Builder(this).build();
		session = new MediaSession.Builder(this, player).build();
        
        player.addListener(new Player.Listener() {
            @Override
            public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                updateMetadata();
            }
        });
        
        Context context = getApplicationContext(); // context must be passed to class
        setMediaNotificationProvider(new CustomNotificationProvider(context));
        
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

@UnstableApi
class CustomNotificationProvider implements MediaNotification.Provider {
    private Context context;
    
    public CustomNotificationProvider(Context context) {
        this.context = context;
    }
    
    @NonNull
    @Override
    public MediaNotification createNotification(
            @NonNull MediaSession mediaSession,
            @NonNull ImmutableList<CommandButton> customLayout,
            @NonNull MediaNotification.ActionFactory actionFactory,
            @NonNull Callback onNotificationChangedCallback) {
        
        DefaultMediaNotificationProvider defaultMediaNotificationProvider = new DefaultMediaNotificationProvider(context);
        defaultMediaNotificationProvider.setSmallIcon(R.drawable.global_app_icon);
        
        Object artist = null;
        if (Action.get_audio_info() != null) {
            artist = Action.get_audio_info().get("artist");
        }
        String artistName = artist != null ? artist.toString() : "";
        Uri artworkUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.drawable.global_app_icon);
	    
	    String title = Global.current_audio == null ? "" : Global.current_audio.getName() + "_test";
	    
	    MediaMetadata metadata = new MediaMetadata.Builder()
			    .setTitle(title)
                .setArtist(artistName)
                .setArtworkUri(artworkUri)
                .build();
        
        mediaSession.getPlayer().setPlaylistMetadata(metadata);
		
        return defaultMediaNotificationProvider.createNotification(
                mediaSession,
                customLayout,
                actionFactory,
                onNotificationChangedCallback);
    }
    
    @Override
    public boolean handleCustomCommand(
            @NonNull MediaSession session,
            @NonNull String action,
            @NonNull Bundle extras) {
        return false;
    }
    
    @NonNull @Override
    public NotificationChannelInfo getNotificationChannelInfo() {
        return new NotificationChannelInfo("__id__", "__name__");
    }
}
