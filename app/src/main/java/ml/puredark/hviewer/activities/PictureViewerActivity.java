package ml.puredark.hviewer.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.Picture;
import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.customs.ExViewPager;
import ml.puredark.hviewer.helpers.HViewerHttpClient;
import ml.puredark.hviewer.helpers.MDStatusBarCompat;
import ml.puredark.hviewer.helpers.RuleParser;

import static android.R.attr.bitmap;


public class PictureViewerActivity extends AppCompatActivity {

    @BindView(R.id.tv_count)
    TextView tvCount;
    @BindView(R.id.view_pager)
    ExViewPager viewPager;

    private PicturePagerAdapter picturePagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_viewer);
        ButterKnife.bind(this);
        MDStatusBarCompat.setImageTransparent(this);

        if (HViewerApplication.temp instanceof PicturePagerAdapter)
            picturePagerAdapter = (PicturePagerAdapter) HViewerApplication.temp;

        if (picturePagerAdapter == null || picturePagerAdapter.getCount() == 0) {
            Toast.makeText(this, "数据错误，请刷新后重试", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        int position = getIntent().getIntExtra("position", 0);

        tvCount.setText((position + 1) + "/" + picturePagerAdapter.getCount());

        viewPager.setAdapter(picturePagerAdapter);

        ViewPager.OnPageChangeListener listener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                tvCount.setText((position + 1) + "/" + picturePagerAdapter.getCount());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        };
        viewPager.addOnPageChangeListener(listener);

        viewPager.setOffscreenPageLimit(5);

        viewPager.setCurrentItem(position);
    }

    public static class PicturePagerAdapter extends PagerAdapter {
        private Site site;
        public List<Picture> pictures;

        public PicturePagerAdapter(Site site, List<Picture> pictures) {
            this.site = site;
            this.pictures = pictures;
        }

        private View[] views = new View[1000];

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
            View view = LayoutInflater.from(container.getContext()).inflate(R.layout.view_picture_viewer, null);
            final ImageView imageView = (ImageView) view.findViewById(R.id.iv_picture);
            final Picture picture = pictures.get(position);
            if(site.picUrlSelector==null){
                picture.pic = picture.url;
            }
            if (picture.pic != null) {
                HViewerApplication.loadImageFromUrl(imageView, picture.pic, site.cookie);
            } else {
                getPictureUrl(imageView, picture, site);
            }
            views[position] = view;
            container.addView(view, 0);
            return view;
        }

        private void getPictureUrl(final ImageView imageView, final Picture picture, final Site site) {
            HViewerHttpClient.get(picture.url, site.getCookies(), new HViewerHttpClient.OnResponseListener() {

                @Override
                public void onSuccess(String contentType, Object result) {
                    if (contentType.contains("image") && result instanceof Bitmap) {
                        picture.pic = picture.url;
                        imageView.setImageBitmap((Bitmap) result);
                        Log.d("PicturePagerAdapter", "result = "+result);
                        Log.d("PicturePagerAdapter", "result got");
                    } else {
                        picture.pic = RuleParser.getPictureUrl((String) result, site.picUrlSelector, picture.url);
                        picture.retries = 0;
                        HViewerApplication.loadImageFromUrl(imageView, picture.pic, site.cookie);
                    }
                }

                @Override
                public void onFailure(HViewerHttpClient.HttpError error) {
                    if (picture.retries < 15) {
                        getPictureUrl(imageView, picture, site);
                        picture.retries++;
                    } else {
                        picture.retries = 0;
                    }
                }
            });
        }
    }

}
