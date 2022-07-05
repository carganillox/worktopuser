package org.drinklink.app.common.viewholder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.drinklink.app.common.viewmodel.ViewModelItem;
import org.drinklink.app.common.viewmodel.ViewModelListItem;
import org.drinklink.app.utils.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;

/**
 *
 */

public class ViewModelHolderFactory {

    private static final String TAG = "ViewModelHolderFactory";

    private ViewTypeCounter viewTypeCounter = new ViewTypeCounter();

    private HashMap<Integer, HolderInstanceCreation> creators = new HashMap<>();

    private HashMap<Class, Integer> viewTypes = new HashMap<>();
    private HashMap<Integer, Integer> viewSubTypes = null;
    private HashMap<Integer, Integer> layoutIds = new HashMap<>();
    private HashMap<Integer, Integer> viewTypesByResource = new HashMap<>();

    private HashMap<Class, SubtypesHandler> subTypeHandlers = null;

    ViewModelHolderConfig config = new ViewModelHolderConfig();

    public ViewModelBaseHolder getHolder(View view, int type){
        ViewModelBaseHolder viewModelBaseHolder = creators.get(type).newHolderInstance(view);
        return viewModelBaseHolder;
    }

    public int getType(Object item){

        int viewSubType = getViewSubType(item);
        if (viewSubType > 0) {
            return viewSubType;
        } else {
            int layoutResource = getViewByResource(item);
            if (layoutResource < 0) {
                Class<?> aClass = getViewClass(item);
                Integer viewType = viewTypes.get(aClass);
                if (viewType == null) {
                    viewType = getViewType(item, config.getLayout(item)); // try for config items
                    if (viewType == null) {
                        Logger.e(TAG, " Type not registered in ViewModelHolderFactory : " +
                                aClass.getSimpleName());
                    }
                }
                return viewType;
            } else {
                Integer viewType = getViewType(item, layoutResource);
                return viewType;
            }
        }
    }

    @Nullable
    private Integer getViewType(Object item, int layoutResource) {
        Integer viewType = viewTypesByResource.get(layoutResource);
        if (viewType == null) { // register first time
            HolderInstanceCreation holderCreator = config.getHolder(item);
            if (holderCreator == null && item instanceof ViewModelItem) {
                holderCreator = getViewModelHolderCreator();
            }

            if (holderCreator != null) {
                viewType = registerLayout(layoutResource, holderCreator);
            } else {
                Logger.e(TAG, "Not supported default type: " + item.getClass().getSimpleName());
            }
        }
        return viewType;
    }

    public ViewModelHolderConfig getConfig() {
        return config;
    }

    private int getViewSubType(Object item) {
        if (subTypeHandlers != null) {
            Class<?> aClass = item.getClass();
            SubtypesHandler handler = subTypeHandlers.get(aClass);
            if (handler != null) {
                int subtype = handler.getSubType(item);
                Integer viewType = viewSubTypes.get(subtype);
                if (viewType == null) {
                    viewType = viewTypeCounter.getNext();
                    viewSubTypes.put(subtype, viewType);
                    Integer origViewType = viewTypes.get(aClass);
                    creators.put(viewType, creators.get(origViewType));
                    layoutIds.put(viewType, layoutIds.get(origViewType));
                }
                return viewType;
            }
        }
        return 0;
    }

    public <T> void registerSubTypeHandler(Class<? extends T> aClass, SubtypesHandler<T> handler) {
        if (subTypeHandlers == null) {
            subTypeHandlers = new HashMap<>();
            viewSubTypes = new HashMap<>();
        }
        subTypeHandlers.put(aClass, handler);
    }

    @NonNull
    private HolderInstanceCreation getViewModelHolderCreator() {
        return new HolderInstanceCreation() {
            @Override
            public ViewModelBaseHolder newHolderInstance(View view) {
                return new ViewModelBaseHolder(view);
            }
        };
    }

    private int getViewByResource(Object item) {
        if (item instanceof ViewModelItem) {
            return ((ViewModelItem)item).getViewByResource();
        }
        return -1;
    }

    private Class getViewClass(Object item) {
        if (item instanceof ViewModelListItem) {
            return ((ViewModelListItem) item).getModelClass();
        } else {
            return item.getClass();
        }
    }

    public <T> void addList(Class<? extends T> modelClass, int layoutResId, final Class<? extends ViewModelBaseHolder<List<T>>> holderClass) {
        registerClass(modelClass, layoutResId, holderClass);
    }

    /**
     * Doesn't work with proguard obfuscation
     */
    @Deprecated
    public <T> void add(Class<? extends T> modelClass, int layoutResId, final Class<? extends ViewModelBaseHolder<T>> holderClass) {
        registerClass(modelClass, layoutResId, holderClass);
    }

    /**
     * Doesn't work with proguard obfuscation
     */
    @Deprecated
    public <T> void add(Class<? extends T> modelClass, int layoutResId, final Class<? extends ViewModelBaseHolder<T>> holderClass, Class secondArgClass, Object secondArg) {
        registerClass(modelClass, layoutResId, holderClass, secondArgClass, secondArg);
    }

    private <T> void registerClass(Class<? extends T> modelClass, int layoutResId, final Class holderClass) {
            registerClass(modelClass, layoutResId, holderClass, null, null);
    }

    private <T> void registerClass(Class<? extends T> modelClass, int layoutResId, final Class holderClass, Class secondArgClass, Object secondArg) {
        add(modelClass, layoutResId, getHolderCreator(holderClass, secondArgClass, secondArg));
    }

    @NonNull
    public static <T> HolderInstanceCreation getHolderCreator(final Class holderClass) {
        return getHolderCreator(holderClass, null, null);
    }

    @NonNull
    public static <T> HolderInstanceCreation getHolderCreator(final Class holderClass, Class secondArgClass, Object secondArg) {
        return new HolderInstanceCreation() {

            Constructor<? extends ViewModelBaseHolder<T>> constructor;
            {
                try {
                    if (secondArgClass != null) {
                        Class<?>[] constructorArgs = new Class<?>[2];
                        constructorArgs[0] = View.class;
                        constructorArgs[1] = secondArgClass;
                        constructor = holderClass.getConstructor(constructorArgs);
                    } else {
                        constructor = holderClass.getConstructor(View.class);
                    }

                } catch (NoSuchMethodException e) {
                    logConstructorError(secondArgClass);
                }
            }

            @Override
            public ViewModelBaseHolder<T> newHolderInstance(View view) {
                ViewModelBaseHolder<T> instance = null;
                try {
                    if (secondArgClass != null) {
                        Object[] constructorArgs = new Object[2];
                        constructorArgs[0] = view;
                        constructorArgs[1] = secondArg;
                        return constructor.newInstance(constructorArgs);
                    } else {
                        return constructor.newInstance(view);
                    }
                } catch (InstantiationException e) {
                    logConstructorError(secondArgClass);
                } catch (IllegalAccessException e) {
                    logConstructorError(secondArgClass);
                } catch (InvocationTargetException e) {
                    logConstructorError(secondArgClass);
                } catch (NullPointerException e) {
                    logConstructorError(secondArgClass);
                }
                return instance;
            }

            private void logConstructorError(Class clazz) {
                Logger.e(TAG, "Constructor accepting only View is missing for class " + holderClass.getSimpleName() + ", second class " +
                                clazz != null ? clazz.getSimpleName() : "no second class");
            }
        };
    }

    public <T> int add(Class<T> modelClass,
                       int layoutResId,
                       HolderInstanceCreation<? extends ViewModelBaseHolder<T>> holderCreator,
                       SubtypesHandler<T> handler) {
        int viewType = add(modelClass, layoutResId, holderCreator);
        registerSubTypeHandler(modelClass, handler);
        return viewType;
    }

    public <T> int addList(Class<T> modelClass, int layoutResId, HolderInstanceCreation<? extends ViewModelBaseHolder<List<T>>> holderCreator) {
        return registerClass(modelClass, layoutResId, holderCreator);
    }

    public <T> int add(Class<T> modelClass, int layoutResId, HolderInstanceCreation<? extends ViewModelBaseHolder<T>> holderCreator) {
        return registerClass(modelClass, layoutResId, holderCreator);
    }

    public <T> void addSuper(Class<T> modelClass, int layoutResId, ViewModelHolderFactory.HolderInstanceCreation<? extends ViewModelBaseHolder<T>> holderCreator) {
        config.add(modelClass, layoutResId, holderCreator);
    }

    private <T> int registerClass(Class<T> modelClass, int layoutResId, HolderInstanceCreation holderCreator) {
        Integer viewType = viewTypeCounter.getViewType(viewTypes, modelClass);
        viewTypes.put(modelClass, viewType);
        addViewType(layoutResId, holderCreator, viewType);
        return viewType;
    }

    private void addViewType(int layoutResId, HolderInstanceCreation holderCreator, Integer viewType) {
        creators.put(viewType, holderCreator);
        layoutIds.put(viewType, layoutResId);
    }

    private int add(int layoutResId, HolderInstanceCreation holderCreator) {
        Integer viewType = viewTypeCounter.getViewType(viewTypesByResource, layoutResId);
        viewTypesByResource.put(layoutResId, viewType);
        addViewType(layoutResId, holderCreator, viewType);
        return viewType;
    }

    private int registerLayout(int layoutResourceId, HolderInstanceCreation holderCreator) {
        return add(layoutResourceId, holderCreator);
    }

    public int getLayout(int viewType) {
        return layoutIds.get(viewType);
    }

    public interface HolderInstanceCreation<T extends ViewModelBaseHolder> {
        T newHolderInstance(View view);
    }

    public interface SubtypesHandler<T> {
        int getSubType(T item);
    }

    private class ViewTypeCounter {
        private int viewTypeCounter = 1;
        public <K, T> Integer getViewType(HashMap<T, Integer> hashMap, K key) {
            Integer viewType = hashMap.get(key);
            if (viewType == null) {
                viewType = getNext();
            }
            return viewType;
        }

        @NonNull
        public Integer getNext() {
            return viewTypeCounter++;
        }
    }
}
