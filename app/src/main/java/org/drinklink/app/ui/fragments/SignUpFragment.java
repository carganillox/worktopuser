/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import org.drinklink.app.R;
import org.drinklink.app.loader.ActionCallback;
import org.drinklink.app.model.SignUpCredentials;
import org.drinklink.app.ui.dialog.DialogManager;
import org.drinklink.app.utils.Logger;

import butterknife.BindView;
import butterknife.OnClick;
import retrofit2.Call;

/**
 *
 */

public class SignUpFragment extends SignInFragment {

    private static final String TAG = "SignUpFragment";

    @BindView(R.id.tv_signin_password_confirm)
    TextView tvPasswordConfirm;

    @BindView(R.id.et_signin_password_confirm)
    EditText etPasswordConfirm;

    @BindView(R.id.check_box_option)
    CheckBox agreeCheckBox;

    @BindView(R.id.btn_pwd_info_container)
    View pwdInfoContainer;

    @BindView(R.id.pwd_rules)
    View pwdRules;

    @OnClick(R.id.button_sign_up)
    public void signUpInitiated() {
        if (!validate()) {
            return;
        }
//        dialog = DialogManager.showAgreeDialog(getActivity(),
//                getString(R.string.terms_and_conditions_title),
//                getText(R.string.terms_and_conditions_message).toString(),
//                getString(R.string.terms_and_condition_agree),
//                () -> signUp());
        signUp();
    }

    @Override
    protected boolean validate() {
        return validatePopulated() && validateConfirmPassword() && validateEmailFormat() && validatePasswordMatch() && validateAgree();
    }

    private boolean validateConfirmPassword() {
        return !showError(TextUtils.isEmpty(etPasswordConfirm.getText()), getString(R.string.error_populate_sign_in_email));
    }

    private boolean validateAgree() {
        return !showError(!agreeCheckBox.isChecked(), getString(R.string.error_agree));
    }

    @Override
    protected boolean showError(boolean showMessage, String errorMessage) {
        if (showMessage) {
            setVisibility(pwdRules, false);
        }
        return super.showError(showMessage, errorMessage);
    }

    private boolean validatePasswordMatch() {
        boolean passwordMatch = etPassword.getText().toString().equals(etPasswordConfirm.getText().toString());
        showError(!passwordMatch, getString(R.string.password_match_error));
        return passwordMatch;
    }

    public void signUp() {
        getAnalytics().signUp();
        tvError.setVisibility(View.INVISIBLE);

        final String userName = etEmail.getText().toString();
        final String password = etPassword.getText().toString();
        String passwordConfirmed = etPasswordConfirm.getText().toString();

        SignUpCredentials credentials = new SignUpCredentials(userName, password, passwordConfirmed);
        Call<Void> authenticate = getApiAuthCalls().signUp(credentials);
        ActionCallback<Void> callback = new ActionCallback<Void>(progressBar, getActivity()) {

            @Override
            public void onSuccess(Void body) {
                Logger.i(TAG, "user created");
                signIn(userName, password, true);
            }

            @Override
            protected void onError(int code, String message, String errorBody) {
                showErrorBody(errorBody);
                showError(true, code == 400 ? errorBody : message);
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

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindEditTextWithLabelVisibility(etPasswordConfirm, tvPasswordConfirm);
        agreeCheckBox.setMovementMethod(LinkMovementMethod.getInstance());
        pwdInfoContainer.setOnClickListener(v -> pwdInfo());
    }

    @OnClick(R.id.btn_pwd_info)
    public void pwdInfo() {
         dialog = DialogManager.showInfoDialog(getActivity(), getString(R.string.password_requirements_title), getString(R.string.password_requirements));
    }

    @Deprecated
    private void mock() {
        etEmail.setText("a@gmail.com");
        etPassword.setText("Password.123");
        etPasswordConfirm.setText("Password.123");
    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_sign_up;
    }
}
