package ml.puredark.hviewer.ui.dataproviders;

import java.util.Collection;

public abstract class AbstractDataProvider<T extends AbstractDataProvider.Data, C extends Collection> {

    public abstract int getCount();

    public abstract C getItems();

    public abstract T getItem(int index);

    public abstract void removeItem(int position);

    public abstract void clear();

    public abstract void setDataSet(C datas);

    public abstract void addItem(T item);

    public abstract void addItem(int position, T item);

    public abstract void addAll(Collection items);

    public static abstract class Data {
        public abstract int getId();
    }
}