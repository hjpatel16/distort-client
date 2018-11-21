package com.unix4all.rypi.distort;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import java.util.regex.Pattern;

/**
 * A login screen that offers login via homeserver / password.
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * Regex Pattern to identify if a string is a valid homeserver address, as well as
     * fetch relevant substring from the URL
     */
    private static final Pattern IS_ADDRESS_PATTERN = Pattern.compile("(http(s)?://)?([a-zA-Z0-9.-]+\\.[a-z]+)(:[0-9]*)?(/[a-zA-Z0-9%/.-]*)?");


    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;


    /**
     * Pass login address and credentials to the conversations activity
     */
    public static final String EXTRA_HOMESERVER = "com.example.myfirstapp.HOMESERVER";
    public static final String EXTRA_CREDENTIAL = "com.example.myfirstapp.CREDENTIAL";


    // UI references.
    private EditText mHomeserverView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // TODO: If store exists with login credentials, bypass this step if login successful

        // Set up the login form.
        mHomeserverView = (EditText) findViewById(R.id.homeserver);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mSignInButton = (Button) findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid homeserver, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mHomeserverView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String homeserverAddr = mHomeserverView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid address.
        if (TextUtils.isEmpty(homeserverAddr)) {
            mHomeserverView.setError(getString(R.string.error_field_required));
            focusView = mHomeserverView;
            cancel = true;
        } else if (!isAddressValid(homeserverAddr)) {
            mHomeserverView.setError(getString(R.string.error_invalid_address));
            focusView = mHomeserverView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(this, homeserverAddr, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isAddressValid(String address) {
        return IS_ADDRESS_PATTERN.matcher(address).matches();
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with improved logic
        return true;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mAddress;
        private final String mCredential;
        private final Context context;
        private int errorCode;

        UserLoginTask(Context ctx, String address, String password) {
            context = ctx;
            mAddress = address;
            mCredential = password;
            errorCode = 0;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            errorCode = 0;

            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                errorCode = -1;
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                Intent intent = new Intent(context, ConversationsActivity.class);
                intent.putExtra(EXTRA_HOMESERVER, mAddress);
                intent.putExtra(EXTRA_CREDENTIAL, mCredential);
                startActivity(intent);
            } else {
                if(errorCode == -1) {
                    mPasswordView.setError(getString(R.string.error_incorrect_password));
                    mPasswordView.requestFocus();
                } else {
                    mHomeserverView.setError(getString(R.string.error_homeserver_could_not_reach));
                    mHomeserverView.requestFocus();
                }
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}
