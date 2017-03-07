package com.falsebank.falsenote;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FalseMainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("jpgt");
        System.loadLibrary("lept");
        System.loadLibrary("tess");
    }

    public static final String PACKAGE_NAME = "com.falsebank.falsenote";
    public static final String DATA_PATH = Environment
            .getExternalStorageDirectory().toString() + "/FalseBank/";

    // You should have the trained data file in assets folder
    // You can get them at:
    // https://github.com/tesseract-ocr/tessdata
    public static final String lang = "eng+kor";

    private static final String TAG = "FalseMainActivity.java";

    protected Button _button;
    // protected ImageView _image;
    protected EditText _field;
    protected String _path;
    protected boolean _taken;

    protected static final String PHOTO_TAKEN = "photo_taken";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_false_main);
        //version check git test why
        if (Build.VERSION.SDK_INT >= 23)
        {
            checkAllPermission();
        }

        String[] paths = new String[] { DATA_PATH, DATA_PATH + "tessdata/" };

        for (String path : paths) {
            File dir = new File(path);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.v(TAG, "ERROR: Creation of directory " + path + " on sdcard failed");
                    return;
                } else {
                    Log.v(TAG, "Created directory " + path + " on sdcard");
                }
            }

        }
        String lang_one[] = lang.split("\\+");
        for(String one : lang_one) {
            dataCopy(one);
        }
        // lang.traineddata file with the app (in assets folder)
        // You can get them at:
        // http://code.google.com/p/tesseract-ocr/downloads/list
        // This area needs work and optimization




        // _image = (ImageView) findViewById(R.id.image);
        _field = (EditText) findViewById(R.id.field);
        _button = (Button) findViewById(R.id.button);
        _button.setOnClickListener(new ButtonClickHandler());

        _path = DATA_PATH + "/ocr.jpg";
    }
    private void dataCopy(String one) {
        // lang.traineddata file with the app (in assets folder)
        // You can get them at:
        // http://code.google.com/p/tesseract-ocr/downloads/list
        // This area needs work and optimization
        if (!(new File(DATA_PATH + "tessdata/" + one + ".traineddata")).exists()) {
            try {

                AssetManager assetManager = getAssets();
                InputStream in = assetManager.open("tessdata/" + one + ".traineddata");
                //GZIPInputStream gin = new GZIPInputStream(in);
                OutputStream out = new FileOutputStream(DATA_PATH
                        + "tessdata/" + one + ".traineddata");

                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                //while ((lenf = gin.read(buff)) > 0) {
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                //gin.close();
                out.close();

                Log.v(TAG, "Copied " + one + " traineddata");
            } catch (IOException e) {
                Log.e(TAG, "Was unable to copy " + one + " traineddata " + e.toString());
            }
        }
    }

    private static final int WRITE_REQUEST_CODE = 1;
    private static final int CAMERA_REQUEST_CODE = 2;
    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(FalseMainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }
    private void checkAllPermission(){
        int result = ContextCompat.checkSelfPermission(FalseMainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(result != PackageManager.PERMISSION_GRANTED){
            requestWritePermission();
        }
        result = ContextCompat.checkSelfPermission(FalseMainActivity.this, Manifest.permission.CAMERA);
        if(result != PackageManager.PERMISSION_GRANTED){
            requestCameraPermission();
        }
    }
    private void requestWritePermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(FalseMainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(FalseMainActivity.this, "Write External Storage permission allows us to do store images. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(FalseMainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_REQUEST_CODE);
        }
    }
    private void requestCameraPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(FalseMainActivity.this, android.Manifest.permission.CAMERA)) {
            Toast.makeText(FalseMainActivity.this, "Use CAMERA permission allows us to take a picture. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(FalseMainActivity.this, new String[]{android.Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case WRITE_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive .");
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                break;
            case CAMERA_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use camera .");
                } else {
                    Log.e("value", "Permission Denied, You cannot use camera .");
                }
                break;
        }
    }
    View.OnClickListener btnClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            Log.v(TAG, "Starting Camera app");
            startCameraActivity();
        }
    };
    public class ButtonClickHandler implements View.OnClickListener {
        public void onClick(View view) {
            Log.v(TAG, "Starting Camera app");
            startCameraActivity();
        }
    }

    // Simple android photo capture:
    // http://labs.makemachine.net/2010/03/simple-android-photo-capture/
    protected void startCameraActivity() {
        File file = new File(_path);
        Uri outputFileUri = Uri.fromFile(file);

        final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

        startActivityForResult(intent, 0);
    }

    @Override   protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.i(TAG, "resultCode: " + resultCode);

        if (resultCode == -1) {
            onPhotoTaken();
        } else {
            Log.v(TAG, "User cancelled");
        }
    }

    @Override   protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(FalseMainActivity.PHOTO_TAKEN, _taken);
    }

    @Override   protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.i(TAG, "onRestoreInstanceState()");
        if (savedInstanceState.getBoolean(FalseMainActivity.PHOTO_TAKEN)) {
            onPhotoTaken();
        }
    }
    private Bitmap getCorrectOrientationTakenPic(String filePath){
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, bounds);

        BitmapFactory.Options opts = new BitmapFactory.Options();
        Bitmap bm = BitmapFactory.decodeFile(filePath, opts);
        int rotationAngle = 0;
        try {
            ExifInterface exif = new ExifInterface(filePath);
            String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
            int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;
        } catch (IOException e) {
            Log.e(TAG, "Couldn't correct orientation: " + e.toString());
        }
        Log.e(TAG, ">>>>>>>rotationAngle:"+rotationAngle+"  bm.getWidth():"+bm.getWidth()+"  bm.getHeight():"+bm.getHeight());
        //정면을 보고 가로로 찍은 사진: 정상 >>>>>>>>>rotationAngle:0  bm.getWidth():5312  bm.getHeight():2988
        //아래를 향하여 가로로 찍은 사진: 비정상 >>>>>>>rotationAngle:90  bm.getWidth():5312  bm.getHeight():2988
        //정면을 보고 세로로 찍은 사진: 정상 >>>>>>>rotationAngle:90  bm.getWidth():5312  bm.getHeight():2988
        //아래를 보고 세로로 찍은 사진: 비정상 >>>>>>>rotationAngle:90  bm.getWidth():5312  bm.getHeight():2988
        Matrix matrix = new Matrix();
        matrix.setRotate(rotationAngle, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);


        try
        {
            Bitmap converted = Bitmap.createBitmap(bm, 0, 0,
                    bm.getWidth(), bm.getHeight(), matrix, true);
            if(bm != converted)
            {
                bm.recycle();
                bm = converted;
            }
        }catch(OutOfMemoryError ex){
            // 메모리가 부족하여 회전을 시키지 못할 경우 그냥 원본을 반환합니다.
        }
        return bm;
    }
    protected void onPhotoTaken() {
        _taken = true;
        Bitmap bitmap = getCorrectOrientationTakenPic(_path);
        /*
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;

        Bitmap bitmap = BitmapFactory.decodeFile(_path, options);

        try {
            ExifInterface exif = new ExifInterface(_path);
            int exifOrientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            Log.v(TAG, "Orient>>>>>>>>>>>>>>>: " + exifOrientation);

            int rotate = 0;

            switch (exifOrientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
            }

            Log.v(TAG, "Rotation>>>>>>>>>>>>>: " + rotate);

            if (rotate != 0) {

                // Getting width & height of the given image.
                int w = bitmap.getWidth();
                int h = bitmap.getHeight();

                // Setting pre rotate
                Matrix mtx = new Matrix();
                mtx.preRotate(rotate);

                // Rotating Bitmap
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
            }

            // Convert to ARGB_8888, required by tess
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        } catch (IOException e) {
            Log.e(TAG, "Couldn't correct orientation: " + e.toString());
        }
        */
        // _image.setImageBitmap( bitmap );            Log.v(TAG, "Before baseApi");

        TessBaseAPI baseApi = new TessBaseAPI();
        baseApi.setDebug(true);
        baseApi.init(DATA_PATH, lang);
        baseApi.setImage(bitmap);

        String recognizedText = baseApi.getUTF8Text();

        baseApi.end();

        // You now have the text in recognizedText var, you can do anything with it.
        // We will display a stripped out trimmed alpha-numeric version of it (if lang is eng)
        // so that garbage doesn't make it to the display.
        Log.v(TAG, "OCRED TEXT: " + recognizedText);

        //if ( lang.equalsIgnoreCase("eng") ) {
        // recognizedText = recognizedText.replaceAll("[^a-zA-Z0-9]+", " ");
        //}            recognizedText = recognizedText.trim();

        if ( recognizedText.length() != 0 ) {
            _field.setText(_field.getText().toString().length() == 0 ? recognizedText : _field.getText() + " " + recognizedText);
            _field.setSelection(_field.getText().toString().length());
        }

        // Cycle done.
    }

    // www.Gaut.am was here
    // Thanks for reading!


    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
