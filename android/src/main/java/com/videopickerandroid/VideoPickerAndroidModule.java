package com.videopickerandroid;

import static android.os.Looper.getMainLooper;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.module.annotations.ReactModule;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static android.os.Looper.getMainLooper;

import com.facebook.common.internal.ImmutableList;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.modules.core.PermissionAwareActivity;
import com.facebook.react.modules.core.PermissionListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableMap;


import com.facebook.react.bridge.WritableArray;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import static androidx.media3.common.MimeTypes.VIDEO_H264;
import static androidx.media3.common.MimeTypes.AUDIO_AAC;
import java.nio.file.Path;
import java.nio.file.Files;
import androidx.media3.common.Format;
import androidx.media3.effect.ScaleToFitTransformation;
import androidx.media3.transformer.Transformer;
import androidx.media3.transformer.TransformationRequest;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.transformer.TransformationException;
import androidx.media3.transformer.TransformationResult;
import androidx.media3.transformer.ProgressHolder;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Looper;
import android.content.DialogInterface;
import android.content.ContentResolver;

import java.util.UUID;
import android.widget.Toast;


@ReactModule(name = VideoPickerAndroidModule.NAME)
public class VideoPickerAndroidModule extends ReactContextBaseJavaModule {
  public static final String NAME = "VideoPickerAndroid";
  private static final String TAG = NAME;

  private static final int IMAGE_PICKER_REQUEST = 1;
  private static final String E_ACTIVITY_DOES_NOT_EXIST = "E_ACTIVITY_DOES_NOT_EXIST";
  private static final String E_CALLBACK_ERROR = "E_CALLBACK_ERROR";
  private static final String E_NO_LIBRARY_PERMISSION = "E_NO_LIBRARY_PERMISSION";
  private static final String E_FAILED_TO_SHOW_PICKER = "E_FAILED_TO_SHOW_PICKER";
  private static final String E_NO_IMAGE_DATA_FOUND = "E_NO_IMAGE_DATA_FOUND";
  private static final String E_ERROR_WHILE_CLEANING_FILES = "E_ERROR_WHILE_CLEANING_FILES";
  private static final String E_VIDEO_SIZE_TOO_BIG = "E_VIDEO_SIZE_TOO_BIG";
  private static final String E_COMPRESS_CANCELLED = "E_COMPRESS_CANCELLED";

  private Promise mPickerPromise;

  private @Nullable ArrayList<Uri> inputFileUris;

  private int inputFileCount;
  @Nullable private List<String> mediaItemsOutputFilePaths;
  @Nullable private Transformer transformer;
  @Nullable private File externalCacheFile;
  private int progressTotal = 0;
  private boolean multiple;
  private boolean compress;
  private int maxFiles;
  private int maxFileSize;
  private ReadableMap options;

  public VideoPickerAndroidModule(ReactApplicationContext reactContext) {
    super(reactContext);

    ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {
      @Override
      public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent intent) {
        if (requestCode == IMAGE_PICKER_REQUEST) {
          if (mPickerPromise != null) {
//                       if (resultCode == Activity.RESULT_CANCELED) {
//                           mPickerPromise.reject(E_PICKER_CANCELLED, "Image picker was cancelled");
//                       } else
            if (resultCode == Activity.RESULT_OK) {
              Uri data = intent.getData();
              ClipData clipData = intent.getClipData();
              System.out.println("--- intente get data");
              System.out.println(intent);
              System.out.println(data);
              System.out.println(clipData);
              List<Uri> inputUris = new ArrayList<>();
              if (intent.getData() == null) { // ユーザーが複数のアイテムを選択した場合、getData() は null を返します。
                if (intent.getClipData() == null) {
                  mPickerPromise.reject(E_NO_IMAGE_DATA_FOUND, "No image data found");
                  return ;
                }
                if (maxFiles > 0 && intent.getClipData().getItemCount() > maxFiles) {
                  mPickerPromise.reject(E_NO_IMAGE_DATA_FOUND, "Can choose max "+maxFiles+" videos.");
                  return ;
                }
                for (int i = 0; i < intent.getClipData().getItemCount(); i++) {
                  Uri uri = intent.getClipData().getItemAt(i).getUri();
                  inputUris.add(uri);
                }
              } else {
                Uri uri = intent.getData();
                inputUris.add(uri);
              }
              if ( maxFileSize > 0 ) {
                for (int i = 0; i < inputUris.size(); i++) {
                  Uri returnUri = inputUris.get(i);
                  Cursor returnCursor =
                    getReactApplicationContext().getContentResolver().query(returnUri, null, null, null, null);
                  int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                  int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                  returnCursor.moveToFirst();
                  Log.d(TAG, "--- nameIndex: " + returnCursor.getString(nameIndex)+", sizeIndex: "+Long.toString(returnCursor.getLong(sizeIndex)) );
                  if ( returnCursor.getLong(sizeIndex) > maxFileSize) {
                    mPickerPromise.reject(E_VIDEO_SIZE_TOO_BIG, "Selected video size too big. " + " nameIndex: " + returnCursor.getString(nameIndex)+", sizeIndex: "+Long.toString(returnCursor.getLong(sizeIndex)));
                  }
                }
              }
              if(compress){
                startTransformation(inputUris);
              }else{
                WritableArray uris = Arguments.createArray();
                for( int i=0;i<inputUris.size();i++){
                  uris.pushString(inputUris.get(i).toString());
                }
                mPickerPromise.resolve(uris);
              }

            }
//                    mPickerPromise = null;
          }
        }
      }
    };
    reactContext.addActivityEventListener(mActivityEventListener);
  }

    @Override
    @NonNull
    public String getName() {
      return NAME;
    }

    private void setConfiguration(final ReadableMap options) {
      compress = options.hasKey("compress") ? options.getBoolean("compress"):false;
      multiple = options.hasKey("multiple") ? options.getBoolean("multiple"):false;
      maxFiles = options.hasKey("maxFiles") ? options.getInt("maxFiles") : 0;
      maxFileSize = options.hasKey("maxFileSize") ? options.getInt("maxFileSize") : 0;
      this.options = options;
  }

  private void permissionsCheck(final Activity activity, final Promise promise, final List<String> requiredPermissions, final Callable<Void> callback) {

    List<String> missingPermissions = new ArrayList<>();
    List<String> supportedPermissions = new ArrayList<>(requiredPermissions);
    Log.d(TAG, "--- permission : requiredPermissions " + requiredPermissions.toString());

    // android 11 introduced scoped storage, and WRITE_EXTERNAL_STORAGE no longer works there
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
      Log.d(TAG, "--- Build.VERSION.SDK_INT > Build.VERSION_CODES.Q " + requiredPermissions.toString());
        supportedPermissions.remove(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    for (String permission : supportedPermissions) {
      Log.d(TAG, "--- permission : supportedPermissions " + permission.toString());
        int status = ActivityCompat.checkSelfPermission(activity, permission);
        if (status != PackageManager.PERMISSION_GRANTED) {
            missingPermissions.add(permission);
        }
    }

    if (!missingPermissions.isEmpty()) {

        ((PermissionAwareActivity) activity).requestPermissions(missingPermissions.toArray(new String[missingPermissions.size()]), 1, new PermissionListener() {

            @Override
            public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
                if (requestCode == 1) {

                    for (int permissionIndex = 0; permissionIndex < permissions.length; permissionIndex++) {
                        String permission = permissions[permissionIndex];
                        int grantResult = grantResults[permissionIndex];
                        Log.d(TAG, "--- onRequestPermissionsResult " + permission+", "+grantResult);

                        if (grantResult == PackageManager.PERMISSION_DENIED) {
                            if (permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                promise.reject(E_NO_LIBRARY_PERMISSION, "E_NO_PERMISSION");
                            } else {
                                // should not happen, we fallback on E_NO_LIBRARY_PERMISSION_KEY rejection for minimal consistency
                                promise.reject(E_NO_LIBRARY_PERMISSION, "E_NO_PERMISSION");
                            }
                            return true;
                        }
                    }

//                    try {
//                        callback.call();
//                    } catch (Exception e) {
//                        promise.reject(E_CALLBACK_ERROR, "Unknown error", e);
//                    }
                }

                return true;
            }
        });

        return;
    }
    Log.d(TAG, "--- onRequestPermissionsResult all permissions granted");

    // all permissions granted
//    try {
//        callback.call();
//    } catch (Exception e) {
//        promise.reject(E_CALLBACK_ERROR, "Unknown error", e);
//    }
  }

  private void deleteRecursive(File fileOrDirectory) {
    if (fileOrDirectory.isDirectory()) {
        for (File child : fileOrDirectory.listFiles()) {
            Log.d(TAG, "--- deleteRecursive path: " + child.getAbsolutePath());
            deleteRecursive(child);
        }
    }

    fileOrDirectory.delete();
  }

  @ReactMethod
  public void clean(final Promise promise) {

      final Activity activity = getCurrentActivity();
      final VideoPickerAndroidModule module = this;

      if (activity == null) {
          promise.reject(E_ACTIVITY_DOES_NOT_EXIST, "Activity doesn't exist");
          return;
      }
      permissionsCheck(activity, promise, Collections.singletonList(Manifest.permission.WRITE_EXTERNAL_STORAGE), new Callable<Void>() {
          @Override
          public Void call() {
              return null;
          }
      });

      try {
        String tmpDir = activity.getCacheDir()+ "/"  + TAG;
          File file = new File(tmpDir);
          if (!file.exists()) throw new Exception("File does not exist");
          Log.d(TAG, "--- clean tmpDir: " + tmpDir + ", path: " + file.getAbsolutePath());
          module.deleteRecursive(file);
          promise.resolve(null);
      } catch (Exception ex) {
          // ex.printStackTrace();
          // promise.reject(E_ERROR_WHILE_CLEANING_FILES, ex.getMessage());
          promise.resolve(null);// on [Error: File does not exist]
      }
  }

  @ReactMethod
  public void cancel(final Promise promise) {
      if ( transformer != null ) {
        transformer.cancel();
        promise.resolve(null);
      }
  }

    @ReactMethod
    public void pickVideo(final ReadableMap options,final Promise promise) {
      final Activity activity = getCurrentActivity();

      if (activity == null) {
        promise.reject(E_ACTIVITY_DOES_NOT_EXIST, "Activity doesn't exist");
        return;
      }

      mPickerPromise = promise;

      setConfiguration(options);
      permissionsCheck(activity, promise, Collections.singletonList(Manifest.permission.WRITE_EXTERNAL_STORAGE), new Callable<Void>() {
        @Override
        public Void call() {
          // initiatePicker(activity);
          mPickerPromise.reject(E_ACTIVITY_DOES_NOT_EXIST, "E_NO_PERMISSION");
          return null;
        }
      });

      try {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        if(this.multiple){
          intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }
        final Intent chooserIntent = Intent.createChooser(intent, "Pick videos");
        activity.startActivityForResult(chooserIntent, IMAGE_PICKER_REQUEST);
      } catch (Exception e) {
        mPickerPromise.reject(E_FAILED_TO_SHOW_PICKER, e);
        mPickerPromise = null;
      }
    }

    private void startTransformation(List<Uri> uris) {
      mediaItemsOutputFilePaths = new ArrayList<>();
      inputFileUris = new ArrayList<>();
      mediaItemsOutputFilePaths = new ArrayList<>();
      inputFileUris.addAll(uris);
      inputFileCount = inputFileUris.size();
      progressTotal = 0;
      startEachTransformation(inputFileUris.remove(0));
    }

    /** Creates a cache file, resetting it if it already exists. */
    private File createExternalCacheFile(String fileName) throws IOException {
//        File file = new File(this.getReactApplicationContext().getExternalCacheDir(), fileName);
      final Activity activity = getCurrentActivity();
      String tmpDir = activity.getCacheDir()+ "/"  + TAG;
      new File(tmpDir).mkdir();
      File file = new File(tmpDir + "/" + fileName);
      if (file.exists() && !file.delete()) {
        throw new IllegalStateException("Could not delete the previous transformer output file");
      }
      if (!file.createNewFile()) {
        throw new IllegalStateException("Could not create the transformer output file");
      }
      return file;
    }
    private void startEachTransformation(Uri inputFileUri) {
      try {
        externalCacheFile = createExternalCacheFile(new Date().getTime()+"transformeroutput.mp4");
        String externalCacheFilePath = externalCacheFile.getAbsolutePath();
        Log.d(TAG, "--- externalCacheFile path: file://" + externalCacheFilePath);

        Transformer transformer =
          new Transformer.Builder(this.getReactApplicationContext())
            .setVideoEffects(ImmutableList.of(
              new ScaleToFitTransformation.Builder().setScale(.5f, .5f).build()
            ))
            .setTransformationRequest(
              new TransformationRequest.Builder().setVideoMimeType(VIDEO_H264).setAudioMimeType(AUDIO_AAC).build())
            .addListener(
              new Transformer.Listener() {
                @Override
                public void onTransformationCompleted(
                  MediaItem mediaItem, TransformationResult transformationResult) {
                  VideoPickerAndroidModule.this.onTransformationCompleted(externalCacheFilePath, mediaItem);
                }

                @Override
                public void onTransformationError(
                  MediaItem mediaItem, TransformationException exception) {
                  VideoPickerAndroidModule.this.onTransformationError(exception);
                }
              })
            .build();
           MediaItem inputMediaItem =
                      new MediaItem.Builder()
                       .setUri(inputFileUri)
                       .setClippingConfiguration(
                              new MediaItem.ClippingConfiguration.Builder()
                              .setStartPositionMs(0)
                              .setEndPositionMs(300_000)
                              .build())
                       .build();
        transformer.startTransformation(inputMediaItem, externalCacheFilePath);
        this.transformer = transformer;

      } catch(Exception e) {
//            promise.reject("Create Event Error", e);
      }

      Handler mainHandler = new Handler(getMainLooper());
      ProgressHolder progressHolder = new ProgressHolder();
      mainHandler.post(
        new Runnable() {
          @Override
          public void run() {
            if (transformer != null
              && transformer.getProgress(progressHolder)
              != Transformer.PROGRESS_STATE_NO_TRANSFORMATION) {
              Log.d(TAG, "--- progress ( "+String.valueOf(progressHolder.progress)+" % )"+" / Total: ( "+String.valueOf(progressTotal+progressHolder.progress/inputFileCount)+" % ) ...");
              WritableMap params = Arguments.createMap();
              params.putInt("progress", progressTotal+progressHolder.progress/inputFileCount);
              sendEvent( "onCompressProgress", params);
              mainHandler.postDelayed(/* r= */ this, /* delayMillis= */ 300);
            }
          }
        });
    }

    private void onTransformationCompleted(String filePath, MediaItem inputMediaItem) {
      Log.d(TAG, "--- OnTransformationCompleted. Output file path: file://"+filePath+" ; Size: "+ (int) new File(filePath).length()/1000000+" MB");
      mediaItemsOutputFilePaths.add(filePath);
      progressTotal+=(100/inputFileCount);
      if( inputFileUris.size() > 0 ){
        startEachTransformation(inputFileUris.remove(0));
      }else{
        WritableArray uris = Arguments.createArray();
        for( int i=0;i<mediaItemsOutputFilePaths.size();i++){
          uris.pushString(mediaItemsOutputFilePaths.get(i).toString());
        }
        WritableMap params = Arguments.createMap();
        params.putInt("progress", 0);
        sendEvent( "onCompressProgress", params);
        mPickerPromise.resolve(uris);

      }

    }

    private void onTransformationError(TransformationException exception) {
      Log.e(TAG, "Transformation error", exception);
    }

    private void sendEvent(
      String eventName,
      @Nullable WritableMap params) {
      this.getReactApplicationContext()
        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
        .emit(eventName, params);
    }

  }


