package com.example.chaudfroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class FirstFragment extends Fragment {

    final int SELECT_IMAGE = 0;
    float[] latLong = new float[2];

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        getActivity().findViewById(R.id.fab).setEnabled(false);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.importPhoto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_IMAGE);

                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), data.getData());
                        System.out.println(data.getData().toString());

                        ExifInterface exif = new ExifInterface(getActivity().getContentResolver().openInputStream(data.getData()));

                        if(exif.getLatLong(latLong)) {
                            System.out.println(latLong[0] + "//" + latLong[1]);

                            String[] PERMISSIONS = {android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION};

                            if(getActivity().checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                                    getActivity().checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                                ActivityCompat.requestPermissions(getActivity(), PERMISSIONS, 112 );
                            }

                            GPSTracker tracker = new GPSTracker(getActivity());
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
                                Toast.makeText(getActivity(), "Vous êtes à plus 100km de la cible, essayer une autre photo", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                getActivity().findViewById(R.id.fab).setEnabled(true);
                            }
                        }
                        else{
                            Toast.makeText(getActivity(), "Données GPS manquantes, selectionnez une nouvelle image", Toast.LENGTH_SHORT).show();
                        }

                        ImageView mImageView;
                        mImageView = (ImageView) getActivity().findViewById(R.id.imageView);
                        mImageView.setImageBitmap(bitmap);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED)  {
                Toast.makeText(getActivity(), "Canceled", Toast.LENGTH_SHORT).show();
            }
        }
    }
}