/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.RequestOptions;

import org.drinklink.app.R;
import org.drinklink.app.common.adapter.ViewModelAdapter;
import org.drinklink.app.common.viewholder.ViewModelHolderFactory;
import org.drinklink.app.dependency.DependencyResolver;
import org.drinklink.app.model.NamedObject;
import org.drinklink.app.ui.viewholder.CheckBoxItemHolder;
import org.drinklink.app.ui.viewholder.RadioButtonItemHolder;
import org.drinklink.app.ui.viewmodel.CheckBoxItem;
import org.drinklink.app.ui.viewmodel.RadioButtonItem;
import org.drinklink.app.utils.TimeSignature;
import org.drinklink.app.utils.Logger;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import lombok.Getter;
import lombok.Setter;

/**
 *
 */
public class DialogManager {

    private static final RequestOptions GLIDE_OPTIONS = new RequestOptions().centerCrop();

    private static final String TAG = "DialogManager";

    private DialogManager() {
    }

    public static Dialog showOkDialog(Activity activity, String title, String message) {
        return showDialog(activity, new OkDialogModel(title, message, () -> {}), R.layout.ok_dialog);
    }

    public static Dialog showOkDialog(Activity activity, String title, String message, Runnable onYes) {
        return showDialog(activity, new OkDialogModel(title, message, onYes), R.layout.ok_dialog);
    }

    public static Dialog showAgreeDialog(Activity activity, String title, String message, String okButton, Runnable onYes) {
        return showDialog(activity, new AgreeDialogModel(title, message, okButton, onYes, () -> {}), R.layout.agree_dialog);
    }

    public static Dialog showOkDialog(Activity activity, String title, String message, Runnable onYes,
                                      Runnable onDismiss) {
        return showDialog(activity, new OkDialogModel(title, message, onYes, onDismiss), R.layout.ok_dialog);
    }

    public static Dialog showYesNoDialogEqual(Activity activity, String title, String message,
                                                                 Runnable onYes,
                                                                 Runnable onNo) {
        return showDialog(activity, new YesNoDialogModel(title, message, onYes, onNo), R.layout.yes_no_dialog_equal);
    }

    public static Dialog showYesNoDialog(Activity activity, String title, String message,
                                         Runnable onYes,
                                         Runnable onNo) {
        return showDialog(activity, new YesNoDialogModel(title, message, onYes, onNo), R.layout.yes_no_dialog);
    }

    public static Dialog showInputDialog(Activity activity, String title, String message,
                                         InputDialogModel.DialogInput<String> onInput,
                                         Runnable onNo) {
        return showDialog(activity, new InputDialogModel(title, message, onInput, onNo), R.layout.input_dialog);
    }

    public static <T extends NamedObject> Dialog showSelectionDialog(Activity activity, String title, String message,
                                                                     List<T> options, List<T> selected,
                                                                     DialogModel.SelectionChanged<T> selectionChanged,
                                                                     boolean multipleSelectionsAllowed) {
        return showSelectionDialog(activity, title, message, options, selected, selectionChanged, multipleSelectionsAllowed, null);

    }

    public static <T extends NamedObject> Dialog showSelectionDialog(Activity activity, String title, String message,
                                                                     List<T> options, List<T> selected,
                                                                     DialogModel.SelectionChanged<T> selectionChanged,
                                                                     boolean multipleSelectionsAllowed,
                                                                     Runnable onDismiss) {

        DialogModel model = multipleSelectionsAllowed ?
                new MultipleSelectionDialogModel(activity, title, message, options, selected, selectionChanged) :
                new DialogModel<>(activity, title, message, options, selected, selectionChanged, onDismiss);

        return showSelectionDialog(activity, model);
    }

    @NonNull
    private static Dialog showSelectionDialog(Activity activity, DialogModel model) {
        return showDialog(activity, model, R.layout.spinner_dialog);
    }

    @NonNull
    private static Dialog showDialog(Activity activity, OkDialogModel model, int dialogLayout) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        // Get the layout inflater
        LayoutInflater inflater = activity.getLayoutInflater();
        View inflate = inflater.inflate(dialogLayout, null);
        builder.setView(inflate);
        ButterKnife.bind(model, inflate);

        // Create the AlertDialog object and return it
        AlertDialog alertDialog = builder.create();
        model.bind(alertDialog);
        Logger.i(TAG, "Show dialog " + model.getTitle());
        alertDialog.show();
        return alertDialog;
    }

    public static Dialog showInfoDialog(Activity activity, String title, String message) {
        return showDialog(activity, new OkDialogModel(title, message), R.layout.info_dialog);
    }

    public static Dialog showInfoDialogWithPhoto(Activity activity, String title, String message, String photoUrls) {
        return showDialog(activity, new OkDialogWithPhotoModel(title, message, photoUrls), R.layout.info_dialog_with_photo);
    }

    public static class AgreeDialogModel extends OkDialogModel {

        @Nullable
        @BindView(R.id.check_box_option)
        CheckBox checkbox;

        @Nullable
        @BindView(R.id.agree_error)
        TextView error;

        public AgreeDialogModel(String title, String message, String okButton, Runnable onYes, Runnable onDismiss) {
            super(title, message, okButton, onYes, onDismiss);
        }

        @Override
        protected void setMessage() {
            if (checkbox == null) {
                setVisibility(textViewMessage, message);
                textViewMessage.setText(message);
            } else {
                textViewMessage.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }

        @Override
        protected boolean isOk() {
            boolean checked = checkbox.isChecked();
            if (!checked) {
                setVisibility(error, !checked);
            }
            return checked;
        }
    }

    @Getter
    @Setter
    public static class OkDialogModel {
        protected String title;
        protected String message;
        protected Runnable onYes;
        protected Runnable onDismiss;
        protected String okButton;

        @BindView(R.id.dialog_title)
        TextView textViewTitle;

        @BindView(R.id.dialog_message)
        TextView textViewMessage;

        @BindView(R.id.btn_select)
        Button btnSelect;

        public OkDialogModel(String title, String message) {
            this(title, message, () -> {
            });
        }

        public OkDialogModel(String title, String message, String okButton, Runnable onYes) {
            this(title, message, onYes, () -> {
            });
            this.okButton = okButton;
        }

        public OkDialogModel(String title, String message, Runnable onYes) {
            this(title, message, onYes, () -> {
            });
        }

        public OkDialogModel(String title, String message, Runnable onYes, Runnable onDismiss) {
            this.title = title;
            this.message = message;
            this.onYes = onYes;
            this.onDismiss = onDismiss;
        }

        public OkDialogModel(String title, String message, String okButton, Runnable onYes, Runnable onDismiss) {
            this.okButton = okButton;
            this.title = title;
            this.message = message;
            this.onYes = onYes;
            this.onDismiss = onDismiss;
        }

        public void bind(AlertDialog alertDialog) {
            setMessage();
            setVisibility(textViewTitle, title);
            textViewTitle.setText(title);
            setButtonListeners(alertDialog);
        }

        protected void setMessage() {
            setVisibility(textViewMessage, message);
            textViewMessage.setText(message);
        }

        protected void setButtonListeners(AlertDialog alertDialog) {
            if (okButton != null) {
                btnSelect.setText(okButton);
            }
            btnSelect.setOnClickListener((v) -> {
                Logger.i(TAG, "on yes button clicked");
                if (!isOk()) {
                    return;
                }
                onYes.run();
                alertDialog.dismiss();
            });
            alertDialog.setOnDismissListener(dialogInterface -> {
                Logger.i(TAG, "on dismiss");
                if (onDismiss != null) {
                    onDismiss.run();
                } else {
                    Logger.e(TAG, "on dismiss is null!!!");
                }
            });
        }

        protected boolean isOk() {
            return true;
        }

        protected static void setVisibility(View view, String message) {
            setVisibility(view, !TextUtils.isEmpty(message));
        }

        protected static void setVisibility(View view, boolean notVisible) {
             view.setVisibility(notVisible ? View.VISIBLE : View.GONE);
        }
    }

    public static class OkDialogWithPhotoModel extends OkDialogModel {

        private String photoUrl;

        @BindView(R.id.carousel_view)
        ImageView photoView;

        public OkDialogWithPhotoModel(String title, String message, String photoUrl) {
            super(title, message);
            this.photoUrl = photoUrl;
        }

        @Override
        public void bind(AlertDialog alertDialog) {
            super.bind(alertDialog);

            photoView.setVisibility(!TextUtils.isEmpty(photoUrl) ? View.VISIBLE : View.GONE);
            load(photoUrl).apply(GLIDE_OPTIONS).into(photoView);
        }

        protected RequestBuilder<Drawable> load(String url) {
            return DependencyResolver.getComponent().getGlideLoader().with().load(url).signature(new TimeSignature());
        }
    }

    public static class InputDialogModel extends YesNoDialogModel {

        private final DialogInput<String> selectionChanged;

        @BindView(R.id.dialog_input)
        EditText editText;

        public InputDialogModel(String title, String message, DialogInput<String> selectionChanged, Runnable onNo) {
            super(title, message);
            this.onYes = () -> onOk();
            this.onNo = onNo;
            this.selectionChanged = selectionChanged;
        }

        @Override
        protected boolean isOk() {
            return !TextUtils.isEmpty(editText.getText().toString());
        }

        private void onOk() {
            selectionChanged.onInputProvided(editText.getText().toString());
        }

        public interface DialogInput<T> {

            void onInputProvided(T input);
        }
    }

    public static class YesNoDialogModel extends OkDialogModel {
        protected Runnable onNo;

        @BindView(R.id.btn_cancel)
        Button btnCancel;

        public YesNoDialogModel(String title, String message) {
            super(title, message);
        }

        public YesNoDialogModel(String title, String message, Runnable onYes, Runnable onNo) {
            super(title, message, onYes);
            this.onNo = onNo;
        }

        @Override
        public void bind(AlertDialog alertDialog) {
            super.bind(alertDialog);
            setButtonListeners(alertDialog);
        }

        @Override
        protected void setButtonListeners(AlertDialog alertDialog) {
            super.setButtonListeners(alertDialog);

            btnCancel.setOnClickListener((v) -> {
                Logger.i(TAG, "on No button clicked");
                onNo.run();
                alertDialog.dismiss();
            });
        }
    }

    public static class DialogModel<T extends NamedObject> extends YesNoDialogModel {


        private final List<T> options;
        private final Activity context;
        private List<T> selected;
        private final SelectionChanged selectionChanged;

        @Nullable
        @BindView(R.id.recycler_view_dialog_options)
        RecyclerView recyclerViewOptions;


        public DialogModel(Activity context, String title, String message, List<T> options,
                           List<T> selected, SelectionChanged<T> selectionChanged, Runnable onDismiss) {
            super(title, message);
            this.context = context;
            this.title = title;
            this.message = message;
            this.options = options;
            this.selected = selected;
            this.selectionChanged = selectionChanged;
            this.onDismiss = onDismiss;
        }

        @Override
        public void bind(AlertDialog alertDialog) {
            super.bind(alertDialog);
            bindOptions();
        }

        @Override
        protected void setButtonListeners(AlertDialog alertDialog) {
            btnCancel.setOnClickListener((v) ->
            {
                alertDialog.dismiss();
            });
            btnSelect.setOnClickListener((v) -> {
                Logger.i(TAG, "on selection changed:" + selected);
                selectionChanged.onSelectionChanged(selected);
                alertDialog.dismiss();
            });
            alertDialog.setOnDismissListener(dialogInterface -> {
                Logger.i(TAG, "on dismiss");
                if (onDismiss != null) {
                    onDismiss.run();
                } else {
                    Logger.e(TAG, "on dismiss is null!!!");
                }
            });
        }

        private void bindOptions() {
            if (recyclerViewOptions != null) {
                ViewModelAdapter adapterInstance = getAdapterInstance(context);
                recyclerViewOptions.setAdapter(adapterInstance);
                recyclerViewOptions.setLayoutManager(new LinearLayoutManager(context));
                adapterInstance.appendItems(toSelectableItem(options, selected));
            }
        }

        protected <T extends NamedObject> List toSelectableItem(List<T> options, List<T> selected) {
            ArrayList<RadioButtonItem> list = new ArrayList<>();
            for (int i = 0; i < options.size(); i ++) {
                T current = options.get(i);
                list.add(createSelectableItem(selected, i, current));
            }
            return list;
        }

        @NonNull
        protected <T extends NamedObject> RadioButtonItem createSelectableItem(List<T> selected, int i, T current) {
            return new RadioButtonItem(i, current, selected.contains(current));
        }

        protected ViewModelAdapter getAdapterInstance(Context context) {

            ViewModelHolderFactory factory = new ViewModelHolderFactory();
            final ViewModelAdapter viewModelAdapter = new ViewModelAdapter(context, factory);

            View.OnClickListener onClick = (view) -> {
                RadioButtonItem tag = (RadioButtonItem) view.getTag();
                clearOtherIfNeeded(viewModelAdapter, tag);
                tag.setChecked(!tag.isChecked());
                if (tag.isChecked()) {
                    selected.add((T) (tag.getItem()));
                } else {
                    selected.remove(tag.getItem());
                }
                viewModelAdapter.notifyDataSetChanged();
            };

            factory.add(RadioButtonItem.class, RadioButtonItemHolder.getLayout(),
                    view -> new RadioButtonItemHolder(view, onClick));

            factory.add(CheckBoxItem.class, CheckBoxItemHolder.getLayout(),
                    view -> new CheckBoxItemHolder(view, onClick));

            return viewModelAdapter;
        }

        protected void clearOtherIfNeeded(ViewModelAdapter viewModelAdapter, RadioButtonItem updatedItem) {
            selected.clear();
            List dataItems = new ArrayList(viewModelAdapter.getDataItems());
            for (Object item : dataItems) {
                if (updatedItem != item) {
                    ((RadioButtonItem) item).setChecked(false);
                }
            }
        }

        public interface SelectionChanged<T extends NamedObject> {

            void onSelectionChanged(List<T> newPosition);
        }
    }

    public static class MultipleSelectionDialogModel<T extends NamedObject> extends DialogModel<T> {

        public MultipleSelectionDialogModel(Activity context, String title, String message, List<T> options, List<T> selected, SelectionChanged<T> selectionChanged) {
            super(context, title, message, options, selected, selectionChanged, null);
        }

        @Override
        protected void clearOtherIfNeeded(ViewModelAdapter viewModelAdapter, RadioButtonItem updatedItem) {
            // no need to clear
        }

        @Override
        protected <T extends NamedObject> RadioButtonItem createSelectableItem(List<T> selected, int i, T current) {
            return new CheckBoxItem(i, current, selected.contains(current));
        }
    }

//    public static class InfoDialogModel<T extends NamedObject>{
//
//        private final String title;
//        private final String message;
//
//
//        @BindView(R.id.dialog_title)
//        TextView textViewTitle;
//
//        @BindView(R.id.dialog_message)
//        TextView textViewMessage;
//
//        @BindView(R.id.btn_select)
//        Button btnSelect;
//
//        public InfoDialogModel(String title, String message) {
//            this.title = title;
//            this.message = message;
//        }
//
//        public void bind(AlertDialog alertDialog) {
//            setVisibility(textViewMessage, message);
//            textViewMessage.setText(message);
//            setVisibility(textViewTitle, title);
//            textViewTitle.setText(title);
//
//            btnSelect.setOnClickListener((v) -> {
//                alertDialog.dismiss();
//            });
//        }
//
//        private static void setVisibility(View view, String message) {
//            view.setVisibility(TextUtils.isEmpty(message) ? View.GONE : View.VISIBLE);
//        }
//    }
}
