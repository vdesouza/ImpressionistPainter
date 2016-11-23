package com.vdesouza.impressionistpainter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity implements OnMenuItemClickListener, SensorEventListener {

    private String TAG = "ImpressionistViewMain";

    private static int RESULT_LOAD_IMAGE = 1;
    private ImpressionistView _impressionistView;
    private TextView brushSettingsTextView;

    // These images are downloaded and added to the Android Gallery when the 'Download Images' button is clicked.
    // This was super useful on the emulator where there are no images by default
    private static String[] IMAGE_URLS ={
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/BoliviaBird_PhotoByJonFroehlich(Medium).JPG",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/BolivianDoor_PhotoByJonFroehlich(Medium).JPG",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/MinnesotaFlower_PhotoByJonFroehlich(Medium).JPG",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/PeruHike_PhotoByJonFroehlich(Medium).JPG",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/ReginaSquirrel_PhotoByJonFroehlich(Medium).JPG",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/SucreDog_PhotoByJonFroehlich(Medium).JPG",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/SucreStreet_PhotoByJonFroehlich(Medium).JPG",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/SucreStreet_PhotoByJonFroehlich2(Medium).JPG",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/SucreWine_PhotoByJonFroehlich(Medium).JPG",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/WashingtonStateFlower_PhotoByJonFroehlich(Medium).JPG",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/JonILikeThisShirt_Medium.JPG",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/JonUW_(853x1280).jpg",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/MattMThermography_Medium.jpg",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/PinkFlower_PhotoByJonFroehlich(Medium).JPG",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/PinkFlower2_PhotoByJonFroehlich(Medium).JPG",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/PurpleFlowerPlusButterfly_PhotoByJonFroehlich(Medium).JPG",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/WhiteFlower_PhotoByJonFroehlich(Medium).JPG",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/YellowFlower_PhotoByJonFroehlich(Medium).JPG",
    };

    // Used for accelerometer sensor
    public static SensorManager sensorManager = null;
    public float accelX;
    public float accelY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _impressionistView = (ImpressionistView)findViewById(R.id.viewImpressionist);
        ImageView imageView = (ImageView)findViewById(R.id.viewImage);
        _impressionistView.setImageView(imageView);
        brushSettingsTextView = (TextView) findViewById(R.id.textViewBrushSettings);

        updateTextView();

        // Get a reference to a SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

    }

    // when accelerometer data changes, move the brush
    float[] mAccelMotion = new float[3];
    float[] mPrevAccelMotion = new float[3];

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        // get sensor data
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mAccelMotion = sensorEvent.values;
        }

        float prevX = accelX;
        float prevY = accelY;
        // calculate x axis changes
        // if there hasn't been movement, don't do anything
        if (mAccelMotion[0] == mPrevAccelMotion[0]) {
            // increment current position by amount moved.
            // sensor data is increased by pow(3) so that when there is
            // more tilt, there is greater distance between paint
            accelX += Math.pow(mAccelMotion[1], 3);
            if (accelX > _impressionistView.getWidth()) {
                accelX = _impressionistView.getWidth();
            } else if (accelX < 0) {
                accelX = 0;
            }
        }
        // calculate y axis changes
        if (mAccelMotion[2] == mPrevAccelMotion[2]) {
            accelY += Math.pow(mAccelMotion[2], 3);
            if (accelY > _impressionistView.getHeight()) {
                accelY = _impressionistView.getHeight();
            } else if (accelY < 0) {
                accelY = 0;
            }
        }

        // get distance between new coordinates and previous
        double distance = Math.sqrt((Math.pow((accelX - prevX), 2) + Math.pow((accelY - prevY), 2)));

        // set brush size based on speed of brush
        float brushSize = (_impressionistView._defaultRadius + (float) distance) / 2;
        if (brushSize > _impressionistView._maxBrushRadius) { brushSize =  _impressionistView._maxBrushRadius;}
        if (brushSize < _impressionistView._minBrushRadius) { brushSize =  _impressionistView._minBrushRadius;}

        int color = _impressionistView.getColorAtTouch(Math.round(accelX), Math.round(accelY));
        _impressionistView.setPaint(color);
        _impressionistView.drawImpressionistPainting(accelX, accelY, brushSize);
        _impressionistView.invalidate();

        mPrevAccelMotion = mAccelMotion;

    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {}

    @Override
    protected void onResume() {
        if (_impressionistView.getBrushEffect().equals(BrushEffect.Accelerometer)){
            // Register this class as a listener for the accelerometer sensor
            sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_FASTEST);
        }
        super.onResume();
    }

    @Override
    protected void onStop() {
        if (_impressionistView.getBrushEffect().equals(BrushEffect.Accelerometer)){
            // Unregister the listener
            sensorManager.unregisterListener(this);
        }
        super.onStop();
    }

    // creates the options on the action bar for download, pick photo, clear, and save
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.file_controls_menus, menu);
        return true;
    }

    // performs actions of options clicked for action bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        // handles actions for selecting tools
        switch (item.getItemId()) {
            case R.id.buttonDownloadImages:
                onButtonClickDownloadImages();
                break;
            case R.id.buttonLoadImage:
                onButtonClickLoadImage();
                break;
            case R.id.buttonClear:
                onButtonClickClear();
                break;
            case R.id.buttonSaveImage:
                saveAlert();
                break;
        }
        return true;
    }


    public void onButtonClickClear() {
        new AlertDialog.Builder(this)
                .setTitle("Clear Painting?")
                .setMessage("Do you really want to clear your painting?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Toast.makeText(MainActivity.this, "Painting cleared", Toast.LENGTH_SHORT).show();
                        _impressionistView.clearPainting();
                    }})
                .setNegativeButton(android.R.string.no, null).show();
    }

    public void onButtonClickSetBrushShape(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.popup_menu_shape);
        popupMenu.show();
    }
    public void onButtonClickSetBrushEffect(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.popup_menu_effect);
        popupMenu.show();
    }
    public void onButtonClickSetBrushColor(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.popup_menu_color);
        popupMenu.show();
    }

    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            // brush shapes
            case R.id.menuCircle:
                Toast.makeText(this, "Circle Brush", Toast.LENGTH_SHORT).show();
                _impressionistView.setBrushType(BrushType.Circle);
                updateTextView();
                return true;
            case R.id.menuSquare:
                Toast.makeText(this, "Square Brush", Toast.LENGTH_SHORT).show();
                _impressionistView.setBrushType(BrushType.Square);
                updateTextView();
                return true;
            case R.id.menuSpray:
                Toast.makeText(this, "Spray Brush", Toast.LENGTH_SHORT).show();
                _impressionistView.setBrushType(BrushType.Spray);
                updateTextView();
                return true;
            // painting effects
            case R.id.menuNormal:
                if (_impressionistView.getBrushEffect().equals(BrushEffect.Accelerometer)){
                    // Unregister the sensor listener
                    sensorManager.unregisterListener(this);
                }
                Toast.makeText(this, "No Effect", Toast.LENGTH_SHORT).show();
                _impressionistView.setBrushEffect(BrushEffect.Defualt);
                updateTextView();
                return true;
            case R.id.menuVelocity:
                if (_impressionistView.getBrushEffect().equals(BrushEffect.Accelerometer)){
                    // Unregister the sensor listener
                    sensorManager.unregisterListener(this);
                }
                Toast.makeText(this, "Velocity Mode", Toast.LENGTH_SHORT).show();
                _impressionistView.setBrushEffect(BrushEffect.Velocity);
                updateTextView();
                return true;
            case R.id.menuAccelerometer:
                // Register this class as a listener for the accelerometer sensor
                sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                        SensorManager.SENSOR_DELAY_NORMAL);
                // Set the center of the canvas as the starting point for the brush
                accelX = _impressionistView.getWidth() / 2;
                accelY = _impressionistView.getHeight() / 2;
                Toast.makeText(this, "Accelerometer Mode: Tilt Device to paint!", Toast.LENGTH_LONG).show();
                _impressionistView.setBrushEffect(BrushEffect.Accelerometer);
                updateTextView();
                return true;
            // brush colors
            case R.id.menuDefaultColor:
                Toast.makeText(this, "Normal Color Mode", Toast.LENGTH_SHORT).show();
                _impressionistView.setBrushColor(BrushColor.Defualt);
                updateTextView();
                return true;
            case R.id.menuBlackWhite:
                Toast.makeText(this, "Grayscale Mode", Toast.LENGTH_SHORT).show();
                _impressionistView.setBrushColor(BrushColor.BlackAndWhite);
                updateTextView();
                return true;
            case R.id.menuComplementary:
                Toast.makeText(this, "Complementary Color Mode", Toast.LENGTH_SHORT).show();
                _impressionistView.setBrushColor(BrushColor.Complementary);
                updateTextView();
                return true;
        }
        return false;
    }

    // updates the text view to show all the brush settings
    private void updateTextView() {
        brushSettingsTextView.setText(getString(R.string.brush_settings,
                _impressionistView.getBrushShapeName(),
                _impressionistView.getBrushColorName(),
                _impressionistView.getBrushEffectName()));
    }

    // Save Dialog
    private void saveAlert() {
        AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
        saveDialog.setTitle("Save drawing");
        saveDialog.setMessage("Save drawing to device Gallery?");
        saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which) {
                fixMediaDir();
                _impressionistView.savePainting();
            }
        });
        saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                dialog.cancel();
            }
        });
        saveDialog.show();
    }

    // fix found on http://stackoverflow.com/a/33069416 to save image to gallery app on emulator
    void fixMediaDir() {
        File sdcard = Environment.getExternalStorageDirectory();
        if (sdcard != null) {
            File mediaDir = new File(sdcard, "DCIM/Camera");
            if (!mediaDir.exists()) {
                mediaDir.mkdirs();
            }
        }
    }

    /**
     * Downloads test images to use in the assignment. Feel free to use any images you want. I only made this
     * as an easy way to get images onto the emulator.
     *
     *
     */
    public void onButtonClickDownloadImages(){

        // Without this call, the app was crashing in the onActivityResult method when trying to read from file system
        FileUtils.verifyStoragePermissions(this);

        // Amazing Stackoverflow post on downloading images: http://stackoverflow.com/questions/15549421/how-to-download-and-save-an-image-in-android
        final BasicImageDownloader imageDownloader = new BasicImageDownloader(new BasicImageDownloader.OnImageLoaderListener() {

            @Override
            public void onError(String imageUrl, BasicImageDownloader.ImageError error) {
                Log.v("BasicImageDownloader", "onError: " + error);
            }

            @Override
            public void onProgressChange(String imageUrl, int percent) {
                Log.v("BasicImageDownloader", "onProgressChange: " + percent);
            }

            @Override
            public void onComplete(String imageUrl, Bitmap downloadedBitmap) {
                File externalStorageDirFile = Environment.getExternalStorageDirectory();
                String externalStorageDirStr = Environment.getExternalStorageDirectory().getAbsolutePath();
                boolean checkStorage = FileUtils.checkPermissionToWriteToExternalStorage(MainActivity.this);
                String guessedFilename = URLUtil.guessFileName(imageUrl, null, null);

                // See: http://developer.android.com/training/basics/data-storage/files.html
                // Get the directory for the user's public pictures directory.
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), guessedFilename);
                try {
                    boolean compressSucceeded = downloadedBitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(file));
                    FileUtils.addImageToGallery(file.getAbsolutePath(), getApplicationContext());
                    Toast.makeText(getApplicationContext(), "Saved to " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        for(String url: IMAGE_URLS){
            imageDownloader.download(url, true);
        }
    }

    /**
     * Loads an image from the Gallery into the ImageView
     *
     */
    public void onButtonClickLoadImage(){

        // Without this call, the app was crashing in the onActivityResult method when trying to read from file system
        FileUtils.verifyStoragePermissions(this);

        Intent i = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }

    /**
     * Called automatically when an image has been selected in the Gallery
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri imageUri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                ImageView imageView = (ImageView) findViewById(R.id.viewImage);

                // destroy the drawing cache to ensure that when a new image is loaded, its cached
                imageView.destroyDrawingCache();
                imageView.setImageBitmap(bitmap);
                imageView.setDrawingCacheEnabled(true);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}

