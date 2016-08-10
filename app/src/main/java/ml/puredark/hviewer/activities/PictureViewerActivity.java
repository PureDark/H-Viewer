package ml.puredark.hviewer.activities;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.Collection;
import ml.puredark.hviewer.beans.Picture;
import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.customs.ExViewPager;
import ml.puredark.hviewer.helpers.HViewerHttpClient;
import ml.puredark.hviewer.helpers.RuleParser;


public class PictureViewerActivity extends AppCompatActivity {

    @BindView(R.id.tv_count)
    TextView tvCount;
    @BindView(R.id.view_pager)
    ExViewPager viewPager;

    private List<Picture> pictures;

    private Site site;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_viewer);
        ButterKnife.bind(this);

        if (HViewerApplication.temp instanceof Site)
            site = (Site) HViewerApplication.temp;
        if (HViewerApplication.temp2 instanceof List<?>)
            pictures = (List<Picture>) HViewerApplication.temp2;

        if (site == null || pictures == null || pictures.size()==0) {
            finish();
            return;
        }

        tvCount.setText((1)+"/"+pictures.size());

        int position = getIntent().getIntExtra("position", 0);

        PicturePagerAdapter viewPagerAdapter = new PicturePagerAdapter();
        viewPager.setAdapter(viewPagerAdapter);

        ViewPager.OnPageChangeListener listener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                tvCount.setText((position+1)+"/"+pictures.size());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        };
        viewPager.addOnPageChangeListener(listener);

        viewPager.setOffscreenPageLimit(3);

        viewPager.setCurrentItem(position);
    }

    private class PicturePagerAdapter extends PagerAdapter {
        private View[] views = new View[pictures.size()];

        @Override
        public int getCount() {
            return pictures.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            if (views[position] != null) {
                container.removeView(views[position]);
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = getLayoutInflater().inflate(R.layout.view_picture_viewer, null);
            final ImageView imageView = (ImageView) view.findViewById(R.id.iv_picture);
            final Picture picture = pictures.get(position);
            if(picture.pic!=null){
                HViewerApplication.loadImageFromUrl(imageView, picture.pic);
            }else
                HViewerHttpClient.get(picture.url, new HViewerHttpClient.OnResponseListener() {
                    @Override
                    public void onSuccess(String result) {
                        picture.pic = RuleParser.getPictureUrl(result, site.picUrlSelector);
                        HViewerApplication.loadImageFromUrl(imageView, picture.pic);
                    }

                    @Override
                    public void onFailure(HViewerHttpClient.HttpError error) {

                    }
                });
            views[position] = view;
            container.addView(view, 0);
            return view;
        }
    }
}
