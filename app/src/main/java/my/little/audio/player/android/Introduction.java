package my.little.audio.player.android;

// This file is part of 'my.little.audio.player.android'
// It is published on GitHub under the LGPLv3 License:
// https://github.com/lomjek/my.little.audio.player.android

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import my.little.audio.player.android.Action.Action;

public class Introduction extends ComponentActivity {
	protected LinearLayout screen_1;
	protected LinearLayout screen_2;
	protected LinearLayout screen_3;
	
	protected ImageView next;
	protected ImageView skip;
	
	private final ActivityResultLauncher<Uri> openDocumentTreeLauncher =
			registerForActivityResult(
					new ActivityResultContracts.OpenDocumentTree(),
					treeUri -> {
						if (treeUri != null) {
							final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
							getContentResolver().takePersistableUriPermission(treeUri, takeFlags);
							Action.set_new_lib_root_path(treeUri);
						}
					});
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.introduction);
		
		screen_1 = findViewById(R.id.introduction_screen_1);
		screen_2 = findViewById(R.id.introduction_screen_2);
		screen_3 = findViewById(R.id.introduction_screen_3);
		
		screen_1.setVisibility(VISIBLE);
		screen_2.setVisibility(GONE);
		screen_3.setVisibility(GONE);
		
		skip = findViewById(R.id.introduction_skip);
		skip.setOnClickListener(view -> finish());
		
		next = findViewById(R.id.introduction_forward);
		next.setOnClickListener(view -> {
			if (screen_1.getVisibility() == VISIBLE){
				screen_1.setVisibility(GONE);
				screen_2.setVisibility(VISIBLE);
			} else if (screen_2.getVisibility() == VISIBLE) {
				screen_2.setVisibility(GONE);
				screen_3.setVisibility(VISIBLE);
			} else {
				finish();
			}
		});
		
		findViewById(R.id.preferences_action_set_library).setOnClickListener(view -> {
			openDocumentTreeLauncher.launch(null);
			screen_1.setVisibility(GONE);
			screen_2.setVisibility(VISIBLE);
		});
	}
}
