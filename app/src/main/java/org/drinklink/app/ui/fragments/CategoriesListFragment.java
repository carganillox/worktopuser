/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.drinklink.app.R;
import org.drinklink.app.common.adapter.ViewModelAdapter;
import org.drinklink.app.common.constants.ExtrasKey;
import org.drinklink.app.common.contract.ListAdapterDataSource;
import org.drinklink.app.common.contract.ListAdapterSource;
import org.drinklink.app.common.contract.ListAdapterSourceCallback;
import org.drinklink.app.common.fragment.CommonListFragment;
import org.drinklink.app.common.viewholder.ViewModelBaseHolder;
import org.drinklink.app.common.viewholder.ViewModelHolderFactory;
import org.drinklink.app.loader.DataLoader;
import org.drinklink.app.model.Drink;
import org.drinklink.app.model.DrinkCategory;
import org.drinklink.app.model.DrinkOption;
import org.drinklink.app.model.DrinkOptionCategory;
import org.drinklink.app.model.Place;
import org.drinklink.app.model.request.MixerRequest;
import org.drinklink.app.model.request.OrderItemRequest;
import org.drinklink.app.ui.viewholder.CategoryListItemHolder;
import org.drinklink.app.ui.viewholder.OrderSummaryHolder;
import org.drinklink.app.ui.viewholder.PlaceHeaderHolder;
import org.drinklink.app.ui.viewholder.SubCategoryListItemHolder;
import org.drinklink.app.ui.viewmodel.DrinkCategoryItem;
import org.drinklink.app.utils.ListUtil;
import org.drinklink.app.utils.Logger;
import org.drinklink.app.workflow.OrderKey;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import lombok.Setter;
import rx.Observable;

/**
 *
 */

public class CategoriesListFragment extends CommonListFragment<DrinkCategoryItem> {

    private static final String TAG = "CategoryListFragment";

    private static final String EXPANDED_KEY = "expanded";

    @Setter
    private Place place;
    private OrderSummaryHolder orderHolder;
    private PlaceHeaderHolder placeHeaderHolder;
    private Set<Integer> expanded = new HashSet<>();

    @BindView(R.id.sliding_layout)
    SlidingUpPanelLayout slidingUpPanelLayout;

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_select_category;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        orderHolder = new OrderSummaryHolder(view, (clickView -> order()), (clickView -> reset()), getActivity(), place);
        placeHeaderHolder = new PlaceHeaderHolder(view);
        placeHeaderHolder.bind(getContext(), 0, place);
    }

    @Override
    public void onDestroyView() {
        orderHolder.unBind();
        placeHeaderHolder.unBind();
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        getProcessor().tryTimeout();
        orderHolder.bind(getContext(), 0, getProcessor());
        orderHolder.register(slidingUpPanelLayout);
    }

    @Override
    public void onPause() {
        orderHolder.unregister();
        super.onPause();
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

    @NonNull
    @Override
    protected ListAdapterSource getListAdapterSource(final ListAdapterSourceCallback<DrinkCategoryItem> callback) {

        DataLoader<DrinkCategoryItem> loader  = new DataLoader<DrinkCategoryItem>(getActivity()) {
            @Override
            protected Observable<DrinkCategoryItem> getObservable(boolean forceRefresh) {
                return getApiService().getMenu(getPlaceId())
                        .flatMapIterable(place -> {
                            updatePlace(place);
                            List<DrinkCategory> menu = place.getMenu();
                            fixParentRelation(menu);
                            updateItems(menu);
                            return menu;
                        })
                        .filter(drinkCategory -> !drinkCategory.isEmpty())
                        .map(drinkCategory -> {
                            DrinkCategoryItem drinkCategoryItem = new DrinkCategoryItem(drinkCategory, getProcessor());
                            drinkCategoryItem.setExpanded(expanded.contains(drinkCategory.getId()));
                            return drinkCategoryItem;
                        });
            }
        };
        return new ListAdapterDataSource<>(callback, loader);
    }

    private void updateItems(List<DrinkCategory> menu) {
        ArrayList<Map.Entry<OrderKey, OrderItemRequest>> entries = new ArrayList<>(getProcessor().getDrinkSelection().entrySet());
        for (Map.Entry<OrderKey, OrderItemRequest > entry: entries) {
            OrderItemRequest value = entry.getValue();
            Logger.i(TAG, "check matched:" + value.getDrinkId());
            Drink drink = null;
            for (DrinkCategory category : menu) {
                drink = category.matchItem(value);
                if (drink != null) {
                    Logger.i(TAG, "drink matched:" + drink.getId());
                    break;
                }
            }
            // remove, and recreate drink if found
            getProcessor().getDrinkSelection().remove(entry.getKey());
            if (drink != null) {
                List<DrinkOption> options = getOptions(drink, value.getSelectedMixers());
                if (options.size() == value.getSelectedMixers().size()) {
                    getProcessor().addDrink(drink, value.isWithIce(), options, value.getQuantity());
                    Logger.i(TAG, "drink added");
                } else {
                    Logger.i(TAG, "skip drink because of mixers mismatch");
                }
            } else {
                Logger.i(TAG, "drink not found");
            }
        }
    }



    private List<DrinkOption> getOptions(Drink drink, List<MixerRequest> selectedMixers) {
        List<DrinkOption> options = new ArrayList<>();
        for (MixerRequest mixer : selectedMixers) {
            for (DrinkOptionCategory cat : drink.getMixerCategories()) {
                DrinkOption matchingMixer = ListUtil.findFirst(cat.getMixers(), item -> mixer.getId() == item.getId());
                if (matchingMixer != null) {
                    options.add(matchingMixer);
                    continue;
                }
            }
        }
        return options;
    }

    private void updatePlace(Place place) {
        place.setUserSelected(true);
        this.place = place;
        placeHeaderHolder.bind(getContext(), 0, place);
        orderHolder.setPlace(place);
        orderHolder.setButtonText(getContext());
        getExtras().putString(ExtrasKey.PLACE_EXTRA, getGson().toJson(place));
//        getProcessor().getPlace().setWorkHours(place.getWorkHours());
    }

    // TODO :this is fix to match current cat/subcat serialization on BE
    private void fixParentRelation(List<DrinkCategory> menu) {
        for (DrinkCategory dc : menu) {
            dc.setParentCategoryFromMenu();
        }
    }

    @Override
    protected ViewModelAdapter getAdapterInstance() {
        return new ViewModelAdapter(getContext(), new ViewModelHolderFactory() {
            {
                View.OnClickListener onClick = (view) -> {

                    // TODO: when category has only one subcategory, open subcategory automatically

                    Object tag = view.getTag();
                    boolean isSubCategory = tag instanceof SubCategoryListItemHolder;
                    DrinkCategoryItem categoryItem = ((ViewModelBaseHolder<DrinkCategoryItem>)tag).getItem();
                    if (!isSubCategory && categoryItem.hasSubCategories()) {
                        collapseAllOther(categoryItem);
                        boolean isExpanded = !categoryItem.isExpanded();
                        categoryItem.setExpanded(isExpanded);
                        //notify instead holder.reBind() in order to have animation
                        adapter.notifySingleItemChanged(categoryItem);
                        if (isExpanded) {
                            expanded.add(categoryItem.getCategory().getId());
                        } else {
                            expanded.remove(categoryItem.getCategory().getId());
                        }
                    } else {
                        Logger.i(TAG, "second level category item clicked");
                        Bundle extras = prepareExtras(categoryItem.getCategory());
                        getNavigation().next(DrinksListFragment.class, extras, true);
                    }
                };

                add(DrinkCategoryItem.class, CategoryListItemHolder.getLayout(),
                        view -> new CategoryListItemHolder(view, onClick));
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            this.expanded = new HashSet(savedInstanceState.getIntegerArrayList(EXPANDED_KEY));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putIntegerArrayList(EXPANDED_KEY, new ArrayList(expanded));
    }

    @NonNull
    private Bundle prepareExtras(DrinkCategory category) {
        Bundle extras = new Bundle();
        extras.putString(ExtrasKey.PLACE_EXTRA, getGson().toJson(place));
        extras.putString(ExtrasKey.SUB_MENU_EXTRA, getGson().toJson(category));
        ArrayList<DrinkCategory> parentCategories = new ArrayList<>();

        DrinkCategory menuPlaceholder = new DrinkCategory();
        parentCategories.add(menuPlaceholder);
        menuPlaceholder.setName(getString(R.string.menu_category));
        menuPlaceholder.setTag(CategoriesListFragment.class.getCanonicalName() + ":");

        List<DrinkCategory> categoriesList = category.getParentCategories(parentCategories);
        for (DrinkCategory cat : categoriesList) {
            String navigationTag = getNavigationTag();
            Logger.i(TAG, "category tag: " + navigationTag);
            cat.setTag(navigationTag);
        }
        extras.putString(ExtrasKey.NAVIGATION_CATEGORIES_EXTRA, getGson().toJson(categoriesList));
        return extras;
    }

    @Override
    public String getNavigationTag() {
        return this.getClass().getCanonicalName() + ":" + getCategoryId();
    }

    public String getCategoryId() {
        return "";
    }

    private void collapseAllOther(DrinkCategoryItem categoryItem) {
        List dataItems = adapter.getDataItems();
        for (Object item: dataItems) {
            DrinkCategoryItem drinkCategory = (DrinkCategoryItem) item;
            if (drinkCategory != categoryItem && drinkCategory.isExpanded()) {
                drinkCategory.setExpanded(false);
                adapter.notifySingleItemChanged(drinkCategory);
            }
        }
    }

    private int getPlaceId() {
        return place != null ? place.getId() : 0;
    }

    @Override
    public void init(Bundle bundle) {
        super.init(bundle);
        String placeString = bundle.getString(ExtrasKey.PLACE_EXTRA);
        int placeId = bundle.getInt(ExtrasKey.PLACE_ID_EXTRA, -1);
        if (placeString != null) {
            place = getGson().fromJson(placeString, Place.class);
            getProcessor().forPlace(place);
        } else {
            place = new Place();
            place.setId(placeId);
        }
    }

    @Override
    protected void finishLoading(boolean completed) {
        super.finishLoading(completed);
        orderHolder.bind(getContext(), 0, getProcessor());
    }
}
