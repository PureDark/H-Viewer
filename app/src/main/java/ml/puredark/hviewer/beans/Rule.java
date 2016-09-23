package ml.puredark.hviewer.beans;

public class Rule {
    public Selector item, idCode, title, uploader, cover, category, datetime, rating, tags, description, pictureUrl, pictureThumbnail, pictureHighRes;

    public Rule() {
    }

    public Rule(Selector item, Selector idCode, Selector title, Selector uploader, Selector cover, Selector category,
                Selector datetime, Selector rating, Selector tags, Selector pictureUrl, Selector pictureThumbnail, Selector pictureHighRes) {
        this.item = item;
        this.idCode = idCode;
        this.title = title;
        this.uploader = uploader;
        this.cover = cover;
        this.category = category;
        this.datetime = datetime;
        this.rating = rating;
        this.tags = tags;
        this.pictureUrl = pictureUrl;
        this.pictureThumbnail = pictureThumbnail;
        this.pictureHighRes = pictureHighRes;
    }

}
