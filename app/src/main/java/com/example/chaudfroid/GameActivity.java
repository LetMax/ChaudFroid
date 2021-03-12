package com.example.chaudfroid;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.io.IOException;

public class GameActivity extends AppCompatActivity {

    float[] latLong = new float[2];
    String adresse;
    Location userLocation;
    Location photoLocation;
    int sensibilite;
    int sensibiliteJaune;
    int sensibiliteBleue;
    int etat = 0;

    int COULEUR_ROUGE = Color.parseColor("#d32f2f");
    int COULEUR_JAUNE = Color.parseColor("#ffeb3b");
    int COULEUR_BLEUE = Color.parseColor("#4fc3f7");
    int FOND_ROUGE = Color.parseColor("#af4448");
    int FOND_JAUNE = Color.parseColor("#ffecb3");
    int FOND_BLEU = Color.parseColor("#e6ffff");
    int TRANSPARENT = Color.parseColor("#10000000");

    Vibrator v;
    long[] pattern_bleu = {0, 100, 100};
    long[] pattern_jaune = {0, 150, 100, 150, 100};
    long[] pattern_rouge = {0, 200, 100, 200, 100, 200, 100};

    ConstraintLayout gameLayout;

    TextView nord1;
    TextView nord2;
    TextView nord3;

    TextView sud1;
    TextView sud2;
    TextView sud3;

    TextView est1;
    TextView est2;
    TextView est3;

    TextView ouest1;
    TextView ouest2;
    TextView ouest3;

    GPSTracker tracker;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        gameLayout = (ConstraintLayout) findViewById(R.id.gameLayout);

        v =(Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        nord1 = findViewById(R.id.textViewBoxNord1);
        nord2 = findViewById(R.id.textViewBoxNord2);
        nord3 = findViewById(R.id.textViewBoxNord3);

        sud1 = findViewById(R.id.textViewBoxSud1);
        sud2 = findViewById(R.id.textViewBoxSud2);
        sud3 = findViewById(R.id.textViewBoxSud3);

        est1 = findViewById(R.id.textViewBoxEst1);
        est2 = findViewById(R.id.textViewBoxEst2);
        est3 = findViewById(R.id.textViewBoxEst3);

        ouest1 = findViewById(R.id.textViewBoxOuest1);
        ouest2 = findViewById(R.id.textViewBoxOuest2);
        ouest3 = findViewById(R.id.textViewBoxOuest3);

        ImageView imageView = findViewById(R.id.imageViewGame);
        imageView.setOutlineAmbientShadowColor(COULEUR_JAUNE);
        imageView.setElevation( (float) 100.0);

        Intent intent = getIntent();
        adresse = intent.getStringExtra(StartGameActivity.MESSAGE);
        sensibilite = intent.getIntExtra(StartGameActivity.MESSAGE_SENSIBILITE, StartGameActivity.SENSIBILITE_DEFAUT);
        if(sensibilite == 0) sensibilite = 1;
        sensibiliteJaune = sensibilite * 5;
        sensibiliteBleue = sensibilite * 10;

        Uri uri = Uri.parse(adresse);


        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);

            ExifInterface exif = new ExifInterface(this.getContentResolver().openInputStream(uri));
            exif.getLatLong(latLong);

            photoLocation = new Location("photoLocation");
            photoLocation.setLatitude(latLong[0]);
            photoLocation.setLongitude(latLong[1]);

            tracker = new GPSTracker(this);
            if (!tracker.canGetLocation()) {
                tracker.showSettingsAlert();
            }

            userLocation = new Location("userLocation");

            userLocation.setLatitude(tracker.getLatitude());
            userLocation.setLongitude(tracker.getLongitude());

            ImageView mImageView = this.findViewById(R.id.imageViewGame);
            mImageView.setImageBitmap(bitmap);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void notifyLocationChanged(Double lat, Double lng){
        System.out.println("POSITION USER CHANGEE");
        System.out.println("PHOTO : " + photoLocation.getLatitude() + "//" + photoLocation.getLongitude());
        System.out.println("USER : " + userLocation.getLatitude() + "//" + userLocation.getLongitude());

        userLocation.setLatitude(lat);
        userLocation.setLongitude(lng);

        Location photoLat = new Location("photoLat");
        photoLat.setLatitude(photoLocation.getLatitude());
        photoLat.setLongitude(0);

        Location photoLng = new Location("photoLng");
        photoLng.setLongitude(photoLocation.getLongitude());
        photoLng.setLatitude(0);

        Location userLat = new Location("userLat");
        userLat.setLatitude(userLocation.getLatitude());
        userLat.setLongitude(0);

        Location userLng = new Location("userLng");
        userLng.setLongitude(userLocation.getLongitude());
        userLng.setLatitude(0);

        float distanceLat = photoLat.distanceTo(userLat);
        float distanceLng = photoLng.distanceTo(userLng);

        System.out.println("DISTANCE : " + distanceLat + "m (lat)// " + distanceLng + "m (lng)");

        float moyenne = (distanceLat + distanceLng) / 2;

        if(distanceLat < sensibilite && distanceLng < sensibilite){
            tracker.stopUsingGPS();
            v.vibrate(500);
            Intent myIntent = new Intent(GameActivity.this, EndActivity.class);
            myIntent.putExtra(StartGameActivity.MESSAGE, adresse); //Optional parameters
            startActivity(myIntent);
        }
        else{
            int ancienEtat = etat;

            gameLayout.setBackgroundColor(FOND_ROUGE);
            etat = 1;
            if(moyenne > sensibiliteJaune) {
                gameLayout.setBackgroundColor(FOND_JAUNE);
                etat = 2;
            }
            if(moyenne > sensibiliteBleue) {
                gameLayout.setBackgroundColor(FOND_BLEU);
                etat = 3;
            }

            if(ancienEtat != etat){
                switch (etat){
                    case 1 :
                        v.vibrate(pattern_rouge, -1);
                        break;
                    case 2 :
                        v.vibrate(pattern_jaune, -1);
                        break;
                    case 3 :
                        v.vibrate(pattern_bleu, -1);
                        break;
                }
            }

            //User au nord de la photo
            if(userLocation.getLatitude() > photoLocation.getLatitude()) {

                nord1.setBackgroundColor(TRANSPARENT);
                nord2.setBackgroundColor(TRANSPARENT);
                nord3.setBackgroundColor(TRANSPARENT);

                sud1.setBackgroundColor(COULEUR_ROUGE);
                if(distanceLat > sensibiliteJaune) sud2.setBackgroundColor(COULEUR_JAUNE);
                else sud2.setBackgroundColor(TRANSPARENT);
                if(distanceLat > sensibiliteBleue) sud3.setBackgroundColor(COULEUR_BLEUE);
                else sud3.setBackgroundColor(TRANSPARENT);
            }
            //User au sud de la photo
            else{
                sud1.setBackgroundColor(TRANSPARENT);
                sud2.setBackgroundColor(TRANSPARENT);
                sud3.setBackgroundColor(TRANSPARENT);

                nord1.setBackgroundColor(COULEUR_ROUGE);
                if(distanceLat > sensibiliteJaune) nord2.setBackgroundColor(COULEUR_JAUNE);
                else nord2.setBackgroundColor(TRANSPARENT);
                if(distanceLat > sensibiliteBleue) nord3.setBackgroundColor(COULEUR_BLEUE);
                else nord3.setBackgroundColor(TRANSPARENT);
            }

            //User à l'est de la photo
            if(userLocation.getLongitude() > photoLocation.getLongitude()) {

                est1.setBackgroundColor(TRANSPARENT);
                est2.setBackgroundColor(TRANSPARENT);
                est3.setBackgroundColor(TRANSPARENT);

                ouest1.setBackgroundColor(COULEUR_ROUGE);
                if(distanceLng > sensibiliteJaune) ouest2.setBackgroundColor(COULEUR_JAUNE);
                else  ouest2.setBackgroundColor(TRANSPARENT);
                if(distanceLng > sensibiliteBleue) ouest3.setBackgroundColor(COULEUR_BLEUE);
                else ouest3.setBackgroundColor(TRANSPARENT);
            }
            //User à l'ouest de la photo
            else{
                ouest1.setBackgroundColor(TRANSPARENT);
                ouest2.setBackgroundColor(TRANSPARENT);
                ouest3.setBackgroundColor(TRANSPARENT);

                est1.setBackgroundColor(COULEUR_ROUGE);
                if(distanceLng > sensibiliteJaune) est2.setBackgroundColor(COULEUR_JAUNE);
                else  est2.setBackgroundColor(TRANSPARENT);
                if(distanceLng > sensibiliteBleue) est3.setBackgroundColor(COULEUR_BLEUE);
                else est3.setBackgroundColor(TRANSPARENT);
            }
        }
    }
}