package ml.puredark.hviewer.beans;

public class LocalCollection extends Collection {
    public Site site;

    public LocalCollection(Collection collection, Site site){
        super(collection.cid, collection.idCode, collection.title, collection.uploader, collection.cover, collection.category,
                collection.datetime, collection.filename, collection.description, collection.rating, collection.referer, collection.tags,
                collection.pictures, collection.videos, collection.comments, collection.preloaded);
        this.site = site;
    }
}
