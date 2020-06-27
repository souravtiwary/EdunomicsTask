package personal.project.taskoneedunomics;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class PostDetailActivity extends AppCompatActivity {

    TextView mTitleTv, mDeatailTv;
    ImageView mImage;
    Button mSaveBtn, mShareBtn, mWallBtn;
    Bitmap bitmap;

    private static final int WRITE_EXTERNAL_STORAGE_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        ActionBar actionBar;
        actionBar = getSupportActionBar();
        actionBar.setTitle("Post Details");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        mTitleTv = findViewById(R.id.titleTv);
        mDeatailTv = findViewById(R.id.descriptionTv);
        mImage = findViewById(R.id.imageView);
        mSaveBtn = findViewById(R.id.saveBtn);
        mShareBtn = findViewById(R.id.shareBtn);
        mWallBtn = findViewById(R.id.wallBtn);

        //getting data from intent

        byte[] bytes = getIntent().getByteArrayExtra("image");
        String title = getIntent().getStringExtra("title");
        String desc = getIntent().getStringExtra("desc");
        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        //set
        mTitleTv.setText(title);
        mDeatailTv.setText(desc);
        mImage.setImageBitmap(bmp);

        //get image from imageview
        bitmap = ((BitmapDrawable)mImage.getDrawable()).getBitmap();

        //save btn
        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_DENIED){
                        String[] permission = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        //show popup to grant permission
                        requestPermissions(permission, WRITE_EXTERNAL_STORAGE_CODE);
                    }
                    else
                    {
                        // permission is already given
                        saveImage();
                    }
                }
                else{
                    //System os is < marshmallow
                    saveImage();
                }

            }
        });

        //share btn
        mShareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //You can share after the update is done
                Toast.makeText(PostDetailActivity.this, "You can share after the update is done :)", Toast.LENGTH_SHORT).show();
            }
        });

        //wall btn
        mWallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(PostDetailActivity.this, "You can set image as wallpaper after the update is done :)", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveImage() {

        //save image to storage with time stamp as name
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(System.currentTimeMillis());
        //path to external storage
        File path = Environment.getExternalStorageDirectory();

        //create folder named "firebase"
        File dir = new File (path+"/Firebase/");
        dir.mkdir();
        //image name
        String imageName = timestamp+".PNG";
        File file = new File(dir, imageName);
        OutputStream outputStream;
        try {
            outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            Toast.makeText(this, "Successfully saved " + imageName+ " to "+dir, Toast.LENGTH_SHORT).show();
        }catch (Exception e){

            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();

        }

    }

    // handle onBackPressed(go to the previous activity)
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case WRITE_EXTERNAL_STORAGE_CODE: {
                //if request is cancelled the result arrarys are empty
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    //permissin is granted
                    saveImage();
                }
                else{
                    //denied
                    Toast.makeText(this, "enable permission to save the image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}