package swati4star.createpdf.fragment;

import static android.app.Activity.RESULT_OK;
import static swati4star.createpdf.util.Constants.REQUEST_CODE_FOR_WRITE_PERMISSION;
import static swati4star.createpdf.util.Constants.STORAGE_LOCATION;
import static swati4star.createpdf.util.Constants.WRITE_PERMISSIONS;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.File;
import java.util.ArrayList;

import swati4star.createpdf.R;
import swati4star.createpdf.adapter.MergeFilesAdapter;
import swati4star.createpdf.databinding.FragmentExtractTextBinding;
import swati4star.createpdf.interfaces.BottomSheetPopulate;
import swati4star.createpdf.interfaces.OnBackPressedInterface;
import swati4star.createpdf.interfaces.OnPDFCreatedInterface;
import swati4star.createpdf.util.BottomSheetCallback;
import swati4star.createpdf.util.BottomSheetUtils;
import swati4star.createpdf.util.CommonCodeUtils;
import swati4star.createpdf.util.Constants;
import swati4star.createpdf.util.DialogUtils;
import swati4star.createpdf.util.FileUtils;
import swati4star.createpdf.util.MorphButtonUtility;
import swati4star.createpdf.util.PermissionsUtils;
import swati4star.createpdf.util.RealPathUtil;
import swati4star.createpdf.util.StringUtils;
import swati4star.createpdf.util.WordToPdfAsync;

public class WordToPdfFragment extends Fragment implements MergeFilesAdapter.OnClickListener,
        BottomSheetPopulate, OnBackPressedInterface, OnPDFCreatedInterface {

    private final int mFileSelectCode = 0;
    private Activity mActivity;
    private FileUtils mFileUtils;
    private Uri mWordFileUri;
    private String mRealPath;
    private BottomSheetUtils mBottomSheetUtils;
    private BottomSheetBehavior mSheetBehavior;
    private SharedPreferences mSharedPreferences;
    private MorphButtonUtility mMorphButtonUtility;
    private boolean mButtonClicked = false;
    private String mFileName;
    private MaterialDialog mMaterialDialog;

    private FragmentExtractTextBinding mBinding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mBinding = FragmentExtractTextBinding.inflate(inflater, container, false);
        View rootView = mBinding.getRoot();

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        mMorphButtonUtility = new MorphButtonUtility(mActivity);
        LinearLayout layoutBottomSheet = rootView.findViewById(R.id.bottom_sheet);
        mSheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);

        mBinding.extractText.setText(R.string.create_pdf);

        mMorphButtonUtility.morphToGrey(mBinding.extractText, mMorphButtonUtility.integer());
        mBinding.extractText.setEnabled(false);
        mBinding.bottomSheet.lottieProgress.setVisibility(View.GONE);
        mSheetBehavior.setBottomSheetCallback(new BottomSheetCallback(mBinding.bottomSheet.upArrow, isAdded()));

        mBinding.bottomSheet.viewFiles.setOnClickListener(v -> {
            mBottomSheetUtils.showHideSheet(mSheetBehavior);
        });

        mBinding.selectPdfFile.setText(R.string.select_word_file);
        mBinding.selectPdfFile.setOnClickListener(v -> {
            if (!mButtonClicked) {
                Uri uri = Uri.parse(Environment.getRootDirectory() + "/");
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                String[] mimeTypes = { "application/msword",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document" };
                intent.setType("*/*");
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                try {
                    startActivityForResult(
                            Intent.createChooser(intent, String.valueOf(R.string.select_file)),
                            mFileSelectCode);
                    mButtonClicked = true;
                } catch (android.content.ActivityNotFoundException ex) {
                    StringUtils.getInstance().showSnackbar(mActivity, R.string.install_file_manager);
                }
            }
        });

        mBinding.extractText.setOnClickListener(v -> {
            openCreatePdf();
        });

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
        mFileUtils = new FileUtils(mActivity);
        mBottomSheetUtils = new BottomSheetUtils(mActivity);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mButtonClicked = false;
        if (requestCode == mFileSelectCode && resultCode == RESULT_OK) {
            if (data == null || data.getData() == null) {
                StringUtils.getInstance().showSnackbar(mActivity, R.string.error_selecting_file);
                return;
            }
            mWordFileUri = data.getData();
            mRealPath = RealPathUtil.getInstance().getRealPath(getContext(), data.getData());
            StringUtils.getInstance().showSnackbar(mActivity, getResources().getString(R.string.text_file_selected));
            mFileName = mFileUtils.getFileName(mWordFileUri);
            if (mFileName != null && !mFileName.endsWith(Constants.docExtension)
                    && !mFileName.endsWith(Constants.docxExtension)) {
                StringUtils.getInstance().showSnackbar(mActivity, R.string.extension_not_supported);
                return;
            }
            mFileName = mActivity.getResources().getString(R.string.text_file_selected) + ": " + mFileName;
            mBinding.tvExtractTextBottom.setText(mFileName);
            mBinding.tvExtractTextBottom.setVisibility(View.VISIBLE);
            mBinding.extractText.setEnabled(true);
            mMorphButtonUtility.morphToSquare(mBinding.extractText, mMorphButtonUtility.integer());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void getRuntimePermissions() {
        PermissionsUtils.getInstance().requestRuntimePermissions(this,
                WRITE_PERMISSIONS,
                REQUEST_CODE_FOR_WRITE_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
            @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionsUtils.getInstance().handleRequestPermissionsResult(mActivity, grantResults,
                requestCode, REQUEST_CODE_FOR_WRITE_PERMISSION, this::openCreatePdf);
    }

    public void openCreatePdf() {
        PermissionsUtils.getInstance().checkStoragePermissionAndProceed(getContext(), this::openPdfDialog);
    }

    private void openPdfDialog() {
        new MaterialDialog.Builder(mActivity)
                .title(R.string.creating_pdf)
                .content(R.string.enter_file_name)
                .input(getString(R.string.example), null, (dialog, input) -> {
                    if (StringUtils.getInstance().isEmpty(input)) {
                        StringUtils.getInstance().showSnackbar(mActivity, R.string.snackbar_name_not_blank);
                    } else {
                        final String inputName = input.toString();
                        if (!mFileUtils.isFileExist(inputName + getString(R.string.pdf_ext))) {
                            createPdfFromWord(inputName);
                        } else {
                            MaterialDialog.Builder builder = DialogUtils.getInstance().createOverwriteDialog(mActivity);
                            builder.onPositive((dialog12, which) -> createPdfFromWord(inputName))
                                    .onNegative((dialog1, which) -> openCreatePdf())
                                    .show();
                        }
                    }
                })
                .show();
    }

    private void createPdfFromWord(String inputName) {
        String storePath = mSharedPreferences.getString(STORAGE_LOCATION,
                StringUtils.getInstance().getDefaultStorageLocation());

        new WordToPdfAsync(mWordFileUri, mRealPath, inputName, storePath, mActivity, this).execute();
    }

    @Override
    public void onPDFCreationStarted() {
        mMaterialDialog = DialogUtils.getInstance().createAnimationDialog(mActivity);
        mMaterialDialog.show();
    }

    @Override
    public void onPDFCreated(boolean success, String path) {
        if (mMaterialDialog != null && mMaterialDialog.isShowing())
            mMaterialDialog.dismiss();

        if (!success) {
            StringUtils.getInstance().showSnackbar(mActivity, R.string.error_word_to_pdf);
        } else {
            StringUtils.getInstance().getSnackbarwithAction(mActivity, R.string.snackbar_pdfCreated)
                    .setAction(R.string.snackbar_viewAction,
                            v -> mFileUtils.openFile(path, FileUtils.FileType.e_PDF))
                    .show();

            mBinding.tvExtractTextBottom.setVisibility(View.GONE);
            mBinding.selectPdfFile.setText(R.string.select_word_file);
            mButtonClicked = false;
        }

        mMorphButtonUtility.morphToGrey(mBinding.extractText, mMorphButtonUtility.integer());
        mBinding.extractText.setEnabled(false);
        mRealPath = null;
        mWordFileUri = null;
    }

    @Override
    public void onPopulate(ArrayList<String> paths) {
        CommonCodeUtils.getInstance().populateUtil(mActivity, paths,
                this, mBinding.bottomSheet.layout, mBinding.bottomSheet.lottieProgress,
                mBinding.bottomSheet.recyclerViewFiles);
    }

    @Override
    public void onItemClick(String path) {
        mSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        mRealPath = path;
        mWordFileUri = Uri.fromFile(new File(path));
        mFileName = FileUtils.getFileName(path);
        mFileName = getResources().getString(R.string.text_file_selected) + ": " + mFileName;
        mBinding.tvExtractTextBottom.setText(mFileName);
        mBinding.tvExtractTextBottom.setVisibility(View.VISIBLE);
        mBinding.extractText.setEnabled(true);
        mMorphButtonUtility.morphToSquare(mBinding.extractText, mMorphButtonUtility.integer());
    }

    @Override
    public void closeBottomSheet() {
        CommonCodeUtils.getInstance().closeBottomSheetUtil(mSheetBehavior);
    }

    @Override
    public boolean checkSheetBehaviour() {
        return CommonCodeUtils.getInstance().checkSheetBehaviourUtil(mSheetBehavior);
    }
}
