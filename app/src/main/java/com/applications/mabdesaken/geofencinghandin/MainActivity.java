package com.applications.mabdesaken.geofencinghandin;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public static final String TAG = "GeoFence";
    public static final int LOCATION_REQUEST_CODE = 1;
    public static final String geoCoderTAG = "geoCodeTAG";
    public static final int radius = 0;

    private GoogleApiClient mGoogleApiClient;
    private Geofence customFence;
    private GeofencingRequest mRequest;
    private PendingIntent mPi;
    private float geoFenceRadius = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up Google API
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


        //button for alert
        Button translateLocationButton = (Button) findViewById(R.id.translateButton);


        translateLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    translateLocation(v);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static LatLng fetchCoordinatesForLocation(Context context, String locationName) throws IOException {

        if (!Geocoder.isPresent()) {
            Log.w(geoCoderTAG, "Geocode not present");
        }

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        String position = String.valueOf(geocoder.getFromLocation(56.150312, 10.204725, 500));
        List<Address> addresses = geocoder.getFromLocationName(locationName, 500);
        LatLng positionLatLng = new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());
        Log.i("Position: ", position);
        Log.i("PositionFromName: ", positionLatLng.toString());
        return positionLatLng;
    }

    public void translateLocation(View view) throws IOException {
        TextView locationNameView = findViewById(R.id.locationInput);
        String textFromView = locationNameView.getText().toString().trim();
        LatLng latLng = fetchCoordinatesForLocation(this, textFromView);
        Log.i("Location: ", latLng.toString());
        customFence = new Geofence.Builder()
                .setRequestId(textFromView)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .setCircularRegion(latLng.latitude, latLng.longitude, geoFenceRadius)
                .build();

        mRequest = new GeofencingRequest.Builder()
                .addGeofence(customFence)
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .build();

        Intent intent = new Intent(this, ReceiveGeoFenceTransitionService.class);
        PendingIntent pi = PendingIntent.getService(this, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
            LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, mRequest, pi);
            Log.d(TAG, "We added the geofence HERE!!!");
        } else {
            ActivityCompat.requestPermissions(this, new String[] {
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION },
                    LOCATION_REQUEST_CODE);
        }
        mGoogleApiClient.disconnect();




    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "Google Play Services connected!");

    }

    /*@Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "Google Play Services connected!");

        // Let's create a Geofence around the Hovedbanegård
        mBanegaardFence = new Geofence.Builder()
                .setRequestId("hovedbanegaard")
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .setCircularRegion(56.1503116, 10.2047365, 1500)
                .build();

        mRequest = new GeofencingRequest.Builder()
                .addGeofence(mBanegaardFence)
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .build();

        // Create an Intent pointing to the IntentService
        Intent intent = new Intent(this,
                dk.au.cs.is2017.banegaardfence.ReceiveGeoFenceTransitionService.class);
        mPi = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
            LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, mRequest, mPi);
            Log.d(TAG, "We added the geofence!");
        } else {
            ActivityCompat.requestPermissions(this, new String[] {
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION },
                    LOCATION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    String message = "Location permission accepted. Geofence will be created.";
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();

                    // OK, request it now
                    LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, mRequest, mPi);
                    Log.d(TAG, "We added the geofence!");

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    String message = "Location permission denied. Geofence will not work.";
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
    */

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Google Play Services connection suspended!");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Google Play Services connection failed!");
    }
}
