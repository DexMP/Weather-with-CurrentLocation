package xyz.rpka.gpstag;

import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.lang.ref.WeakReference;

public class OwnPermissionListener implements PermissionListener {

    /* Variables */
    private WeakReference<MainActivity> activityReference;
    private MainActivity activity;

    OwnPermissionListener(MainActivity activityContext) {
        this.activityReference = new WeakReference<>(activityContext);
    }

    @Override
    public void onPermissionGranted(PermissionGrantedResponse response) {
        activity = activityReference.get();
        if(activity != null) {
            activity.beginLocationUpdates(MainActivity.LAUNCH_SOURCE_START);
        }
    }

    @Override
    public void onPermissionDenied(PermissionDeniedResponse response) {
        activity = activityReference.get();
        assert activity != null;
        if(response.isPermanentlyDenied()) {
            activity.showErrorState(MainActivity.ERROR_DENIED_PERMANENTLY);
        } else {
            activity.showErrorState(MainActivity.ERROR_PERMISSION_DENIED);
        }
    }

    @Override
    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
            token.continuePermissionRequest();
    }
}
