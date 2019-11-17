package com.filepicker.file_save_picker;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/** FileSavePickerPlugin */
public class FileSavePickerPlugin implements FlutterPlugin, MethodCallHandler {

  private static final int WRITE_REQUEST_CODE = 43;
  private static final int REQUEST_CODE = WRITE_REQUEST_CODE;
  private static final int PERM_CODE = (FileSavePickerPlugin.class.hashCode() + 50) & 0x0000ffff;
  private static final String TAG = "FileSavePicker";
  private static final String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;

  private static byte[] fileData;
  private static Result result;
  private static Registrar instance;
  private static String mimeType;
  private static String filename;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    final MethodChannel channel = new MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(),
        "file_save_picker");
    channel.setMethodCallHandler(new FileSavePickerPlugin());
  }

  // This static function is optional and equivalent to onAttachedToEngine. It
  // supports the old
  // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
  // plugin registration via this function while apps migrate to use the new
  // Android APIs
  // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
  //
  // It is encouraged to share logic between onAttachedToEngine and registerWith
  // to keep
  // them functionally equivalent. Only one of onAttachedToEngine or registerWith
  // will be called
  // depending on the user's project. onAttachedToEngine or registerWith must both
  // be defined
  // in the same class.
  public static void registerWith(Registrar registrar) {
    if (registrar.activity() == null) {
      return;
    }

    final MethodChannel channel = new MethodChannel(registrar.messenger(), "file_save_picker");
    channel.setMethodCallHandler(new FileSavePickerPlugin());

    instance = registrar;
    instance.addActivityResultListener(new PluginRegistry.ActivityResultListener() {
      @Override
      public boolean onActivityResult(int requestCode, int resultCode, final Intent data) {

        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {

          new Thread(new Runnable() {
            @Override
            public void run() {
              if (data != null) {
                if (data.getData() != null) {
                  Uri uri = data.getData();

                  if (uri != null) {
                    writeFileContent(uri);
                  }

                  runOnUiThread(result, uri.toString(), true, "");
                } else {
                  runOnUiThread(result, null, false, "Unknown activity error, please file an issue.");
                }
              } else {
                runOnUiThread(result, null, false, "Unknown activity error, please file an issue.");
              }
            }
          }).start();
          return true;

        } else if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_CANCELED) {
          result.success(null);
          return true;
        } else if (requestCode == REQUEST_CODE) {
          result.error(TAG, "Unknown activity error, please fill an issue.", null);
        }
        return false;
      }

      private void writeFileContent(Uri uri) {
        try {

          ParcelFileDescriptor pfd = instance.activity().getContentResolver().openFileDescriptor(uri, "w");
          FileOutputStream fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());
          fileOutputStream.write(fileData);
          fileOutputStream.close();
          pfd.close();
        } catch (FileNotFoundException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });

    instance.addRequestPermissionsResultListener(new PluginRegistry.RequestPermissionsResultListener() {
      @Override
      public boolean onRequestPermissionsResult(int requestCode, String[] strings, int[] grantResults) {
        if (requestCode == PERM_CODE && grantResults.length > 0
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          startFileExplorer();
          return true;
        }
        return false;
      }
    });
  }

  private static void runOnUiThread(final Result result, final String path, final boolean success,
      final String errorMessage) {
    instance.activity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (success) {
          result.success(path);
        } else if (path != null) {
          result.error(TAG, (String) errorMessage, null);
        } else {
          result.notImplemented();
        }
      }
    });
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    if (call.method.equals("save_file")) {
      // result.success("Android " + android.os.Build.VERSION.RELEASE);

      FileSavePickerPlugin.result = result;

      FileSavePickerPlugin.fileData = (byte[]) call.argument("bytes");
      FileSavePickerPlugin.mimeType = (String) call.argument("mimeType");
      FileSavePickerPlugin.filename = (String) call.argument("filename");

      Log.i(TAG, "Checking argument: " + call.argument("bytes").toString());
      Log.i(TAG, "Checking argument: " + call.argument("mimeType").toString());
      Log.i(TAG, "Checking argument: " + call.argument("filename").toString());

      startFileExplorer();

    } else {
      result.notImplemented();
    }
  }

  private static boolean checkPermission() {
    Activity activity = instance.activity();
    Log.i(TAG, "Checking permission: " + permission);
    return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(activity, permission);
  }

  private static void requestPermission() {
    Activity activity = instance.activity();
    Log.i(TAG, "Requesting permission: " + permission);
    String[] perm = { permission };
    ActivityCompat.requestPermissions(activity, perm, PERM_CODE);
  }

  @SuppressWarnings("deprecation")
  private static void startFileExplorer() {
    Intent intent;

    if (!checkPermission()) {
      requestPermission();
      return;
    }

    intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
    intent.addCategory(Intent.CATEGORY_OPENABLE);
    intent.setType(FileSavePickerPlugin.mimeType);
    intent.putExtra(Intent.EXTRA_TITLE, FileSavePickerPlugin.filename);

    if (intent.resolveActivity(instance.activity().getPackageManager()) == null) {
      Log.e(TAG, "Can't find a valid activity to handle the request. Make sure you've a file explorer installed.");
      result.error(TAG, "Can't handle the provided file type.", null);
      return;
    }

    // Start
    instance.activity().startActivityForResult(intent, REQUEST_CODE);
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
  }
}
