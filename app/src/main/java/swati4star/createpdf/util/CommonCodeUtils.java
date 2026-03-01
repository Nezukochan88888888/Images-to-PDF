package swati4star.createpdf.util;

import android.app.Activity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import swati4star.createpdf.R;
import swati4star.createpdf.adapter.ExtractImagesAdapter;
import swati4star.createpdf.adapter.MergeFilesAdapter;
import swati4star.createpdf.model.HomePageItem;

public class CommonCodeUtils {

    Map<Integer, HomePageItem> mFragmentPositionMap;

    public static CommonCodeUtils getInstance() {
        return CommonCodeUtils.SingletonHolder.INSTANCE;
    }

    /**
     * updates the output recycler view if paths.size > 0
     * else give the main view
     */
    public void populateUtil(Activity mActivity, ArrayList<String> paths,
            MergeFilesAdapter.OnClickListener onClickListener,
            RelativeLayout layout, LottieAnimationView animationView,
            RecyclerView recyclerView) {

        if (paths == null || paths.isEmpty()) {
            layout.setVisibility(View.GONE);
        } else {
            // Init recycler view
            recyclerView.setVisibility(View.VISIBLE);
            MergeFilesAdapter mergeFilesAdapter = new MergeFilesAdapter(mActivity,
                    paths, false, onClickListener);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(mActivity);
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setAdapter(mergeFilesAdapter);
            recyclerView.addItemDecoration(new ViewFilesDividerItemDecoration(mActivity));
        }
        animationView.setVisibility(View.GONE);
    }

    /**
     * sets the appropriate text to success Text View & display images in adapter
     */
    public void updateView(Activity mActivity, int imageCount, ArrayList<String> outputFilePaths,
            TextView successTextView, LinearLayout options, RecyclerView mCreatedImages,
            ExtractImagesAdapter.OnFileItemClickedListener listener) {

        if (imageCount == 0) {
            StringUtils.getInstance().showSnackbar(mActivity, R.string.extract_images_failed);
            return;
        }

        String text = String.format(mActivity.getString(R.string.extract_images_success), imageCount);
        StringUtils.getInstance().showSnackbar(mActivity, text);
        successTextView.setVisibility(View.VISIBLE);
        options.setVisibility(View.VISIBLE);
        ExtractImagesAdapter extractImagesAdapter = new ExtractImagesAdapter(mActivity, outputFilePaths,
                listener);
        // init recycler view for displaying generated image list
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(mActivity);
        successTextView.setText(text);
        mCreatedImages.setVisibility(View.VISIBLE);
        mCreatedImages.setLayoutManager(mLayoutManager);
        // set up adapter
        mCreatedImages.setAdapter(extractImagesAdapter);
        mCreatedImages.addItemDecoration(new ViewFilesDividerItemDecoration(mActivity));
    }

    /**
     * Closes the bottom sheet if it is expanded
     */
    public void closeBottomSheetUtil(BottomSheetBehavior sheetBehavior) {
        if (checkSheetBehaviourUtil(sheetBehavior)) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    /**
     * Checks whether the bottom sheet is expanded or collapsed
     */
    public boolean checkSheetBehaviourUtil(BottomSheetBehavior sheetBehavior) {
        return sheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED;
    }

    private void addFragmentPosition(boolean homePageItems, int iconA, int iconB,
            int iconId, int drawableId, int titleString) {
        mFragmentPositionMap.put(homePageItems ? iconA : iconB,
                new HomePageItem(iconId, drawableId, titleString));
    }

    public Map<Integer, HomePageItem> fillNavigationItemsMap(boolean homePageItems) {
        mFragmentPositionMap = new HashMap<>();
        addFragmentPosition(homePageItems, R.id.images_to_pdf,
                R.id.images_to_pdf_fav, R.id.nav_camera, R.drawable.ic_menu_camera, R.string.images_to_pdf);
        addFragmentPosition(homePageItems, R.id.qr_barcode_to_pdf,
                R.id.qr_barcode_to_pdf_fav, R.id.nav_qrcode, R.drawable.ic_qrcode_24dp, R.string.qr_barcode_pdf);
        addFragmentPosition(homePageItems, R.id.excel_to_pdf,
                R.id.excel_to_pdf_fav, R.id.nav_excel_to_pdf, R.drawable.ic_excel, R.string.excel_to_pdf);
        addFragmentPosition(homePageItems, R.id.view_files, R.id.view_files_fav,
                R.id.nav_gallery, R.drawable.ic_menu_gallery, R.string.viewFiles);
        addFragmentPosition(homePageItems, R.id.rotate_pages, R.id.rotate_pages_fav,
                R.id.nav_gallery, R.drawable.baseline_crop_rotate_24, R.string.rotate_pages);
        addFragmentPosition(homePageItems, R.id.extract_text, R.id.extract_text_fav,
                R.id.nav_text_extract, R.drawable.ic_broken_image_black_24dp, R.string.extract_text);
        addFragmentPosition(homePageItems, R.id.add_watermark, R.id.add_watermark_fav,
                R.id.nav_add_watermark, R.drawable.ic_branding_watermark_black_24dp, R.string.add_watermark);
        addFragmentPosition(homePageItems, R.id.merge_pdf, R.id.merge_pdf_fav,
                R.id.nav_merge, R.drawable.ic_merge_type_black_24dp, R.string.merge_pdf);
        addFragmentPosition(homePageItems, R.id.split_pdf, R.id.split_pdf_fav,
                R.id.nav_split, R.drawable.ic_call_split_black_24dp, R.string.split_pdf);
        addFragmentPosition(homePageItems, R.id.text_to_pdf, R.id.text_to_pdf_fav,
                R.id.nav_text_to_pdf, R.drawable.ic_text_format_black_24dp, R.string.text_to_pdf);
        addFragmentPosition(homePageItems, R.id.compress_pdf, R.id.compress_pdf_fav,
                R.id.nav_compress_pdf, R.drawable.ic_compress_image, R.string.compress_pdf);
        addFragmentPosition(homePageItems, R.id.remove_pages, R.id.remove_pages_fav,
                R.id.nav_remove_pages, R.drawable.ic_remove_circle_black_24dp, R.string.remove_pages);
        addFragmentPosition(homePageItems, R.id.rearrange_pages, R.id.rearrange_pages_fav,
                R.id.nav_rearrange_pages, R.drawable.ic_rearrange, R.string.reorder_pages);
        addFragmentPosition(homePageItems, R.id.extract_images, R.id.extract_images_fav,
                R.id.nav_extract_images, R.drawable.ic_broken_image_black_24dp, R.string.extract_images);
        addFragmentPosition(homePageItems, R.id.view_history, R.id.view_history_fav,
                R.id.nav_history, R.drawable.ic_history_black_24dp, R.string.history);
        addFragmentPosition(homePageItems, R.id.pdf_to_images, R.id.pdf_to_images_fav,
                R.id.nav_pdf_to_images, R.drawable.ic_image_black_24dp, R.string.pdf_to_images);
        addFragmentPosition(homePageItems, R.id.add_password, R.id.add_password_fav,
                R.id.nav_add_password, R.drawable.ic_lock_black_24dp, R.string.add_password);
        addFragmentPosition(homePageItems, R.id.remove_password, R.id.remove_password_fav,
                R.id.nav_remove_password, R.drawable.ic_lock_open_black_24dp, R.string.remove_password);
        addFragmentPosition(homePageItems, R.id.add_images, R.id.add_images_fav,
                R.id.nav_add_images, R.drawable.ic_add_black_24dp, R.string.add_images);
        addFragmentPosition(homePageItems, R.id.remove_duplicates_pages_pdf,
                R.id.remove_duplicates_pages_pdf_fav, R.id.nav_remove_duplicate_pages,
                R.drawable.ic_remove_duplicate_square_black, R.string.remove_duplicate_pages);
        addFragmentPosition(homePageItems, R.id.invert_pdf, R.id.invert_pdf_fav,
                R.id.nav_invert_pdf, R.drawable.ic_invert_color_24dp, R.string.invert_pdf);
        addFragmentPosition(homePageItems, R.id.zip_to_pdf, R.id.zip_to_pdf_fav,
                R.id.nav_zip_to_pdf, R.drawable.ic_zip_to_pdf, R.string.zip_to_pdf);
        addFragmentPosition(homePageItems, R.id.add_text, R.id.add_text_fav,
                R.id.nav_add_text, R.drawable.ic_text_format_black_24dp, R.string.add_text);
        addFragmentPosition(homePageItems, R.id.pdf_to_docx, 0,
                R.id.nav_pdf_to_docx, R.drawable.ic_insert_drive_file_black_24dp, R.string.pdf_to_docx);
        addFragmentPosition(homePageItems, R.id.word_to_pdf, 0,
                R.id.nav_word_to_pdf, R.drawable.ic_baseline_picture_as_pdf_24, R.string.word_to_pdf);
        return mFragmentPositionMap;
    }

    private static class SingletonHolder {
        static final CommonCodeUtils INSTANCE = new CommonCodeUtils();
    }
}
