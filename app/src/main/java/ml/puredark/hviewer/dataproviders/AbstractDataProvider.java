package ml.puredark.hviewer.dataproviders;

import java.util.Collection;
import java.util.List;

public abstract class AbstractDataProvider<T extends AbstractDataProvider.Data> {

    public abstract int getCount();

    public abstract List getItems();

    public abstract T getItem(int index);

    public abstract void removeItem(int position);

    public abstract void clear();

    public abstract void addItem(T item);

    public abstract void addItem(int position, T item);

    public abstract void addAll(Collection items);

    public static abstract class Data {
        public abstract int getId();
    }
}