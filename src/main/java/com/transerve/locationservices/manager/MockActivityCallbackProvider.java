package com.transerve.locationservices.manager;

import android.util.Log;

import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.tasks.Task;

import static android.content.ContentValues.TAG;

public class MockActivityCallbackProvider extends ActivityCallbackProvider{

    public MockActivityCallbackProvider() {
        super(null);
    }

    public static MockActivityCallbackProvider getMocker() {
        return null;
    }

    public Task checkLocationSettings(LocationSettingsRequest mLocationSettingsRequest) {
        Log.i(TAG, "No Activity attached. Can't request Persmissions");
        return null;
    }

    public void requestPermissions(String[] strings, int requestPermissionsRequestCode) {
        Log.i(TAG, "No Activity attached. Can't request Persmissions");
    }

    public boolean checkSelfPermission(String accessFineLocation) {
        Log.i(TAG, "No Activity attached. Can't request Persmissions");
        return false;
    }

    public void showGPSSettingDialog(Exception e, int data) {
        Log.i(TAG, "No Activity attached. Can't show enable gps dialog");
    }

    @Override
    public boolean isAttached() {
        return false;
    }
}
