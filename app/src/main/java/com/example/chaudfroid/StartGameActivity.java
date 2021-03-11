package com.example.chaudfroid;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.navigation.fragment.NavHostFragment;

import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class StartGameActivity extends AppCompatActivity {
    float[] latLong = new float[2];
    public static final String MESSAGE = "com.example.chaudfroid.ADRESSE";

    @Override
    @RequiresApi(api = Build.VERSION_CODES.N)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_game);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        String adresse = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        Uri u = Uri.parse(adresse);

        Bitmap bitmap = null;
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(this.getContentResolver().openInputStream(u));

            if(exif.getLatLong(latLong)) {
               bitmap= MediaStore.Images.Media.getBitmap(this.getContentResolver(), u);

               System.out.println(latLong[0] + "//" + latLong[1]);

                String[] PERMISSIONS = {android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION};

                if(this.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        this.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this, PERMISSIONS, 112 );
                }

                GPSTracker tracker = new GPSTracker(this);
                if (!tracker.canGetLocation()) {
                    tracker.showSettingsAlert();
                } else {
                    System.out.println(tracker.getLatitude() + "//" + tracker.getLongitude());
                }

                Location userLocation = new Location("userLocation");

                userLocation.setLatitude(tracker.getLatitude());
                userLocation.setLongitude(tracker.getLongitude());

                Location photoLocation = new Location("photoLocation");

                photoLocation.setLatitude(latLong[0]);
                photoLocation.setLongitude(latLong[1]);

                float distance = userLocation.distanceTo(photoLocation);

                System.out.println("Distance : " + distance + "m");

                if(distance > 100000){
                    Toast.makeText(this, "Vous êtes à plus 100km de la cible, essayer une autre photo", Toast.LENGTH_SHORT).show();
                }
                else{
                    this.findViewById(R.id.fab).setEnabled(true);
                }
            }
            else{
                Toast.makeText(this, "Données GPS manquantes, selectionnez une nouvelle image", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        ImageView mImageView;
        mImageView = (ImageView) this.findViewById(R.id.imageView);
        mImageView.setImageBitmap(bitmap);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(StartGameActivity.this, GameActivity.class);
                myIntent.putExtra(MESSAGE, adresse); //Optional parameters
                startActivity(myIntent);
            }
        });

        SeekBar seekBar = this.findViewById(R.id.seekBar);
        TextView textView = this.findViewById(R.id.textView);
        textView.setText(50 + " mètres");

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 50;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue;
                textView.setText(progress + " mètres");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                textView.setText(progress + " mètres");
            }
        });
    }
}