package my.little.audio.player.android.fileInfo;

import androidx.annotation.NonNull;

import java.util.Locale;

public class fileInfoTools {
	@NonNull
	public static String formatFileSize(long bytes) {
		if (bytes <= 0) return "0 B";
		
		final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
		int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
		
		return String.format(Locale.US, "%.2f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
	}
	
	@NonNull
	public static String formatDuration(long pb_duration) {
		long h = pb_duration / 3600;
		long m = (pb_duration % 3600) / 60;
		long s = pb_duration % 60;

		if (h > 0) {
			return String.format(Locale.US, "%d:%02d:%02d", h, m, s);
		} else {
			return String.format(Locale.US, "%02d:%02d", m, s);
		}
	}
}
