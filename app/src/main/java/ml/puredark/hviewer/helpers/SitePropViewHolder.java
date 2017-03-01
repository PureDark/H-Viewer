package ml.puredark.hviewer.helpers;

import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
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
import ml.puredark.hviewer.beans.Category;
import ml.puredark.hviewer.beans.CommentRule;
import ml.puredark.hviewer.beans.PictureRule;
import ml.puredark.hviewer.beans.Rule;
import ml.puredark.hviewer.beans.Selector;
import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.beans.SiteGroup;
import ml.puredark.hviewer.ui.adapters.CategoryInputAdapter;
import ml.puredark.hviewer.ui.dataproviders.ListDataProvider;
import smtchahal.materialspinner.MaterialSpinner;

import static java.util.regex.Pattern.DOTALL;

/**
 * Created by PureDark on 2016/8/14.
 */


public class SitePropViewHolder {
    @BindView(R.id.input_sitegroup)
    MaterialSpinner inputGroup;
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
    @BindView(R.id.input_header)
    MaterialEditText inputHeader;

    @BindView(R.id.btn_disable_hproxy)
    LinearLayout btnDisableHProxy;
    @BindView(R.id.btn_waterfall_as_list)
    LinearLayout btnWaterfallAsList;
    @BindView(R.id.btn_waterfall_as_grid)
    LinearLayout btnWaterfallAsGrid;
    @BindView(R.id.checkbox_disable_hproxy)
    AppCompatCheckBox checkBoxDisableHProxy;
    @BindView(R.id.checkbox_waterfall_as_list)
    AppCompatCheckBox checkBoxWaterfallAsList;
    @BindView(R.id.checkbox_waterfall_as_grid)
    AppCompatCheckBox checkBoxWaterfallAsGrid;

    @BindView(R.id.btn_category)
    TextView btnCategory;
    @BindView(R.id.rv_category)
    RecyclerView rvCategory;

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
    @BindView(R.id.input_galleryRule_description_selector)
    MaterialEditText inputGalleryRuleDescriptionSelector;
    @BindView(R.id.input_galleryRule_description_regex)
    MaterialEditText inputGalleryRuleDescriptionRegex;
    @BindView(R.id.input_galleryRule_description_replacement)
    MaterialEditText inputGalleryRuleDescriptionReplacement;
    @BindView(R.id.input_galleryRule_tags_selector)
    MaterialEditText inputGalleryRuleTagsSelector;
    @BindView(R.id.input_galleryRule_tags_regex)
    MaterialEditText inputGalleryRuleTagsRegex;
    @BindView(R.id.input_galleryRule_tags_replacement)
    MaterialEditText inputGalleryRuleTagsReplacement;
    @BindView(R.id.input_galleryRule_pictureItem_selector)
    MaterialEditText inputGalleryRulePictureItemSelector;
    @BindView(R.id.input_galleryRule_pictureItem_regex)
    MaterialEditText inputGalleryRulePictureItemRegex;
    @BindView(R.id.input_galleryRule_pictureItem_replacement)
    MaterialEditText inputGalleryRulePictureItemReplacement;
    @BindView(R.id.input_galleryRule_pictureThumbnail_selector)
    MaterialEditText inputGalleryRulePictureThumbnailSelector;
    @BindView(R.id.input_galleryRule_pictureThumbnail_regex)
    MaterialEditText inputGalleryRulePictureThumbnailRegex;
    @BindView(R.id.input_galleryRule_pictureThumbnail_replacement)
    MaterialEditText inputGalleryRulePictureThumbnailReplacement;
    @BindView(R.id.input_galleryRule_pictureUrl_selector)
    MaterialEditText inputGalleryRulePictureUrlSelector;
    @BindView(R.id.input_galleryRule_pictureUrl_regex)
    MaterialEditText inputGalleryRulePictureUrlRegex;
    @BindView(R.id.input_galleryRule_pictureUrl_replacement)
    MaterialEditText inputGalleryRulePictureUrlReplacement;
    @BindView(R.id.input_galleryRule_pictureHighRes_selector)
    MaterialEditText inputGalleryRulePictureHighResSelector;
    @BindView(R.id.input_galleryRule_pictureHighRes_regex)
    MaterialEditText inputGalleryRulePictureHighResRegex;
    @BindView(R.id.input_galleryRule_pictureHighRes_replacement)
    MaterialEditText inputGalleryRulePictureHighResReplacement;
    @BindView(R.id.input_galleryRule_commentItem_selector)
    MaterialEditText inputGalleryRuleCommentItemSelector;
    @BindView(R.id.input_galleryRule_commentItem_regex)
    MaterialEditText inputGalleryRuleCommentItemRegex;
    @BindView(R.id.input_galleryRule_commentItem_replacement)
    MaterialEditText inputGalleryRuleCommentItemReplacement;
    @BindView(R.id.input_galleryRule_commentAvatar_selector)
    MaterialEditText inputGalleryRuleCommentAvatarSelector;
    @BindView(R.id.input_galleryRule_commentAvatar_regex)
    MaterialEditText inputGalleryRuleCommentAvatarRegex;
    @BindView(R.id.input_galleryRule_commentAvatar_replacement)
    MaterialEditText inputGalleryRuleCommentAvatarReplacement;
    @BindView(R.id.input_galleryRule_commentAuthor_selector)
    MaterialEditText inputGalleryRuleCommentAuthorSelector;
    @BindView(R.id.input_galleryRule_commentAuthor_regex)
    MaterialEditText inputGalleryRuleCommentAuthorRegex;
    @BindView(R.id.input_galleryRule_commentAuthor_replacement)
    MaterialEditText inputGalleryRuleCommentAuthorReplacement;
    @BindView(R.id.input_galleryRule_commentDatetime_selector)
    MaterialEditText inputGalleryRuleCommentDatetimeSelector;
    @BindView(R.id.input_galleryRule_commentDatetime_regex)
    MaterialEditText inputGalleryRuleCommentDatetimeRegex;
    @BindView(R.id.input_galleryRule_commentDatetime_replacement)
    MaterialEditText inputGalleryRuleCommentDatetimeReplacement;
    @BindView(R.id.input_galleryRule_commentContent_selector)
    MaterialEditText inputGalleryRuleCommentContentSelector;
    @BindView(R.id.input_galleryRule_commentContent_regex)
    MaterialEditText inputGalleryRuleCommentContentRegex;
    @BindView(R.id.input_galleryRule_commentContent_replacement)
    MaterialEditText inputGalleryRuleCommentContentReplacement;


    @BindView(R.id.btn_extraRule)
    TextView btnExtraRule;
    @BindView(R.id.layout_extraRule)
    LinearLayout layoutExtraRule;
    @BindView(R.id.input_extraRule_item_selector)
    MaterialEditText inputExtraRuleItemSelector;
    @BindView(R.id.input_extraRule_item_regex)
    MaterialEditText inputExtraRuleItemRegex;
    @BindView(R.id.input_extraRule_item_replacement)
    MaterialEditText inputExtraRuleItemReplacement;
    @BindView(R.id.input_extraRule_idCode_selector)
    MaterialEditText inputExtraRuleIdCodeSelector;
    @BindView(R.id.input_extraRule_idCode_regex)
    MaterialEditText inputExtraRuleIdCodeRegex;
    @BindView(R.id.input_extraRule_idCode_replacement)
    MaterialEditText inputExtraRuleIdCodeReplacement;
    @BindView(R.id.input_extraRule_title_selector)
    MaterialEditText inputExtraRuleTitleSelector;
    @BindView(R.id.input_extraRule_title_regex)
    MaterialEditText inputExtraRuleTitleRegex;
    @BindView(R.id.input_extraRule_title_replacement)
    MaterialEditText inputExtraRuleTitleReplacement;
    @BindView(R.id.input_extraRule_uploader_selector)
    MaterialEditText inputExtraRuleUploaderSelector;
    @BindView(R.id.input_extraRule_uploader_regex)
    MaterialEditText inputExtraRuleUploaderRegex;
    @BindView(R.id.input_extraRule_uploader_replacement)
    MaterialEditText inputExtraRuleUploaderReplacement;
    @BindView(R.id.input_extraRule_cover_selector)
    MaterialEditText inputExtraRuleCoverSelector;
    @BindView(R.id.input_extraRule_cover_regex)
    MaterialEditText inputExtraRuleCoverRegex;
    @BindView(R.id.input_extraRule_cover_replacement)
    MaterialEditText inputExtraRuleCoverReplacement;
    @BindView(R.id.input_extraRule_category_selector)
    MaterialEditText inputExtraRuleCategorySelector;
    @BindView(R.id.input_extraRule_category_regex)
    MaterialEditText inputExtraRuleCategoryRegex;
    @BindView(R.id.input_extraRule_category_replacement)
    MaterialEditText inputExtraRuleCategoryReplacement;
    @BindView(R.id.input_extraRule_datetime_selector)
    MaterialEditText inputExtraRuleDatetimeSelector;
    @BindView(R.id.input_extraRule_datetime_regex)
    MaterialEditText inputExtraRuleDatetimeRegex;
    @BindView(R.id.input_extraRule_datetime_replacement)
    MaterialEditText inputExtraRuleDatetimeReplacement;
    @BindView(R.id.input_extraRule_rating_selector)
    MaterialEditText inputExtraRuleRatingSelector;
    @BindView(R.id.input_extraRule_rating_regex)
    MaterialEditText inputExtraRuleRatingRegex;
    @BindView(R.id.input_extraRule_rating_replacement)
    MaterialEditText inputExtraRuleRatingReplacement;
    @BindView(R.id.input_extraRule_description_selector)
    MaterialEditText inputExtraRuleDescriptionSelector;
    @BindView(R.id.input_extraRule_description_regex)
    MaterialEditText inputExtraRuleDescriptionRegex;
    @BindView(R.id.input_extraRule_description_replacement)
    MaterialEditText inputExtraRuleDescriptionReplacement;
    @BindView(R.id.input_extraRule_tags_selector)
    MaterialEditText inputExtraRuleTagsSelector;
    @BindView(R.id.input_extraRule_tags_regex)
    MaterialEditText inputExtraRuleTagsRegex;
    @BindView(R.id.input_extraRule_tags_replacement)
    MaterialEditText inputExtraRuleTagsReplacement;
    @BindView(R.id.input_extraRule_pictureItem_selector)
    MaterialEditText inputExtraRulePictureItemSelector;
    @BindView(R.id.input_extraRule_pictureItem_regex)
    MaterialEditText inputExtraRulePictureItemRegex;
    @BindView(R.id.input_extraRule_pictureItem_replacement)
    MaterialEditText inputExtraRulePictureItemReplacement;
    @BindView(R.id.input_extraRule_pictureThumbnail_selector)
    MaterialEditText inputExtraRulePictureThumbnailSelector;
    @BindView(R.id.input_extraRule_pictureThumbnail_regex)
    MaterialEditText inputExtraRulePictureThumbnailRegex;
    @BindView(R.id.input_extraRule_pictureThumbnail_replacement)
    MaterialEditText inputExtraRulePictureThumbnailReplacement;
    @BindView(R.id.input_extraRule_pictureUrl_selector)
    MaterialEditText inputExtraRulePictureUrlSelector;
    @BindView(R.id.input_extraRule_pictureUrl_regex)
    MaterialEditText inputExtraRulePictureUrlRegex;
    @BindView(R.id.input_extraRule_pictureUrl_replacement)
    MaterialEditText inputExtraRulePictureUrlReplacement;
    @BindView(R.id.input_extraRule_pictureHighRes_selector)
    MaterialEditText inputExtraRulePictureHighResSelector;
    @BindView(R.id.input_extraRule_pictureHighRes_regex)
    MaterialEditText inputExtraRulePictureHighResRegex;
    @BindView(R.id.input_extraRule_pictureHighRes_replacement)
    MaterialEditText inputExtraRulePictureHighResReplacement;
    @BindView(R.id.input_extraRule_commentItem_selector)
    MaterialEditText inputExtraRuleCommentItemSelector;
    @BindView(R.id.input_extraRule_commentItem_regex)
    MaterialEditText inputExtraRuleCommentItemRegex;
    @BindView(R.id.input_extraRule_commentItem_replacement)
    MaterialEditText inputExtraRuleCommentItemReplacement;
    @BindView(R.id.input_extraRule_commentAvatar_selector)
    MaterialEditText inputExtraRuleCommentAvatarSelector;
    @BindView(R.id.input_extraRule_commentAvatar_regex)
    MaterialEditText inputExtraRuleCommentAvatarRegex;
    @BindView(R.id.input_extraRule_commentAvatar_replacement)
    MaterialEditText inputExtraRuleCommentAvatarReplacement;
    @BindView(R.id.input_extraRule_commentAuthor_selector)
    MaterialEditText inputExtraRuleCommentAuthorSelector;
    @BindView(R.id.input_extraRule_commentAuthor_regex)
    MaterialEditText inputExtraRuleCommentAuthorRegex;
    @BindView(R.id.input_extraRule_commentAuthor_replacement)
    MaterialEditText inputExtraRuleCommentAuthorReplacement;
    @BindView(R.id.input_extraRule_commentDatetime_selector)
    MaterialEditText inputExtraRuleCommentDatetimeSelector;
    @BindView(R.id.input_extraRule_commentDatetime_regex)
    MaterialEditText inputExtraRuleCommentDatetimeRegex;
    @BindView(R.id.input_extraRule_commentDatetime_replacement)
    MaterialEditText inputExtraRuleCommentDatetimeReplacement;
    @BindView(R.id.input_extraRule_commentContent_selector)
    MaterialEditText inputExtraRuleCommentContentSelector;
    @BindView(R.id.input_extraRule_commentContent_regex)
    MaterialEditText inputExtraRuleCommentContentRegex;
    @BindView(R.id.input_extraRule_commentContent_replacement)
    MaterialEditText inputExtraRuleCommentContentReplacement;

    private CategoryInputAdapter categoryInputAdapter;
    private Site lastSite;
    private List<SiteGroup> siteGroups;

    public SitePropViewHolder(View view, List<SiteGroup> siteGroups) {
        ButterKnife.bind(this, view);
        if (lastSite == null)
            lastSite = new Site();
        this.siteGroups = siteGroups;
        String[] groupTitles = new String[siteGroups.size()];
        for (int i = 0; i < siteGroups.size(); i++) {
            groupTitles[i] = siteGroups.get(i).title;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_item, groupTitles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        inputGroup.setAdapter(adapter);
        btnDisableHProxy.setOnClickListener(v -> {
            if (checkBoxDisableHProxy.isChecked()) {
                checkBoxDisableHProxy.setChecked(false);
            } else {
                checkBoxDisableHProxy.setChecked(true);
            }
        });
        btnWaterfallAsList.setOnClickListener(v -> {
            if (checkBoxWaterfallAsList.isChecked()) {
                checkBoxWaterfallAsList.setChecked(false);
                String newFlags = removeFlag(inputFlag.getText().toString(), Site.FLAG_WATERFALL_AS_LIST);
                inputFlag.setText(newFlags);
            } else {
                checkBoxWaterfallAsList.setChecked(true);
                String newFlags = addFlag(inputFlag.getText().toString(), Site.FLAG_WATERFALL_AS_LIST);
                inputFlag.setText(newFlags);
            }
        });
        btnWaterfallAsGrid.setOnClickListener(v -> {
            if (checkBoxWaterfallAsGrid.isChecked()) {
                checkBoxWaterfallAsGrid.setChecked(false);
                String newFlags = removeFlag(inputFlag.getText().toString(), Site.FLAG_WATERFALL_AS_GRID);
                inputFlag.setText(newFlags);
            } else {
                checkBoxWaterfallAsGrid.setChecked(true);
                String newFlags = addFlag(inputFlag.getText().toString(), Site.FLAG_WATERFALL_AS_GRID);
                inputFlag.setText(newFlags);
            }
        });
        checkBoxWaterfallAsList.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                String newFlags = addFlag(inputFlag.getText().toString(), Site.FLAG_WATERFALL_AS_LIST);
                inputFlag.setText(newFlags);
            } else {
                String newFlags = removeFlag(inputFlag.getText().toString(), Site.FLAG_WATERFALL_AS_LIST);
                inputFlag.setText(newFlags);
            }
        });
        checkBoxWaterfallAsGrid.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                String newFlags = addFlag(inputFlag.getText().toString(), Site.FLAG_WATERFALL_AS_GRID);
                inputFlag.setText(newFlags);
            } else {
                String newFlags = removeFlag(inputFlag.getText().toString(), Site.FLAG_WATERFALL_AS_GRID);
                inputFlag.setText(newFlags);
            }
        });
        btnCategory.setOnClickListener(v -> {
            if (rvCategory.getVisibility() == View.GONE) {
                rvCategory.setVisibility(View.VISIBLE);
                btnCategory.setText("-" + btnCategory.getText().toString().substring(1));
            } else {
                rvCategory.setVisibility(View.GONE);
                btnCategory.setText("+" + btnCategory.getText().toString().substring(1));
            }
        });
        btnIndexRule.setOnClickListener(v -> {
            if (layoutIndexRule.getVisibility() == View.GONE) {
                layoutIndexRule.setVisibility(View.VISIBLE);
                btnIndexRule.setText("-" + btnIndexRule.getText().toString().substring(1));
            } else {
                layoutIndexRule.setVisibility(View.GONE);
                btnIndexRule.setText("+" + btnIndexRule.getText().toString().substring(1));
            }
        });
        btnSearchRule.setOnClickListener(v -> {
            if (layoutSearchRule.getVisibility() == View.GONE) {
                layoutSearchRule.setVisibility(View.VISIBLE);
                btnSearchRule.setText("-" + btnSearchRule.getText().toString().substring(1));
            } else {
                layoutSearchRule.setVisibility(View.GONE);
                btnSearchRule.setText("+" + btnSearchRule.getText().toString().substring(1));
            }
        });
        btnGalleryRule.setOnClickListener(v -> {
            if (layoutGalleryRule.getVisibility() == View.GONE) {
                layoutGalleryRule.setVisibility(View.VISIBLE);
                btnGalleryRule.setText("-" + btnGalleryRule.getText().toString().substring(1));
            } else {
                layoutGalleryRule.setVisibility(View.GONE);
                btnGalleryRule.setText("+" + btnGalleryRule.getText().toString().substring(1));
            }
        });
        btnExtraRule.setOnClickListener(v -> {
            if (layoutExtraRule.getVisibility() == View.GONE) {
                layoutExtraRule.setVisibility(View.VISIBLE);
                btnExtraRule.setText("-" + btnExtraRule.getText().toString().substring(1));
            } else {
                layoutExtraRule.setVisibility(View.GONE);
                btnExtraRule.setText("+" + btnExtraRule.getText().toString().substring(1));
            }
        });

        categoryInputAdapter = new CategoryInputAdapter(new ListDataProvider(new ArrayList()));
        rvCategory.setAdapter(categoryInputAdapter);
    }

    public String addFlag(String flagStr, String flagToBeAdded) {
        if (!flagStr.contains(flagToBeAdded)) {
            if (!flagStr.endsWith("|"))
                flagStr += "|";
            flagStr += flagToBeAdded;
        }
        return flagStr;
    }

    public String removeFlag(String flagStr, String flagToBeRemoved) {
        if (!flagStr.contains(flagToBeRemoved))
            return flagStr;
        String newFlags = "";
        String[] flags = flagStr.split("\\|");
        for (String flag : flags) {
            flag = flag.trim();
            if (!TextUtils.isEmpty(flag) && !flagToBeRemoved.equals(flag))
                newFlags += flag + "|";
        }
        if (newFlags.endsWith("|"))
            newFlags = newFlags.substring(0, newFlags.length() - 1);
        return newFlags;
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
        for (int i = 0; i < siteGroups.size(); i++) {
            if (siteGroups.get(i).gid == site.gid) {
                inputGroup.setSelection(i + 1);
                break;
            }
        }
        inputTitle.setText(site.title);
        inputIndexUrl.setText(site.indexUrl);
        inputGalleryUrl.setText(site.galleryUrl);
        inputSearchUrl.setText(site.searchUrl);
        inputLoginUrl.setText(site.loginUrl);
        inputCookie.setText(site.cookie);
        inputHeader.setText(site.header);
        inputFlag.setText(site.flag);
        checkBoxDisableHProxy.post(() -> checkBoxDisableHProxy.setChecked(site.disableHProxy));
        checkBoxWaterfallAsList.post(() -> checkBoxWaterfallAsList.setChecked(site.hasFlag(Site.FLAG_WATERFALL_AS_LIST)));
        checkBoxWaterfallAsGrid.post(() -> checkBoxWaterfallAsGrid.setChecked(site.hasFlag(Site.FLAG_WATERFALL_AS_GRID)));

        if (site.categories != null) {
            categoryInputAdapter.getDataProvider().addAll(site.categories);
            categoryInputAdapter.notifyDataSetChanged();
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
        }

        if (site.galleryRule != null) {
            if (site.galleryRule.item != null) {
                inputGalleryRuleItemSelector.setText(joinSelector(site.galleryRule.item));
                inputGalleryRuleItemRegex.setText(site.galleryRule.item.regex);
                inputGalleryRuleItemReplacement.setText(site.galleryRule.item.replacement);
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
            if (site.galleryRule.description != null) {
                inputGalleryRuleDescriptionSelector.setText(joinSelector(site.galleryRule.description));
                inputGalleryRuleDescriptionRegex.setText(site.galleryRule.description.regex);
                inputGalleryRuleDescriptionReplacement.setText(site.galleryRule.description.replacement);
            }
            if (site.galleryRule.tags != null) {
                inputGalleryRuleTagsSelector.setText(joinSelector(site.galleryRule.tags));
                inputGalleryRuleTagsRegex.setText(site.galleryRule.tags.regex);
                inputGalleryRuleTagsReplacement.setText(site.galleryRule.tags.replacement);
            }
            if (site.galleryRule.pictureRule != null) {
                if (site.galleryRule.pictureRule.item != null) {
                    inputGalleryRulePictureItemSelector.setText(joinSelector(site.galleryRule.pictureRule.item));
                    inputGalleryRulePictureItemRegex.setText(site.galleryRule.pictureRule.item.regex);
                    inputGalleryRulePictureItemReplacement.setText(site.galleryRule.pictureRule.item.replacement);
                }
                if (site.galleryRule.pictureRule.thumbnail != null) {
                    inputGalleryRulePictureThumbnailSelector.setText(joinSelector(site.galleryRule.pictureRule.thumbnail));
                    inputGalleryRulePictureThumbnailRegex.setText(site.galleryRule.pictureRule.thumbnail.regex);
                    inputGalleryRulePictureThumbnailReplacement.setText(site.galleryRule.pictureRule.thumbnail.replacement);
                }
                if (site.galleryRule.pictureRule.url != null) {
                    inputGalleryRulePictureUrlSelector.setText(joinSelector(site.galleryRule.pictureRule.url));
                    inputGalleryRulePictureUrlRegex.setText(site.galleryRule.pictureRule.url.regex);
                    inputGalleryRulePictureUrlReplacement.setText(site.galleryRule.pictureRule.url.replacement);
                }
                if (site.galleryRule.pictureRule.highRes != null) {
                    inputGalleryRulePictureHighResSelector.setText(joinSelector(site.galleryRule.pictureRule.highRes));
                    inputGalleryRulePictureHighResRegex.setText(site.galleryRule.pictureRule.highRes.regex);
                    inputGalleryRulePictureHighResReplacement.setText(site.galleryRule.pictureRule.highRes.replacement);
                }
            } else {
                if (site.galleryRule.pictureThumbnail != null) {
                    inputGalleryRulePictureThumbnailSelector.setText(joinSelector(site.galleryRule.pictureThumbnail));
                    inputGalleryRulePictureThumbnailRegex.setText(site.galleryRule.pictureThumbnail.regex);
                    inputGalleryRulePictureThumbnailReplacement.setText(site.galleryRule.pictureThumbnail.replacement);
                }
                if (site.galleryRule.pictureUrl != null) {
                    inputGalleryRulePictureUrlSelector.setText(joinSelector(site.galleryRule.pictureUrl));
                    inputGalleryRulePictureUrlRegex.setText(site.galleryRule.pictureUrl.regex);
                    inputGalleryRulePictureUrlReplacement.setText(site.galleryRule.pictureUrl.replacement);
                }
                if (site.galleryRule.pictureHighRes != null) {
                    inputGalleryRulePictureHighResSelector.setText(joinSelector(site.galleryRule.pictureHighRes));
                    inputGalleryRulePictureHighResRegex.setText(site.galleryRule.pictureHighRes.regex);
                    inputGalleryRulePictureHighResReplacement.setText(site.galleryRule.pictureHighRes.replacement);
                }
            }
            if (site.galleryRule.commentRule != null) {
                if (site.galleryRule.commentRule.item != null) {
                    inputGalleryRuleCommentItemSelector.setText(joinSelector(site.galleryRule.commentRule.item));
                    inputGalleryRuleCommentItemRegex.setText(site.galleryRule.commentRule.item.regex);
                    inputGalleryRuleCommentItemReplacement.setText(site.galleryRule.commentRule.item.replacement);
                }
                if (site.galleryRule.commentRule.avatar != null) {
                    inputGalleryRuleCommentAvatarSelector.setText(joinSelector(site.galleryRule.commentRule.avatar));
                    inputGalleryRuleCommentAvatarRegex.setText(site.galleryRule.commentRule.avatar.regex);
                    inputGalleryRuleCommentAvatarReplacement.setText(site.galleryRule.commentRule.avatar.replacement);
                }
                if (site.galleryRule.commentRule.author != null) {
                    inputGalleryRuleCommentAuthorSelector.setText(joinSelector(site.galleryRule.commentRule.author));
                    inputGalleryRuleCommentAuthorRegex.setText(site.galleryRule.commentRule.author.regex);
                    inputGalleryRuleCommentAuthorReplacement.setText(site.galleryRule.commentRule.author.replacement);
                }
                if (site.galleryRule.commentRule.datetime != null) {
                    inputGalleryRuleCommentDatetimeSelector.setText(joinSelector(site.galleryRule.commentRule.datetime));
                    inputGalleryRuleCommentDatetimeRegex.setText(site.galleryRule.commentRule.datetime.regex);
                    inputGalleryRuleCommentDatetimeReplacement.setText(site.galleryRule.commentRule.datetime.replacement);
                }
                if (site.galleryRule.commentRule.content != null) {
                    inputGalleryRuleCommentContentSelector.setText(joinSelector(site.galleryRule.commentRule.content));
                    inputGalleryRuleCommentContentRegex.setText(site.galleryRule.commentRule.content.regex);
                    inputGalleryRuleCommentContentReplacement.setText(site.galleryRule.commentRule.content.replacement);
                }
            } else {
                if (site.galleryRule.commentItem != null) {
                    inputGalleryRuleCommentItemSelector.setText(joinSelector(site.galleryRule.commentItem));
                    inputGalleryRuleCommentItemRegex.setText(site.galleryRule.commentItem.regex);
                    inputGalleryRuleCommentItemReplacement.setText(site.galleryRule.commentItem.replacement);
                }
                if (site.galleryRule.commentAvatar != null) {
                    inputGalleryRuleCommentAvatarSelector.setText(joinSelector(site.galleryRule.commentAvatar));
                    inputGalleryRuleCommentAvatarRegex.setText(site.galleryRule.commentAvatar.regex);
                    inputGalleryRuleCommentAvatarReplacement.setText(site.galleryRule.commentAvatar.replacement);
                }
                if (site.galleryRule.commentAuthor != null) {
                    inputGalleryRuleCommentAuthorSelector.setText(joinSelector(site.galleryRule.commentAuthor));
                    inputGalleryRuleCommentAuthorRegex.setText(site.galleryRule.commentAuthor.regex);
                    inputGalleryRuleCommentAuthorReplacement.setText(site.galleryRule.commentAuthor.replacement);
                }
                if (site.galleryRule.commentDatetime != null) {
                    inputGalleryRuleCommentDatetimeSelector.setText(joinSelector(site.galleryRule.commentDatetime));
                    inputGalleryRuleCommentDatetimeRegex.setText(site.galleryRule.commentDatetime.regex);
                    inputGalleryRuleCommentDatetimeReplacement.setText(site.galleryRule.commentDatetime.replacement);
                }
                if (site.galleryRule.commentContent != null) {
                    inputGalleryRuleCommentContentSelector.setText(joinSelector(site.galleryRule.commentContent));
                    inputGalleryRuleCommentContentRegex.setText(site.galleryRule.commentContent.regex);
                    inputGalleryRuleCommentContentReplacement.setText(site.galleryRule.commentContent.replacement);
                }
            }

            if (site.extraRule != null) {
                if (site.extraRule.item != null) {
                    inputExtraRuleItemSelector.setText(joinSelector(site.extraRule.item));
                    inputExtraRuleItemRegex.setText(site.extraRule.item.regex);
                    inputExtraRuleItemReplacement.setText(site.extraRule.item.replacement);
                }
                if (site.extraRule.idCode != null) {
                    inputExtraRuleIdCodeSelector.setText(joinSelector(site.extraRule.idCode));
                    inputExtraRuleIdCodeRegex.setText(site.extraRule.idCode.regex);
                    inputExtraRuleIdCodeReplacement.setText(site.extraRule.idCode.replacement);
                }
                if (site.extraRule.title != null) {
                    inputExtraRuleTitleSelector.setText(joinSelector(site.extraRule.title));
                    inputExtraRuleTitleRegex.setText(site.extraRule.title.regex);
                    inputExtraRuleTitleReplacement.setText(site.extraRule.title.replacement);
                }
                if (site.extraRule.uploader != null) {
                    inputExtraRuleUploaderSelector.setText(joinSelector(site.extraRule.uploader));
                    inputExtraRuleUploaderRegex.setText(site.extraRule.uploader.regex);
                    inputExtraRuleUploaderReplacement.setText(site.extraRule.uploader.replacement);
                }
                if (site.extraRule.cover != null) {
                    inputExtraRuleCoverSelector.setText(joinSelector(site.extraRule.cover));
                    inputExtraRuleCoverRegex.setText(site.extraRule.cover.regex);
                    inputExtraRuleCoverReplacement.setText(site.extraRule.cover.replacement);
                }
                if (site.extraRule.category != null) {
                    inputExtraRuleCategorySelector.setText(joinSelector(site.extraRule.category));
                    inputExtraRuleCategoryRegex.setText(site.extraRule.category.regex);
                    inputExtraRuleCategoryReplacement.setText(site.extraRule.category.replacement);
                }
                if (site.extraRule.datetime != null) {
                    inputExtraRuleDatetimeSelector.setText(joinSelector(site.extraRule.datetime));
                    inputExtraRuleDatetimeRegex.setText(site.extraRule.datetime.regex);
                    inputExtraRuleDatetimeReplacement.setText(site.extraRule.datetime.replacement);
                }
                if (site.extraRule.rating != null) {
                    inputExtraRuleRatingSelector.setText(joinSelector(site.extraRule.rating));
                    inputExtraRuleRatingRegex.setText(site.extraRule.rating.regex);
                    inputExtraRuleRatingReplacement.setText(site.extraRule.rating.replacement);
                }
                if (site.extraRule.description != null) {
                    inputExtraRuleDescriptionSelector.setText(joinSelector(site.extraRule.description));
                    inputExtraRuleDescriptionRegex.setText(site.extraRule.description.regex);
                    inputExtraRuleDescriptionReplacement.setText(site.extraRule.description.replacement);
                }
                if (site.extraRule.tags != null) {
                    inputExtraRuleTagsSelector.setText(joinSelector(site.extraRule.tags));
                    inputExtraRuleTagsRegex.setText(site.extraRule.tags.regex);
                    inputExtraRuleTagsReplacement.setText(site.extraRule.tags.replacement);
                }

                if (site.extraRule.pictureRule != null) {
                    if (site.extraRule.pictureRule.item != null) {
                        inputExtraRulePictureItemSelector.setText(joinSelector(site.extraRule.pictureRule.item));
                        inputExtraRulePictureItemRegex.setText(site.extraRule.pictureRule.item.regex);
                        inputExtraRulePictureItemReplacement.setText(site.extraRule.pictureRule.item.replacement);
                    }
                    if (site.extraRule.pictureRule.thumbnail != null) {
                        inputExtraRulePictureThumbnailSelector.setText(joinSelector(site.extraRule.pictureRule.thumbnail));
                        inputExtraRulePictureThumbnailRegex.setText(site.extraRule.pictureRule.thumbnail.regex);
                        inputExtraRulePictureThumbnailReplacement.setText(site.extraRule.pictureRule.thumbnail.replacement);
                    }
                    if (site.extraRule.pictureRule.url != null) {
                        inputExtraRulePictureUrlSelector.setText(joinSelector(site.extraRule.pictureRule.url));
                        inputExtraRulePictureUrlRegex.setText(site.extraRule.pictureRule.url.regex);
                        inputExtraRulePictureUrlReplacement.setText(site.extraRule.pictureRule.url.replacement);
                    }
                    if (site.extraRule.pictureRule.highRes != null) {
                        inputExtraRulePictureHighResSelector.setText(joinSelector(site.extraRule.pictureRule.highRes));
                        inputExtraRulePictureHighResRegex.setText(site.extraRule.pictureRule.highRes.regex);
                        inputExtraRulePictureHighResReplacement.setText(site.extraRule.pictureRule.highRes.replacement);
                    }
                } else {
                    if (site.extraRule.pictureThumbnail != null) {
                        inputExtraRulePictureThumbnailSelector.setText(joinSelector(site.extraRule.pictureThumbnail));
                        inputExtraRulePictureThumbnailRegex.setText(site.extraRule.pictureThumbnail.regex);
                        inputExtraRulePictureThumbnailReplacement.setText(site.extraRule.pictureThumbnail.replacement);
                    }
                    if (site.extraRule.pictureUrl != null) {
                        inputExtraRulePictureUrlSelector.setText(joinSelector(site.extraRule.pictureUrl));
                        inputExtraRulePictureUrlRegex.setText(site.extraRule.pictureUrl.regex);
                        inputExtraRulePictureUrlReplacement.setText(site.extraRule.pictureUrl.replacement);
                    }
                    if (site.extraRule.pictureHighRes != null) {
                        inputExtraRulePictureHighResSelector.setText(joinSelector(site.extraRule.pictureHighRes));
                        inputExtraRulePictureHighResRegex.setText(site.extraRule.pictureHighRes.regex);
                        inputExtraRulePictureHighResReplacement.setText(site.extraRule.pictureHighRes.replacement);
                    }
                }
                if (site.extraRule.commentRule != null) {
                    if (site.extraRule.commentRule.item != null) {
                        inputExtraRuleCommentItemSelector.setText(joinSelector(site.extraRule.commentRule.item));
                        inputExtraRuleCommentItemRegex.setText(site.extraRule.commentRule.item.regex);
                        inputExtraRuleCommentItemReplacement.setText(site.extraRule.commentRule.item.replacement);
                    }
                    if (site.extraRule.commentRule.avatar != null) {
                        inputExtraRuleCommentAvatarSelector.setText(joinSelector(site.extraRule.commentRule.avatar));
                        inputExtraRuleCommentAvatarRegex.setText(site.extraRule.commentRule.avatar.regex);
                        inputExtraRuleCommentAvatarReplacement.setText(site.extraRule.commentRule.avatar.replacement);
                    }
                    if (site.extraRule.commentRule.author != null) {
                        inputExtraRuleCommentAuthorSelector.setText(joinSelector(site.extraRule.commentRule.author));
                        inputExtraRuleCommentAuthorRegex.setText(site.extraRule.commentRule.author.regex);
                        inputExtraRuleCommentAuthorReplacement.setText(site.extraRule.commentRule.author.replacement);
                    }
                    if (site.extraRule.commentRule.datetime != null) {
                        inputExtraRuleCommentDatetimeSelector.setText(joinSelector(site.extraRule.commentRule.datetime));
                        inputExtraRuleCommentDatetimeRegex.setText(site.extraRule.commentRule.datetime.regex);
                        inputExtraRuleCommentDatetimeReplacement.setText(site.extraRule.commentRule.datetime.replacement);
                    }
                    if (site.extraRule.commentRule.content != null) {
                        inputExtraRuleCommentContentSelector.setText(joinSelector(site.extraRule.commentRule.content));
                        inputExtraRuleCommentContentRegex.setText(site.extraRule.commentRule.content.regex);
                        inputExtraRuleCommentContentReplacement.setText(site.extraRule.commentRule.content.replacement);
                    }
                } else {
                    if (site.extraRule.commentItem != null) {
                        inputExtraRuleCommentItemSelector.setText(joinSelector(site.extraRule.commentItem));
                        inputExtraRuleCommentItemRegex.setText(site.extraRule.commentItem.regex);
                        inputExtraRuleCommentItemReplacement.setText(site.extraRule.commentItem.replacement);
                    }
                    if (site.extraRule.commentAvatar != null) {
                        inputExtraRuleCommentAvatarSelector.setText(joinSelector(site.extraRule.commentAvatar));
                        inputExtraRuleCommentAvatarRegex.setText(site.extraRule.commentAvatar.regex);
                        inputExtraRuleCommentAvatarReplacement.setText(site.extraRule.commentAvatar.replacement);
                    }
                    if (site.extraRule.commentAuthor != null) {
                        inputExtraRuleCommentAuthorSelector.setText(joinSelector(site.extraRule.commentAuthor));
                        inputExtraRuleCommentAuthorRegex.setText(site.extraRule.commentAuthor.regex);
                        inputExtraRuleCommentAuthorReplacement.setText(site.extraRule.commentAuthor.replacement);
                    }
                    if (site.extraRule.commentDatetime != null) {
                        inputExtraRuleCommentDatetimeSelector.setText(joinSelector(site.extraRule.commentDatetime));
                        inputExtraRuleCommentDatetimeRegex.setText(site.extraRule.commentDatetime.regex);
                        inputExtraRuleCommentDatetimeReplacement.setText(site.extraRule.commentDatetime.replacement);
                    }
                    if (site.extraRule.commentContent != null) {
                        inputExtraRuleCommentContentSelector.setText(joinSelector(site.extraRule.commentContent));
                        inputExtraRuleCommentContentRegex.setText(site.extraRule.commentContent.regex);
                        inputExtraRuleCommentContentReplacement.setText(site.extraRule.commentContent.replacement);
                    }
                }
            }
        }
    }

    public Site fromEditTextToSite(boolean editSelector) {

        //categories
        List<Category> categories = categoryInputAdapter.getDataProvider().getItems();
        for (int i = 0; i < categories.size(); i++) {
            Category category = categories.get(i);
            if ("".equals(category.title) || "".equals(category.url)) {
                categories.remove(i);
                i--;
            } else {
                category.cid = i + 1;
            }
        }

        if (!editSelector) {
            int groupPos = inputGroup.getSelectedItemPosition();
            lastSite.gid = (groupPos >= 0) ? siteGroups.get(groupPos).gid : 0;
            lastSite.title = loadString(inputTitle);
            lastSite.indexUrl = loadString(inputIndexUrl);
            lastSite.galleryUrl = loadString(inputGalleryUrl);
            lastSite.searchUrl = loadString(inputSearchUrl);
            lastSite.loginUrl = loadString(inputLoginUrl);
            lastSite.cookie = loadString(inputCookie);
            lastSite.header = loadString(inputHeader);
            lastSite.flag = loadString(inputFlag);
            lastSite.disableHProxy = checkBoxDisableHProxy.isChecked();
            if (categories.size() > 0)
                lastSite.categories = categories;
            else
                lastSite.categories = null;
        } else {
            Site newSite = new Site();
            newSite.gid = siteGroups.get(inputGroup.getSelectedItemPosition()).gid;
            newSite.title = loadString(inputTitle);
            newSite.indexUrl = loadString(inputIndexUrl);
            newSite.galleryUrl = loadString(inputGalleryUrl);
            newSite.searchUrl = loadString(inputSearchUrl);
            newSite.loginUrl = loadString(inputLoginUrl);
            newSite.cookie = loadString(inputCookie);
            newSite.header = loadString(inputHeader);
            newSite.flag = loadString(inputFlag);
            newSite.disableHProxy = checkBoxDisableHProxy.isChecked();
            if (categories.size() > 0)
                newSite.categories = categories;

            //index rule
            newSite.indexRule = (newSite.indexRule == null) ? new Rule() : newSite.indexRule;
            newSite.indexRule.item = loadSelector(inputIndexRuleItemSelector, inputIndexRuleItemRegex, inputIndexRuleItemReplacement);
            newSite.indexRule.idCode = loadSelector(inputIndexRuleIdCodeSelector, inputIndexRuleIdCodeRegex, inputIndexRuleIdCodeReplacement);
            newSite.indexRule.title = loadSelector(inputIndexRuleTitleSelector, inputIndexRuleTitleRegex, inputIndexRuleTitleReplacement);
            newSite.indexRule.uploader = loadSelector(inputIndexRuleUploaderSelector, inputIndexRuleUploaderRegex, inputIndexRuleUploaderReplacement);
            newSite.indexRule.cover = loadSelector(inputIndexRuleCoverSelector, inputIndexRuleCoverRegex, inputIndexRuleCoverReplacement);
            newSite.indexRule.category = loadSelector(inputIndexRuleCategorySelector, inputIndexRuleCategoryRegex, inputIndexRuleCategoryReplacement);
            newSite.indexRule.datetime = loadSelector(inputIndexRuleDatetimeSelector, inputIndexRuleDatetimeRegex, inputIndexRuleDatetimeReplacement);
            newSite.indexRule.rating = loadSelector(inputIndexRuleRatingSelector, inputIndexRuleRatingRegex, inputIndexRuleRatingReplacement);
            newSite.indexRule.tags = loadSelector(inputIndexRuleTagsSelector, inputIndexRuleTagsRegex, inputIndexRuleTagsReplacement);

            //search rule
            newSite.searchRule = (newSite.searchRule == null) ? new Rule() : newSite.searchRule;
            newSite.searchRule.item = loadSelector(inputSearchRuleItemSelector, inputSearchRuleItemRegex, inputSearchRuleItemReplacement);
            newSite.searchRule.idCode = loadSelector(inputSearchRuleIdCodeSelector, inputSearchRuleIdCodeRegex, inputSearchRuleIdCodeReplacement);
            newSite.searchRule.title = loadSelector(inputSearchRuleTitleSelector, inputSearchRuleTitleRegex, inputSearchRuleTitleReplacement);
            newSite.searchRule.uploader = loadSelector(inputSearchRuleUploaderSelector, inputSearchRuleUploaderRegex, inputSearchRuleUploaderReplacement);
            newSite.searchRule.cover = loadSelector(inputSearchRuleCoverSelector, inputSearchRuleCoverRegex, inputSearchRuleCoverReplacement);
            newSite.searchRule.category = loadSelector(inputSearchRuleCategorySelector, inputSearchRuleCategoryRegex, inputSearchRuleCategoryReplacement);
            newSite.searchRule.datetime = loadSelector(inputSearchRuleDatetimeSelector, inputSearchRuleDatetimeRegex, inputSearchRuleDatetimeReplacement);
            newSite.searchRule.rating = loadSelector(inputSearchRuleRatingSelector, inputSearchRuleRatingRegex, inputSearchRuleRatingReplacement);
            newSite.searchRule.tags = loadSelector(inputSearchRuleTagsSelector, inputSearchRuleTagsRegex, inputSearchRuleTagsReplacement);

            if (newSite.searchRule.isEmpty())
                newSite.searchRule = null;

            //gallery rule
            newSite.galleryRule = (newSite.galleryRule == null) ? new Rule() : newSite.galleryRule;
            newSite.galleryRule.item = loadSelector(inputGalleryRuleItemSelector, inputGalleryRuleItemRegex, inputGalleryRuleItemReplacement);
            newSite.galleryRule.title = loadSelector(inputGalleryRuleTitleSelector, inputGalleryRuleTitleRegex, inputGalleryRuleTitleReplacement);
            newSite.galleryRule.uploader = loadSelector(inputGalleryRuleUploaderSelector, inputGalleryRuleUploaderRegex, inputGalleryRuleUploaderReplacement);
            newSite.galleryRule.cover = loadSelector(inputGalleryRuleCoverSelector, inputGalleryRuleCoverRegex, inputGalleryRuleCoverReplacement);
            newSite.galleryRule.category = loadSelector(inputGalleryRuleCategorySelector, inputGalleryRuleCategoryRegex, inputGalleryRuleCategoryReplacement);
            newSite.galleryRule.datetime = loadSelector(inputGalleryRuleDatetimeSelector, inputGalleryRuleDatetimeRegex, inputGalleryRuleDatetimeReplacement);
            newSite.galleryRule.rating = loadSelector(inputGalleryRuleRatingSelector, inputGalleryRuleRatingRegex, inputGalleryRuleRatingReplacement);
            newSite.galleryRule.description = loadSelector(inputGalleryRuleDescriptionSelector, inputGalleryRuleDescriptionRegex, inputGalleryRuleDescriptionReplacement);
            newSite.galleryRule.tags = loadSelector(inputGalleryRuleTagsSelector, inputGalleryRuleTagsRegex, inputGalleryRuleTagsReplacement);

            newSite.galleryRule.pictureRule = (newSite.galleryRule.pictureRule == null) ? new PictureRule() : newSite.galleryRule.pictureRule;
            newSite.galleryRule.pictureRule.item = loadSelector(inputGalleryRulePictureItemSelector, inputGalleryRulePictureItemRegex, inputGalleryRulePictureItemReplacement);
            newSite.galleryRule.pictureRule.thumbnail = loadSelector(inputGalleryRulePictureThumbnailSelector, inputGalleryRulePictureThumbnailRegex, inputGalleryRulePictureThumbnailReplacement);
            newSite.galleryRule.pictureRule.url = loadSelector(inputGalleryRulePictureUrlSelector, inputGalleryRulePictureUrlRegex, inputGalleryRulePictureUrlReplacement);
            newSite.galleryRule.pictureRule.highRes = loadSelector(inputGalleryRulePictureHighResSelector, inputGalleryRulePictureHighResRegex, inputGalleryRulePictureHighResReplacement);

            newSite.galleryRule.commentRule = (newSite.galleryRule.commentRule == null) ? new CommentRule() : newSite.galleryRule.commentRule;
            newSite.galleryRule.commentRule.item = loadSelector(inputGalleryRuleCommentItemSelector, inputGalleryRuleCommentItemRegex, inputGalleryRuleCommentItemReplacement);
            newSite.galleryRule.commentRule.avatar = loadSelector(inputGalleryRuleCommentAvatarSelector, inputGalleryRuleCommentAvatarRegex, inputGalleryRuleCommentAvatarReplacement);
            newSite.galleryRule.commentRule.author = loadSelector(inputGalleryRuleCommentAuthorSelector, inputGalleryRuleCommentAuthorRegex, inputGalleryRuleCommentAuthorReplacement);
            newSite.galleryRule.commentRule.datetime = loadSelector(inputGalleryRuleCommentDatetimeSelector, inputGalleryRuleCommentDatetimeRegex, inputGalleryRuleCommentDatetimeReplacement);
            newSite.galleryRule.commentRule.content = loadSelector(inputGalleryRuleCommentContentSelector, inputGalleryRuleCommentContentRegex, inputGalleryRuleCommentContentReplacement);
            if (newSite.galleryRule.commentRule.isEmpty())
                newSite.galleryRule.commentRule = null;

            //extra rule
            newSite.extraRule = (newSite.extraRule == null) ? new Rule() : newSite.extraRule;
            newSite.extraRule.item = loadSelector(inputExtraRuleItemSelector, inputExtraRuleItemRegex, inputExtraRuleItemReplacement);
            newSite.extraRule.title = loadSelector(inputExtraRuleTitleSelector, inputExtraRuleTitleRegex, inputExtraRuleTitleReplacement);
            newSite.extraRule.uploader = loadSelector(inputExtraRuleUploaderSelector, inputExtraRuleUploaderRegex, inputExtraRuleUploaderReplacement);
            newSite.extraRule.cover = loadSelector(inputExtraRuleCoverSelector, inputExtraRuleCoverRegex, inputExtraRuleCoverReplacement);
            newSite.extraRule.category = loadSelector(inputExtraRuleCategorySelector, inputExtraRuleCategoryRegex, inputExtraRuleCategoryReplacement);
            newSite.extraRule.datetime = loadSelector(inputExtraRuleDatetimeSelector, inputExtraRuleDatetimeRegex, inputExtraRuleDatetimeReplacement);
            newSite.extraRule.rating = loadSelector(inputExtraRuleRatingSelector, inputExtraRuleRatingRegex, inputExtraRuleRatingReplacement);
            newSite.extraRule.description = loadSelector(inputExtraRuleDescriptionSelector, inputExtraRuleDescriptionRegex, inputExtraRuleDescriptionReplacement);
            newSite.extraRule.tags = loadSelector(inputExtraRuleTagsSelector, inputExtraRuleTagsRegex, inputExtraRuleTagsReplacement);

            newSite.extraRule.pictureRule = (newSite.extraRule.pictureRule == null) ? new PictureRule() : newSite.extraRule.pictureRule;
            newSite.extraRule.pictureRule.item = loadSelector(inputExtraRulePictureItemSelector, inputExtraRulePictureItemRegex, inputExtraRulePictureItemReplacement);
            newSite.extraRule.pictureRule.thumbnail = loadSelector(inputExtraRulePictureThumbnailSelector, inputExtraRulePictureThumbnailRegex, inputExtraRulePictureThumbnailReplacement);
            newSite.extraRule.pictureRule.url = loadSelector(inputExtraRulePictureUrlSelector, inputExtraRulePictureUrlRegex, inputExtraRulePictureUrlReplacement);
            newSite.extraRule.pictureRule.highRes = loadSelector(inputExtraRulePictureHighResSelector, inputExtraRulePictureHighResRegex, inputExtraRulePictureHighResReplacement);

            newSite.extraRule.commentRule = (newSite.extraRule.commentRule == null) ? new CommentRule() : newSite.extraRule.commentRule;
            newSite.extraRule.commentRule.item = loadSelector(inputExtraRuleCommentItemSelector, inputExtraRuleCommentItemRegex, inputExtraRuleCommentItemReplacement);
            newSite.extraRule.commentRule.avatar = loadSelector(inputExtraRuleCommentAvatarSelector, inputExtraRuleCommentAvatarRegex, inputExtraRuleCommentAvatarReplacement);
            newSite.extraRule.commentRule.author = loadSelector(inputExtraRuleCommentAuthorSelector, inputExtraRuleCommentAuthorRegex, inputExtraRuleCommentAuthorReplacement);
            newSite.extraRule.commentRule.datetime = loadSelector(inputExtraRuleCommentDatetimeSelector, inputExtraRuleCommentDatetimeRegex, inputExtraRuleCommentDatetimeReplacement);
            newSite.extraRule.commentRule.content = loadSelector(inputExtraRuleCommentContentSelector, inputExtraRuleCommentContentRegex, inputExtraRuleCommentContentReplacement);
            if (newSite.extraRule.commentRule.isEmpty())
                newSite.extraRule.commentRule = null;

            if (newSite.extraRule.isEmpty())
                newSite.extraRule = null;

            if (lastSite != null)
                lastSite.replace(newSite);
            else
                lastSite = newSite;
        }

        return lastSite;
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
