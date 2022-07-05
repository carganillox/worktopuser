package org.drinklink.app.common.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.gson.Gson;

import org.drinklink.app.R;
import org.drinklink.app.api.ApiAuthService;
import org.drinklink.app.api.ApiService;
import org.drinklink.app.dependency.ApplicationModule;
import org.drinklink.app.dependency.DependencyResolver;
import org.drinklink.app.loader.ActionCallback;
import org.drinklink.app.loader.ProgressBarCounter;
import org.drinklink.app.model.NamedObject;
import org.drinklink.app.notifications.NotificationsTokenUpdateService;
import org.drinklink.app.persistence.DataStorage;
import org.drinklink.app.persistence.PreferencesStorage;
import org.drinklink.app.ui.dialog.DialogManager;
import org.drinklink.app.ui.fragments.PaymentFragment;
import org.drinklink.app.ui.navigation.NavigationManager;
import org.drinklink.app.utils.Analytics;
import org.drinklink.app.utils.ListUtil;
import org.drinklink.app.utils.Logger;
import org.drinklink.app.utils.TextWatcherAdapter;
import org.drinklink.app.workflow.IOrderProcessor;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import lombok.AccessLevel;
import lombok.Getter;
import retrofit2.Call;
import rx.Single;

public abstract class DrinkLinkFragment extends Fragment {

    private static final String TAG = "DrinkLinkFragment";

    private NavigationManager navigation;

    protected Dialog dialog;
    protected boolean isResumed;

    protected abstract int getFragmentLayout();

    @Inject
    @Named(ApplicationModule.API_SERVICE)
    ApiService apiCalls;

    @Inject
    ApiAuthService apiAuthCalls;

    @Inject
    Gson gson;

    @Inject
    IOrderProcessor processor;

    @Getter(AccessLevel.PROTECTED)
    @Inject
    PreferencesStorage preferencesStorage;

    @Getter(AccessLevel.PROTECTED)
    @Inject
    DataStorage dataStorage;

    @Getter(AccessLevel.PROTECTED)
    @Inject
    protected NotificationsTokenUpdateService notificationsTokenUpdateService;

    @Getter
    protected Analytics analytics;

    protected ProgressBarCounter progressBar;
    private List<ActionCallback<?>> callbacks;
    protected Bundle extras;

    private Unbinder bind;

    public DrinkLinkFragment() {
        DependencyResolver.getComponent().inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(getFragmentLayout(), container, false);
        bind = ButterKnife.bind(this, rootView);
        progressBar = new ProgressBarCounter(rootView.findViewById(R.id.loading_progressbar), getActivityForProgressBar());
        Logger.d(TAG, "onCreateView");
        analytics = new Analytics(getActivity(), TAG);
        return rootView;
    }

    protected FragmentActivity getActivityForProgressBar() {
        return null;
    }

    @Override
    public void onDestroyView() {
        Logger.d(TAG, "onDestroyView");
        super.onDestroyView();
        if (bind != null) {
            bind.unbind();
        }
    }

    @Override
    public void onResume() {
        Logger.d(TAG, "onResume");
        super.onResume();
        isResumed = true; // this is important to be before following code
        if (callbacks != null) {
            synchronized (callbacks) {
                List<ActionCallback<?>> executed = ListUtil.select(callbacks, item -> item.onResume());
                callbacks.removeAll(executed);
            }
        }
    }

    @Override
    public void onPause() {
        Logger.d(TAG, "onPause");
        isResumed = false;
        super.onPause();
        if (callbacks != null) {
            synchronized (callbacks) {
                List<ActionCallback<?>> executed = ListUtil.select(callbacks, item -> item.onPause());
                callbacks.removeAll(executed);
            }
        }
    }

    protected <T> ActionCallback<T> trackCallback(ActionCallback<T> callback) {
        Logger.d(TAG, "trackCall");
        if (callbacks == null) {
            callbacks = new ArrayList<>();
        }
        ActionCallback<T> callbackWrapper = callback;//new TrackedCallback<>(ProgressBarCounter.NO_PROGRESS_BAR, callback);
        synchronized (callbacks) {
            callbacks.add(callbackWrapper);
            if (!isResumed) {
                Logger.d(TAG, "pause callback");
                callbackWrapper.onPause();
            }
        }
        return callbackWrapper;
    }

    public void removeCallback(ActionCallback callback) {
        synchronized (callbacks) {
            callbacks.remove(callback);
        }
    }

    protected void showProgressBar(boolean show) {
        if (show) {
            progressBar.increase();
        } else {
            progressBar.decrease();
        }
    }

    protected void showToast(int resId) {
        showToast(getString(resId));
    }

    protected void showToast(String message) {
        Context context = getContext();
        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
        Logger.i(TAG, "showToast: " + message);
    }

    protected Gson getGson() {
        return gson;
    }

    protected IOrderProcessor getProcessor() {
        return processor;
    }

    protected ApiService getApiCalls() {
        return apiCalls;
    }

    protected ApiAuthService getApiAuthCalls() {
        return apiAuthCalls;
    }

    /**
     * Override where needed to use bundle from Acivity
     *
     * @param bundle {@link Bundle}
     */
    public void init(Bundle bundle) {
        extras = bundle;
    }

    protected static void setVisibility(View view, boolean isVisible) {
        view.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    public boolean onBackPress() {
        return false;
    }

    @Override
    public void startActivity(Intent intent) {
        if (isResumed) {
            hideKeyboard(getActivity());
        }
        super.startActivity(intent);
    }

    public void setNavigation(NavigationManager navigation) {
        this.navigation = navigation;
    }

    protected NavigationManager getNavigation() {
        return navigation;
    }

    public String getNavigationTag() {
        return Integer.toString(this.hashCode());
    }

    public Bundle getExtras() {
        if (extras == null) {
            extras = new Bundle();
        }
        return extras;
    }

    public static void bindEditTextWithLabelVisibility(EditText et, final View tv) {
        et.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if (s.length() == 0) {
                    setVisibility(tv, false);
                } else if (tv.getVisibility() != View.VISIBLE) {
                    setVisibility(tv, true);
                }
            }
        });
    }

    protected <T extends NamedObject> void selectOption(String title, String message, List<T> options,
                                                        final AppCompatButton spinnerButton, int noSelectionResId,
                                                        PaymentFragment.SelectionChanged<T> selectionChanged, T noSelection) {
        selectOption(title, message, options, spinnerButton, noSelectionResId, selectionChanged, noSelection, null);
    }

    protected <T extends NamedObject> void selectOption(String title, String message, List<T> options,
                                                        final AppCompatButton spinnerButton, int noSelectionResId,
                                                        PaymentFragment.SelectionChanged<T> selectionChanged, T noSelection,
                                                        Runnable onDismiss) {

        final String defaultString = getString(noSelectionResId);
        if (noSelection != null) {
            final String defaultStringLower = defaultString.toLowerCase();
            String noSelectionString = getString(R.string.dialog_spinner_no_selection_format, defaultStringLower);

            noSelection.setName(noSelectionString);
            options = new ArrayList<>(options != null ? options : new ArrayList<>());
            options.add(0, noSelection);
        }

        T tag = (T) spinnerButton.getTag();
        List<T> initialSelectedList = new ArrayList<T>();
        if (tag != null) {
            initialSelectedList.add(tag);
        }
        dialog = DialogManager.showSelectionDialog(getActivity(),
                title,
                message,
                options,
                initialSelectedList,
                selectedList -> {
                    T selected = null;
                    if (selectedList != null && !selectedList.isEmpty()) {
                        selected = selectedList.get(0);
                    }
                    boolean isSelected = selected != null && selected.getId() > 0;
                    if (!isSelected) {
                        selected = null;
                    }
                    String text = isSelected ? selected.getSelectedName() : defaultString;
                    setSelectedOption(spinnerButton, selected, text);
                    selectionChanged.onSelectionChanged(selected);
                },
                false,
                onDismiss
        );
    }

    protected <T extends NamedObject> void setSelectedOption(AppCompatButton spinnerButton, T selected, String text) {
        spinnerButton.setText(text);
        spinnerButton.setTag(selected);
    }

    protected boolean closeDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            return true;
        }
        return false;
    }

    protected void bindSpinnersDrawableRight(AppCompatButton... buttons) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Drawable drawable = AppCompatResources.getDrawable(getContext(), R.drawable.ico_arrow_down);
            for (AppCompatButton button : buttons) {
                button.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, drawable, null);
            }
        }
    }

    protected <T> void subscribe(Single<T> observable, ActionCallback<T> callback) {
        observable.subscribe(
                item -> callback.onSuccess(item),
                throwable -> callback.onFailure(null, throwable));
    }

    public static void hideKeyboard(Activity activity) {
        if (activity == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    public class TrackedCallback<T> extends ActionCallback<T> {

        ActionCallback<T> callback;

        public TrackedCallback(ProgressBarCounter progressBar, ActionCallback<T> callback) {
            super(progressBar, DrinkLinkFragment.this.getActivity());
            this.callback = callback;
        }

        @Override
        public void onSuccess(T body) {
            removeCallback(this);
            callback.onSuccess(body);
        }

        @Override
        public void onError(int code, String message, String errorBody) {
            removeCallback(this);
            callback.executeOnError(code, message, errorBody);
        }

        @Override
        public void onFailure(Call<T> call, Throwable t) {
            removeCallback(this);
            callback.onFailure(call, t);
        }
    }
}
