package com.example.chaudfroid;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;

public class StartGameActivity extends AppCompatActivity {
    float[] latLong = new float[2];
    public static final String MESSAGE = "com.example.chaudfroid.ADRESSE";
    public static final String MESSAGE_SENSIBILITE = "com.example.chaudfroid.SENSIBILITE";
    public static final int SENSIBILITE_DEFAUT = 50;
    private int sensibilite = SENSIBILITE_DEFAUT;

    @Override
    @RequiresApi(api = Build.VERSION_CODES.N)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_game);
        /*Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/

        Intent intent = getIntent();
        String adresse = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        Uri u = Uri.parse(adresse);

        Bitmap bitmap = null;
        ExifInterface exif;
        try {
            exif = new ExifInterface(this.getContentResolver().openInputStream(u));

            if(exif.getLatLong(latLong)) {
               bitmap= MediaStore.Images.Media.getBitmap(this.getContentResolver(), u);

                String[] PERMISSIONS = {android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION};

                if(this.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        this.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this, PERMISSIONS, 112 );
                }

                GPSTracker tracker = new GPSTracker(this);
                if (!tracker.canGetLocation()) {
                    tracker.showSettingsAlert();
                }

                Location userLocation = new Location("userLocation");

                userLocation.setLatitude(tracker.getLatitude());
                userLocation.setLongitude(tracker.getLongitude());
                tracker.stopUsingGPS();

                Location photoLocation = new Location("photoLocation");

                photoLocation.setLatitude(latLong[0]);
                photoLocation.setLongitude(latLong[1]);

                float distance = userLocation.distanceTo(photoLocation);

                if(distance > 100000){
                    Toast.makeText(this, "Vous êtes à plus 100km de la cible, essayer une autre photo", Toast.LENGTH_LONG).show();
                }
                else{
                    this.findViewById(R.id.fab).setEnabled(true);
                }
            }
            else{
                Toast.makeText(this, "Données GPS manquantes, selectionnez une nouvelle image", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        ImageView mImageView;
        mImageView = (ImageView) this.findViewById(R.id.imageView);
        mImageView.setImageBitmap(bitmap);

        SeekBar seekBar = this.findViewById(R.id.seekBar);
        TextView textView = this.findViewById(R.id.textView);
        textView.setText(sensibilite + " mètres");

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                sensibilite = progresValue;
                textView.setText(sensibilite + " mètres");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                textView.setText(sensibilite + " mètres");
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent myIntent = new Intent(StartGameActivity.this, GameActivity.class);
            myIntent.putExtra(MESSAGE, adresse); //Optional parameters
            myIntent.putExtra(MESSAGE_SENSIBILITE, sensibilite);
            startActivity(myIntent);
        });
    }
}