package org.drinklink.app.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 *
 */

public class ListUtil {

    public static List merge(List first, List second) {
        if (second == null) {
            return first;
        }
        if (first == null) {
            return second;
        }
        ArrayList merged = new ArrayList<>(first.size() + second.size());
        merged.addAll(first);
        merged.addAll(second);
        return merged;
    }

    public static <T> void remove(List<T> list, Condition<T> condition) {
        if (list != null) {
            Iterator<T> iter = list.iterator();
            while (iter.hasNext()) {
                T item = iter.next();
                if (condition.isMatch(item)) {
                    iter.remove();
                    break;
                }
            }
        }
    }

    public static <T> int count(List<T> list, Condition<T> condition) {
        int count = 0;
        if (list != null) {
            Iterator<T> iter = list.iterator();
            while (iter.hasNext()) {
                count += condition.isMatch(iter.next()) ? 1 : 0;
            }
        }
        return count;
    }

    public static <T> List<T> select(List<T> list, Condition<T> condition) {
        return select(list, condition, new Transform<T, T>() {
            @Override
            public T transform(T item) {
                return item;
            }
        });
    }
    public static <T, S> ArrayList<S> select(Collection<T> list, Condition<T> condition, Transform<T, S> transform) {
        ArrayList<S> retValue = new ArrayList<>();
        if (list != null) {
            Iterator<T> iter = list.iterator();
            while (iter.hasNext()) {
                T item = iter.next();
                if (condition == null || condition.isMatch(item)) {
                    retValue.add(transform.transform(item));
                }
            }
        }
        return retValue;
    }

    public static <T, S> ArrayList<S> transform(Collection<T> list, Transform<T, S> transform) {
        ArrayList<S> retValue = new ArrayList<>();
        if (list != null) {
            Iterator<T> iter = list.iterator();
            while (iter.hasNext()) {
                T item = iter.next();
                retValue.add(transform.transform(item));
            }
        }
        return retValue;
    }

    public static <T> T findFirst(Collection<T> list, Condition<T> condition) {
        T result = null;
        if (list != null) {
            Iterator<T> iterarot = list.iterator();
            loopBlock : while (iterarot.hasNext()) {
                T item = iterarot.next();
                if (condition.isMatch(item)) {
                    result = item;
                    break loopBlock;
                }
            }
        }
        return result;
    }

    public static <T> int findFirstPosition(List<T> list, Condition<T> condition) {
        int result = -1;
        if (list != null) {
            Iterator<T> iterarot = list.iterator();
            int index = 0;
            loopBlock :while (iterarot.hasNext()) {
                T item = iterarot.next();
                if (condition.isMatch(item)) {
                    result = index;
                    break loopBlock;
                }
                index++;
            }
        }
        return result;
    }

    public static <T> String transformListToQueryParamsList(List<T> list) {
        String string = "[";
        if (list != null) {
            for (int index=0; index<list.size(); index++) {
                if (index > 0) {
                    string += ",";
                }
                string += list.get(index).toString();
            }
        }
        string += "]";

        return string;
    }

    public static <T> ArrayList<T> asList(T item) {
        ArrayList<T> array = new ArrayList<>();
        array.add(item);
        return array;
    }

    public interface Condition<T> {
        boolean isMatch(T item);
    }

    public interface Transform<T, S> {
        S transform(T item);
    }
}
