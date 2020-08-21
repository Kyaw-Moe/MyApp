package com.myapp.myapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN =123 ;
    private static final String TAG ="MainActivity" ;
    private FirebaseAuth mAuth;
    GoogleSignInClient mGoogleSignInClient;
    SignInButton btn;
    LinearLayout signInView;
    TextView userName, gMail, tvId;
    ImageView imageView;
    CallbackManager mCallbackManager;
    LoginButton loginButton;
    Button btnCheck;
    ConnectivityReceiver connectivityReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        checkConnection();

        setContentView(R.layout.activity_main);
        FacebookSdk.sdkInitialize(getApplicationContext());
        signInView = findViewById(R.id.sign_view);
        userName = findViewById(R.id.tv_hello);
        gMail = findViewById(R.id.tv_g_mail);
        tvId = findViewById(R.id.tv_id);
        btnCheck = findViewById(R.id.btn_check);
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.EXTRA_CAPTIVE_PORTAL);

         connectivityReceiver = new ConnectivityReceiver();
        registerReceiver(connectivityReceiver, intentFilter);
        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*checkInternetConnection();*/
            }
        });

        Button btnSignOut = findViewById(R.id.btn_sign_out);
        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(getApplicationContext(),gso);
        mAuth = FirebaseAuth.getInstance();
        btn = findViewById(R.id.sigIn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        mCallbackManager = CallbackManager.Factory.create();
        loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                // ...
            }
        });

    }



    private void changeActivity() {

            Intent intent = new Intent(this, OfflineActivity.class);
            startActivity(intent);

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(connectivityReceiver);
    }

    private void showSnackBar(boolean isConnected) {
        String message;
        int color;

        if (isConnected) {
            message = "You are Online .. !!";
            color = Color.WHITE;

        }
        else {
            message = "You are Offline .. !!";
            color = Color.RED;
        }

        Snackbar snackbar = Snackbar.make(findViewById(R.id.ll_view), message, Snackbar.LENGTH_LONG);

        View view = snackbar.getView();
        TextView textView = view.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(color);
        snackbar.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
//                e.printStackTrace();
                // Google Sign In failed, update UI appropriately

            }
        }
        else
        {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);

        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.

                            updateUI(null);
                        }

                    }
                });
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void signOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        reStart();
                    }
                });
    }

    private void reStart() {
        signInView.setVisibility(View.GONE);
        btn.setVisibility(View.VISIBLE);

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void updateUI(FirebaseUser currentUser) {
        if(currentUser!=null)
        {
            btn.setVisibility(View.GONE);
            userName.setText(currentUser.getDisplayName());
            gMail.setText(currentUser.getEmail());
            tvId.setText(currentUser.getProviderId());
            imageView = findViewById(R.id.image_view);
            Glide.with(getApplicationContext())
                    .load(currentUser.getPhotoUrl())
                    .into(imageView);
            signInView.setVisibility(View.VISIBLE);
            final Button btn = findViewById(R.id.logOutBtn);
            btn.setVisibility(View.VISIBLE);
            loginButton.setVisibility(View.GONE);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAuth.signOut();;
                    LoginManager.getInstance().logOut();
                    loginButton.setVisibility(View.VISIBLE);
                    btn.setVisibility(View.GONE);
                }
            });
        }
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });

    }

//    public void checkConnection() {
//
//        ConnectivityManager manager = (ConnectivityManager)
//                getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
//        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
//        if (null != activeNetwork) {
//
//            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
//                Toast.makeText(this, "Wifi Enable", Toast.LENGTH_SHORT).show();
//            }
//            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
//                Toast.makeText(this, "Data Network Enable", Toast.LENGTH_SHORT).show();
//            }
//        }
//        else {
//            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
//        }
//    }



}