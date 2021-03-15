package com.example.chaudfroid;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class EndActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    String adresse;
    String adresseNouvellePhoto;

    File file = null;

    Button boutonPhoto;
    Button boutonPartager;

    Location userLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);

        Intent intent = getIntent();
        adresse = intent.getStringExtra(StartGameActivity.MESSAGE);

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(adresse));

            ImageView mImageView = this.findViewById(R.id.imageViewEnd);
            mImageView.setImageBitmap(bitmap);

        } catch (IOException e) {
            e.printStackTrace();
        }

        boutonPhoto = findViewById(R.id.buttonPhoto);
        boutonPhoto.setOnClickListener(v -> {

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            try {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getApplicationContext(), "Impossible de prendre une photo.", Toast.LENGTH_LONG).show();
            }
        });

        boutonPartager = findViewById(R.id.buttonShare);
        boutonPartager.setOnClickListener(v -> {

            Intent intentShareFile = new Intent(Intent.ACTION_SEND);

            intentShareFile.setType(URLConnection.guessContentTypeFromName(file.getName()));

            Uri uri = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName()+".fileprovider", file);

            intentShareFile.putExtra(Intent.EXTRA_STREAM, uri);

            List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(intentShareFile, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            startActivity(intentShareFile);
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ImageView imageView = findViewById(R.id.imageViewEnd2);
            imageView.setImageBitmap(imageBitmap);

            try {
                file = createImageFile();
                OutputStream fOut = new FileOutputStream(file);
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);

                fOut.flush();
                fOut.close();

                GPSTracker tracker = new GPSTracker(this);
                if (!tracker.canGetLocation()) {
                    tracker.showSettingsAlert();
                }

                ExifInterface exif = new ExifInterface(file);

                userLocation = new Location("userLocation");
                userLocation.setLatitude(tracker.getLatitude());
                userLocation.setLongitude(tracker.getLongitude());
                tracker.stopUsingGPS();

                exif.setGpsInfo(userLocation);

                exif.saveAttributes();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            boutonPartager.setEnabled(true);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpeg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        adresseNouvellePhoto = image.getAbsolutePath();
        return image;
    }
}