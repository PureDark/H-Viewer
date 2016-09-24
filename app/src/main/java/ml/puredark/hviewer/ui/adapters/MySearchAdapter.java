package ml.puredark.hviewer.ui.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.widget.Filter;

import com.miguelcatalan.materialsearchview.SearchAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by PureDark on 2016/8/12.
 */

public class MySearchAdapter extends SearchAdapter {

    private ArrayList<String> data = new ArrayList<>();
    private String[] suggestions;

    public MySearchAdapter(Context context, String[] suggestions) {
        super(context, suggestions);
        this.suggestions = suggestions;
    }

    public MySearchAdapter(Context context, String[] suggestions, Drawable suggestionIcon, boolean ellipsize) {
        super(context, suggestions, suggestionIcon, ellipsize);
        this.suggestions = suggestions;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (!TextUtils.isEmpty(constraint)) {
                    String[] keywords = constraint.toString().split(" ");
                    String keyword = keywords[keywords.length - 1];

                    // Retrieve the autocomplete results.
                    List<String> searchData = new ArrayList<>();

                    for (String string : suggestions) {
                        if (string.toLowerCase().startsWith(keyword.toLowerCase()) && string.length() != keyword.length()) {
                            searchData.add(string);
                        }
                    }

                    // Assign the data to the FilterResults
                    filterResults.values = searchData;
                    filterResults.count = searchData.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results.values != null) {
                    data = (ArrayList<String>) results.values;
                    notifyDataSetChanged();
                }
            }
        };
        return filter;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}
