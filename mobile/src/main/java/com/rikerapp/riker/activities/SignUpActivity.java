package com.rikerapp.riker.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.gson.JsonObject;
import com.rikerapp.riker.BuildConfig;
import com.rikerapp.riker.R;
import com.rikerapp.riker.RikerApp;
import com.rikerapp.riker.Utils;
import com.rikerapp.riker.dialogfragments.SimpleDialogFragment;
import com.rikerapp.riker.model.HttpResponseTuple;
import com.rikerapp.riker.model.RikerErrorType;
import com.rikerapp.riker.model.User;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public final class SignUpActivity extends BaseActivity implements SimpleDialogFragment.Callbacks {

    // result codes
    public static final int RESULT_CODE_SIGN_UP_SUCCESSFUL = 4;

    private ProgressBar progressBar;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Button submitButton;

    public static Intent makeIntent(final Context context) {
        return new Intent(context, SignUpActivity.class);
    }

    private final void handleSubmit() {
        final List<String> errorMessagesList = new ArrayList<>();
        final String email = Utils.validateEmailEditText(errorMessagesList, emailEditText, "E-mail address required.", "E-mail address not valid.");
        final String password = Utils.validateNonNullStringEditText(errorMessagesList, passwordEditText, "Password required.");
        final String confirmPassword = Utils.validateNonNullStringEditText(errorMessagesList, confirmPasswordEditText, password != null ? "Please re-enter your password." : null);
        if (password != null && confirmPassword != null) {
            if (!password.equals(confirmPassword)) {
                errorMessagesList.add("Passwords do not match.");
            }
        }
        if (errorMessagesList.size() == 0) {
            enableForm(false);
            progressBar.setVisibility(View.VISIBLE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
            /*new AsyncTask<Void, Void, HttpResponseTuple>() {
                @Override
                protected final HttpResponseTuple doInBackground(final Void... voids) {
                    final JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("user/email", email);
                    jsonObject.addProperty("user/password", password);
                    Timber.d("heellllo");
                    return Utils.executeHttpPost(jsonObject, BuildConfig.rikerUsersUri, "user", true);
                }

                @Override
                protected final void onPostExecute(final HttpResponseTuple responseTuple) {
                    super.onPostExecute(responseTuple);
                    progressBar.setVisibility(View.GONE);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    enableForm(true);
                    Utils.handleHttpResponse(responseTuple,
                            () -> handleSignUpSuccessResponse(responseTuple.responseBody, responseTuple.locationHeaderVal, responseTuple.authTokenHeaderVal),
                            () -> showNetworkProblemDialog(),
                            errors -> showValidationErrors(errors),
                            () -> RikerErrorType.SaveUserError.errorTuples(),
                            () -> showServerErrorDialog(),
                            null, // not possible
                            () -> showServerBusyDialog());
                }
            }.execute();*/
        } else {
            showValidationErrors(errorMessagesList);
        }
    }

    private final void handleSignUpSuccessResponse(final JsonObject responseBody, final String locationHeader, final String authToken) {
        // Next Steps:

        // 1. Deep save the user
        final User remoteUser = User.toUser(responseBody, locationHeader);
        final RikerApp rikerApp = (RikerApp)getApplication();
        final User localUser = rikerApp.dao.user();
        remoteUser.localIdentifier = localUser.localIdentifier;
        rikerApp.dao.deepSaveUser(remoteUser);

        // 2. Check if any existing local records exist; if so, prompt user if they should be preserved?
        //    If "No" to preserve, delete them.

        // 3. Port over RLocalDaoImpl.m, postDeepSaveUserHookIsAccountCreation function
        // 4. Associate user with Firebase Analytics and Crashlytics (port over RUtils/analyticsInitializeUser
        // 5. Store authentication token in SharedPrefs
        // 6. Display green success dialog indicating their account was created.  On dismiss, if "Yes"
        //    to preserve, prompt with new dialog if they want to sync their records to their account.
        // 7. If "Yes" to sync, then port over RCreateAccountController.m / line 331
    }

    private final void enableForm(final boolean enable) {
        emailEditText.setEnabled(enable);
        passwordEditText.setEnabled(enable);
        confirmPasswordEditText.setEnabled(enable);
        submitButton.setEnabled(enable);
    }

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        configureAppBar();
        logScreen(getTitle());

        progressBar = findViewById(R.id.progressBar);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        submitButton = findViewById(R.id.submitButton);
        progressBar.setVisibility(View.GONE);
        submitButton.setOnClickListener(view -> handleSubmit());
        confirmPasswordEditText.setOnEditorActionListener((textView, actionId, keyEvent) -> {
           if (actionId == EditorInfo.IME_ACTION_DONE) {
               handleSubmit();
               return true;
           }
           return false;
        });
    }
}
