/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.viewholder;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;

import org.drinklink.app.R;
import org.drinklink.app.common.viewholder.ViewModelBaseHolder;
import org.drinklink.app.model.DrinkOption;
import org.drinklink.app.model.DrinkOptionCategory;
import org.drinklink.app.model.NamedObject;
import org.drinklink.app.ui.dialog.DialogManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;


public class DrinkOptionCategoryListItemHolder extends ViewModelBaseHolder<DrinkOptionCategory> {

    private final Activity activity;
    private final DrinkOptionSelection selection;
    private final DrinkOptionSelection PLACEHOLDER = selected -> {};

    @BindView(R.id.list_item)
    ToggleButton btnMixer;

    public DrinkOptionCategoryListItemHolder(View itemView, Activity activity, DrinkOptionSelection selection) {
        super(itemView);
        this.activity = activity;
        this.selection = selection != null ? selection : PLACEHOLDER;
    }

    @Override
    public void bind(Context ctx, final DrinkOptionCategory item) {
        // TODO: remove, for testing only :
        //item.setMultipleSelectionAlowed(true);

        super.bind(ctx, item);
        this.ctx = ctx;

        btnMixer.setOnCheckedChangeListener(null);
        btnMixer.setEnabled(!isPreview());
        setMixButton(item.getSelectedMixers());
        btnMixer.setOnCheckedChangeListener(getOnCheckedChangeListener());
        btnMixer.setTag(new ArrayList(item.getSelectedMixers()));
    }

    @NonNull
    protected CompoundButton.OnCheckedChangeListener getOnCheckedChangeListener() {
        return (compoundButton, checked) -> {
            if (item.isMultipleSelectionAlowed() || checked) {
                showMixerDialog();
            } else {
                clearSelection();
            }
        };
    }

    private void showMixerDialog() {
        Dialog dialog = DialogManager.showSelectionDialog(activity,
                    item.getName(),
                    activity.getString(R.string.message_add_mix, item.getName()),
                    getMixers(),
                    new ArrayList(item.getSelectedMixers()),
                    this::setSelectedMixer,
                    item.isMultipleSelectionAlowed());

        // un-check if cancel
        dialog.setOnDismissListener(dialogInterface -> {
            boolean isChecked = !((List) btnMixer.getTag()).isEmpty();
            setOptionChecked(isChecked);
        });
    }

    private List<DrinkOption> getMixers() {
        return item.getMixers();
    }

    private void setSelectedMixer(List<DrinkOption> mixers) {
        List<DrinkOption> currentMixers = (List<DrinkOption>)btnMixer.getTag();
        boolean equal = mixers.containsAll(currentMixers) && currentMixers.containsAll(mixers);
        if (!equal) {
            setMixButton(mixers);
            ArrayList<DrinkOption> selectedMixers = new ArrayList<>(mixers);
            btnMixer.setTag(selectedMixers);
            item.setSelectedMixers(new ArrayList<>(mixers));
            selection.onSelectionChanged(item);
        }
    }

    private <T extends NamedObject> void setMixButton(List<T> mixers) {
        boolean isSelected = !mixers.isEmpty();
        String optionOrCategory = isSelected ? getMixerName(mixers, item.getName()) : item.getName();
        btnMixer.setTextOn(optionOrCategory);
        btnMixer.setTextOff(item.getName());
        // needed because setTextOn doesn't refresh the text
        btnMixer.setText(optionOrCategory);
    }

    private void clearSelection() {
        btnMixer.setTag(new ArrayList<>());
        item.getSelectedMixers().clear();
        selection.onSelectionChanged(item);
    }

    private <T extends NamedObject> String getMixerName(@NonNull List<T> mixers, String name) {
        return mixers.size() == 1 ? mixers.get(0).getName() : String.format("%dx %s", mixers.size(), name);
    }

    protected void setOptionChecked(boolean checked) {
        btnMixer.setOnCheckedChangeListener(null);
        btnMixer.setChecked(checked);
        btnMixer.setOnCheckedChangeListener(getOnCheckedChangeListener());
    }

    protected boolean isPreview() {
        return false;
    }

    public static int getLayout() {
        return R.layout.list_item_drink_option_category;
    }

    public interface DrinkOptionSelection {
        void onSelectionChanged(DrinkOptionCategory item);
    }
}
