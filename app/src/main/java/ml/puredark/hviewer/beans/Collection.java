package ml.puredark.hviewer.beans;

import java.lang.reflect.Field;

import ml.puredark.hviewer.dataproviders.AbstractDataProvider;

public class Collection extends AbstractDataProvider.Data{
	public final static int TYPE_ARTICLE = 1;
	public final static int TYPE_PICTURE = 2;
	public final static int TYPE_VIDEO = 3;
	public final static int TYPE_PERSON = 4;
	public int cid, type;
	public String title, author, cover, description, datetime;
	public String categoryTitle;
	public int category, subcategory;
	public boolean isFromLocal = false;

	public Collection(String title, String cover, int type, boolean isFromLocal){
		this.title = title;
		this.cover = cover;
		this.type = type;
		this.isFromLocal = isFromLocal;
		author = "佚名";
		description = "";
		datetime = "";
        categoryTitle = "";
	}
	
	public Collection(int cid, String title, String author, String cover, String description, String datetime, int type, int category, int subcategory){
		this.cid = cid;
		this.title = title;
		this.author = author;
		this.cover = cover;
		this.description = description;
		this.datetime = datetime;
		this.type = type;
		this.category = category;
		this.subcategory = subcategory;
	}

	public void setCategoryTitle(String title){
		this.categoryTitle = title;
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
