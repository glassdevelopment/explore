package uy.edu.fing.proygrad.explore;

import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by gonzalomelov on 1/13/15.
 */
public class PhotoHandler implements Camera.PictureCallback {

    private static final String TAG = PhotoHandler.class.getName();

    private static final String CANT_CREATE_DIRECTORY = "Can't create directory to save image";
    private static final String FILE_NAME = "CameraAPIDemo";
    private static final String FILE_NOT_SAVED = "File not saved";
    private static final String IMAGE_SAVED = "New image saved";

    /**
     * Stores the picture taken in SD pictures directory with name FILE_NAME
     * @param data photo bytes
     * @param camera camera object
     */
    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        File pictureFileDir = getDir();

        if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {
            Log.d(TAG, CANT_CREATE_DIRECTORY);
            CameraManager.releaseCamera();
            return;
        }

        Date photoDate = new Date();
        String date = new SimpleDateFormat("yyyymmddhhmmss").format(photoDate);
        String photoFile = "Picture_" + date + ".jpg";

        String filename = pictureFileDir.getPath() + File.separator + photoFile;

        File pictureFile = new File(filename);

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();
        }
        catch (Exception error) {
            Log.d(TAG, FILE_NOT_SAVED + error.getMessage());
        }
        finally {
            CameraManager.releaseCamera();
        }
    }

    /**
     * Gets the file FILE_NAME object in SD pictures directory
     * @return File object
     */
    private File getDir() {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(dir, FILE_NAME);
    }
}

