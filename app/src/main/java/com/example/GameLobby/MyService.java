package com.example.GameLobby;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MyService extends Service {

    FirebaseDatabase database;
    DatabaseReference hostLobbies;
    boolean host;
    String uID, lobbyNum, playerNumGiven;


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
       super.onStartCommand(intent, flags, startId);
        database = FirebaseDatabase.getInstance();

        Bundle extras = intent.getExtras();
        if(extras!= null) {
            host = extras.getBoolean("host");
            lobbyNum = extras.getString("lobbyNum");
            hostLobbies = database.getReference().child("host").child(lobbyNum);
            uID = extras.getString("uID");
            if(extras.getString("playerNumGiven") != null) {
                playerNumGiven = extras.getString("playerNumGiven");
            }
            Log.i("PlayerNumGiven", "PlayerNumGiven is " + playerNumGiven);
        }


        return START_NOT_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.i("Services", "On task removed has started");
        super.onTaskRemoved(rootIntent);

        if (host) {
            Log.i("Services", "Removing host has started");
            hostLobbies.removeValue();
            Log.i("Services", "Removing host has finished");
        } else {
            Log.i("Services", "Removing player has started");
           // hostLobbies.child("plyrClsdNum").setValue(Integer.parseInt(playerNumGiven));
            hostLobbies.child("plyrClsdApp").setValue("true," + playerNumGiven);

        }
        stopSelf();

        System.exit(0);
    }
}
