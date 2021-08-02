package floatwindowpermission;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import java.util.ArrayList;
import java.util.List;

public class TPermission {
    public  final  static String TAG="TPermission";
    public static final int CAPTURE_PERMISSION_REQUEST_CODE = 0x1123;
    public static final int OVERLAY_PERMISSION_REQUEST_CODE = 0x1124;
    public static  final String[] permissionManifest = {
            Manifest.permission.SYSTEM_ALERT_WINDOW,
            Manifest.permission.INTERNET,
            Manifest.permission.CAMERA,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    public static void requestNormalPermission(Activity mActivity){
        try{
            List<String> unHasPermissions = new ArrayList<>();
            for (String permission : permissionManifest) {
                int successFlag = PackageManager.PERMISSION_GRANTED;
                int checkRes = PermissionChecker.checkSelfPermission(mActivity, permission);
                if (checkRes != successFlag) {
                    unHasPermissions.add(permission);
                }
            }
            if ( unHasPermissions.size()>0){
                String[] unPers = new String[unHasPermissions.size()];
                for( int i=0;i<unPers.length; i++){
                    unPers[i] = unHasPermissions.get(i);
                }
                ActivityCompat.requestPermissions(mActivity, unPers, 1);
            }
        }
        catch (Exception er){
            Log.e(TAG, "requestNormalPermission: "+er.getMessage() );
        }
    }
    public static boolean hasOverlayPermission( Activity mActivity){
        boolean res = false;
        try{
            res= FloatWindowManager.getInstance().applyFloatWindow(mActivity);
        }
        catch (Exception er){
            Log.e(TAG, "hasOverlayPermission: "+er.getMessage() );
        }
        return res;
    }
    public static void requestCaptureScreenPermission(final Activity mActivity ){
        try{
            if (Build.VERSION.SDK_INT >= 24) {
                MediaProjectionManager mediaProjectionManager = (MediaProjectionManager)
                        mActivity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                try {
                    Intent intent = mediaProjectionManager.createScreenCaptureIntent();
                    mActivity.startActivityForResult(intent, CAPTURE_PERMISSION_REQUEST_CODE);
                } catch (ActivityNotFoundException ex) {
                    ex.printStackTrace();
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText( mActivity , "Start ScreenRecording failed, current device is NOT suuported!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                Toast.makeText(mActivity, "录屏需要5.0版本以上", Toast.LENGTH_LONG).show();
            }
        }
        catch (Exception er){
            Log.e(TAG, "requestCaptureScreenPermission: "+er.getMessage() );
        }
    }
}
