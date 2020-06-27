package personal.project.taskoneedunomics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class AddPostActivity extends AppCompatActivity {

    EditText mTitleEt, mDescEt;
    ImageView mPostTv;
    Button mUploadBtn;

    //Folterpath for Firebase Storage
    String mStoragePath = "All_image_Uploads/";
    //Root database name for firebase database
    String mDatabasePath = "Data";

    String cTitile, cDescr, cImage;

    //Creating URI
    Uri mFilePathUri;

    StorageReference mStorageReference;
    DatabaseReference mdatabaseReference;

    ProgressDialog mprogressDialog;

    //Image request code for choosing image
    int IMAGE_REQUEST_CODE = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        mTitleEt = findViewById(R.id.pTitleEt);
        mDescEt = findViewById(R.id.pDescEt);
        mPostTv = findViewById(R.id.pImageTv);
        mUploadBtn = findViewById(R.id.pUploadBtn);

        Bundle intent = getIntent().getExtras();
        if(intent !=null){
            cTitile = intent.getString("cTitle");
            cDescr = intent.getString("cDescr");
            cImage = intent.getString("cImage");

            mTitleEt.setText(cTitile);
            Picasso.get().load(cImage).into(mPostTv);
            mUploadBtn.setText("Update");
        }


        //image click listener
        mPostTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select image"), IMAGE_REQUEST_CODE);

            }
        });

        //btn click listener
        mUploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mUploadBtn.getText().equals("Upload")){
                    //call method to upload data
                    uploadDataToFirebase();
                }
                else
                {
                    beginUpdate();
                }


            }
        });

        //assign FirebaseStoreage instance to storage ref obj
        mStorageReference = FirebaseStorage.getInstance().getReference();
        mdatabaseReference = FirebaseDatabase.getInstance().getReference(mDatabasePath);
        mprogressDialog = new ProgressDialog(AddPostActivity.this);
    }

    private void beginUpdate() {
        mprogressDialog.setMessage("Updating...");
        mprogressDialog.show();
        deletePreviousImage();
    }

    private void deletePreviousImage(){
        StorageReference mPrictureRef = FirebaseStorage.getInstance().getReference(cImage);
        mPrictureRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(AddPostActivity.this, "Upload new photo", Toast.LENGTH_SHORT).show();

                uploadNewImage();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                mprogressDialog.dismiss();
            }
        });
    }

    private void uploadNewImage() {
        String imageName = System.currentTimeMillis()+".png";
        StorageReference storageReference = mStorageReference.child(mStoragePath+ imageName);
        Bitmap bitmap = ((BitmapDrawable)mPostTv.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();
        final UploadTask uploadTask = storageReference.putBytes(data);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Toast.makeText(AddPostActivity.this, "Done", Toast.LENGTH_SHORT).show();
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                Uri downloadUri = uriTask.getResult();
                updateDatabase(downloadUri.toString());
            }
        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        mprogressDialog.dismiss();
                        Toast.makeText(AddPostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateDatabase(final String s) {

        final String title = mTitleEt.getText().toString();
        final String descr = mDescEt.getText().toString();
        FirebaseDatabase mFirebaseDatabse = FirebaseDatabase.getInstance("Data");
        DatabaseReference mRef = mFirebaseDatabse.getReference("Data");

        Query query = mRef.orderByChild("title").equalTo(cTitile);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    ds.getRef().child("title").setValue(title);
                    ds.getRef().child("search").setValue(title.toLowerCase());
                    ds.getRef().child("description").setValue(descr);
                    ds.getRef().child("image").setValue(s);
                }
                mprogressDialog.dismiss();
                Toast.makeText(AddPostActivity.this, "Done", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(AddPostActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void uploadDataToFirebase()  {
        //check whether filepathuri is not empty
        //Toast.makeText(this, ""+mFilePathUri, Toast.LENGTH_SHORT).show();
        if(mFilePathUri != null){
            mprogressDialog.setTitle("Image is Uploading");
            mprogressDialog.show();
            StorageReference storageReference = mStorageReference.child(mStoragePath + System.currentTimeMillis()
                    + "." +getFileExtention(mFilePathUri));

            //adding onSuccess to storageRef
            storageReference.putFile(mFilePathUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!urlTask.isSuccessful());
                            Uri downloadUrl = urlTask.getResult();


                            String mPostTitle = mTitleEt.getText().toString().trim();
                            String mPostDescr = mDescEt.getText().toString().trim();
                            mprogressDialog.dismiss();
                            Toast.makeText(AddPostActivity.this, "Image Uploaded", Toast.LENGTH_SHORT).show();
                            UploadImageInfo imageUploadInfo = new UploadImageInfo(mPostTitle, mPostDescr, downloadUrl.toString(), mPostTitle.toLowerCase());
                            //getting image upload id
                            String imageUploadId = mdatabaseReference.push().getKey();
                            //adding image upload id's child element into databaseRefrence
                            mdatabaseReference.child(imageUploadId).setValue(imageUploadInfo);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mprogressDialog.dismiss();
                            Toast.makeText(AddPostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            mprogressDialog.setTitle("Image is Uploading");
                        }
                    });

        } else {
            Toast.makeText(this, "Nothing to load", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST_CODE
                && requestCode == RESULT_OK
                && data != null
                && data.getData() != null){

            mFilePathUri = data.getData();

            try {
                // getting image
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mFilePathUri);
                //setting bitmap
                mPostTv.setImageBitmap(bitmap);
            }
            catch (Exception e){
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        }
    }


    // method to get the selected image file extention
    private String getFileExtention(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }


}