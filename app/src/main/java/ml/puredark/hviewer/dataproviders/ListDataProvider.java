package ml.puredark.hviewer.dataproviders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ListDataProvider<T extends AbstractDataProvider.Data> extends AbstractDataProvider<T, List> {

    private List<T> items;

    public ListDataProvider(List<T> items) {
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public List<T> getItems() {
        return items;
    }

    @Override
    public T getItem(int index) {
        if (index < 0 || index >= getCount()) {
            throw new IndexOutOfBoundsException("index = " + index);
        }

        return items.get(index);
    }

    @Override
    public void removeItem(int position) {
        items.remove(position);
    }

    @Override
    public void clear() {
        items = new ArrayList<>();
    }

    @Override
    public void setDataSet(List datas) {
        items = datas;
    }

    @Override
    public void addItem(T item) {
        items.add(item);
    }

    @Override
    public void addItem(int position, T item) {
        items.add(position, item);
    }

    @Override
    public void addAll(Collection items) {
        this.items.addAll(items);
    }

    public void moveItem(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) {
            return;
        }
        final T item = items.remove(fromPosition);
        items.add(toPosition, item);
    }
}
