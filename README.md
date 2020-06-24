# LocationService
Android library to simplify the ease of getting location from location provider. We have written a wrapper on top of Fused location provider.
Improved accuracy & threshold checks for minimum horizontal accuracy and using **Kalman Filter**. <br/>

Application context is only necessary while initialising location manager and run-time will be asked automatically if not given.



# Installing 

### Step 1:- Add it in your root build.gradle at the end of repositories:

```
all projects {
        repositories {
            ...
            maven { url 'https://jitpack.io' }
        }
    }
````
### Step 2:- Add the dependency:
```
 implementation 'com.github.aadit33:LocationService:1.9.2'
```

## Add the required permissions
For fine location (GPS location), add the following permission in your AndroidManifest.xml:
```
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```
For coarse location (network location), add the following permission in your AndroidManifest.xml:
```
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-feature android:name="android.hardware.location.gps" />
```
# Retrieve the location from the device
Add the following code in activity/ fragment

    private CoordinateManager locationManager;

    private void requestLocationUpdates() {
        locationManager = new CoordinateManager(getApplication());
        locationManager.activityAttached(this);
        locationManager.addObserver(locationObserver);
    }
    
        private DisposableObserver<TTNewLocation> locationObserver = new DisposableObserver<TTNewLocation>() {
        @Override
        public void onNext(TTNewLocation ttNewLocation) {
           Toast.makeText(MainActivity.this, "Latitude:" + ttNewLocation.getLat() + " And Longitude:" + ttNewLocation.getLng() + " isAccurate:"
                    + ttNewLocation.getAccurate() + " Accuracy:" + ttNewLocation.getAccuracy(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onComplete() {

        }
    }
    
    //call for run time permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        locationManager.onRequestPermissionResult(requestCode, permissions, grantResults);
    }
    
        @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        locationManager.onPermissionReceived(requestCode , resultCode);
    }
    
    //stop location updates
    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationManager.removeObserver(locationObserver);
    }
    
    
    
    
