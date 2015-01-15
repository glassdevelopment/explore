package uy.edu.fing.proygrad.explore;

import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

    private static final MediaType MEDIA_TYPE_JPG =  MediaType.parse("image/jpeg");

    private Context context;

    public PhotoHandler(Context context) {
        this.context = context;
    }

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

            // Upload the file and then delete it
            processImage(pictureFile);
        }
        catch (Exception error) {
            Log.d(TAG, FILE_NOT_SAVED + error.getMessage());
        }
        finally {
            CameraManager.releaseCamera();
        }
    }

    private void processImage(File pictureFile) {
        // Read the app folder on Pictures folder and the file with the specified name
        // Upload it to camfind via post
        // Start something that each 2 sec pulls camfind to check if the processing has completed
        // If true then show notifications on glass of the objects found
        // Delete the file


        OkHttpClient client = new OkHttpClient();
        RequestBody body = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addPart(
                        Headers.of("Content-Disposition", "form-data; name=\"focus[x]\""),
                        RequestBody.create(null, "480")
                )
                .addPart(
                        Headers.of("Content-Disposition", "form-data; name=\"focus[y]\""),
                        RequestBody.create(null, "640")
                )
                .addPart(
                        Headers.of("Content-Disposition", "form-data; name=\"image_request[altitude]\""),
                        RequestBody.create(null, "27.912109375")
                )
                .addPart(
                        Headers.of("Content-Disposition", "form-data; name=\"image_request[language]\""),
                        RequestBody.create(null, "en")
                )
                .addPart(
                        Headers.of("Content-Disposition", "form-data; name=\"image_request[latitude]\""),
                        RequestBody.create(null, "35.8714220766008")
                )
                .addPart(
                        Headers.of("Content-Disposition", "form-data; name=\"image_request[locale]\""),
                        RequestBody.create(null, "en_US")
                )
                .addPart(
                        Headers.of("Content-Disposition", "form-data; name=\"image_request[longitude]\""),
                        RequestBody.create(null, "14.35832030022")
                )
//                .addPart(
//                        Headers.of("Content-Disposition", "form-data; name=\"image_request[remote_image_url]\""),
//                        RequestBody.create(null, "http://upload.wikimedia.org/wikipedia/en/2/2d/Mashape_logo.png")
//                )
                .addPart(
                        Headers.of("Content-Disposition", "form-data; name=\"image_request[image]\"; filename=\"explore.png\""),
                        RequestBody.create(MEDIA_TYPE_JPG, pictureFile)
                )
                .build();

        Request request = new Request.Builder()
                .url("https://camfind.p.mashape.com/image_requests")
                .header("X-Mashape-Key", "t1ohJF4J0tmsh3ZinpXxzpqwel0Sp1QHKaUjsnz1rKK7wVbzaR")
//                .header("Content-Type", "application/x-www-form-urlencoded")
//                .header("Accept", "application/json")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.e(TAG, e.getMessage());
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String responseString = response.body().string();
                Log.e(TAG, responseString);

                if (!response.isSuccessful()) throw new IOException("Unexpected code:  " + response);

                try {
                    JSONObject json = new JSONObject(responseString);
                    String token = json.getString("token");
                    Log.e(TAG, token);

                    Intent intent = new Intent();
                    intent.setAction("uy.edu.fing.proygrad.explore.TOKEN_INTENT");
                    intent.putExtra("token", token);
                    PhotoHandler.this.context.sendBroadcast(intent);

                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        });




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

