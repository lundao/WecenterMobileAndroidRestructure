package org.iflab.wecentermobileandroidrestructure.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.apache.http.Header;
import org.iflab.wecentermobileandroidrestructure.R;
import org.iflab.wecentermobileandroidrestructure.http.AsyncHttpWecnter;
import org.iflab.wecentermobileandroidrestructure.http.RelativeUrl;
import org.iflab.wecentermobileandroidrestructure.model.ImageInfo;
import org.iflab.wecentermobileandroidrestructure.tools.CameraPhotoUtil;
import org.iflab.wecentermobileandroidrestructure.tools.Global;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Calendar;

/**
 * Created by hcjcch on 15/6/6.
 */

public class PersonalCenterEditActivity extends BaseActivity {

    private Uri fileUri;
    private final int RESULT_REQUEST_PHOTO = 1005;
    private final int RESULT_REQUEST_PHOTO_CROP = 1006;
    private TextView userImageSelect;
    private Uri fileCropUri;
    private ImageView imgUser;
    private TextView birthDaySelect;
    private Calendar calendar;
    private TextView txtMale;
    private Bundle bundle;
    private Intent intent;
    private RadioGroup radioGroup;
    private RadioButton radioMale;
    private int sexCheck;
    private TextView save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_center_edit);
        calendar = Calendar.getInstance();
        intent = getIntent();
        bundle = intent.getBundleExtra("bundle");
        setToolBar();
        findViews();
        setViews();
    }


    private void findViews() {
        userImageSelect = (TextView) findViewById(R.id.txt_user_img);
        imgUser = (ImageView) findViewById(R.id.img_user);
        birthDaySelect = (TextView) findViewById(R.id.txt_birthday_select);
        radioGroup = (RadioGroup) findViewById(R.id.radiogroup_sex);
        radioMale = (RadioButton) findViewById(R.id.radio_sex_male);
        radioMale.setChecked(true);
        save = (TextView) findViewById(R.id.txt_cave_user_information);
    }

    private void setViews() {
        userImageSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUserImage();
            }
        });

        birthDaySelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(PersonalCenterEditActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radio_sex_male:
                        sexCheck = 1;
                        break;
                    case R.id.radio_sex_female:
                        sexCheck = 2;
                        break;
                    case R.id.radio_sex_no:
                        sexCheck = 3;
                        break;
                }
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadOtherInformation();
            }
        });
    }

    private void setToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(Color.parseColor("#00ffffff"));
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        toolbar.setTitle("信息修改");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        setSupportActionBar(toolbar);
    }

    private void camera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = CameraPhotoUtil.getOutputMediaFileUri();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, RESULT_REQUEST_PHOTO);
    }

    private void photo() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_REQUEST_PHOTO);
    }

    private void setUserImage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("更换头像")
                .setItems(new String[]{"相机", "图片"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            camera();
                        } else {
                            photo();
                        }
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_REQUEST_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    fileUri = data.getData();
                }
                fileCropUri = CameraPhotoUtil.getOutputMediaFileUri();
                cropImageUri(fileUri, fileCropUri, 640, 640, RESULT_REQUEST_PHOTO_CROP);
            }
        } else if (requestCode == RESULT_REQUEST_PHOTO_CROP) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    String filePath = Global.getPath(this, fileCropUri);
                    ImageLoader.getInstance().displayImage(ImageInfo.pathAddPreFix(filePath), imgUser, PhotoPickActivity.optionsImage);
                    if (filePath == null) {
                        Toast.makeText(PersonalCenterEditActivity.this, "文件失剪裁败", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    File file = new File(filePath);
                    RequestParams params = new RequestParams();
                    try {
                        params.put("user_avatar", file);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    uploadAvatar(params);
                } catch (Exception e) {

                }
            }
        }
    }

    private void cropImageUri(Uri uri, Uri outputUri, int outputX, int outputY, int requestCode) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", outputX);
        intent.putExtra("outputY", outputY);
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
        intent.putExtra("return-data", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true); // no face detection
        startActivityForResult(intent, requestCode);
    }

    private void uploadAvatar(RequestParams params) {
        AsyncHttpWecnter.post(RelativeUrl.USER_IMG_EDIT, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String json = new String(responseBody);
                boolean jsonProgress = jsonPreproccess(json);
                if (jsonProgress) return;
                try {
                    JSONObject jsonObject = new JSONObject(new String(responseBody));
                    JSONObject rsm = jsonObject.getJSONObject("rsm");
                    String priview = rsm.getString("preview");
                    if (priview != null) {
                        //TODO 上传成功

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }

            @Override
            public void onFinish() {
                super.onFinish();
            }

        });
    }

    private void uploadOtherInformation() {
        RequestParams params = new RequestParams();
        params.put("uid", bundle.getInt("uid"));
        params.put("user_name", bundle.getString("userName"));
        params.put("sex", sexCheck);
        params.put("signature", "青山依旧在，几度夕阳红!");
        AsyncHttpWecnter.post(RelativeUrl.USER_INFORMATION_EDIT, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                System.out.println(new String(responseBody));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                System.out.println(new String(responseBody));
            }

            @Override
            public void onFinish() {
                super.onFinish();
            }
        });
    }

}