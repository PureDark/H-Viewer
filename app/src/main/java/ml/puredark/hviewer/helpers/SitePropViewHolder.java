package ml.puredark.hviewer.helpers;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.adapters.CategoryInputAdapter;
import ml.puredark.hviewer.beans.Category;
import ml.puredark.hviewer.beans.Rule;
import ml.puredark.hviewer.beans.Selector;
import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.dataproviders.ListDataProvider;

import static java.util.regex.Pattern.DOTALL;

/**
 * Created by PureDark on 2016/8/14.
 */


public class SitePropViewHolder {
    @BindView(R.id.input_title)
    MaterialEditText inputTitle;
    @BindView(R.id.input_indexUrl)
    MaterialEditText inputIndexUrl;
    @BindView(R.id.input_galleryUrl)
    MaterialEditText inputGalleryUrl;
    @BindView(R.id.input_searchUrl)
    MaterialEditText inputSearchUrl;
    @BindView(R.id.input_loginUrl)
    MaterialEditText inputLoginUrl;
    @BindView(R.id.input_flag)
    MaterialEditText inputFlag;
    @BindView(R.id.input_cookie)
    MaterialEditText inputCookie;

    @BindView(R.id.btn_category)
    TextView btnCategory;
    @BindView(R.id.rv_category)
    RecyclerView rvCategory;

    @BindView(R.id.input_picUrlSelector_selector)
    MaterialEditText inputPicUrlSelectorSelector;
    @BindView(R.id.input_picUrlSelector_regex)
    MaterialEditText inputPicUrlSelectorRegex;
    @BindView(R.id.input_picUrlSelector_replacement)
    MaterialEditText inputPicUrlSelectorReplacement;


    @BindView(R.id.btn_indexRule)
    TextView btnIndexRule;
    @BindView(R.id.layout_indexRule)
    LinearLayout layoutIndexRule;
    @BindView(R.id.input_indexRule_item_selector)
    MaterialEditText inputIndexRuleItemSelector;
    @BindView(R.id.input_indexRule_item_regex)
    MaterialEditText inputIndexRuleItemRegex;
    @BindView(R.id.input_indexRule_item_replacement)
    MaterialEditText inputIndexRuleItemReplacement;
    @BindView(R.id.input_indexRule_idCode_selector)
    MaterialEditText inputIndexRuleIdCodeSelector;
    @BindView(R.id.input_indexRule_idCode_regex)
    MaterialEditText inputIndexRuleIdCodeRegex;
    @BindView(R.id.input_indexRule_idCode_replacement)
    MaterialEditText inputIndexRuleIdCodeReplacement;
    @BindView(R.id.input_indexRule_title_selector)
    MaterialEditText inputIndexRuleTitleSelector;
    @BindView(R.id.input_indexRule_title_regex)
    MaterialEditText inputIndexRuleTitleRegex;
    @BindView(R.id.input_indexRule_title_replacement)
    MaterialEditText inputIndexRuleTitleReplacement;
    @BindView(R.id.input_indexRule_uploader_selector)
    MaterialEditText inputIndexRuleUploaderSelector;
    @BindView(R.id.input_indexRule_uploader_regex)
    MaterialEditText inputIndexRuleUploaderRegex;
    @BindView(R.id.input_indexRule_uploader_replacement)
    MaterialEditText inputIndexRuleUploaderReplacement;
    @BindView(R.id.input_indexRule_cover_selector)
    MaterialEditText inputIndexRuleCoverSelector;
    @BindView(R.id.input_indexRule_cover_regex)
    MaterialEditText inputIndexRuleCoverRegex;
    @BindView(R.id.input_indexRule_cover_replacement)
    MaterialEditText inputIndexRuleCoverReplacement;
    @BindView(R.id.input_indexRule_category_selector)
    MaterialEditText inputIndexRuleCategorySelector;
    @BindView(R.id.input_indexRule_category_regex)
    MaterialEditText inputIndexRuleCategoryRegex;
    @BindView(R.id.input_indexRule_category_replacement)
    MaterialEditText inputIndexRuleCategoryReplacement;
    @BindView(R.id.input_indexRule_datetime_selector)
    MaterialEditText inputIndexRuleDatetimeSelector;
    @BindView(R.id.input_indexRule_datetime_regex)
    MaterialEditText inputIndexRuleDatetimeRegex;
    @BindView(R.id.input_indexRule_datetime_replacement)
    MaterialEditText inputIndexRuleDatetimeReplacement;
    @BindView(R.id.input_indexRule_rating_selector)
    MaterialEditText inputIndexRuleRatingSelector;
    @BindView(R.id.input_indexRule_rating_regex)
    MaterialEditText inputIndexRuleRatingRegex;
    @BindView(R.id.input_indexRule_rating_replacement)
    MaterialEditText inputIndexRuleRatingReplacement;
    @BindView(R.id.input_indexRule_tags_selector)
    MaterialEditText inputIndexRuleTagsSelector;
    @BindView(R.id.input_indexRule_tags_regex)
    MaterialEditText inputIndexRuleTagsRegex;
    @BindView(R.id.input_indexRule_tags_replacement)
    MaterialEditText inputIndexRuleTagsReplacement;
    @BindView(R.id.input_indexRule_pictureUrl_selector)
    MaterialEditText inputIndexRulePictureUrlSelector;
    @BindView(R.id.input_indexRule_pictureUrl_regex)
    MaterialEditText inputIndexRulePictureUrlRegex;
    @BindView(R.id.input_indexRule_pictureUrl_replacement)
    MaterialEditText inputIndexRulePictureUrlReplacement;
    @BindView(R.id.input_indexRule_pictureThumbnail_selector)
    MaterialEditText inputIndexRulePictureThumbnailSelector;
    @BindView(R.id.input_indexRule_pictureThumbnail_regex)
    MaterialEditText inputIndexRulePictureThumbnailRegex;
    @BindView(R.id.input_indexRule_pictureThumbnail_replacement)
    MaterialEditText inputIndexRulePictureThumbnailReplacement;


    @BindView(R.id.btn_searchRule)
    TextView btnSearchRule;
    @BindView(R.id.layout_searchRule)
    LinearLayout layoutSearchRule;
    @BindView(R.id.input_searchRule_item_selector)
    MaterialEditText inputSearchRuleItemSelector;
    @BindView(R.id.input_searchRule_item_regex)
    MaterialEditText inputSearchRuleItemRegex;
    @BindView(R.id.input_searchRule_item_replacement)
    MaterialEditText inputSearchRuleItemReplacement;
    @BindView(R.id.input_searchRule_idCode_selector)
    MaterialEditText inputSearchRuleIdCodeSelector;
    @BindView(R.id.input_searchRule_idCode_regex)
    MaterialEditText inputSearchRuleIdCodeRegex;
    @BindView(R.id.input_searchRule_idCode_replacement)
    MaterialEditText inputSearchRuleIdCodeReplacement;
    @BindView(R.id.input_searchRule_title_selector)
    MaterialEditText inputSearchRuleTitleSelector;
    @BindView(R.id.input_searchRule_title_regex)
    MaterialEditText inputSearchRuleTitleRegex;
    @BindView(R.id.input_searchRule_title_replacement)
    MaterialEditText inputSearchRuleTitleReplacement;
    @BindView(R.id.input_searchRule_uploader_selector)
    MaterialEditText inputSearchRuleUploaderSelector;
    @BindView(R.id.input_searchRule_uploader_regex)
    MaterialEditText inputSearchRuleUploaderRegex;
    @BindView(R.id.input_searchRule_uploader_replacement)
    MaterialEditText inputSearchRuleUploaderReplacement;
    @BindView(R.id.input_searchRule_cover_selector)
    MaterialEditText inputSearchRuleCoverSelector;
    @BindView(R.id.input_searchRule_cover_regex)
    MaterialEditText inputSearchRuleCoverRegex;
    @BindView(R.id.input_searchRule_cover_replacement)
    MaterialEditText inputSearchRuleCoverReplacement;
    @BindView(R.id.input_searchRule_category_selector)
    MaterialEditText inputSearchRuleCategorySelector;
    @BindView(R.id.input_searchRule_category_regex)
    MaterialEditText inputSearchRuleCategoryRegex;
    @BindView(R.id.input_searchRule_category_replacement)
    MaterialEditText inputSearchRuleCategoryReplacement;
    @BindView(R.id.input_searchRule_datetime_selector)
    MaterialEditText inputSearchRuleDatetimeSelector;
    @BindView(R.id.input_searchRule_datetime_regex)
    MaterialEditText inputSearchRuleDatetimeRegex;
    @BindView(R.id.input_searchRule_datetime_replacement)
    MaterialEditText inputSearchRuleDatetimeReplacement;
    @BindView(R.id.input_searchRule_rating_selector)
    MaterialEditText inputSearchRuleRatingSelector;
    @BindView(R.id.input_searchRule_rating_regex)
    MaterialEditText inputSearchRuleRatingRegex;
    @BindView(R.id.input_searchRule_rating_replacement)
    MaterialEditText inputSearchRuleRatingReplacement;
    @BindView(R.id.input_searchRule_tags_selector)
    MaterialEditText inputSearchRuleTagsSelector;
    @BindView(R.id.input_searchRule_tags_regex)
    MaterialEditText inputSearchRuleTagsRegex;
    @BindView(R.id.input_searchRule_tags_replacement)
    MaterialEditText inputSearchRuleTagsReplacement;
    @BindView(R.id.input_searchRule_pictureUrl_selector)
    MaterialEditText inputSearchRulePictureUrlSelector;
    @BindView(R.id.input_searchRule_pictureUrl_regex)
    MaterialEditText inputSearchRulePictureUrlRegex;
    @BindView(R.id.input_searchRule_pictureUrl_replacement)
    MaterialEditText inputSearchRulePictureUrlReplacement;
    @BindView(R.id.input_searchRule_pictureThumbnail_selector)
    MaterialEditText inputSearchRulePictureThumbnailSelector;
    @BindView(R.id.input_searchRule_pictureThumbnail_regex)
    MaterialEditText inputSearchRulePictureThumbnailRegex;
    @BindView(R.id.input_searchRule_pictureThumbnail_replacement)
    MaterialEditText inputSearchRulePictureThumbnailReplacement;


    @BindView(R.id.btn_galleryRule)
    TextView btnGalleryRule;
    @BindView(R.id.layout_galleryRule)
    LinearLayout layoutGalleryRule;
    @BindView(R.id.input_galleryRule_item_selector)
    MaterialEditText inputGalleryRuleItemSelector;
    @BindView(R.id.input_galleryRule_item_regex)
    MaterialEditText inputGalleryRuleItemRegex;
    @BindView(R.id.input_galleryRule_item_replacement)
    MaterialEditText inputGalleryRuleItemReplacement;
    @BindView(R.id.input_galleryRule_idCode_selector)
    MaterialEditText inputGalleryRuleIdCodeSelector;
    @BindView(R.id.input_galleryRule_idCode_regex)
    MaterialEditText inputGalleryRuleIdCodeRegex;
    @BindView(R.id.input_galleryRule_idCode_replacement)
    MaterialEditText inputGalleryRuleIdCodeReplacement;
    @BindView(R.id.input_galleryRule_title_selector)
    MaterialEditText inputGalleryRuleTitleSelector;
    @BindView(R.id.input_galleryRule_title_regex)
    MaterialEditText inputGalleryRuleTitleRegex;
    @BindView(R.id.input_galleryRule_title_replacement)
    MaterialEditText inputGalleryRuleTitleReplacement;
    @BindView(R.id.input_galleryRule_uploader_selector)
    MaterialEditText inputGalleryRuleUploaderSelector;
    @BindView(R.id.input_galleryRule_uploader_regex)
    MaterialEditText inputGalleryRuleUploaderRegex;
    @BindView(R.id.input_galleryRule_uploader_replacement)
    MaterialEditText inputGalleryRuleUploaderReplacement;
    @BindView(R.id.input_galleryRule_cover_selector)
    MaterialEditText inputGalleryRuleCoverSelector;
    @BindView(R.id.input_galleryRule_cover_regex)
    MaterialEditText inputGalleryRuleCoverRegex;
    @BindView(R.id.input_galleryRule_cover_replacement)
    MaterialEditText inputGalleryRuleCoverReplacement;
    @BindView(R.id.input_galleryRule_category_selector)
    MaterialEditText inputGalleryRuleCategorySelector;
    @BindView(R.id.input_galleryRule_category_regex)
    MaterialEditText inputGalleryRuleCategoryRegex;
    @BindView(R.id.input_galleryRule_category_replacement)
    MaterialEditText inputGalleryRuleCategoryReplacement;
    @BindView(R.id.input_galleryRule_datetime_selector)
    MaterialEditText inputGalleryRuleDatetimeSelector;
    @BindView(R.id.input_galleryRule_datetime_regex)
    MaterialEditText inputGalleryRuleDatetimeRegex;
    @BindView(R.id.input_galleryRule_datetime_replacement)
    MaterialEditText inputGalleryRuleDatetimeReplacement;
    @BindView(R.id.input_galleryRule_rating_selector)
    MaterialEditText inputGalleryRuleRatingSelector;
    @BindView(R.id.input_galleryRule_rating_regex)
    MaterialEditText inputGalleryRuleRatingRegex;
    @BindView(R.id.input_galleryRule_rating_replacement)
    MaterialEditText inputGalleryRuleRatingReplacement;
    @BindView(R.id.input_galleryRule_tags_selector)
    MaterialEditText inputGalleryRuleTagsSelector;
    @BindView(R.id.input_galleryRule_tags_regex)
    MaterialEditText inputGalleryRuleTagsRegex;
    @BindView(R.id.input_galleryRule_tags_replacement)
    MaterialEditText inputGalleryRuleTagsReplacement;
    @BindView(R.id.input_galleryRule_pictureUrl_selector)
    MaterialEditText inputGalleryRulePictureUrlSelector;
    @BindView(R.id.input_galleryRule_pictureUrl_regex)
    MaterialEditText inputGalleryRulePictureUrlRegex;
    @BindView(R.id.input_galleryRule_pictureUrl_replacement)
    MaterialEditText inputGalleryRulePictureUrlReplacement;
    @BindView(R.id.input_galleryRule_pictureThumbnail_selector)
    MaterialEditText inputGalleryRulePictureThumbnailSelector;
    @BindView(R.id.input_galleryRule_pictureThumbnail_regex)
    MaterialEditText inputGalleryRulePictureThumbnailRegex;
    @BindView(R.id.input_galleryRule_pictureThumbnail_replacement)
    MaterialEditText inputGalleryRulePictureThumbnailReplacement;

    private CategoryInputAdapter categoryInputAdapter;
    private Site lastSite;

    public SitePropViewHolder(View view) {
        ButterKnife.bind(this, view);
        btnCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rvCategory.getVisibility() == View.GONE) {
                    rvCategory.setVisibility(View.VISIBLE);
                    btnCategory.setText("-" + btnCategory.getText().toString().substring(1));
                } else {
                    rvCategory.setVisibility(View.GONE);
                    btnCategory.setText("+" + btnCategory.getText().toString().substring(1));
                }
            }
        });
        btnIndexRule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (layoutIndexRule.getVisibility() == View.GONE) {
                    layoutIndexRule.setVisibility(View.VISIBLE);
                    btnIndexRule.setText("-" + btnIndexRule.getText().toString().substring(1));
                } else {
                    layoutIndexRule.setVisibility(View.GONE);
                    btnIndexRule.setText("+" + btnIndexRule.getText().toString().substring(1));
                }
            }
        });
        btnSearchRule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (layoutSearchRule.getVisibility() == View.GONE) {
                    layoutSearchRule.setVisibility(View.VISIBLE);
                    btnSearchRule.setText("-" + btnSearchRule.getText().toString().substring(1));
                } else {
                    layoutSearchRule.setVisibility(View.GONE);
                    btnSearchRule.setText("+" + btnSearchRule.getText().toString().substring(1));
                }
            }
        });
        btnGalleryRule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (layoutGalleryRule.getVisibility() == View.GONE) {
                    layoutGalleryRule.setVisibility(View.VISIBLE);
                    btnGalleryRule.setText("-" + btnGalleryRule.getText().toString().substring(1));
                } else {
                    layoutGalleryRule.setVisibility(View.GONE);
                    btnGalleryRule.setText("+" + btnGalleryRule.getText().toString().substring(1));
                }
            }
        });

        categoryInputAdapter = new CategoryInputAdapter(new ListDataProvider(new ArrayList()));
        rvCategory.setAdapter(categoryInputAdapter);
    }

    public String joinSelector(Selector selector) {
        String select = (selector.selector != null) ? "$(\"" + selector.selector + "\")" : "";
        String function = (selector.fun != null && !"".equals(selector.fun)) ? "." + selector.fun : "";
        String parameter = (selector.param != null && !"".equals(selector.param)) ? "(\"" + selector.param + "\")"
                : ("".equals(function)) ? "" : "()";
        String join = select + function + parameter;
        return join;
    }

    public Selector splitSelector(Selector selector) {
        Pattern pattern = Pattern.compile("\\$\\(\"(.*?)\"\\).?(\\w*)?\\(?\"?([a-zA-z0-9_-]*)\"?\\)?", DOTALL);
        Matcher matcher = pattern.matcher(selector.selector);
        if (matcher.find() && matcher.groupCount() >= 3) {
            selector.selector = matcher.group(1);
            selector.fun = matcher.group(2);
            selector.param = matcher.group(3);
            if ("".equals(selector.fun))
                selector.fun = null;
            if ("".equals(selector.param))
                selector.param = null;
        }
        return selector;
    }

    public void fillSitePropEditText(Site site) {
        lastSite = site;
        inputTitle.setText(site.title);
        inputIndexUrl.setText(site.indexUrl);
        inputGalleryUrl.setText(site.galleryUrl);
        inputSearchUrl.setText(site.searchUrl);
        inputLoginUrl.setText(site.loginUrl);
        inputCookie.setText(site.cookie);
        inputFlag.setText(site.flag);

        if (site.categories != null) {
            categoryInputAdapter.getDataProvider().addAll(site.categories);
            categoryInputAdapter.notifyDataSetChanged();
        }

        if (site.picUrlSelector != null) {
            inputPicUrlSelectorSelector.setText(joinSelector(site.picUrlSelector));
            inputPicUrlSelectorRegex.setText(site.picUrlSelector.regex);
            inputPicUrlSelectorReplacement.setText(site.picUrlSelector.replacement);
        }
        if (site.indexRule != null) {
            if (site.indexRule.item != null) {
                inputIndexRuleItemSelector.setText(joinSelector(site.indexRule.item));
                inputIndexRuleItemRegex.setText(site.indexRule.item.regex);
                inputIndexRuleItemReplacement.setText(site.indexRule.item.replacement);
            }
            if (site.indexRule.idCode != null) {
                inputIndexRuleIdCodeSelector.setText(joinSelector(site.indexRule.idCode));
                inputIndexRuleIdCodeRegex.setText(site.indexRule.idCode.regex);
                inputIndexRuleIdCodeReplacement.setText(site.indexRule.idCode.replacement);
            }
            if (site.indexRule.title != null) {
                inputIndexRuleTitleSelector.setText(joinSelector(site.indexRule.title));
                inputIndexRuleTitleRegex.setText(site.indexRule.title.regex);
                inputIndexRuleTitleReplacement.setText(site.indexRule.title.replacement);
            }
            if (site.indexRule.uploader != null) {
                inputIndexRuleUploaderSelector.setText(joinSelector(site.indexRule.uploader));
                inputIndexRuleUploaderRegex.setText(site.indexRule.uploader.regex);
                inputIndexRuleUploaderReplacement.setText(site.indexRule.uploader.replacement);
            }
            if (site.indexRule.cover != null) {
                inputIndexRuleCoverSelector.setText(joinSelector(site.indexRule.cover));
                inputIndexRuleCoverRegex.setText(site.indexRule.cover.regex);
                inputIndexRuleCoverReplacement.setText(site.indexRule.cover.replacement);
            }
            if (site.indexRule.category != null) {
                inputIndexRuleCategorySelector.setText(joinSelector(site.indexRule.category));
                inputIndexRuleCategoryRegex.setText(site.indexRule.category.regex);
                inputIndexRuleCategoryReplacement.setText(site.indexRule.category.replacement);
            }
            if (site.indexRule.datetime != null) {
                inputIndexRuleDatetimeSelector.setText(joinSelector(site.indexRule.datetime));
                inputIndexRuleDatetimeRegex.setText(site.indexRule.datetime.regex);
                inputIndexRuleDatetimeReplacement.setText(site.indexRule.datetime.replacement);
            }
            if (site.indexRule.rating != null) {
                inputIndexRuleRatingSelector.setText(joinSelector(site.indexRule.rating));
                inputIndexRuleRatingRegex.setText(site.indexRule.rating.regex);
                inputIndexRuleRatingReplacement.setText(site.indexRule.rating.replacement);
            }
            if (site.indexRule.tags != null) {
                inputIndexRuleTagsSelector.setText(joinSelector(site.indexRule.tags));
                inputIndexRuleTagsRegex.setText(site.indexRule.tags.regex);
                inputIndexRuleTagsReplacement.setText(site.indexRule.tags.replacement);
            }
            if (site.indexRule.pictureUrl != null) {
                inputIndexRulePictureUrlSelector.setText(joinSelector(site.indexRule.pictureUrl));
                inputIndexRulePictureUrlRegex.setText(site.indexRule.pictureUrl.regex);
                inputIndexRulePictureUrlReplacement.setText(site.indexRule.pictureUrl.replacement);
            }
            if (site.indexRule.pictureThumbnail != null) {
                inputIndexRulePictureThumbnailSelector.setText(joinSelector(site.indexRule.pictureThumbnail));
                inputIndexRulePictureThumbnailRegex.setText(site.indexRule.pictureThumbnail.regex);
                inputIndexRulePictureThumbnailReplacement.setText(site.indexRule.pictureThumbnail.replacement);
            }
        }

        if (site.searchRule != null) {
            if (site.searchRule.item != null) {
                inputSearchRuleItemSelector.setText(joinSelector(site.searchRule.item));
                inputSearchRuleItemRegex.setText(site.searchRule.item.regex);
                inputSearchRuleItemReplacement.setText(site.searchRule.item.replacement);
            }
            if (site.searchRule.idCode != null) {
                inputSearchRuleIdCodeSelector.setText(joinSelector(site.searchRule.idCode));
                inputSearchRuleIdCodeRegex.setText(site.searchRule.idCode.regex);
                inputSearchRuleIdCodeReplacement.setText(site.searchRule.idCode.replacement);
            }
            if (site.searchRule.title != null) {
                inputSearchRuleTitleSelector.setText(joinSelector(site.searchRule.title));
                inputSearchRuleTitleRegex.setText(site.searchRule.title.regex);
                inputSearchRuleTitleReplacement.setText(site.searchRule.title.replacement);
            }
            if (site.searchRule.uploader != null) {
                inputSearchRuleUploaderSelector.setText(joinSelector(site.searchRule.uploader));
                inputSearchRuleUploaderRegex.setText(site.searchRule.uploader.regex);
                inputSearchRuleUploaderReplacement.setText(site.searchRule.uploader.replacement);
            }
            if (site.searchRule.cover != null) {
                inputSearchRuleCoverSelector.setText(joinSelector(site.searchRule.cover));
                inputSearchRuleCoverRegex.setText(site.searchRule.cover.regex);
                inputSearchRuleCoverReplacement.setText(site.searchRule.cover.replacement);
            }
            if (site.searchRule.category != null) {
                inputSearchRuleCategorySelector.setText(joinSelector(site.searchRule.category));
                inputSearchRuleCategoryRegex.setText(site.searchRule.category.regex);
                inputSearchRuleCategoryReplacement.setText(site.searchRule.category.replacement);
            }
            if (site.searchRule.datetime != null) {
                inputSearchRuleDatetimeSelector.setText(joinSelector(site.searchRule.datetime));
                inputSearchRuleDatetimeRegex.setText(site.searchRule.datetime.regex);
                inputSearchRuleDatetimeReplacement.setText(site.searchRule.datetime.replacement);
            }
            if (site.searchRule.rating != null) {
                inputSearchRuleRatingSelector.setText(joinSelector(site.searchRule.rating));
                inputSearchRuleRatingRegex.setText(site.searchRule.rating.regex);
                inputSearchRuleRatingReplacement.setText(site.searchRule.rating.replacement);
            }
            if (site.searchRule.tags != null) {
                inputSearchRuleTagsSelector.setText(joinSelector(site.searchRule.tags));
                inputSearchRuleTagsRegex.setText(site.searchRule.tags.regex);
                inputSearchRuleTagsReplacement.setText(site.searchRule.tags.replacement);
            }
            if (site.searchRule.pictureUrl != null) {
                inputSearchRulePictureUrlSelector.setText(joinSelector(site.searchRule.pictureUrl));
                inputSearchRulePictureUrlRegex.setText(site.searchRule.pictureUrl.regex);
                inputSearchRulePictureUrlReplacement.setText(site.searchRule.pictureUrl.replacement);
            }
            if (site.searchRule.pictureThumbnail != null) {
                inputSearchRulePictureThumbnailSelector.setText(joinSelector(site.searchRule.pictureThumbnail));
                inputSearchRulePictureThumbnailRegex.setText(site.searchRule.pictureThumbnail.regex);
                inputSearchRulePictureThumbnailReplacement.setText(site.searchRule.pictureThumbnail.replacement);
            }
        }

        if (site.galleryRule != null) {
            if (site.galleryRule.item != null) {
                inputGalleryRuleItemSelector.setText(joinSelector(site.galleryRule.item));
                inputGalleryRuleItemRegex.setText(site.galleryRule.item.regex);
                inputGalleryRuleItemReplacement.setText(site.galleryRule.item.replacement);
            }
            if (site.galleryRule.idCode != null) {
                inputGalleryRuleIdCodeSelector.setText(joinSelector(site.galleryRule.idCode));
                inputGalleryRuleIdCodeRegex.setText(site.galleryRule.idCode.regex);
                inputGalleryRuleIdCodeReplacement.setText(site.galleryRule.idCode.replacement);
            }
            if (site.galleryRule.title != null) {
                inputGalleryRuleTitleSelector.setText(joinSelector(site.galleryRule.title));
                inputGalleryRuleTitleRegex.setText(site.galleryRule.title.regex);
                inputGalleryRuleTitleReplacement.setText(site.galleryRule.title.replacement);
            }
            if (site.galleryRule.uploader != null) {
                inputGalleryRuleUploaderSelector.setText(joinSelector(site.galleryRule.uploader));
                inputGalleryRuleUploaderRegex.setText(site.galleryRule.uploader.regex);
                inputGalleryRuleUploaderReplacement.setText(site.galleryRule.uploader.replacement);
            }
            if (site.galleryRule.cover != null) {
                inputGalleryRuleCoverSelector.setText(joinSelector(site.galleryRule.cover));
                inputGalleryRuleCoverRegex.setText(site.galleryRule.cover.regex);
                inputGalleryRuleCoverReplacement.setText(site.galleryRule.cover.replacement);
            }
            if (site.galleryRule.category != null) {
                inputGalleryRuleCategorySelector.setText(joinSelector(site.galleryRule.category));
                inputGalleryRuleCategoryRegex.setText(site.galleryRule.category.regex);
                inputGalleryRuleCategoryReplacement.setText(site.galleryRule.category.replacement);
            }
            if (site.galleryRule.datetime != null) {
                inputGalleryRuleDatetimeSelector.setText(joinSelector(site.galleryRule.datetime));
                inputGalleryRuleDatetimeRegex.setText(site.galleryRule.datetime.regex);
                inputGalleryRuleDatetimeReplacement.setText(site.galleryRule.datetime.replacement);
            }
            if (site.galleryRule.rating != null) {
                inputGalleryRuleRatingSelector.setText(joinSelector(site.galleryRule.rating));
                inputGalleryRuleRatingRegex.setText(site.galleryRule.rating.regex);
                inputGalleryRuleRatingReplacement.setText(site.galleryRule.rating.replacement);
            }
            if (site.galleryRule.tags != null) {
                inputGalleryRuleTagsSelector.setText(joinSelector(site.galleryRule.tags));
                inputGalleryRuleTagsRegex.setText(site.galleryRule.tags.regex);
                inputGalleryRuleTagsReplacement.setText(site.galleryRule.tags.replacement);
            }
            if (site.galleryRule.pictureUrl != null) {
                inputGalleryRulePictureUrlSelector.setText(joinSelector(site.galleryRule.pictureUrl));
                inputGalleryRulePictureUrlRegex.setText(site.galleryRule.pictureUrl.regex);
                inputGalleryRulePictureUrlReplacement.setText(site.galleryRule.pictureUrl.replacement);
            }
            if (site.galleryRule.pictureThumbnail != null) {
                inputGalleryRulePictureThumbnailSelector.setText(joinSelector(site.galleryRule.pictureThumbnail));
                inputGalleryRulePictureThumbnailRegex.setText(site.galleryRule.pictureThumbnail.regex);
                inputGalleryRulePictureThumbnailReplacement.setText(site.galleryRule.pictureThumbnail.replacement);
            }
        }
    }

    public Site fromEditTextToSite() {
        Site site = new Site();
        site.title = loadString(inputTitle);
        site.indexUrl = loadString(inputIndexUrl);
        site.galleryUrl = loadString(inputGalleryUrl);
        site.searchUrl = loadString(inputSearchUrl);
        site.loginUrl = loadString(inputLoginUrl);
        site.cookie = loadString(inputCookie);
        site.flag = loadString(inputFlag);

        //categories
        List<Category> categories = categoryInputAdapter.getDataProvider().getItems();
        for (int i = 0; i < categories.size(); i++) {
            Category category = categories.get(i);
            if("".equals(category.title) || "".equals(category.url)){
                categories.remove(i);
                i--;
            }else {
                category.cid = i + 1;
            }
        }
        if(categories.size()>0)
            site.categories = categories;

        site.picUrlSelector = loadSelector(inputPicUrlSelectorSelector, inputPicUrlSelectorRegex, inputPicUrlSelectorReplacement);

        //index rule
        site.indexRule = new Rule();
        site.indexRule.item = loadSelector(inputIndexRuleItemSelector, inputIndexRuleItemRegex, inputIndexRuleItemReplacement);
        site.indexRule.idCode = loadSelector(inputIndexRuleIdCodeSelector, inputIndexRuleIdCodeRegex, inputIndexRuleIdCodeReplacement);
        site.indexRule.title = loadSelector(inputIndexRuleTitleSelector, inputIndexRuleTitleRegex, inputIndexRuleTitleReplacement);
        site.indexRule.uploader = loadSelector(inputIndexRuleUploaderSelector, inputIndexRuleUploaderRegex, inputIndexRuleUploaderReplacement);
        site.indexRule.cover = loadSelector(inputIndexRuleCoverSelector, inputIndexRuleCoverRegex, inputIndexRuleCoverReplacement);
        site.indexRule.category = loadSelector(inputIndexRuleCategorySelector, inputIndexRuleCategoryRegex, inputIndexRuleCategoryReplacement);
        site.indexRule.datetime = loadSelector(inputIndexRuleDatetimeSelector, inputIndexRuleDatetimeRegex, inputIndexRuleDatetimeReplacement);
        site.indexRule.rating = loadSelector(inputIndexRuleRatingSelector, inputIndexRuleRatingRegex, inputIndexRuleRatingReplacement);
        site.indexRule.tags = loadSelector(inputIndexRuleTagsSelector, inputIndexRuleTagsRegex, inputIndexRuleTagsReplacement);
        site.indexRule.pictureUrl = loadSelector(inputIndexRulePictureUrlSelector, inputIndexRulePictureUrlRegex, inputIndexRulePictureUrlReplacement);
        site.indexRule.pictureThumbnail = loadSelector(inputIndexRulePictureThumbnailSelector, inputIndexRulePictureThumbnailRegex, inputIndexRulePictureThumbnailReplacement);


        //search rule
        site.searchRule = new Rule();
        site.searchRule.item = loadSelector(inputSearchRuleItemSelector, inputSearchRuleItemRegex, inputSearchRuleItemReplacement);
        site.searchRule.idCode = loadSelector(inputSearchRuleIdCodeSelector, inputSearchRuleIdCodeRegex, inputSearchRuleIdCodeReplacement);
        site.searchRule.title = loadSelector(inputSearchRuleTitleSelector, inputSearchRuleTitleRegex, inputSearchRuleTitleReplacement);
        site.searchRule.uploader = loadSelector(inputSearchRuleUploaderSelector, inputSearchRuleUploaderRegex, inputSearchRuleUploaderReplacement);
        site.searchRule.cover = loadSelector(inputSearchRuleCoverSelector, inputSearchRuleCoverRegex, inputSearchRuleCoverReplacement);
        site.searchRule.category = loadSelector(inputSearchRuleCategorySelector, inputSearchRuleCategoryRegex, inputSearchRuleCategoryReplacement);
        site.searchRule.datetime = loadSelector(inputSearchRuleDatetimeSelector, inputSearchRuleDatetimeRegex, inputSearchRuleDatetimeReplacement);
        site.searchRule.rating = loadSelector(inputSearchRuleRatingSelector, inputSearchRuleRatingRegex, inputSearchRuleRatingReplacement);
        site.searchRule.tags = loadSelector(inputSearchRuleTagsSelector, inputSearchRuleTagsRegex, inputSearchRuleTagsReplacement);
        site.searchRule.pictureUrl = loadSelector(inputSearchRulePictureUrlSelector, inputSearchRulePictureUrlRegex, inputSearchRulePictureUrlReplacement);
        site.searchRule.pictureThumbnail = loadSelector(inputSearchRulePictureThumbnailSelector, inputSearchRulePictureThumbnailRegex, inputSearchRulePictureThumbnailReplacement);

        if (site.searchRule.item == null && site.searchRule.idCode == null
                && site.searchRule.title == null && site.searchRule.uploader == null
                && site.searchRule.cover == null && site.searchRule.category == null
                && site.searchRule.datetime == null && site.searchRule.rating == null
                && site.searchRule.tags == null && site.searchRule.pictureUrl == null
                && site.searchRule.pictureThumbnail == null)
            site.searchRule = null;

        //gallery rule
        site.galleryRule = new Rule();
        site.galleryRule.item = loadSelector(inputGalleryRuleItemSelector, inputGalleryRuleItemRegex, inputGalleryRuleItemReplacement);
        site.galleryRule.idCode = loadSelector(inputGalleryRuleIdCodeSelector, inputGalleryRuleIdCodeRegex, inputGalleryRuleIdCodeReplacement);
        site.galleryRule.title = loadSelector(inputGalleryRuleTitleSelector, inputGalleryRuleTitleRegex, inputGalleryRuleTitleReplacement);
        site.galleryRule.uploader = loadSelector(inputGalleryRuleUploaderSelector, inputGalleryRuleUploaderRegex, inputGalleryRuleUploaderReplacement);
        site.galleryRule.cover = loadSelector(inputGalleryRuleCoverSelector, inputGalleryRuleCoverRegex, inputGalleryRuleCoverReplacement);
        site.galleryRule.category = loadSelector(inputGalleryRuleCategorySelector, inputGalleryRuleCategoryRegex, inputGalleryRuleCategoryReplacement);
        site.galleryRule.datetime = loadSelector(inputGalleryRuleDatetimeSelector, inputGalleryRuleDatetimeRegex, inputGalleryRuleDatetimeReplacement);
        site.galleryRule.rating = loadSelector(inputGalleryRuleRatingSelector, inputGalleryRuleRatingRegex, inputGalleryRuleRatingReplacement);
        site.galleryRule.tags = loadSelector(inputGalleryRuleTagsSelector, inputGalleryRuleTagsRegex, inputGalleryRuleTagsReplacement);
        site.galleryRule.pictureUrl = loadSelector(inputGalleryRulePictureUrlSelector, inputGalleryRulePictureUrlRegex, inputGalleryRulePictureUrlReplacement);
        site.galleryRule.pictureThumbnail = loadSelector(inputGalleryRulePictureThumbnailSelector, inputGalleryRulePictureThumbnailRegex, inputGalleryRulePictureThumbnailReplacement);

        if(lastSite!=null)
            site.extraRule = lastSite.extraRule;
        Log.d("SitePropViewHolder", "site.extraRule:" + site.extraRule);
        if (site.indexUrl == null || site.galleryUrl == null ||
                site.indexRule.item == null || site.indexRule.idCode == null ||
                site.galleryRule.item == null || site.galleryRule.pictureUrl == null)
            return null;
        else
            return site;
    }

    private String loadString(EditText editText) {
        String text = editText.getText().toString();
        return ("".equals(text.trim())) ? null : text;
    }

    private Selector loadSelector(EditText inputSelector, EditText inputRegex, EditText inputReplace) {
        Selector selector = new Selector();
        String sel = inputSelector.getText().toString();
        if (!"".equals(sel.trim())) {
            selector.selector = sel;
            selector = splitSelector(selector);
        }
        String regex = inputRegex.getText().toString();
        if (!"".equals(regex.trim())) {
            selector.regex = regex;
        }
        String replace = inputReplace.getText().toString();
        if (!"".equals(replace.trim())) {
            selector.replacement = replace;
        }
        return (selector.selector == null) ? null : selector;
    }

}
