package ru.arttomilov.devartestjob;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSettings;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.UpdateAttributesHandler;
import com.amazonaws.regions.Regions;

import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private CognitoUserPool userPool;
    private EditText edLogin;
    private EditText edPassword;
    private Button btnUseSavedUserCredentials;
    private Button btnLogin;
    private LinearLayout loginData;
    private AuthenticationHandler authenticationHandler;
    private SharedPreferences sharedPreferences;
    private CognitoUser cognitoUser;
    private UserCredentials userCredentials;
    private ProgressDialog progress;
    private ProgressDialog progressChangeData;
    private CognitoUserSession _cognitoUserSession;
    private CognitoUserDetails _cognitoUserDetails;
    private GetDetailsHandler getDetailsHandler;
    private TextView userDetails;
    private Button btnChangeUserParams;
    private EditText edPhone;
    private EditText edNewPassword;
    private EditText edConfirmPassword;
    private Button btnCancel;
    private LinearLayout changeData;

    private Button btnLogout;
    private Button btnChangePassword;
    private Button btnChangePhone;
    private CognitoUserAttributes userAttributes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        changeData = (LinearLayout) findViewById(R.id.changeData);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnChangePhone = (Button) findViewById(R.id.btnChangePhone);
        btnChangePassword = (Button) findViewById(R.id.btnChangePassword);
        edConfirmPassword = (EditText) findViewById(R.id.edConfirmPassword);
        edNewPassword = (EditText) findViewById(R.id.edNewPassword);
        edPhone = (EditText) findViewById(R.id.edPhone);
        btnChangeUserParams = (Button) findViewById(R.id.btnChangeUserParams);

        userDetails = (TextView) findViewById(R.id.userDetails);
        userDetails.setMovementMethod(new ScrollingMovementMethod());
        loginData = (LinearLayout) findViewById(R.id.loginData);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogout = (Button) findViewById(R.id.btnLogout);
        btnUseSavedUserCredentials = (Button) findViewById(R.id.btnUseSavedUserCredentials);
        edPassword = (EditText) findViewById(R.id.edPassword);
        edLogin = (EditText) findViewById(R.id.edLogin);

        userPool = new CognitoUserPool(getApplicationContext(),
                getResources().getString(R.string.PoolId),
                getResources().getString(R.string.PoolAppClientId),
                getResources().getString(R.string.PoolAppClientSecret),
                Regions.fromName(getResources().getString(R.string.Region))
        );

        progress = new ProgressDialog(this);
        progress.setTitle("Log on ...");
        progress.setMessage("Wait ...");

        progressChangeData = new ProgressDialog(this);
        progressChangeData.setTitle("Change password and phone number ...");
        progressChangeData.setMessage("Wait ...");


        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                edPhone.setText("");
                edNewPassword.setText("");
                edConfirmPassword.setText("");
                loginData.setVisibility(View.VISIBLE);
                changeData.setVisibility(View.GONE);
            }
        });

        btnChangeUserParams.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginData.setVisibility(View.GONE);
                changeData.setVisibility(View.VISIBLE);
            }
        });

        btnChangePhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(edPhone.getText().toString().isEmpty()){
                    Toast.makeText(LoginActivity.this, "Fill in the Phone number field", Toast.LENGTH_LONG).show();
                    edPhone.requestFocus();
                    return;
                }
                if(cognitoUser!=null){
                    progressChangeData.setTitle("Change  phone number ...");
                    if(_cognitoUserDetails!=null){
                        progressChangeData.show();
                        userAttributes = new CognitoUserAttributes();//_cognitoUserDetails.getAttributes();
                        userAttributes.addAttribute(UserCredentials.PHONE_NUMBER, edPhone.getText().toString());
                        cognitoUser.updateAttributesInBackground(userAttributes, new UpdateAttributesHandler() {

                            @Override
                            public void onSuccess(List<CognitoUserCodeDeliveryDetails> list) {
                                progressChangeData.dismiss();
                                Toast.makeText(LoginActivity.this, "Phone number have been changed", Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onFailure(Exception exception) {
                                progressChangeData.dismiss();
                                Log.d("Exception", exception.getMessage());
                                Toast.makeText(LoginActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });

                    }

                }
            }
        });

        btnChangePassword.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               if(edNewPassword.getText().toString().isEmpty()){
                   Toast.makeText(LoginActivity.this, "Fill in the New password field", Toast.LENGTH_LONG).show();
                   edPassword.requestFocus();
                   return;
               }
               if(edConfirmPassword.getText().toString().isEmpty()){
                   Toast.makeText(LoginActivity.this, "Fill in the Confirm password field", Toast.LENGTH_LONG).show();
                   edConfirmPassword.requestFocus();
                   return;
               }
               if(!edNewPassword.getText().toString().equals(edConfirmPassword.getText().toString())){
                   Toast.makeText(LoginActivity.this, "Passwords are not equal", Toast.LENGTH_LONG).show();
                   edPassword.requestFocus();
                   return;
               }



               if(cognitoUser!=null){
                   progressChangeData.setTitle("Change password ...");
                   progressChangeData.show();

                   cognitoUser.changePasswordInBackground(userCredentials.getPassword(), edNewPassword.getText().toString(), new GenericHandler() {
                       @Override
                       public void onSuccess() {
                           progressChangeData.dismiss();
                           Toast.makeText(LoginActivity.this, "Password have been changed", Toast.LENGTH_LONG).show();
                       }

                       @Override
                       public void onFailure(Exception exception) {
                           progressChangeData.dismiss();
                           Log.d("Exception", exception.getMessage());
                           Toast.makeText(LoginActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
                       }
                   });
               }




           }
       });

       btnLogout.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               cognitoUser.signOut();
               btnChangeUserParams.setVisibility(View.GONE);
               btnLogout.setVisibility(View.GONE);
               btnLogin.setVisibility(View.VISIBLE);
               btnUseSavedUserCredentials.setVisibility(View.VISIBLE);
               edLogin.setEnabled(true);
               edPassword.setEnabled(true);
           }
       });

        getDetailsHandler = new GetDetailsHandler() {

            @Override
            public void onSuccess(CognitoUserDetails cognitoUserDetails) {
                progress.dismiss();
                _cognitoUserDetails = cognitoUserDetails;
                userCredentials.setUserId(cognitoUser.getUserId());
                userCredentials.setCurrentAccessToken(_cognitoUserSession.getAccessToken().getJWTToken());
                userDetails.setText(userCredentials.UserIdAndAccessTokenToString());
                btnChangeUserParams.setVisibility(View.VISIBLE);
                btnLogout.setVisibility(View.VISIBLE);
                btnLogin.setVisibility(View.GONE);
                btnUseSavedUserCredentials.setVisibility(View.GONE);
                edLogin.setEnabled(false);
                edPassword.setEnabled(false);
            }

            @Override
            public void onFailure(Exception exception) {
                progress.dismiss();
                Log.d("Exception", exception.getMessage());
                Toast.makeText(LoginActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
            }
        };


        authenticationHandler = new AuthenticationHandler() {

            @Override
            public void onSuccess(CognitoUserSession cognitoUserSession, CognitoDevice cognitoDevice) {
                _cognitoUserSession = cognitoUserSession;
                cognitoUser.getDetailsInBackground(getDetailsHandler);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(UserCredentials.LOGIN, userCredentials.getLogin());
                editor.putString(UserCredentials.PASSWORD, userCredentials.getPassword());
                editor.apply();

            }

            @Override
            public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String s) {

                AuthenticationDetails authenticationDetails = new AuthenticationDetails(userCredentials.getLogin(), userCredentials.getPassword(), null);

                authenticationContinuation.setAuthenticationDetails(authenticationDetails);

                authenticationContinuation.continueTask();
            }

            @Override
            public void getMFACode(MultiFactorAuthenticationContinuation multiFactorAuthenticationContinuation) {

            }

            @Override
            public void authenticationChallenge(ChallengeContinuation challengeContinuation) {

            }

            @Override
            public void onFailure(Exception exception) {
                progress.dismiss();
                Log.d("Exception", exception.getMessage());
                Toast.makeText(LoginActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
            }
        };

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(edLogin.getText().toString().isEmpty()){
                    Toast.makeText(LoginActivity.this, "Fill in the Login field", Toast.LENGTH_LONG).show();
                    edLogin.requestFocus();
                    return;
                }
                if(edPassword.getText().toString().isEmpty()){
                    Toast.makeText(LoginActivity.this, "Fill in the Password field", Toast.LENGTH_LONG).show();
                    edPassword.requestFocus();
                    return;
                }

                progress.show();
                if(userCredentials==null){
                    userCredentials = new UserCredentials(edLogin.getText().toString(), edPassword.getText().toString());
                }
                else {
                    userCredentials.setLogin(edLogin.getText().toString());
                    userCredentials.setPassword(edPassword.getText().toString());
                }
                cognitoUser = userPool.getUser(userCredentials.getLogin());
                cognitoUser.getSessionInBackground(authenticationHandler);
            }


        });

        btnUseSavedUserCredentials.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                userCredentials = new UserCredentials(sharedPreferences.getString(UserCredentials.LOGIN,""),
                        sharedPreferences.getString(UserCredentials.PASSWORD,""),
                        sharedPreferences.getString(UserCredentials.PHONE_NUMBER,""),
                        sharedPreferences.getString(UserCredentials.EMAIL,"")
                        );
                edLogin.setText(userCredentials.getLogin());
                edPassword.setText(userCredentials.getPassword());
            }
        });

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }
}
