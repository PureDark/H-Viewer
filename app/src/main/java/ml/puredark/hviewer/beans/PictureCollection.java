package ml.puredark.hviewer.beans;

import java.util.List;

public class PictureCollection extends Collection{
	public List<Tag> tags;
	public List<Picture> pictures;

	public PictureCollection(int cid, String title, String author, String cover, String description,
                             String datetime, List<Tag> tags, List<Picture> pictures) {
		super(cid, title, author, cover, description, datetime, Collection.TYPE_PICTURE);
        this.tags = tags;
		this.pictures = pictures;
	}
}
