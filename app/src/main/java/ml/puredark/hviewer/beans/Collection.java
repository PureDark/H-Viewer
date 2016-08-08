package ml.puredark.hviewer.beans;

import java.lang.reflect.Field;

import ml.puredark.hviewer.dataproviders.AbstractDataProvider;

public class Collection extends AbstractDataProvider.Data{
	public final static int TYPE_PICTURE = 1;
	public int cid, type;
	public String title, author, cover, description, datetime;
	
	public Collection(int cid, String title, String author, String cover, String description, String datetime, int type){
		this.cid = cid;
		this.title = title;
		this.author = author;
		this.cover = cover;
		this.description = description;
		this.datetime = datetime;
        this.type = type;
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
