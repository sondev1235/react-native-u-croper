package com.mobix.son;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;


import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Promise;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import java.util.HashMap;
import java.util.Map;
import static android.app.Activity.RESULT_OK;

import javax.annotation.Nonnull;

public class UCroperModule extends ReactContextBaseJavaModule implements ActivityEventListener {

    private static ReactApplicationContext reactContext;

    private static final String DURATION_SHORT_KEY = "SHORT";
    private static final String DURATION_LONG_KEY = "LONG";
    private static final String E_ACTIVITY_DOES_NOT_EXIST = "E_ACTIVITY_DOES_NOT_EXIST";
    private static final String ERROR_WOOPS = "ERROR_WOOPS";
    private static final String E_CROP_PICKER_CANCELLED = "E_CROP_PICKER_CANCELLED";
    private static final String E_NO_IMAGE_DATA_FOUND = "E_NO_IMAGE_DATA_FOUND";
    private static final int CROP_URI_REQUEST = 100;

    private Promise mCroperPromise;



    public UCroperModule(@Nonnull ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        reactContext.addActivityEventListener(this);
    }

    @Nonnull
    @Override
    public String getName() {
        return "UCroper";
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put(DURATION_SHORT_KEY, Toast.LENGTH_SHORT);
        constants.put(DURATION_LONG_KEY, Toast.LENGTH_LONG);
        return constants;
    }

    @ReactMethod
    public void show(String message, int duration) {
        Toast.makeText(getReactApplicationContext(), message, duration).show();
    }

    @ReactMethod
    public void openCropImage(String sourceUri, String fileName, Promise promise) {
        Activity currentActivity = getCurrentActivity();

        if (currentActivity == null) {
            mCroperPromise.reject(E_ACTIVITY_DOES_NOT_EXIST, "Activity doesn't exist");
            return;
        }

        mCroperPromise = promise;

        try{
            Intent intent = new Intent(currentActivity, UCroperActivity.class);
            intent.putExtra(Constants.SOURCE_URI, sourceUri);
            intent.putExtra(Constants.FILE_NAME, fileName);
            currentActivity.startActivityForResult(intent, CROP_URI_REQUEST);
        }catch (Exception e){
            mCroperPromise.reject(ERROR_WOOPS, e);
            mCroperPromise = null;
        }
    }


    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {

        if (requestCode == CROP_URI_REQUEST) {
            if (mCroperPromise != null) {
                if (resultCode == Activity.RESULT_CANCELED) {
                    mCroperPromise.reject(E_CROP_PICKER_CANCELLED, "Crop image picker was cancelled");
                } else if (resultCode == Activity.RESULT_OK) {
                    String uri =  data.getStringExtra(Constants.RESULT_URI);
                    if (uri == null) {
                        mCroperPromise.reject(E_NO_IMAGE_DATA_FOUND, "No image data found");
                    } else {
                        mCroperPromise.resolve(uri);
                    }
                }
                mCroperPromise = null;
            }
        }

    }

    @Override
    public void onNewIntent(Intent intent) {

    }
}
