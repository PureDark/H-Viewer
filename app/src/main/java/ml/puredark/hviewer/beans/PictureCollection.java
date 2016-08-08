package ml.puredark.hviewer.beans;

import java.util.List;

public class PictureCollection extends Collection{
	public List<Picture> pictures;

	public PictureCollection(String title, String cover, boolean isFromLocal, List<Picture> pictures){
		super(title, cover, Collection.TYPE_PICTURE, isFromLocal);
		this.pictures = pictures;
	}

	public PictureCollection(int cid, String title, String author, String cover, String description, String datetime, int category, int subcategory, List<Picture> pictures) {
		super(cid, title, author, cover, description, datetime, Collection.TYPE_PICTURE, category, subcategory);
		this.pictures = pictures;
	}
}
