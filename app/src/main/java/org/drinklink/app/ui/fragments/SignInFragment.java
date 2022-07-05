/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.drinklink.app.BuildConfig;
import org.drinklink.app.R;
import org.drinklink.app.common.fragment.DrinkLinkFragment;
import org.drinklink.app.loader.ActionCallback;
import org.drinklink.app.model.Credentials;
import org.drinklink.app.model.Token;
import org.drinklink.app.persistence.AuthToken;
import org.drinklink.app.persistence.model.SettingsPreferences;
import org.drinklink.app.ui.activities.SignUpActivity;
import org.drinklink.app.ui.dialog.DialogManager;
import org.drinklink.app.utils.IntentUtils;
import org.drinklink.app.utils.Logger;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.Optional;
import retrofit2.Call;

/**
 *
 */

public class SignInFragment extends DrinkLinkFragment {

    private static final String TAG = "SignInFragment";

    @BindView(R.id.tv_signin_email)
    TextView tvEmail;

    @BindView(R.id.et_signin_email)
    EditText etEmail;

    @BindView(R.id.tv_signin_password)
    TextView tvPassword;

    @BindView(R.id.et_signin_password)
    EditText etPassword;

    @Nullable
    @BindView(R.id.text_forgot_password)
    TextView tvForgotPassword;

    @Nullable
    @BindView(R.id.text_sing_up)
    TextView tvSignUp;

    @BindView(R.id.sign_in_error)
    TextView tvError;


    @OnClick(R.id.button_sign_in)
    @Optional
    public void signIn() {
        tvError.setVisibility(View.INVISIBLE);
        String userName = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        if (validate()) {
            getAnalytics().login();
            signIn(userName, password, false);
        }
    }

    protected boolean validate() {
        return validatePopulated();
    }

    protected boolean validateEmailFormat() {
        boolean matches = Patterns.EMAIL_ADDRESS.matcher(etEmail.getText()).matches();
        showError(!matches, getString(R.string.email_format_invalid));
        return matches;
    }

    protected boolean validatePopulated() {
        boolean isPopulated = !TextUtils.isEmpty(etEmail.getText()) && !TextUtils.isEmpty(etPassword.getText());
        showError(!isPopulated, getString(R.string.error_populate_sign_in_email));
        return isPopulated;
    }

    protected boolean showError(boolean showMessage, String errorMessage) {
        if (showMessage) {
            tvError.setVisibility(View.VISIBLE);
            tvError.setText(errorMessage);
        }
        return showMessage;
    }

    protected void signIn(String userName, String password, boolean isSignUp) {
        Credentials credentials = new Credentials(userName, password);
        Call<Token> authenticate = getApiAuthCalls().authenticate(credentials);
        ActionCallback<Token> callback = new ActionCallback<Token>(progressBar, getActivity()) {
            @Override
            public void onSuccess(Token body) {
                Logger.i(TAG, "token refreshed");
                getPreferencesStorage().save(new AuthToken(body.getToken(), body.getRefreshToken(), userName, null, false));
                SettingsPreferences settingsPreferences = getPreferencesStorage().getSettingsPreferences();
                String billEmail = settingsPreferences.getEmail();
                if (TextUtils.isEmpty(billEmail)) {
                    Logger.i(TAG, "save username as billing email");
                    settingsPreferences.setEmail(userName);
                    getPreferencesStorage().save(settingsPreferences);
                }
                updateToken();
            }

            @Override
            protected void onError(int code, String message, String errorBody) {
                if (isSignUp) {
                    DialogManager.showOkDialog(getActivity(),
                            getString(R.string.sign_up_confirm_title),
                            getString(R.string.sign_up_confirm_message),
                            () -> getActivity().finish());
                } else {
                    showErrorBody(errorBody);
                    showError(true,
                            code == 401 ?
                                    getString(R.string.login_failed_unauthorized) :
                                    message);
                }

            }
        };
        authenticate.enqueue(trackCallback(callback));
    }

    private void showErrorBody(String errorBody) {
        if (!TextUtils.isEmpty(errorBody)) {
            dialog = DialogManager.showOkDialog(getActivity(),
                    getString(R.string.error_an_error), errorBody);
        }
    }

    private void updateToken() {
//        notificationsTokenUpdateService.subscribe();
        String token = notificationsTokenUpdateService.getNotificationsToken();
        Call<String> authenticate = getApiCalls().sendNotificationsToken(token);
        authenticate.enqueue(trackCallback(new ActionCallback<String>(progressBar, getActivity()) {
            @Override
            public void onSuccess(String body) {
                Logger.i(TAG, body);
                getActivity().finish();
            }

            @Override
            protected void onError(int code, String message, String errorBody) {
                FirebaseCrashlytics.getInstance().log("Notifications token upload failed");
                if (!TextUtils.isEmpty(errorBody)) {
                    dialog = DialogManager.showOkDialog(getActivity(),
                            getString(R.string.error_an_error), errorBody, () -> {
                                getActivity().finish();
                            });
                } else {
                    getActivity().finish();
                }
            }
        }));
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindEditTextWithLabelVisibility(etEmail, tvEmail);
        bindEditTextWithLabelVisibility(etPassword, tvPassword);

        setupButtonLinks();
        tvError.setVisibility(View.INVISIBLE);

        //TODO: remove
        //prePopulate();
    }

    private void setupButtonLinks() {
        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(view1 -> forgotPassword());
        }
        if (tvSignUp != null) {
            tvSignUp.setOnClickListener(view1 -> signUp());
        }
    }

    @Deprecated
    private void prePopulate() {
        String username = getPreferencesStorage().getAuthToken().getUsername();
        if (BuildConfig.DEBUG) {
            username = "mirko.gun.mirko.53";
            etPassword.setText("Test.123" );
        } else {
            if (username == null) {
                username = "visla";
            }
            etPassword.setText("V.v1sl4vsk1" );
        }
        etEmail.setText(username);
    }

    private void forgotPassword() {
        showToast("Forgot password ... not implemented");
    }

    private void signUp() {
        Intent intent = new Intent(getContext(), SignUpActivity.class);
        intent.setFlags(IntentUtils.OVER_EXISTING);
        startActivity(intent);
        getActivity().finish();
    }


    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_sign_in;
    }
}
