package ml.puredark.hviewer.dataproviders;

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

public class ListDataProvider<T extends AbstractDataProvider.Data> extends AbstractDataProvider<T> {

    private List<T> items;

    public ListDataProvider(List<T> items) {
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

    public void addItem(int position, T item) {
        items.add(position, item);
    }

    public void addAll(Collection items) {
        this.items.addAll(items);
    }
}
