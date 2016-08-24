package ml.puredark.hviewer.beans;

import java.lang.reflect.Field;
import java.util.List;

import ml.puredark.hviewer.dataproviders.AbstractDataProvider;

public class LocalCollection extends Collection {
    public Site site;

    public LocalCollection(Collection collection, Site site){
        super(collection.cid, collection.idCode, collection.title, collection.uploader, collection.cover, collection.category,
                collection.datetime, collection.rating, collection.referer, collection.tags, collection.pictures);
        this.site = site;
    }
}
