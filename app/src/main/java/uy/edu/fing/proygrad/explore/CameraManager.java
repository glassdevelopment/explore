package uy.edu.fing.proygrad.explore;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;

/**
 * Created by gonzalomelov on 1/13/15.
 */
public class CameraManager {

    public static final String TAG = CameraManager.class.getName();

    private static final String NO_CAMERA_FEATURE = "No camera on this device";
    private static final String NO_FRONT_FACING_CAMERA_FOUND = "No front facing camera found";

    private static Camera camera;

    public static void openCamera() {
        int cameraId = findRearFacingCamera();
        try {
            camera = Camera.open(cameraId);
            // Work around for Camera preview issues.
            Camera.Parameters params = camera.getParameters();
            params.setPreviewFpsRange(30000, 30000);
            camera.setParameters(params);
        }
        catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public static void takePicture() {
        try {
            SurfaceTexture surfaceTexture = new SurfaceTexture(10);
            camera.setPreviewTexture(surfaceTexture);
            camera.startPreview();
            camera.takePicture(null, null, new PhotoHandler());
        }
        catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

    }

    public static void releaseCamera() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    private static int findFrontFacingCamera() {
        return findCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
    }

    private static int findRearFacingCamera() {
        return findCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
    }

    private static int findCamera(int cameraType) {
        int cameraId = -1;

        // Search for the rear camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i=0; i<numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == cameraType) {
                cameraId = i;
                break;
            }
        }

        return cameraId;
    }
}

