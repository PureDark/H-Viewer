package ml.puredark.hviewer.dataproviders;

import java.util.ArrayList;
import java.util.List;

public class DataProvider<T extends DataProvider.Data> {

    public static abstract class Data {
        public abstract int getId();
    }

    private List<T> items;

    public DataProvider(List<T> items) {
        this.items = items;
    }

    public int getCount() {
        return items.size();
    }

    public List<T> getItems() {
        return items;
    }

    public Data getItem(int index) {
        if (index < 0 || index >= getCount()) {
            throw new IndexOutOfBoundsException("index = " + index);
        }

        return items.get(index);
    }

    public void removeItem(int position) {
        items.remove(position);
    }

    public void clear() {
        items = new ArrayList<>();
    }

    public void addItem(T item) {

        items.add(item);
    }

    public void addItem(int position, Data item) {
        items.add(position, (T) item);
    }

    public void addAll(java.util.Collection items) {
        this.items.addAll(items);
    }
}
