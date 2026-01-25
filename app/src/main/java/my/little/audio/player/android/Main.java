package my.little.audio.player.android;

import android.content.Intent;

import android.net.Uri;

import android.os.Bundle;
import android.os.Environment;

import android.provider.DocumentsContract;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.annotation.NonNull;

import my.little.audio.player.android.Action.Action;

public class Main extends ComponentActivity {

  private final ActivityResultLauncher<Uri> openDocumentTreeLauncher =
      registerForActivityResult(
          new ActivityResultContracts.OpenDocumentTree(),
          treeUri -> {
            if (treeUri != null) {
              final int takeFlags =
                  Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
              getContentResolver().takePersistableUriPermission(treeUri, takeFlags);
              Action.set_new_lib_root_path(getPathFromUri(treeUri));
            }
          });

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    Signals.subscribeToEvent("requestPathFromSysDialog", this::request_library_root_from_sys_dialog);
  }

  private void request_library_root_from_sys_dialog() {
	  openDocumentTreeLauncher.launch(null);
  }

  @NonNull
  private String getPathFromUri(Uri uri) {
    if (uri == null) {
      return "";
    }
    String docId = DocumentsContract.getTreeDocumentId(uri);
    String[] split = docId.split(":");
    if (split.length == 0) {
      return "";
    }
    String type = split[0];

    if ("primary".equalsIgnoreCase(type)) {
      if (split.length > 1) {
        return Environment.getExternalStorageDirectory() + "/" + split[1];
      } else {
        return Environment.getExternalStorageDirectory().getPath();
      }
    }
    return "";
  }
}
