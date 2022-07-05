/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.gson.reflect.TypeToken;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.drinklink.app.R;
import org.drinklink.app.common.adapter.ViewModelAdapter;
import org.drinklink.app.common.constants.ExtrasKey;
import org.drinklink.app.common.fragment.DrinkLinkFragment;
import org.drinklink.app.common.viewholder.UpdateSingleFunction;
import org.drinklink.app.common.viewholder.ViewModelHolderFactory;
import org.drinklink.app.model.DrinkCategory;
import org.drinklink.app.model.Place;
import org.drinklink.app.ui.dialog.DialogManager;
import org.drinklink.app.ui.viewholder.AnimatedButtonHolder;
import org.drinklink.app.ui.viewholder.DrinkListItemBaseHolder;
import org.drinklink.app.ui.viewholder.DrinkListItemHolder;
import org.drinklink.app.ui.viewholder.DrinkTopCategoryListItemHolder;
import org.drinklink.app.ui.viewholder.NavigationCategoryListItemHolder;
import org.drinklink.app.ui.viewholder.OrderSummaryHolder;
import org.drinklink.app.ui.viewholder.PlaceHeaderHolder;
import org.drinklink.app.ui.viewmodel.DrinkCategoryItem;
import org.drinklink.app.ui.viewmodel.DrinkSelectionItem;
import org.drinklink.app.ui.viewmodel.FirstNavigationDrinkCategory;
import org.drinklink.app.ui.viewmodel.NavigationDrinkCategory;
import org.drinklink.app.utils.DrinkIconUtils;
import org.drinklink.app.utils.ListUtil;
import org.drinklink.app.utils.Logger;
import org.drinklink.app.workflow.IOrderProcessor;
import org.drinklink.app.workflow.OrderProcessor;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import butterknife.BindView;

/**
 *
 */

public class DrinksListFragment extends DrinkLinkFragment {

    private static final String TAG = "DrinkListFragment";

    private Place place;
    private DrinkCategory drinkCategory;
    private List<NavigationDrinkCategory> navigationCategories;
    private ViewModelAdapter adapter;
    private ViewModelAdapter navigationAdapter;
    private OrderSummaryHolder orderHolder;
    private PlaceHeaderHolder placeHeaderHolder;
    private AnimatedButtonHolder addButtonAnimationHolder;
    private DrinkTopCategoryListItemHolder drinkTopCategoryListItemHolder;

    @BindView(R.id.main_list)
    RecyclerView list;

    @BindView(R.id.list_category_navigation)
    RecyclerView navigation;

    @BindView(R.id.sliding_layout)
    SlidingUpPanelLayout slidingUpPanelLayout;

    @BindView(R.id.add_button_container)
    View addButtonContainer;

    @BindView(R.id.logo_drink_category)
    AppCompatImageView drinkCategoryLogo;

    private IOrderProcessor currentProcessor;

    List<DrinkListItemHolder> drinkHolders = new ArrayList<>();
    Set<String> selectedDrinks = new HashSet<>();
    DrinkListItemBaseHolder.CountTracker countTracker;

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_select_drink;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        orderHolder = new OrderSummaryHolder(view, (clickView -> order()), (clickView -> reset()), getActivity(), place);
        countTracker = new DrinkListItemBaseHolder.CountTracker(getProcessor());
        orderHolder.bind(getContext(), 0, getProcessor());
        placeHeaderHolder = new PlaceHeaderHolder(view);
        placeHeaderHolder.bind(getContext(), 0, place);
        addButtonAnimationHolder = new AnimatedButtonHolder(addButtonContainer);
        addButtonAnimationHolder.bind(getContext(), 0, false);

        bindCategory(view);
        initCategory();
    }

    private void bindCategory(View view) {
        drinkTopCategoryListItemHolder = new DrinkTopCategoryListItemHolder(view, getAddClickListener());
        DrinkCategoryItem categoryItem = new DrinkCategoryItem(drinkCategory, getProcessor());
        drinkTopCategoryListItemHolder.bind(getContext(), 0, categoryItem);

        int drawableResourceId = DrinkIconUtils.getDrinkIcon(getContext(), drinkCategory.getIconId());
        drinkCategoryLogo.setImageResource(drawableResourceId);
    }

    @NonNull
    private View.OnClickListener getAddClickListener() {
        return view -> {
            if (selectedDrinks.isEmpty()) {
                return;
            }
            if (countTracker.getCount() > IOrderProcessor.MAX_DRINK_COUNT) {
                DialogManager.showInfoDialog(getActivity(),
                        getString(R.string.max_drink_count_number),
                        getString(R.string.max_drink_count_message));
                return;
            }
            for (DrinkListItemHolder holder : drinkHolders) {
                holder.syncToProcessor();
            }
            getProcessor().merge(currentProcessor);

            countTracker.reset();
            addButtonAnimationHolder.setButtons(false);
            selectedDrinks.clear();
            currentProcessor.reset();
            Logger.i(TAG, "animate");
            orderHolder.animate();
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        orderHolder.register(slidingUpPanelLayout);
    }

    @Override
    public void onPause() {
        orderHolder.unregister();
        super.onPause();
    }

    private void initCategory() {
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = getAdapterInstance();
        adapter.appendItems(setDrinkCategory(drinkCategory));
        list.setAdapter(adapter);
        currentProcessor = new OrderProcessor(); // TODO: check if this is mandatory

        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(getContext());
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setJustifyContent(JustifyContent.FLEX_START);
        navigation.setLayoutManager(layoutManager);
        navigationAdapter = getNavigationAdapterInstance();
        navigationAdapter.appendItems(navigationCategories);
        navigation.setAdapter(navigationAdapter);
    }

    private ArrayList<DrinkSelectionItem> setDrinkCategory(DrinkCategory category) {
        return ListUtil.transform(category.getDrinks(), item -> new DrinkSelectionItem(item));
    }

    private ViewModelAdapter getNavigationAdapterInstance() {
        return new ViewModelAdapter(getContext(), new ViewModelHolderFactory() {
            {
                View.OnClickListener navigationClick = view -> {
                    Logger.d(TAG, "Navigation clicked");
                    Object tag = view.getTag();
                    NavigationDrinkCategory item = ((NavigationCategoryListItemHolder<NavigationDrinkCategory>) tag).getItem();
                    getNavigation().previous(item.getTag());
                };
                add(NavigationDrinkCategory.class, NavigationCategoryListItemHolder.getLayout(),
                        (view) -> new NavigationCategoryListItemHolder(view, navigationClick));
                add(FirstNavigationDrinkCategory.class, NavigationCategoryListItemHolder.getLayoutFirst(),
                        (view) -> new NavigationCategoryListItemHolder(view, navigationClick));

            }
        });
    }

    protected ViewModelAdapter getAdapterInstance() {
        return new ViewModelAdapter(getContext(), new ViewModelHolderFactory() {
            {
                add(DrinkSelectionItem.class, DrinkListItemHolder.getLayout(), view -> {
                    DrinkListItemHolder drinkListItemHolder = new DrinkListItemHolder(view, currentProcessor, getActivity(), countTracker, getSelectedCountUpdateCallback());
                    drinkHolders.add(drinkListItemHolder);
                    return drinkListItemHolder;
                });
            }
        });
    }

    @NotNull
    private UpdateSingleFunction<Integer, Boolean> getSelectedCountUpdateCallback() {
        final String guid = UUID.randomUUID().toString();
        return count -> {
            if (count > 0) {
                selectedDrinks.add(guid);
            } else {
                selectedDrinks.remove(guid);
            }
            addButtonAnimationHolder.setButtons(!selectedDrinks.isEmpty());
            return true;
        };
    }

    @Override
    public void onDestroyView() {
        orderHolder.unBind();
        placeHeaderHolder.unBind();
        super.onDestroyView();
    }

    private void order() {
        Logger.d(TAG, "Proceed with payment ...");
        //TODO: this is not working
        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        resetPreviousPaymentDetails();
        getAnalytics().beginCheckout();
        getNavigation().next(PaymentFragment.class, getExtras(), true);
    }

    private void resetPreviousPaymentDetails() {
        getProcessor().setTip(null);
        getProcessor().setDiscount(null);
        getProcessor().setVipCharge(BigDecimal.ZERO);
    }

    private void reset() {
        getProcessor().reset();
        orderHolder.reBind();
    }

    @Override
    public void init(Bundle bundle) {
        super.init(bundle);
        String placeString = bundle.getString(ExtrasKey.PLACE_EXTRA);
        place = getGson().fromJson(placeString, Place.class);
        String drinkCategoryString = bundle.getString(ExtrasKey.SUB_MENU_EXTRA);
        drinkCategory = getGson().fromJson(drinkCategoryString, DrinkCategory.class);
        String navigationCategoriesString = bundle.getString(ExtrasKey.NAVIGATION_CATEGORIES_EXTRA);
        navigationCategories = getGson().fromJson(navigationCategoriesString,
                new TypeToken<List<NavigationDrinkCategory>>() {
                }.getType());
        setLast();
        setFirst();
        getProcessor().forPlace(place);
    }

    private void setFirst() {
        if (navigationCategories.isEmpty()) {
            return;
        }
        NavigationDrinkCategory navigationDrinkCategory = navigationCategories.get(0);
        navigationCategories.remove(0);
        navigationCategories.add(0, new FirstNavigationDrinkCategory(navigationDrinkCategory));
    }

    private void setLast() {
        if (!navigationCategories.isEmpty()) {
            navigationCategories.get(navigationCategories.size() - 1).setLast(true);
        }
    }
}
