package com.julespoitevin.foregroundserviceexample;

import static android.content.ContentValues.TAG;
import static com.julespoitevin.foregroundserviceexample.App.CHANNEL_ID;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentProvider;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.loader.app.LoaderManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.type.LatLng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class ExampleService extends Service {

    //Location Declarations

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;


    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String MY_ID = "E2rbXuJfGNscMSBf2vuy";


    public void GetCurrentUpdate() {

        Log.i("Msg","Trying to get current location... ");
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(15000);
        locationRequest.setFastestInterval(10000);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Toast.makeText(ExampleService.this,"Location found : "+locationResult.getLastLocation().getLongitude()+", "+locationResult.getLastLocation().getLatitude() , Toast.LENGTH_SHORT).show();

                //Get the reference of my user's collection!
                DocumentReference docRef = db.collection("users").document(MY_ID);


                //Get the number of measurements inside of my document :
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Long n = (Long) document.getData().get("n");



                                //Search for location



                                // Create a new user with a first and last name
                                Map<String, Object> user = new HashMap<>();


                                List<Double> tryCoordinates = Arrays.asList(locationResult.getLastLocation().getLongitude(), locationResult.getLastLocation().getLatitude());

                                Hashtable<String, Object> my_dict = new Hashtable<String, Object>();
                                my_dict.put("longitude",locationResult.getLastLocation().getLongitude());
                                my_dict.put("latitude",locationResult.getLastLocation().getLatitude());
                                my_dict.put("time",new Date());

                                user.put(n.toString(), my_dict);


                                // Add a new document with a generated ID
                                db.collection("users").document(MY_ID)
                                        .set(user, SetOptions.merge())
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "DocumentSnapshot successfully written!");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w(TAG, "Error writing document", e);
                                            }
                                        });

                                db.collection("users").document(MY_ID).update("n",n+1);

                            } else {
                                Log.d(TAG, "No such document");

                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });



            }
        }, Looper.getMainLooper());

    }




    @Override
    public void onCreate() {
        super.onCreate();


        //Location

        GetCurrentUpdate();






        final long period = 15000; //Interval between measurements
        /*
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {

                //-------------------Every X seconds part----------------
                Log.i("timerRestarted","Hi! I waited 15s!");

                //Get the reference of my user's collection!
                DocumentReference docRef = db.collection("users").document(MY_ID);

                //Get the number of measurements inside of my document :
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Long n = (Long) document.getData().get("n");



                                //Search for location



                                // Create a new user with a first and last name
                                Map<String, Object> user = new HashMap<>();



                                user.put(n.toString(), "Ada");


                                // Add a new document with a generated ID
                                db.collection("users").document(MY_ID)
                                        .set(user, SetOptions.merge())
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "DocumentSnapshot successfully written!");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w(TAG, "Error writing document", e);
                                            }
                                        });

                                db.collection("users").document(MY_ID).update("n",n+1);

                            } else {
                                Log.d(TAG, "No such document");

                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });






            }
        }, 0, period);

        */
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String input = intent.getStringExtra("inputExtra");

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this,CHANNEL_ID)
                .setContentTitle("Example Service !!")
                .setContentText("Your app is running ! "+input)
                .setSmallIcon(R.drawable.ic_baseline_my_location_24)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        return START_NOT_STICKY;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
