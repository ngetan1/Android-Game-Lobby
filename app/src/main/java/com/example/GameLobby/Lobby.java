package com.example.GameLobby;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Lobby extends FirstScreen {


    private static final int REQUEST_INVITE = 30;
    FirebaseDatabase database;
    DatabaseReference hostLobbies;
    DatabaseReference player;
    FirebaseUser user;
    ValueEventListener valueEventListener;

    String value;
    String playerNumGiven, playerID;
    int prevPlyrLft, prevPlyrJnd, numPlyrJnd;
    Button leave, invPlyr, startBtn;
    Boolean host;
    String lobbyNum;

    Bundle extras;
    Intent i;

    TextView p1, p2, p3, p4, p5, p6, p7, p8;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lobbyNum = getIntent().getStringExtra("lobbyNum");
        setTitle("Lobby " + lobbyNum);
        setContentView(R.layout.activity_lobby);
        user = FirebaseAuth.getInstance().getCurrentUser();

        leave = (Button) findViewById(R.id.leaveBtn);
        invPlyr = (Button) findViewById(R.id.invBtn);
        startBtn = (Button) findViewById(R.id.startBtn);



        host = getIntent().getExtras().getBoolean("host");


        database = FirebaseDatabase.getInstance();
        hostLobbies = database.getReference().child("host").child(lobbyNum);
        player = hostLobbies.child("users").child(user.getUid());
        player.child("playerName").setValue(user.getDisplayName());

        extras = new Bundle();
        i = new Intent(this, MyService.class);

        extras.putString("lobbyNum", lobbyNum);
        extras.putString("uID", user.getUid());
        extras.putBoolean("host", host);

        p1 = (TextView) findViewById(R.id.player1);
        p2 = (TextView) findViewById(R.id.player2);
        p3 = (TextView) findViewById(R.id.player3);
        p4 = (TextView) findViewById(R.id.player4);
        p5 = (TextView) findViewById(R.id.player5);
        p6 = (TextView) findViewById(R.id.player6);
        p7 = (TextView) findViewById(R.id.player7);
        p8 = (TextView) findViewById(R.id.player8);

        if (host) {
            i.putExtras(extras);
            startService(i);
            player.child("playerNum").setValue(1);
            hostLobbies.child("roomAvailable").setValue("2,3,4,5,6,7,8,");
            hostLobbies.child("prevPlyrLft").setValue(0);
            hostLobbies.child("prevPlyrJnd").setValue(0);
            hostLobbies.child("numPlyrJnd").setValue(1);
            hostLobbies.child("gameStarted").setValue(false);
            hostLobbies.child("plyrClsdApp").setValue("false,0");
            numPlyrJnd = 0;


            leave.setText("End Session");
            startBtn.setVisibility(View.VISIBLE);

            startBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hostLobbies.child("numPlyrJnd").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.getValue(Integer.class) >= 2){
                                hostLobbies.child("gameStarted").setValue(true);
  //                              startActivity(new Intent(Lobby.this, Game.class));
                            }
                            else{
                                Toast.makeText(Lobby.this, "Must have another player joined", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                }
            });

            leave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showLeaveSessionAlert();
                }
            });

            //Checks if player closed the application and kicks the player out.
            hostLobbies.child("plyrClsdApp").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        String[] value = dataSnapshot.getValue(String.class).split(",");
                        removePlayerFromLobby(Integer.parseInt(value[1]));
                        hostLobbies.child("plyrClsdApp").setValue("false,0");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        } else {  //if player is not host


            hostLobbies.child("roomAvailable").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    value = dataSnapshot.getValue(String.class);

                    if (!value.equals("")) {
                        String[] roomsAvailable = value.split(",");
                        playerNumGiven = roomsAvailable[0];
                        extras.putString("playerNumGiven", playerNumGiven);
                        i.putExtras(extras);
                        startService(i);
                        switch (playerNumGiven) {
                            case "2":
                                player.child("playerNum").setValue(2);
                                break;
                            case "3":
                                player.child("playerNum").setValue(3);
                                break;
                            case "4":
                                player.child("playerNum").setValue(4);
                                break;
                            case "5":
                                player.child("playerNum").setValue(5);
                                break;
                            case "6":
                                player.child("playerNum").setValue(6);
                                break;
                            case "7":
                                player.child("playerNum").setValue(7);
                                break;
                            case "8":
                                player.child("playerNum").setValue(8);
                                break;
                        }
                        value = value.substring(2, value.length());
                        hostLobbies.child("prevPlyrJnd").setValue(Integer.parseInt(playerNumGiven));

                        hostLobbies.child("roomAvailable").setValue(value);
                        hostLobbies.child("numPlyrJnd").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                hostLobbies.child("numPlyrJnd").setValue(dataSnapshot.getValue(Integer.class) + 1);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }


                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value

                       Log.w("TAG", "Failed to read value.", error.toException());
                }
            });


            leave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                   showLeaveSessionAlert();
                }
            });

        }

        invPlyr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onInviteClicked(lobbyNum);
            }
        });

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    if (dataSnapshot.getValue(Integer.class) > numPlyrJnd) {
                        //when a user joins lobby



                        hostLobbies.child("prevPlyrJnd").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                //This if statement determines if the host eneded the game
                                if(dataSnapshot.exists()) {
                                    prevPlyrJnd = dataSnapshot.getValue(Integer.class);

                                    hostLobbies.child("users").addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                if (ds.child("playerNum").exists()) {
                                                    updateUI(ds.child("playerNum").getValue(Integer.class), ds.child("playerName").getValue(String.class), Color.GREEN);
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });

                    } else if (dataSnapshot.getValue(Integer.class) < numPlyrJnd) {
                        //when a user leaves

                        hostLobbies.child("prevPlyrLft").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                //This if statement determines if the host eneded the game
                                if(dataSnapshot.exists()) {
                                    prevPlyrLft = dataSnapshot.getValue(Integer.class);
                                    if (prevPlyrLft != 0) {
                                        hostLobbies.child("users").addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                    if (ds.child("playerNum").exists()) {
                                                        updateUI(prevPlyrLft, "Player" + Integer.toString(prevPlyrLft), Color.RED);
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });


                    }
                    numPlyrJnd = dataSnapshot.getValue(Integer.class);
                }
                else{
                    if (!host) {
                        Toast.makeText(Lobby.this, "Host ended the game, or something went wrong with server", Toast.LENGTH_SHORT).show();
                    }
                    startActivity(new Intent(Lobby.this, FirstScreen.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        hostLobbies.child("numPlyrJnd").addValueEventListener(valueEventListener);

    }

    private void updateUI(int playerNum, String playerName, int color){
        switch (playerNum){
            case 1:
                p1.setText(playerName);
                p1.setBackgroundColor(color);
                break;
            case 2:
                p2.setText(playerName);
                p2.setBackgroundColor(color);
                break;
            case 3:
                p3.setText(playerName);
                p3.setBackgroundColor(color);
                break;
            case 4:
                p4.setText(playerName);
                p4.setBackgroundColor(color);
                break;
            case 5:
                p5.setText(playerName);
                p5.setBackgroundColor(color);
                break;
            case 6:
                p6.setText(playerName);
                p6.setBackgroundColor(color);
                break;
            case 7:
                p7.setText(playerName);
                p7.setBackgroundColor(color);
                break;
            case 8:
                p8.setText(playerName);
                p8.setBackgroundColor(color);

                break;
        }
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("TAG", "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                // Get the invitation IDs of all sent messages
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                for (String id : ids) {
                    Log.d("TAG", "onActivityResult: sent invitation " + id);
                }
            } else {
                // Sending failed or it was canceled, show failure message to the user
                // ...
            }
        }
    }

    private void onInviteClicked(String lobbyNum) {

        Intent intent = new AppInviteInvitation.IntentBuilder("Game Invitation")
                //.setDeepLink(Uri.parse("https://karta4.page.link/d38h"))
                .setMessage(mAuth.getCurrentUser().getDisplayName() + " has invited you to the game\n"
                        + "Enter code to join game:\n" + lobbyNum)

                .build();
        startActivityForResult(intent, REQUEST_INVITE);

    }

    public void removePlayerFromLobby(final int playerNum){
        Log.i("playerID", "The player Num is " + playerNum);
        hostLobbies.child("users").addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.child("playerNum").getValue(Integer.class) == playerNum) {
                        playerID = ds.getKey();
                        Log.i("playerID", "The player ID is " +playerID);
                        hostLobbies.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String s= dataSnapshot.child("roomAvailable").getValue(String.class);
                                int npj = dataSnapshot.child("numPlyrJnd").getValue(Integer.class);
                                hostLobbies.child("roomAvailable").setValue(Integer.toString(playerNum) + "," + s);
                                hostLobbies.child("prevPlyrLft").setValue(playerNum);
                                hostLobbies.child("numPlyrJnd").setValue(npj-1);
                                hostLobbies.child("users").child(playerID).removeValue();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        break;

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        showLeaveSessionAlert();
    }

    public void showLeaveSessionAlert(){
        final AlertDialog.Builder altDial = new  AlertDialog.Builder(Lobby.this);
        altDial.setMessage("Do you want to leave the session?").setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(host){

                            endSession();
                        }else{
                            leaveSession();
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog leaveAlert = altDial.create();
        leaveAlert.show();
    }

    public void endSession(){
        hostLobbies.child("numPlyrJnd").removeEventListener(valueEventListener);
        hostLobbies.removeValue();
        stopService(i);
        startActivity(new Intent(Lobby.this, FirstScreen.class));
        finish();
    }

    public void leaveSession(){
        hostLobbies.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                value = dataSnapshot.child("roomAvailable").getValue(String.class);
                numPlyrJnd = dataSnapshot.child("numPlyrJnd").getValue(Integer.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        hostLobbies.child("roomAvailable").setValue(playerNumGiven + "," + value);
        hostLobbies.child("prevPlyrLft").setValue(Integer.parseInt(playerNumGiven));
        hostLobbies.child("numPlyrJnd").setValue(numPlyrJnd-1);
        numPlyrJnd = 0;
        player.removeValue();
        hostLobbies.child("numPlyrJnd").removeEventListener(valueEventListener);
        startActivity(new Intent(Lobby.this, FirstScreen.class));
        finish();
    }
}
