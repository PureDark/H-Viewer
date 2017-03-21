package ml.puredark.hviewer.beans;

public class LocalCollection extends Collection {
    public Site site;
    public int index = 0;
    public int gid = 0;

    public LocalCollection(Collection collection, Site site) {
        super(collection.cid, collection.idCode, collection.title, collection.uploader, collection.cover, collection.category,
                collection.datetime, collection.description, collection.rating, collection.referer, collection.tags,
                collection.pictures, collection.videos, collection.comments, collection.preloaded);
        this.site = site;
    }
}
