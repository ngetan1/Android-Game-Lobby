package com.example.GameLobby;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;

import java.util.Random;

public class FirstScreen extends GoogleActivity{
    private static final int REQUEST_INVITE = 40 ;

    Button hostBtn;
    Button joinBtn;
    Button signOutButton;
    Boolean host = false;
    Dialog ThisDialog;
    String lobbyNum;
    FirebaseDatabase database;
    DatabaseReference ref;
    TextView error, welcomeTxt;





    public void setLobbyNum(String lobbyNum) {
        this.lobbyNum = lobbyNum;
    }

    public String getLobbyNum() {
        return lobbyNum;
    }

    public void setHost(Boolean host) {
        this.host = host;
    }

    public Boolean getHost() {
        return host;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Game");
        setContentView(R.layout.activity_google);


        welcomeTxt = (TextView) findViewById(R.id.welcomeTxt);
        hostBtn = (Button) findViewById(R.id.hostBtn);
        joinBtn = (Button) findViewById(R.id.joinBtn);
        signOutButton = (Button) findViewById(R.id.signOutButton);

        signInButton = findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    welcomeTxt.setText("Welcome, " + mAuth.getCurrentUser().getDisplayName());
                    signInButton.setVisibility(View.GONE);
                    signOutButton.setVisibility(View.VISIBLE);
                }
            }
        };

        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseAuth.getInstance().signOut();
                signOut();
            }
        });

        hostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isSignedIn()) {
                    Toast.makeText(FirstScreen.this, "Not signed in", Toast.LENGTH_SHORT).show();
                } else {

                    setHost(true);
                    Random rand  = new Random();
                    setLobbyNum(Integer.toString(rand.nextInt(9000) + 1000));
                    onInviteClicked(getLobbyNum());

                }
            }
        });

        joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isSignedIn()) {
                    Toast.makeText(FirstScreen.this, "Not signed in", Toast.LENGTH_SHORT).show();
                } else {
//                    signInButton.setVisibility(View.GONE);
//                    signOutButton.setVisibility(View.VISIBLE);
                    ThisDialog = new Dialog(FirstScreen.this);
                    ThisDialog.setTitle("Enter Code");
                    ThisDialog.setContentView(R.layout.dialog_template);
                    final EditText codeEntered = (EditText) ThisDialog.findViewById(R.id.code);
                    Button enterBtn = (Button) ThisDialog.findViewById(R.id.enterBtn);
                    Button cancelBtn = (Button) ThisDialog.findViewById(R.id.cancelBtn);
                    error = (TextView) ThisDialog.findViewById(R.id.error);
                    enterBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setHost(false);
                            setLobbyNum(codeEntered.getText().toString());
                            database = FirebaseDatabase.getInstance();
                            ref = database.getReference();

                            ref.child("host").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists() && dataSnapshot.hasChild(getLobbyNum())) {
                                        if(dataSnapshot.child(getLobbyNum()).child("gameStarted").getValue(Boolean.class) == false) {
                                            if (dataSnapshot.child(getLobbyNum()).child("numPlyrJnd").getValue(Integer.class) < 8) {
                                                Intent intent = new Intent(FirstScreen.this, Lobby.class);
                                                intent.putExtra("lobbyNum", getLobbyNum());
                                                intent.putExtra("host", getHost());

                                                startActivity(intent);
                                                ThisDialog.cancel();
                                                //   finish();
                                            } else {
                                                error.setText("Room is currently full. Please host a game");
                                            }
                                        } else {
                                            error.setText("Game already in Session. Please enter another code or host a game");
                                        }
                                    } else {
                                        error.setText("Room not Found. Please try again or host a game");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });


                        }
                    });
                    ThisDialog.show();

                    cancelBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ThisDialog.cancel();
                        }
                    });

                }
            }
        });

        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        // Get deep link from result (may be null if no link is found)
                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                        }


                        // Handle the deep link. For example, open the linked
                        // content, or apply promotional credit to the user's
                        // account.
                        // ...

                        // ...
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("TAG", "getDynamicLink:onFailure", e);
                    }
                });


        // ATTENTION: This was auto-generated to handle app links.
        Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("On Activity Result", "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                // Get the invitation IDs of all sent messages
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                for (String id : ids) {
                    Log.d("TAG", "onActivityResult: sent invitation " + id);
                }

                    Intent intent = new Intent(FirstScreen.this, Lobby.class);
                    intent.putExtra("lobbyNum", getLobbyNum());
                    intent.putExtra("host", getHost());
                    startActivity(intent);
                //    finish();

            } else {
                // Sending failed or it was canceled, show failure message to the user
                // ...
            }
        }
    }

    private void onInviteClicked(String lobbyNum) {

        Intent intent = new AppInviteInvitation.IntentBuilder("Game Invitation")

                .setMessage(mAuth.getCurrentUser().getDisplayName() + " has invited you to the game\n"
                + "Enter code to join game:\n" + lobbyNum)

                .build();
        startActivityForResult(intent, REQUEST_INVITE);

    }


    private boolean isSignedIn() {
        return GoogleSignIn.getLastSignedInAccount(this) != null;
    }

    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(FirstScreen.this, "Signed Out", Toast.LENGTH_SHORT).show();
                        welcomeTxt.setText("Not Signed In!");
                        signInButton.setVisibility(View.VISIBLE);
                        signOutButton.setVisibility(View.GONE);
                    }
                });
    }

}
