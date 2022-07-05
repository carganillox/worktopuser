/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.viewholder;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.drinklink.app.R;
import org.drinklink.app.common.adapter.ViewModelAdapter;
import org.drinklink.app.common.viewholder.UpdateSingleFunction;
import org.drinklink.app.common.viewholder.ViewModelBaseHolder;
import org.drinklink.app.common.viewholder.ViewModelHolderFactory;
import org.drinklink.app.model.Drink;
import org.drinklink.app.model.DrinkOption;
import org.drinklink.app.model.DrinkOptionCategory;
import org.drinklink.app.model.IPrice;
import org.drinklink.app.model.NamedObject;
import org.drinklink.app.ui.dialog.DialogManager;
import org.drinklink.app.ui.viewmodel.DrinkOptionItem;
import org.drinklink.app.ui.viewmodel.IDrinkItem;
import org.drinklink.app.utils.Analytics;
import org.drinklink.app.utils.ListUtil;
import org.drinklink.app.utils.Logger;
import org.drinklink.app.workflow.IOrderProcessor;
import org.drinklink.app.workflow.IOrderProcessorPreview;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import lombok.NoArgsConstructor;

import static org.drinklink.app.workflow.IOrderProcessor.MAX_DRINK_COUNT;


public abstract class DrinkListItemBaseHolder<S extends NamedObject & IPrice, T extends IDrinkItem<S>, P extends IOrderProcessorPreview> extends ViewModelBaseHolder<T> {

    private static final String TAG = "DrinkListItemBaseHolder";

    protected final P processor;
    protected final Activity activity;

    @BindView(R.id.list_item)
    ViewGroup container;

    @BindView(R.id.lbl_name)
    TextView name;

    @BindView(R.id.lbl_description)
    TextView description;

    @BindView(R.id.lbl_package)
    TextView tvPackage;

    @BindView(R.id.lbl_unit_price)
    TextView tvUnitPrice;

    @BindView(R.id.lbl_count)
    TextView count;

    @BindView(R.id.lbl_count2)
    TextView count2;

    @Nullable @BindView(R.id.btn_add)
    ImageButton add;

    @Nullable @BindView(R.id.btn_remove)
    ImageButton remove;

    @BindView(R.id.mixer_categories)
    RecyclerView mixersListView;

    @BindView(R.id.btn_additional_info_container)
    View additionalInfoContainer;

    @BindView(R.id.btn_additional_info)
    View additionalInfo;

    View.OnClickListener onAddClick;
    View.OnClickListener onRemoveClick;
    UpdateSingleFunction<Integer, Boolean> selectedCountChangeListener;

    DrinkOptionItem iceOption = new DrinkOptionItem();
    List<DrinkOptionCategory> drinkMixerCategories = new ArrayList<>();
    CountTracker countTracker;

    Analytics analytics;

//    protected abstract OrderItemRequest getItemRequest(IOrderProcessor processor, T drink);

    public DrinkListItemBaseHolder(View itemView, P processor, Activity activity, CountTracker countTracker, UpdateSingleFunction<Integer, Boolean> onUpdate) {
        super(itemView);
        this.processor = processor;
        this.activity = activity;
        this.selectedCountChangeListener = onUpdate != null ? onUpdate : UpdateSingleFunction.PLACEHOLDER;
        this.countTracker = countTracker;
        onAddClick = (view) -> addDrink(processor, view);
        onRemoveClick = (view) -> increment(processor, view, -1);
    }

    private void addDrink(P processor, View view) {
        if(!increment(processor, view, 1)){
            DialogManager.showInfoDialog(activity,
                    ctx.getString(R.string.max_drink_count_number),
                    ctx.getString(R.string.max_drink_count_message));
        }
    }

    @Override
    public void bind(Context ctx, int position, T item) {
        super.bind(ctx, position, item);
        Drink drink = item.getDrink();

        name.setText(drink.getName());
        description.setText(drink.getDescription());
        setInfoVisibility(description, !TextUtils.isEmpty(drink.getDescription()));
        boolean hasAdditionalInfo = !TextUtils.isEmpty(drink.getAdditionalInfo()) || hasPhoto(drink);
        setInfoVisibility(additionalInfo, hasAdditionalInfo);
        if (hasAdditionalInfo) {
            additionalInfoContainer.setOnClickListener((view) -> additionalInfo());
        }
        tvPackage.setText(drink.getVolume() != null ? drink.getVolume() : "");
        addIncrementListeners();

        updateCount(ctx, item.getCount());
        updatePrice(drink, item.getSelectedMixers());

        drinkMixerCategories.clear();
        mixersListView.setLayoutManager(new GridLayoutManager(ctx, 2));
        List mixerCategories = getMixerCategories(drink);
        mixersListView.setAdapter(getMixersAdapter(mixerCategories));
    }

    private boolean hasPhoto(Drink drink) {
        return !TextUtils.isEmpty(drink.getImagePath());
    }

    protected void setInfoVisibility(View description, boolean b) {
        setVisibility(description, b);
    }


    protected void addIncrementListeners() {
        if (remove != null && add != null) {
            setClickListenerWithHolder(add, onAddClick);
            setClickListenerWithHolder(count2, onAddClick);
            setClickListenerWithHolder(remove, onRemoveClick);
            setClickListenerWithHolder(count, onRemoveClick);
        }
    }

    @NonNull
    private List getMixerCategories(Drink drink) {
        List mixerCategories = new ArrayList<>();
        if (drink.isAllowIce() && (!isPreview() || item.isWithIce())) {
            iceOption.setSelected(item.isWithIce());
            iceOption.setSelectedLabel(ctx.getString(R.string.option_with_ice));
            iceOption.setNotSelectedLabel(ctx.getString(R.string.option_add_ice));
            mixerCategories.add(iceOption);
        }
        if (drink.isAllowMixers()) {
            for (DrinkOptionCategory cat : drink.getAvailableOptions()) {
                if (!cat.getMixers().isEmpty()) {
                    List<DrinkOption> select = ListUtil.select(cat.getMixers(), option -> hasOption(option));
                    if (!isPreview() || !select.isEmpty()) {
                        addMixerCategories(mixerCategories, drinkMixerCategories, cat, select);
                    }
                }
            }
        }
        return mixerCategories;
    }

    protected void addMixerCategories(List mixerCategories, List trackedCategories, DrinkOptionCategory cat, List<DrinkOption> select) {
        DrinkOptionCategory drinkCat = new DrinkOptionCategory(cat);
        drinkCat.setSelectedMixers(select);
        mixerCategories.add(drinkCat);
        trackedCategories.add(drinkCat);
    }

    protected boolean isPreview() {
        return false;
    }

    private boolean hasOption(final DrinkOption option) {
        return ListUtil.findFirst(item.getSelectedMixers(), currentOption -> currentOption.getId() == option.getId()) != null;
    }

    @NonNull
    protected ViewModelAdapter getMixersAdapter(List items) {
        final DrinkOptionCategoryListItemHolder.DrinkOptionSelection drinkOptionSelection = select -> {
            updatePrice(item.getDrink());
            optionChanged.run();
        };

        return new ViewModelAdapter(ctx, items, new ViewModelHolderFactory() {
            {
                add(DrinkOptionCategory.class, DrinkOptionCategoryListItemHolder.getLayout(),
                        view -> new DrinkOptionCategoryListItemHolder(view, activity, drinkOptionSelection));
                add(DrinkOptionItem.class, DrinkOptionListItemHolder.getLayout(), view -> new DrinkOptionListItemHolder(view, optionChanged));
            }
        });
    }

    Runnable optionChanged = new Runnable() {
        @Override
        public void run() {
            if (item.getCount() == 0) {
                onAddClick.onClick(add);
            }
        }
    };

    @NonNull
    private void updateCount(Context ctx, int count) {
        if (remove != null) {
            remove.setEnabled(count > 0);
        }
        String countString = ctx.getString(R.string.drink_count_format, count);
        this.count.setText(countString.substring(0, 1));
        count2.setText(countString.substring(1, 2));
    }

    private void updatePrice(Drink drink) {
        List<DrinkOption> mixers = getSelectedMixers(drinkMixerCategories);
        BigDecimal mixerPrice = BigDecimal.ZERO;
        for (DrinkOption price : mixers) {
            mixerPrice = mixerPrice.add(price.getPrice());
        }
        updatePrice(drink, mixerPrice);
    }

    private void updatePrice(Drink drink, List<S> selectedMixer) {
        BigDecimal mixerPrice = BigDecimal.ZERO;
        if (selectedMixer != null) {
            for (S price : selectedMixer) {
                mixerPrice = mixerPrice.add(price.getPrice());
            }
        }
        updatePrice(drink, mixerPrice);
    }

    private void updatePrice(Drink drink, BigDecimal mixerPrice) {
        tvUnitPrice.setText(ctx.getString(R.string.order_amount_format_unit, drink.getPrice().add(mixerPrice)));

    }

    protected boolean increment(P processor, View view, int increment) {
        if (getTotalCount(processor) + increment > MAX_DRINK_COUNT) {
            Logger.d(TAG, "drink increment greater than max");
            return false;
        }
        logAnalytics(increment > 0);
        item.increment(increment);
        countTracker.update(this, item.getCount());
        DrinkListItemBaseHolder holder = (DrinkListItemBaseHolder)view.getTag();
        holder.updateCount(ctx, item.getCount());
        selectedCountChangeListener.apply(item.getCount());

        Logger.d(TAG, "drink increment: " +
                item.getDrink().getName() + " , increment: " + increment);
        return true;
    }

    private void logAnalytics(boolean increase) {
        if (analytics == null) {
            analytics = new Analytics(ctx, TAG);
        }
        if (increase) {
            analytics.addToCart();
        } else {
            analytics.removeFromChart();
        }
    }

    protected int getTotalCount(P processor) {
        return countTracker.getCount();
    }

    public void syncToProcessor() {
        Drink drink = item.getDrink();
        List<DrinkOption> mixers = getSelectedMixers(drinkMixerCategories);
        processor.addDrink(drink, iceOption.isSelected(), mixers, item.getCount());
        item.reset();
        reBind();
        Logger.d(TAG, "update processor for drink:" + drink.getName());
    }

    protected List<DrinkOption> getSelectedMixers(List<DrinkOptionCategory> drinkMixerCategories) {
        List<DrinkOption> mixers = new ArrayList<>();
        for (DrinkOptionCategory category : drinkMixerCategories) {
            mixers.addAll(category.getSelectedMixers());
        }
        return mixers;
    }

    @OnClick(R.id.btn_additional_info)
    public void additionalInfo() {
        Drink drink = item.getDrink();
        DialogManager.showInfoDialogWithPhoto(activity, drink.getName(), drink.getAdditionalInfo(), drink.getImageUrl());
    }

    public static int getLayout() {
        return R.layout.list_item_drink;
    }

    public static int getPreviewLayout() {
        return R.layout.list_item_drink_preview;
    }

    public static int getCodeAndStatusLayout() {
        return R.layout.list_item_drink_code_and_status;
    }

    @NoArgsConstructor
    public static class CountTracker {
        private IOrderProcessor processor;
        Map<Object, Integer> tracker = new HashMap<>();

        public CountTracker(IOrderProcessor processor) {
            this.processor = processor;
        }

        public void update(Object key, Integer value) {
            tracker.put(key, value);
        }

        public int getCount() {
            int sum = 0;
            for (Integer count : tracker.values()) {
                sum += count;
            }
            return sum + (processor != null ? processor.getCount() : 0);
        }

        public void reset() {
            tracker.clear();
        }
    }
}
