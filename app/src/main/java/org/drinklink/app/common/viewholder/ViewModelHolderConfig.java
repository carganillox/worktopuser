package org.drinklink.app.common.viewholder;

import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */

public class ViewModelHolderConfig {

    private List<ConfigItem> customLayoutHolders;

    private ViewModelHolderFactory.HolderInstanceCreation getTextViewModelHolderCreator() {
        return new ViewModelHolderFactory.HolderInstanceCreation() {
            @Override
            public ViewModelBaseHolder newHolderInstance(View view) {
                return new TextViewModelHolder(view);
            }
        };
    }

    private ViewModelHolderFactory.HolderInstanceCreation getButtonModelHolderCreator() {
        return new ViewModelHolderFactory.HolderInstanceCreation() {
            @Override
            public ViewModelBaseHolder newHolderInstance(View view) {
                return new ButtonModelHolder(view);
            }
        };
    }

    private ViewModelHolderFactory.HolderInstanceCreation getClickableHolderCreator() {
        return new ViewModelHolderFactory.HolderInstanceCreation() {
            @Override
            public ViewModelBaseHolder newHolderInstance(View view) {
                return new ViewModelClickableItemHolder(view);
            }
        };
    }

    public <T> void add(Class<? extends T> modelClass, final Class<? extends ViewModelBaseHolder<T>> holderClass) {
        ViewModelHolderFactory.HolderInstanceCreation holderCreator = ViewModelHolderFactory.getHolderCreator(holderClass);
        add(modelClass, holderCreator);
    }

    public <T> void add(Class<? extends T> modelClass, ViewModelHolderFactory.HolderInstanceCreation holderCreator) {
        add(modelClass, null, holderCreator);
    }

    public <T> void add(Class<? extends T> modelClass, Integer layout, ViewModelHolderFactory.HolderInstanceCreation holderCreator) {
        if (customLayoutHolders == null) {
            customLayoutHolders = new ArrayList<>();
        }
        customLayoutHolders.add(new ConfigItem(modelClass, holderCreator, layout));
    }

//    public void addTextViewModel() {
//        add(TextViewModelItem.class, getTextViewModelHolderCreator());
//    }
//
//    public void addButtonViewModel() {
//        add(OneButtonItem.class, getButtonModelHolderCreator());
//    }
//
//    public void addClickableViewModel() {
//        add(ViewModelClickableItem.class, getClickableHolderCreator());
//    }

    public ViewModelHolderFactory.HolderInstanceCreation getHolder(Object item) {
        return customLayoutHolders != null ? findHolder(item) : null;
    }

    private ViewModelHolderFactory.HolderInstanceCreation findHolder(Object item) {
        for (ConfigItem pair : customLayoutHolders) {
            if (pair.clazz.isInstance(item)) {
                return pair.holderCreator;
            }
        }
        return null;
    }

    public int getLayout(Object item) {
        if (customLayoutHolders == null) {
            return -1;
        }

        for(ConfigItem pair: customLayoutHolders) {
            if (pair.clazz.isInstance(item) && pair.layout != null) {
                return pair.layout;
            }
        }
        return -1;
    }

    public class ConfigItem {
        public Class clazz;
        public ViewModelHolderFactory.HolderInstanceCreation holderCreator;
        public Integer layout;

        public ConfigItem(Class clazz, ViewModelHolderFactory.HolderInstanceCreation holderCreator, Integer layout) {
            this.clazz = clazz;
            this.holderCreator = holderCreator;
            this.layout = layout;
        }
    }
}
