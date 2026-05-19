package my.little.audio.player.android;

// This file is part of 'my.little.audio.player.android'
// It is published on GitHub under the LGPLv3 Licence:
// https://github.com/Pordohiq/my.little.audio.player.android

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.ForwardingPlayer;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.DefaultMediaNotificationProvider;
import androidx.media3.session.LibraryResult;
import androidx.media3.session.MediaLibraryService;
import androidx.media3.session.MediaSession;
import androidx.media3.session.SessionError;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Futures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import my.little.audio.player.android.ResTree.Directory;
import my.little.audio.player.android.ResTree.DiskElement;
import my.little.audio.player.android.ResTree.Music;
import my.little.audio.player.android.ResTree.ResTree;
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
							.setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_MIXED)
							.build())
					.build();
			return Futures.immediateFuture(LibraryResult.ofItem(rootItem, params));
		}

		@NonNull @Override
		public ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> onGetChildren(
				@NonNull MediaLibrarySession session, @NonNull MediaSession.ControllerInfo browser,
				@NonNull String parentId, int page, int pageSize, @Nullable LibraryParams params) {
			List<DiskElement> elements = null;
			if (parentId.equals("root")) {
				elements = ResTree.library;
			} else {
				DiskElement element = ResTree.get_element_at_path(Arrays.asList(parentId.split("/")));
				if (element instanceof Directory) {
					elements = ((Directory) element).getChildren();
				}
			}

			if (elements == null) {
				return Futures.immediateFuture(LibraryResult.ofError(SessionError.ERROR_BAD_VALUE));
			}

			List<MediaItem> mediaItems = new ArrayList<>();
			for (DiskElement element : elements) {
				String mediaId = parentId.equals("root") ? element.getName() : parentId + "/" + element.getName();
				MediaMetadata metadata = new MediaMetadata.Builder()
						.setTitle(element.getName())
						.setIsBrowsable(element instanceof Directory)
						.setIsPlayable(element instanceof Music)
						.setMediaType(element instanceof Directory ? MediaMetadata.MEDIA_TYPE_FOLDER_MIXED : MediaMetadata.MEDIA_TYPE_MUSIC)
						.build();
				mediaItems.add(new MediaItem.Builder()
						.setMediaId(mediaId)
						.setMediaMetadata(metadata)
						.setUri(element.getUri())
						.build());
			}

			List<MediaItem> resultItems;
			if (page < 0 || pageSize < 1) {
				resultItems = mediaItems;
			} else {
				int fromIndex = page * pageSize;
				int toIndex = Math.min(fromIndex + pageSize, mediaItems.size());
				if (fromIndex >= mediaItems.size()) {
					resultItems = new ArrayList<>();
				} else {
					resultItems = mediaItems.subList(fromIndex, toIndex);
				}
			}
			return Futures.immediateFuture(LibraryResult.ofItemList(resultItems, params));
		}

		@NonNull @Override
		public ListenableFuture<LibraryResult<MediaItem>> onGetItem(
				@NonNull MediaLibrarySession session, @NonNull MediaSession.ControllerInfo browser, @NonNull String mediaId) {
			if (mediaId.equals("root")) {
				return onGetLibraryRoot(session, browser, null);
			}
			DiskElement element = ResTree.get_element_at_path(Arrays.asList(mediaId.split("/")));
			if (element == null) {
				return Futures.immediateFuture(LibraryResult.ofError(SessionError.ERROR_BAD_VALUE));
			}
			MediaMetadata metadata = new MediaMetadata.Builder()
					.setTitle(element.getName())
					.setIsBrowsable(element instanceof Directory)
					.setIsPlayable(element instanceof Music)
					.setMediaType(element instanceof Directory ? MediaMetadata.MEDIA_TYPE_FOLDER_MIXED : MediaMetadata.MEDIA_TYPE_MUSIC)
					.build();
			MediaItem item = new MediaItem.Builder()
					.setMediaId(mediaId)
					.setMediaMetadata(metadata)
					.setUri(element.getUri())
					.build();
			return Futures.immediateFuture(LibraryResult.ofItem(item, null));
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
			NotificationChannel channel = new NotificationChannel("channel_id", getString(R.string.global_app_name), NotificationManager.IMPORTANCE_LOW);
			notificationManager.createNotificationChannel(channel);
		}
		
		super.onCreate();
		ExoPlayer player = new ExoPlayer.Builder(this).build();
		
		ForwardingPlayer forwardingPlayer = new ForwardingPlayer(player){
			@Override
			public void seekToNext() {
				Global.play_next();
			}
			
			@Override
			public void seekToNextMediaItem() {
				Global.play_next();
			}
			
			@Override
			public void seekToPrevious() {
				Global.play_previous();
			}
			
			@Override
			public void seekToPreviousMediaItem() {
				Global.play_previous();
			}

			@NonNull @Override
			public Player.Commands getAvailableCommands() {
				return super.getAvailableCommands().buildUpon()
						.add(Player.COMMAND_SEEK_TO_NEXT)
						.add(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
						.add(Player.COMMAND_SEEK_TO_PREVIOUS)
						.add(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
						.build();
			}

			@Override
			public boolean isCommandAvailable(int command) {
				return command == Player.COMMAND_SEEK_TO_NEXT
						|| command == Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM
						|| command == Player.COMMAND_SEEK_TO_PREVIOUS
						|| command == Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM
						|| super.isCommandAvailable(command);
			}
		};
		
		mediaLibrarySession = new MediaLibrarySession.Builder(this, forwardingPlayer, callback).build();
		
		DefaultMediaNotificationProvider mediaNotificationProvider = new DefaultMediaNotificationProvider.Builder(this)
				.setChannelId("channel_id")
				.setChannelName(R.string.global_app_name)
				.build();
		mediaNotificationProvider.setSmallIcon(R.drawable.global_app_icon_webp);
		
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
