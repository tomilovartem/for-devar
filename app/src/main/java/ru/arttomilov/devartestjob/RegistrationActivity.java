package ru.arttomilov.devartestjob;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler;
import com.amazonaws.regions.Regions;

public class RegistrationActivity extends AppCompatActivity {

    private CognitoUserPool userPool;

    
    private Button btnRegister;
    private CognitoUserAttributes cognitoUserAttributes;
    private SignUpHandler signUpHandler;
    private EditText edConfirmationCode;
    private Button btnConfirm;
    private Button btnBackToRegister;
    private LinearLayout confirm;
    private EditText edName;
    private EditText edPhone;
    private EditText edEmail;
    private EditText edPassword;
    private EditText edConfirmPassword;
    private LinearLayout register;
    private ProgressDialog progress;

    private CognitoUser _cognitoUser;


    private static String destination = "A confirmation code has been sent to your %s, enter it in the field below";
    private TextView tvDestination;
    private GenericHandler genericHandler;
    private LinearLayout confirmed;
    private TextView credentials;
    private Button btnSaveCredentials;
    private Button btnBackToMain;
    private UserCredentials userCredentials;
    private SharedPreferences sharedPreferences;
    private Button btnGetDevicePhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        
        register = (LinearLayout) findViewById(R.id.register);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        edConfirmPassword = (EditText) findViewById(R.id.edConfirmPassword);
        edPassword = (EditText) findViewById(R.id.edPassword);
        edEmail = (EditText) findViewById(R.id.edEmail);
        edPhone = (EditText) findViewById(R.id.edPhone);
        edName = (EditText) findViewById(R.id.edName);

        confirm = (LinearLayout) findViewById(R.id.confirm);
        confirmed = (LinearLayout) findViewById(R.id.confirmed);
        btnBackToMain = (Button) findViewById(R.id.btnBackToMain);
        btnSaveCredentials = (Button) findViewById(R.id.btnSaveCredentials);
        credentials = (TextView) findViewById(R.id.credentials);
        btnBackToRegister = (Button) findViewById(R.id.btnBackToRegister);
        btnConfirm = (Button) findViewById(R.id.btnConfirm);
        btnGetDevicePhoneNumber = (Button) findViewById(R.id.btnGetDevicePhoneNumber);
        edConfirmationCode = (EditText) findViewById(R.id.edConfirmationCode);
        tvDestination = (TextView) findViewById(R.id.tvDestination);
        progress = new ProgressDialog(this);
        progress.setTitle("Processing data ...");
        progress.setMessage("Wait ...");

        userPool = new CognitoUserPool(getApplicationContext(),
                getResources().getString(R.string.PoolId),
                getResources().getString(R.string.PoolAppClientId),
                getResources().getString(R.string.PoolAppClientSecret),
                Regions.fromName(getResources().getString(R.string.Region))
        );



        btnGetDevicePhoneNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edPhone.setText(getPhoneNumber());
            }
        });


        edPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                edName.setText(edPhone.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        cognitoUserAttributes = new CognitoUserAttributes();



        signUpHandler = new SignUpHandler() {

            @Override
            public void onSuccess(CognitoUser cognitoUser, boolean userConfirmed, CognitoUserCodeDeliveryDetails cognitoUserCodeDeliveryDetails) {
                progress.dismiss();
                if(!userConfirmed) {
                    register.setVisibility(View.GONE);
                    confirm.setVisibility(View.VISIBLE);
                    _cognitoUser = cognitoUser;
                    tvDestination.setText(String.format(destination, cognitoUserCodeDeliveryDetails.getAttributeName() +":" + cognitoUserCodeDeliveryDetails.getDestination()));
                }
                else {

                }
            }

            @Override
            public void onFailure(Exception exception) {
                progress.dismiss();
                Log.d("Exception", exception.getMessage());
                Toast.makeText(RegistrationActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
            }
        };

        genericHandler = new GenericHandler() {
            @Override
            public void onSuccess() {
                progress.dismiss();
                confirm.setVisibility(View.GONE);
                confirmed.setVisibility(View.VISIBLE);

                if(userCredentials!=null){
                    credentials.setText(
                            userCredentials.toString());
                }

            }

            @Override
            public void onFailure(Exception exception) {
                progress.dismiss();
                Log.d("Exception", exception.getMessage());
                Toast.makeText(RegistrationActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
            }
        };

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(_cognitoUser!=null){
                    if(edConfirmationCode.getText().toString().isEmpty()){
                        Toast.makeText(RegistrationActivity.this, "Fill in the confirmation code field", Toast.LENGTH_LONG).show();
                        edConfirmationCode.requestFocus();
                        return;
                    }
                    progress.show();
                    _cognitoUser.confirmSignUpInBackground(edConfirmationCode.getText().toString(), false, genericHandler);
                }
            }
        });

        btnBackToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _cognitoUser = null;
                edName.setText("");
                edPhone.setText("");
                edEmail.setText("");
                edPassword.setText("");
                edConfirmPassword.setText("");
                edConfirmationCode.setText("");
                register.setVisibility(View.VISIBLE);
                confirm.setVisibility(View.GONE);
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(edPhone.getText().toString().isEmpty()){
                    Toast.makeText(RegistrationActivity.this, "Fill in the Phone number field", Toast.LENGTH_LONG).show();
                    edPhone.requestFocus();
                    return;
                }
                if(edEmail.getText().toString().isEmpty()){
                    Toast.makeText(RegistrationActivity.this, "Fill in the Email field", Toast.LENGTH_LONG).show();
                    edEmail.requestFocus();
                    return;
                }
                if(edPassword.getText().toString().isEmpty()){
                    Toast.makeText(RegistrationActivity.this, "Fill in the Password field", Toast.LENGTH_LONG).show();
                    edPassword.requestFocus();
                    return;
                }
                if(edConfirmPassword.getText().toString().isEmpty()){
                    Toast.makeText(RegistrationActivity.this, "Fill in the Confirm password field", Toast.LENGTH_LONG).show();
                    edConfirmPassword.requestFocus();
                    return;
                }
                if(!edPassword.getText().toString().equals(edConfirmPassword.getText().toString())){
                    Toast.makeText(RegistrationActivity.this, "Passwords are not equal", Toast.LENGTH_LONG).show();
                    edPassword.requestFocus();
                    return;
                }

                userCredentials =  new UserCredentials(edName.getText().toString(), edPassword.getText().toString(), edPhone.getText().toString(), edEmail.getText().toString());



                progress.show();
                userPool.signUpInBackground(userCredentials.getPhone(), userCredentials.getPassword(), userCredentials.getCognitoUserAttributes(), null, signUpHandler);
            }
        });

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        edPhone.requestFocus();


        btnBackToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btnSaveCredentials.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(userCredentials!=null){
                    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(UserCredentials.LOGIN, userCredentials.getLogin());
                    editor.putString(UserCredentials.PASSWORD, userCredentials.getPassword());
                    editor.putString(UserCredentials.GIVEN_NAME, userCredentials.getLogin());
                    editor.putString(UserCredentials.PHONE_NUMBER, userCredentials.getPhone());
                    editor.putString(UserCredentials.EMAIL, userCredentials.getEmail());
                    editor.apply();
                    Toast.makeText(RegistrationActivity.this, "Your credentials have been saved", Toast.LENGTH_LONG).show();
                }
            }
        });
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



    private String getPhoneNumber() {
        TelephonyManager phoneMgr = (TelephonyManager) getSystemService(getApplicationContext().TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(RegistrationActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return "";
        }

        return phoneMgr.getLine1Number();
    }



}
