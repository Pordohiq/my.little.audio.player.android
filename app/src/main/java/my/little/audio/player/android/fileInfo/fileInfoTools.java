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
}
