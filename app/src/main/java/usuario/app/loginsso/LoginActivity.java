package usuario.app.loginsso;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.vision.text.Text;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import io.fabric.sdk.android.Fabric;
import retrofit.RetrofitError;
import retrofit.client.Response;

/*
* Author: Robson Souza
* Objective: Introduction to Single Sign On (SSO) with Facebook, Twitter and Google.
*
* References:
*   https://auth0.com/blog/2016/02/08/how-to-authenticate-on-android-using-social-logins/
*
*   For Facebook:
*       https://developers.facebook.com/quickstarts/846476228830301/?platform=android
*       https://trinitytuts.com/integrating-facebook-sdk-application-android-studio/
*       https://trinitytuts.com/integrate-facebook-and-its-login-system-in-app/
*   For Google:
*       https://developers.google.com/identity/sign-in/android/start
*       https://developers.google.com/identity/sign-in/android/sign-in
*       https://developers.google.com/identity/sign-in/android/disconnect#sign_out_users
*
*   For Android
*       http://www.androidwarriors.com/2015/11/twitter-login-android-studio-example.html
*
* Command to get certificated(hash code) for Facebook:
*   Debug
*   keytool -exportcert -alias androiddebugkey -keystore ~/.android/debug.keystore | "C:\Users\robson.souza\Documents\Projetos\SingleSignOn\openssl\bin\openssl.exe" sha1 -binary | "C:\Users\robson.souza\Documents\Projetos\SingleSignOn\openssl\bin\openssl.exe" base64
*   Result sample: nd0l9MIsQzrlWeclw9ACuaIxvVU=
*
* Command to get certificated(hash code) for Google:
*   keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
*   Result sample of hash key SHA: 341800310280-vqapeihc095p6qmselavdpg35lrr0nas.apps.googleusercontent.com
*
* For certificated(hash code) for Twitter please see: https://apps.twitter.com
**/

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener{

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "Uz1jmQpsLA2UrHUP5MPglrHAI";
    private static final String TWITTER_SECRET = "2fXZQHd707XeHqlsSVdbCr11TLVd71jlj6Fo8tgaGauARRM6ER";


    CallbackManager callbackManager;

    //Signin button
    private SignInButton signInButton;
    private Button btnSignOutGoogle;
    private Button btnSignOutTwitter;
    private TwitterLoginButton loginTwitterButton;

    //Signing Options
    private GoogleSignInOptions gso;
    //google api client
    private GoogleApiClient mGoogleApiClient;
    //Signin constant to check the activity result
    private int RC_SIGN_IN = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));


        //region Facebook

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        setContentView(R.layout.activity_login);
        AppEventsLogger.activateApp(getApplication());

        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AlertDialog.Builder dialogo = new AlertDialog.Builder(LoginActivity.this);
                dialogo.setTitle("Resultado do Login");
                dialogo.setMessage("Sucesso");
                dialogo.setNeutralButton("Ok", null);
                dialogo.show();

            }

            @Override
            public void onCancel() {
                AlertDialog.Builder dialogo = new AlertDialog.Builder(LoginActivity.this);
                dialogo.setTitle("Resultado do Login");
                dialogo.setMessage("Cancelado");
                dialogo.setNeutralButton("Ok", null);
                dialogo.show();

            }

            @Override
            public void onError(FacebookException error) {
                AlertDialog.Builder dialogo = new AlertDialog.Builder(LoginActivity.this);
                dialogo.setTitle("Resultado do Login");
                dialogo.setMessage("Erro:"+error.getMessage());
                dialogo.setNeutralButton("Ok", null);
                dialogo.show();
            }
        });
        //endregion

        //region Google

        btnSignOutGoogle = (Button) findViewById(R.id.btnSignOutGoogle);

        //Initializing google signin option
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        //Initializing signinbutton
        signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setScopes(gso.getScopeArray());

        //Initializing google api client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        //Setting onclick listener to signing button
        signInButton.setOnClickListener(this);
        btnSignOutGoogle.setOnClickListener(this);
        //endregion

        //region Twitter
        loginTwitterButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
        btnSignOutTwitter = (Button) findViewById(R.id.btnSignOutTwitter);

        loginTwitterButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                // The TwitterSession is also available through:
                // Twitter.getInstance().core.getSessionManager().getActiveSession()
                TwitterSession session = result.data;
                // TODO: Remove toast and use the TwitterSession's userID
                // with your app's user model
                String msg = "@" + session.getUserName() + " logged in! (#" + session.getUserId() + ")";
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
            @Override
            public void failure(TwitterException exception) {
                Log.d("TwitterKit", "Login with Twitter failure", exception);
            }
        });

        btnSignOutTwitter.setOnClickListener(this);

        //endregion
    }

    //This function will option signing intent
    private void signIn() {
        //Creating an intent
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);

        //Starting intent for result
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    //This function will option signout
    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                    }
                });
    }

    //region callback para o login do Google+
    //After the signing we are calling this function
    /*
    private void handleSignInResult(GoogleSignInResult result) {
        //If the login succeed
        if (result.isSuccess()) {
            //Getting google account
            GoogleSignInAccount acct = result.getSignInAccount();

            //Displaying name and email
            textViewName.setText(acct.getDisplayName());
            textViewEmail.setText(acct.getEmail());

            //Initializing image loader
            imageLoader = CustomVolleyRequest.getInstance(this.getApplicationContext())
                    .getImageLoader();

            imageLoader.get(acct.getPhotoUrl().toString(),
                    ImageLoader.getImageListener(profilePhoto,
                            R.mipmap.ic_launcher,
                            R.mipmap.ic_launcher));

            //Loading image
            profilePhoto.setImageUrl(acct.getPhotoUrl().toString(), imageLoader);

        } else {
            //If login fails
            Toast.makeText(this, "Login Failed", Toast.LENGTH_LONG).show();
        }
    }
    */
    //endregion


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        loginTwitterButton.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            //Calling a new function to handle signin
            //handleSignInResult(result);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == signInButton) {
            //Calling signin
            signIn();
        }
        if (v ==  btnSignOutGoogle) {
             signOut();
        }
        if (v ==  btnSignOutTwitter) {
            logoutTwitter();
        }
    }

    public void logoutTwitter() {
        CookieSyncManager.createInstance(this);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeSessionCookie();
        Twitter.getSessionManager().clearActiveSession();
        Twitter.logOut();
    }

    public static void ClearCookies(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            CookieSyncManager cookieSyncMngr=CookieSyncManager.createInstance(context);
            cookieSyncMngr.startSync();
            CookieManager cookieManager=CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
