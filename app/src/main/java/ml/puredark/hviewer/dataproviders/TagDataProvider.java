package ml.puredark.hviewer.dataproviders;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ml.puredark.hviewer.beans.Tag;

public class TagDataProvider extends AbstractDataProvider {
    private List<Tag> items;

    public TagDataProvider(List<Tag> items) {
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public List<Tag> getItems() {
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
        items.add((Tag) item);
    }

    @Override
    public void addItem(int position, Data item) {
        items.add(position, (Tag) item);
    }

    @Override
    public void addAll(Collection items) {
        this.items.addAll(items);
    }
}
