package ml.puredark.hviewer.dataproviders;

import java.util.ArrayList;
import java.util.List;

import ml.puredark.hviewer.beans.Collection;

public class CollectionDataProvider extends AbstractDataProvider {
    private List<Collection> items;

    public CollectionDataProvider(List<Collection> items) {
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public List<Collection> getItems() {
        return items;
    }

    @Override
    public Data getItem(int index) {
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
    public void addItem(Data item) {
        items.add((Collection) item);
    }

    @Override
    public void addItem(int position, Data item) {
        items.add(position, (Collection) item);
    }

    @Override
    public void addAll(java.util.Collection items) {
        this.items.addAll(items);
    }
}
