package com.transerve.locationservices.manager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GnssStatus;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.observers.DisposableObserver;

import static android.os.Build.VERSION_CODES.M;

public class CoordinateManager {
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private FusedLocationProviderClient mFusedLocationClient;
    private ActivityCallbackProvider activityCallback;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    public static boolean grantedPermission = false;
    public static boolean locationUpdateStarted = false;
    private String TAG = "PERMISSION ";
    KalmanLatLong kalmanFilter;
    long runStartTimeInMillis;
    float currentSpeed = 0.0f; // meters/second
    private LocationObserver disposeBag;
    private LocationManager locationManager;
    private LocationListener locationListener = null;
    private GpsStatus.NmeaListener nmeaListener = null;
    private GnssStatus.Callback mGnssStatusListener;
    private JSONObject object;

    public CoordinateManager(Application application) {
        disposeBag = new LocationObserver();
        object = new JSONObject();
        registerListener();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(application);
        locationManager = (LocationManager) application.getSystemService(Context.LOCATION_SERVICE);
        kalmanFilter = new KalmanLatLong(3);
        // Kick off the process of building the LocationCallback, LocationRequest, and
        // LocationSettingsRequest objects.
        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();
        setRunTimePermission();
        activityCallback = ActivityCallbackProvider.getMocker();
        locationUpdateStarted = false;
    }

    private void addStatusListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            addGnssStatusListener();
        } else {
            //   addLegacyStatusListener();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void addGnssStatusListener() {
        mGnssStatusListener = new GnssStatus.Callback() {
            @Override
            public void onSatelliteStatusChanged(GnssStatus status) {
                updateGnssStatus(status);
            }
        };
        if (!getActivityCallback().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            return;
        }
        locationManager.registerGnssStatusCallback(mGnssStatusListener);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void updateGnssStatus(GnssStatus status){
        int mSvCount = 0;
        int mUsedInFixCount = 0;
        final int length = status.getSatelliteCount();

        while (mSvCount < length) {
           SatelliteStatus satStatus = new SatelliteStatus(status.getSvid(mSvCount), GpsTestUtil.getGnssConstellationType(status.getConstellationType(mSvCount)),
                    status.getCn0DbHz(mSvCount),
                    status.hasAlmanacData(mSvCount),
                    status.hasEphemerisData(mSvCount),
                    status.usedInFix(mSvCount),
                    status.getElevationDegrees(mSvCount),
                    status.getAzimuthDegrees(mSvCount));
            if (GpsTestUtil.isGnssCarrierFrequenciesSupported()) {
                if (status.hasCarrierFrequencyHz(mSvCount)) {
                    satStatus.setHasCarrierFrequency(true);
                    satStatus.setCarrierFrequencyH(status.getCarrierFrequencyHz(mSvCount));
                }
            }

            if (satStatus.getUsedInFix()) {
                mUsedInFixCount++;
            }

            mSvCount++;
        }
        try {
            object.put("SATELLITE_COUNT",mSvCount);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void activityAttached(Activity activity) {
        activityCallback = new ActivityCallbackProvider(activity);
        if (!locationUpdateStarted) {
            startLocationUpdates();
        }
    }

    public void activityDetached() {
        activityCallback = null;
    }

    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                filterAndAddLocation(locationResult.getLastLocation());
            }
        };
    }

    private void createLocationRequest() {
        runStartTimeInMillis = (long) (SystemClock.elapsedRealtimeNanos() / 1000000);
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    private void setRunTimePermission() {
        if (Build.VERSION.SDK_INT >= M) {
            if (checkPermission()) {
                //  Log.d(TAG, "ALREADY GIVEN PERMISSION ");
                startLocationUpdates();
            } else {
                //set location permission
                //  Log.d(TAG, "SHOW PERMISSION DIALOG");
                if (!getActivityCallback().isAttached()) {
                    Log.e("PERMISSION", "PERMISSION NOT GRANTED FOR LOCATION");
                } else {
                    getActivityCallback().requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS_REQUEST_CODE);
                }
            }
        } else {
            //Log.d(TAG, "NO RUN TIME PERMISSION FOR < 6  ");
            startLocationUpdates();
        }
    }

    private ActivityCallbackProvider getActivityCallback() {
        if (activityCallback == null) {
            return ActivityCallbackProvider.getMocker();
        } else {
            return activityCallback;
        }
    }

    private boolean checkPermission() {
        return getActivityCallback().checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION);
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        if (!getActivityCallback().isAttached()) {
            Log.w("", "No activity is attached to location manager");
            //Here we will start without checking the conditions since we don't
            //have the context but it might be running in the background
            try {
                requestLocationUpdates();
            } catch (Exception e) {
                Log.e(TAG, "startLocationUpdates: Error occurred might be since there is no activity attached");
                locationUpdateStarted = false;
            }
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
            locationManager.addNmeaListener(nmeaListener);
            addStatusListener();
            // Begin by checking if the device has the necessary location settings.
            getActivityCallback().checkLocationSettings(mLocationSettingsRequest)
                    .addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                        @Override
                        public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                            Log.i(TAG, "All location settings are satisfied.");
                            //noinspection MissingPermission
                            if (!getActivityCallback().checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
                                return;
                            }
                            requestLocationUpdates();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            int statusCode = ((ApiException) e).getStatusCode();
                            switch (statusCode) {
                                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                    Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                            "location settings ");
                                    if (!getActivityCallback().isAttached()) {
                                        Log.e("PERMISSION", "LOCATION SERVICES ARE DISABLED");
                                    } else {
                                        getActivityCallback().showGPSSettingDialog(e, REQUEST_CHECK_SETTINGS);
                                    }
                                    break;
                                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                    break;
                                default:
                            }
                        }
                    });
        }
    }

    public void onPermissionReceived(int requestCode, int resultCode) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        if (!getActivityCallback().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                            return;
                        }
                        requestLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                    default:
                }
                break;
            default:
        }
    }

    @SuppressLint("MissingPermission")
    private void requestLocationUpdates() {
        mFusedLocationClient.requestLocationUpdates(mLocationRequest
                , mLocationCallback, Looper.myLooper());
        locationUpdateStarted = true;
    }

    public void onRequestPermissionResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationUpdates();
                    grantedPermission = true;
                } else {
                }
                break;
            default:
        }
    }

    @SuppressLint("NewApi")
    private long getLocationAge(Location newLocation) {
        long locationAge;
        if (android.os.Build.VERSION.SDK_INT >= 17) {
            long currentTimeInMilli = (long) (SystemClock.elapsedRealtimeNanos() / 1000000);
            long locationTimeInMilli = (long) (newLocation.getElapsedRealtimeNanos() / 1000000);
            locationAge = currentTimeInMilli - locationTimeInMilli;
        } else {
            locationAge = System.currentTimeMillis() - newLocation.getTime();
        }
        return locationAge;
    }

    private boolean filterAndAddLocation(Location location) {
        long age = getLocationAge(location);
        if (age > 5 * 1000) { //more than 5 seconds
            Log.d(TAG, "Location is old");
            // oldLocationList.add(location);
            return false;
        }

        if (location.getAccuracy() <= 0) {
            Log.d(TAG, "Latitid and longitude values are invalid.");
            // noAccuracyLocationList.add(location);
            return false;
        }

        //setAccuracy(newLocation.getAccuracy());
        float horizontalAccuracy = location.getAccuracy();
        if (horizontalAccuracy > 10) { //10meter filter
            Log.d(TAG, "Accuracy is too low.");
            // inaccurateLocationList.add(location);
            disposeBag.notifyAll(new TTNewLocation(location.getLatitude(), location.getLongitude(), false, location.getAccuracy(), location.getBearing(), location.getAltitude(), object));
            return false;
        }


        /* Kalman Filter */
        float Qvalue;

        long locationTimeInMillis = (long) (location.getElapsedRealtimeNanos() / 1000000);
        long elapsedTimeInMillis = locationTimeInMillis - runStartTimeInMillis;

        if (currentSpeed == 0.0f) {
            Qvalue = 3.0f; //3 meters per second
        } else {
            Qvalue = currentSpeed; // meters per second
        }

        kalmanFilter.Process(location.getLatitude(), location.getLongitude(), location.getAccuracy(), elapsedTimeInMillis, Qvalue);
        double predictedLat = kalmanFilter.get_lat();
        double predictedLng = kalmanFilter.get_lng();

        Location predictedLocation = new Location("");//provider name is unecessary
        predictedLocation.setLatitude(predictedLat);//your coords of course
        predictedLocation.setLongitude(predictedLng);
        predictedLocation.setAccuracy(kalmanFilter.get_accuracy());
        float predictedDeltaInMeters = predictedLocation.distanceTo(location);

        if (predictedDeltaInMeters > 60) {
            Log.d(TAG, "Kalman Filter detects mal GPS, we should probably remove this from track");
            kalmanFilter.consecutiveRejectCount += 1;

            if (kalmanFilter.consecutiveRejectCount > 3) {
                kalmanFilter = new KalmanLatLong(3); //reset Kalman Filter if it rejects more than 3 times in raw.
            }

            return false;
        } else {
            kalmanFilter.consecutiveRejectCount = 0;
        }

        System.out.println("lat" + predictedLocation.getLatitude()
                + "lon" + predictedLocation.getLongitude());
        Log.d(TAG, "Location quality is good enough.");
        //Code to notify all observers that we got a good location
        disposeBag.notifyAll(new TTNewLocation(predictedLocation.getLatitude()
                , predictedLocation.getLongitude(), true, predictedLocation.getAccuracy()
                , location.getBearing(), location.getAltitude(), object));
        return true;
    }

    public void addObserver(DisposableObserver<TTNewLocation> observer) {
        disposeBag.add(observer);
    }

    public void removeObserver(DisposableObserver<TTNewLocation> observer) {
        if (!observer.isDisposed()) {
            observer.dispose();
        }
        //No need to check is present since it is checked inside the remove code
        disposeBag.remove(observer);
    }

    private void clearObservers() {
        // TODO: 03-11-2018 Call this from onstop
        disposeBag.clear();
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void stopLocationUpdates() {
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            mFusedLocationClient.removeLocationUpdates(mLocationCallback)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                        }
                    });
        }
        if (locationManager != null) {
            locationManager.unregisterGnssStatusCallback(mGnssStatusListener);
            locationManager.removeUpdates(locationListener);
            locationManager.removeNmeaListener(nmeaListener);
        }
    }

    //class for handling all active observers
    private class LocationObserver {
        List<DisposableObserver<TTNewLocation>> locationObservers;

        public LocationObserver() {
            init();
        }

        private void init() {
            locationObservers = new ArrayList<>();
        }

        public void add(DisposableObserver<TTNewLocation> observer) {
            if (locationObservers == null) {
                init();
            }
            if (observer != null) {
                locationObservers.add(observer);
            }
        }

        public void clear() {
            if (locationObservers != null) {
                for (int i = 0; i < locationObservers.size(); i++) {
                    DisposableObserver observer = locationObservers.get(i);
                    observer.dispose();
                }
                locationObservers.clear();
            }
        }

        public void remove(DisposableObserver<TTNewLocation> observer) {
            if (locationObservers == null) {
                init();
            } else {
                if (observer != null && locationObservers.contains(observer)) {
                    locationObservers.remove(observer);
                }
            }
        }

        public void notifyAll(TTNewLocation newLocation) {
            if (locationObservers == null) {
                init();
            } else {
                for (int i = 0; i < locationObservers.size(); i++) {
                    DisposableObserver<TTNewLocation> observer = locationObservers.get(i);
                    observer.onNext(newLocation);
                }
            }
        }
    }

    private void registerListener() {
        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location loc) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                switch (status) {
                    case LocationProvider.OUT_OF_SERVICE:
                        break;
                    case LocationProvider.TEMPORARILY_UNAVAILABLE:
                        break;
                    case LocationProvider.AVAILABLE:
                        break;
                }

            }
        };

        nmeaListener = new GpsStatus.NmeaListener() {
            public void onNmeaReceived(long timestamp, String message) {
                if (message.startsWith("$GNGSA") || message.startsWith("$GPGSA")) {
                    DilutionOfPrecision dop = NmeaUtils.getDop(message);
                    if (dop != null) {
                        try {
                            object.put("HDOP", dop.getHorizontalDop());
                            object.put("PDOP", dop.getPositionDop());
                            object.put("VDOP", dop.getVerticalDop());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        };
    }
}
