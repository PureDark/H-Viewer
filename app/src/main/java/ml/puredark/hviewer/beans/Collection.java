package ml.puredark.hviewer.beans;

import java.lang.reflect.Field;
import java.util.List;

import ml.puredark.hviewer.dataproviders.AbstractDataProvider;

public class Collection extends AbstractDataProvider.Data{
	public int cid;
	public String title, uploader, cover, category, datetime;
	public float rating;
	public List<Tag> tags;
	public List<Picture> pictures;
	
	public Collection(int cid, String title, String uploader, String cover, String category,
					  String datetime, float rating, List<Tag> tags, List<Picture> pictures){
		this.cid = cid;
		this.title = title;
		this.uploader = uploader;
		this.cover = cover;
		this.category = category;
		this.datetime = datetime;
		this.rating = rating;
		this.tags = tags;
		this.pictures = pictures;
	}


	@Override
	public int getId() {
		return cid;
	}

	@Override
	public boolean equals(Object obj){
		if((obj instanceof Collection)){
			Collection item = (Collection) obj;
            boolean result = true;
			Field[] fs = Collection.class.getDeclaredFields();
            try {
                for (Field f : fs) {
                    f.setAccessible(true);
                    Object v1 = f.get(this);
                    Object v2 = f.get(item);
                    result &= equals(v1, v2);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return result;
		}
		return false;
	}

	public boolean equals(Object obj1, Object obj2) {
		if (obj1 == obj2) {
			return true;
		}
		if (obj1 == null || obj2 == null) {
			return false;
		}
		return obj1.equals(obj2);
	}
}
