/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.viewholder;

import android.content.Context;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import org.drinklink.app.R;
import org.drinklink.app.common.viewholder.ViewModelBaseHolder;
import org.drinklink.app.model.BillingAddress;
import org.drinklink.app.persistence.model.SettingsPreferences;
import org.drinklink.app.utils.Logger;
import org.jetbrains.annotations.NotNull;

import butterknife.BindView;

import static org.drinklink.app.common.fragment.DrinkLinkFragment.bindEditTextWithLabelVisibility;


public class BillingAddressHolder extends ViewModelBaseHolder<SettingsPreferences> {

    private static final String TAG = "BillingAddressHolder";
    private final boolean isEmailOptional;

    @BindView(R.id.tv_bill_email)
    TextView tvBillEmail;

    @BindView(R.id.et_bill_email)
    EditText etBillEmail;

    @BindView(R.id.check_box_bill_to_cardholder)
    CheckBox cbBillToCardholder;

    @BindView(R.id.et_bill_first_name)
    EditText etFirstName;

    @BindView(R.id.tv_bill_first_name)
    View tvFirstName;

    @BindView(R.id.et_bill_last_name)
    EditText etLastName;

    @BindView(R.id.tv_bill_last_name)
    TextView tvLastName;

    @BindView(R.id.et_bill_address)
    EditText etAddress;

    @BindView(R.id.tv_bill_address)
    View tvAddress;

    private boolean isFullNameOnOneLine;

    public BillingAddressHolder(View itemView, boolean isEmailOptional, boolean isFullNameOnOneLine) {
        super(itemView);
        this.isEmailOptional = isEmailOptional;
        this.isFullNameOnOneLine = isFullNameOnOneLine;
    }

    @Override
    public void bind(Context ctx, int position, SettingsPreferences settingsPreferences) {
        super.bind(ctx, position, settingsPreferences);
        bindEditTextWithLabelVisibility(etBillEmail, tvBillEmail);
        bindEditTextWithLabelVisibility(etFirstName, tvFirstName);
        bindEditTextWithLabelVisibility(etLastName, tvLastName);
        bindEditTextWithLabelVisibility(etAddress, tvAddress);

        boolean showLastName = !isFullNameOnOneLine && !settingsPreferences.isBillToCardHolder();
        setVisibility(etLastName, showLastName);
        setVisibility(tvLastName, showLastName);

        setValue(settingsPreferences.getEmail(), etBillEmail, tvBillEmail);
        setValue(getFirstOrFullName(settingsPreferences), etFirstName, tvFirstName);
        setValue(settingsPreferences.getLastName(), etLastName, tvLastName);
        setValue(settingsPreferences.getFullAddress(), etAddress, tvAddress);

        cbBillToCardholder.setOnCheckedChangeListener((compoundButton, isChecked) -> setNameVisibility(!isChecked));
        cbBillToCardholder.setChecked(settingsPreferences.isBillToCardHolder());
    }

    private String getFirstOrFullName(SettingsPreferences settingsPreferences) {
        if (!isFullNameOnOneLine) {
            return settingsPreferences.getFirstName();
        } else {
            String fullName = "";
            if (!TextUtils.isEmpty(settingsPreferences.getFirstName())) {
                fullName = settingsPreferences.getFirstName();
            }
            if (!TextUtils.isEmpty(settingsPreferences.getLastName())) {
                fullName += ", " + settingsPreferences.getLastName();
            }
            return fullName;
        }
    }

    private void setValue(String value, EditText editText, View textView) {
        boolean isEmpty = TextUtils.isEmpty(value);
        editText.setText(value);
        setVisibility(textView, !isEmpty);
    }

    /**
     * This is a placeholder for billing visibility which is in this version always visible
     * @param visible
     */
    @Deprecated()
    public void setBillingVisibilityPlaceholder(boolean visible) {
//        setBillingAddressVisibility(visible);
    }

    private void setBillingAddressVisibility(boolean visible) {
        setNameVisibility(visible);
        setVisibility(cbBillToCardholder, visible);
        if (isEmailOptional) {
            String emailCopy = ctx.getString(visible ? R.string.bill_to_email : R.string.bill_to_email_optional);
            etBillEmail.setHint(emailCopy);
            tvBillEmail.setText(emailCopy);
        }
    }

    public void setNameVisibility(boolean visible) {
        visible &= !cbBillToCardholder.isChecked();
        setVisibility(etFirstName, visible);
        setVisibility(etLastName, visible && !isFullNameOnOneLine);
        setEditTextVisibility(tvFirstName, etFirstName, visible);
        setEditTextVisibility(tvLastName, etLastName, visible && !isFullNameOnOneLine);
    }

    private void setEditTextVisibility(View textView, EditText editText, boolean visible) {
        setVisibility(textView, visible && !TextUtils.isEmpty(editText.getText()));
    }

    public boolean isBillingValid() {
        return isValidEmail() && isNameValid() && !TextUtils.isEmpty(etAddress.getText());
    }

    private boolean isNameValid() {
        return cbBillToCardholder.isChecked() ||
               !TextUtils.isEmpty(etFirstName.getText()) && (!TextUtils.isEmpty(etLastName.getText()) || isFullNameOnOneLine);
    }

    public boolean isValidEmail() {
        return !isEmailEmpty() && isValidEmailFormat();
    }

    public boolean isValidOrEmptyEmail() {
        return isEmailEmpty() || isValidEmailFormat();
    }

    private boolean isEmailEmpty() {
        return TextUtils.isEmpty(etBillEmail.getText());
    }

    private boolean isValidEmailFormat() {
        return Patterns.EMAIL_ADDRESS.matcher(etBillEmail.getText()).matches();
    }

    public void captureValues() {
        etBillEmail.clearFocus();
        etLastName.clearFocus();
        etFirstName.clearFocus();
        item.setBillToCardHolder(cbBillToCardholder.isChecked());
        item.setEmail(etBillEmail.getText().toString());
        item.setFirstName(getFirstName());
        item.setLastName(getLastName());
        item.setFullAddress(etAddress.getText().toString());
        item.setBillToCardHolder(cbBillToCardholder.isChecked());
    }

    @NotNull
    private String getLastName() {
        if (!isFullNameOnOneLine) {
            return etLastName.getText().toString();
        } else {
            String firstName = etFirstName.getText().toString();
            String[] split = firstName.split(",");
            return split.length > 1 ? split[1].trim() : null;
        }
    }

    @NotNull
    private String getFirstName() {
        String firstName = etFirstName.getText().toString();
        if (!isFullNameOnOneLine) {
            return firstName;
        } else {
            String[] split = firstName.split(",");
            return split.length > 0 ? split[0].trim() : null;
        }
    }

    public <T extends BillingAddress> T setCardBillingAddress(T billingAddress, String cardHolderName) {
        if (cbBillToCardholder.isChecked()) {
            Logger.i(TAG, "cardholder " + cardHolderName);
            if (cardHolderName != null) {
                String parts[] = cardHolderName.split(" ", 2);
                billingAddress.setFirstName(parts[0]);
                if (parts.length > 1) {
                    billingAddress.setLastName(parts[1]);
                }
            }
        } else {
            billingAddress.setFirstName(getFirstName());
            billingAddress.setLastName(getLastName());
        }
        billingAddress.setEmail(etBillEmail.getText().toString());
        setAddress(billingAddress);
        billingAddress.setBillToCardHolder(cbBillToCardholder.isChecked());
        return billingAddress;
    }

    private void setAddress(BillingAddress newCard) {
        String fullAddress = etAddress.getText().toString();
        newCard.setFullAddress(fullAddress);
        String[] address = fullAddress.split(",");
        if (address.length > 0) {
            newCard.setAddress(address[0].trim());
        }
        if (address.length > 1) {
            newCard.setCity(address[1].trim());
        }
        if (address.length > 2) {
            newCard.setCountryCode(address[2].trim());
        }
    }

    public void setVisibilityAccordingToAddress() {
        if (isBillingValid()) {
            setVisibility(false,
                    tvBillEmail,
                    etBillEmail,
                    cbBillToCardholder,
                    etFirstName,
                    tvFirstName,
                    etLastName,
                    tvLastName,
                    etAddress,
                    tvAddress);
        } else {
            setVisibility(true,
                    etBillEmail,
                    cbBillToCardholder,
                    etAddress);
        }
    }
}
