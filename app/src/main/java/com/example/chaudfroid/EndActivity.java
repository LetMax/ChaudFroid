package com.example.chaudfroid;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.media.ExifInterface;
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
import androidx.appcompat.widget.Toolbar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EndActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    String adresse;
    String adresseNouvellePhoto;

    Button boutonPhoto;
    Button boutonPartager;

    Location userLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        adresse = intent.getStringExtra(StartGameActivity.MESSAGE);
        System.out.println("PHOTO DE BASE URI : " + adresse);

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
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_STREAM, adresseNouvellePhoto);
            sendIntent.setType("image/*");

            Intent shareIntent = Intent.createChooser(sendIntent, null);
            startActivity(shareIntent);
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
                File file = createImageFile();
                OutputStream fOut = new FileOutputStream(file);
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                fOut.flush();
                fOut.close();

                MediaStore.Images.Media.insertImage(getContentResolver(),file.getAbsolutePath(),file.getName(),file.getName());

                ExifInterface exif = new ExifInterface(file.getCanonicalPath());
                ExifInterface exif2 = new ExifInterface(this.getContentResolver().openInputStream(Uri.parse(adresse)));

                GPSTracker tracker = new GPSTracker(this);
                if (!tracker.canGetLocation()) {
                    tracker.showSettingsAlert();
                }

                userLocation = new Location("userLocation");
                userLocation.setLatitude(tracker.getLatitude());
                userLocation.setLongitude(tracker.getLongitude());
                tracker.stopUsingGPS();

                double alat = Math.abs(userLocation.getLatitude());
                String dms = Location.convert(alat, Location.FORMAT_SECONDS);
                String[] splits = dms.split(":");
                String[] secnds = (splits[2]).split("\\.");
                String seconds;
                if(secnds.length==0) seconds = splits[2];
                else seconds = secnds[0];

                String latitudeStr = splits[0] + "/1," + splits[1] + "/1," + seconds + "/1";
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, latitudeStr);
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, userLocation.getLatitude()>0?"N":"S");

                double alon = Math.abs(userLocation.getLongitude());

                dms = Location.convert(alon, Location.FORMAT_SECONDS);
                splits = dms.split(":");
                secnds = (splits[2]).split("\\.");

                if(secnds.length==0) seconds = splits[2];
                else seconds = secnds[0];

                String longitudeStr = splits[0] + "/1," + splits[1] + "/1," + seconds + "/1";

                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, longitudeStr);
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, userLocation.getLongitude()>0?"E":"W");
                exif.setAttribute(ExifInterface.TAG_DATETIME,"2013:06:21 00:00:07");
                exif.saveAttributes();
                System.out.println("IMAGE : " + exif2.getAttribute(ExifInterface.TAG_GPS_LATITUDE) + " (lat) // " + exif2.getAttribute(ExifInterface.TAG_GPS_LONGITUDE) + " (long)");
                System.out.println("EXIF DE MERDE : " + exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE) + " (lat) // " + exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE) + " (long)");

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
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