package com.example.virtualwear;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.File;
import java.util.HashMap;


public class AddClothActivity extends AppCompatActivity {

    EditText edtxt_name;
    ImageView imgv_up_cloth;
    Button btn_upload,btn_back;
    Uri image_uri;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_cloth);
        Intent i= getIntent();
        String msg = i.getStringExtra("pass");
        if(msg != null && msg.equals("true"))
            startActivity(new Intent(this,AllClothesActivity.class));
        edtxt_name = findViewById(R.id.edtxt_img_name);
        imgv_up_cloth = findViewById(R.id.imgv_img_upload);
        btn_upload = findViewById(R.id.btn_upload);
        btn_back = findViewById(R.id.btn_back);
        imgv_up_cloth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent,103);
            }
        });

        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                upload_image();
//                if(new File(Environment.getExternalStorageDirectory().getAbsolutePath().toString()+"/Pictures/Title.jpg").exists())
//                    new File(Environment.getExternalStorageDirectory().getAbsolutePath().toString()+"/Pictures/Title.jpg").delete();
            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private void upload_image() {
        if(edtxt_name.getText().toString().equals("")){
            Toast.makeText(this, "Enter Image Description", Toast.LENGTH_SHORT).show();
            return;
        }
        if(image_uri == null) {
            Toast.makeText(this, "Select a Image to Upload", Toast.LENGTH_SHORT).show();
            return;
        }
        ProgressDialog loading = ProgressDialog.show(AddClothActivity.this,"Uploading your Image","Uploading",true,true);
        String fileName = String.valueOf(System.currentTimeMillis());

        StorageReference sReference = FirebaseStorage.getInstance().getReference().child(fileName);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        try {
//            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image_uri);
//            bitmap = Bitmap.createScaledBitmap(bitmap,440,581,true);
//            String path = MediaStore.Images.Media.insertImage(this.getContentResolver(), bitmap, "Title", null);
//            image_uri = Uri.parse(path);
//            imgv_up_cloth.setImageURI(image_uri);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
        String finalFileName = fileName;
        sReference.putFile(image_uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    sReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            HashMap<String,Object>  map = new HashMap<>();
                            map.put("file_name", finalFileName);
                            map.put("img_name",edtxt_name.getText().toString());
                            map.put("img_url" , uri.toString());
                            map.put("email", FirebaseAuth.getInstance().getCurrentUser().getEmail());

                            db.collection("Clothes").add(map).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    if(task.isSuccessful()) {
                                        imgv_up_cloth.setImageResource(R.drawable.notemsg);
                                        edtxt_name.setText("");
                                        loading.dismiss();
                                        Toast.makeText(AddClothActivity.this, "Uploaded Successfully", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(AddClothActivity.this,AllClothesActivity.class));
                                    } else{
                                        loading.dismiss();
                                        Toast.makeText(AddClothActivity.this, "Uploading Failed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });
                }else {
                    loading.dismiss();
                    Toast.makeText(AddClothActivity.this, "Uploading Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == 103 && data != null && data.getData() != null ){
            image_uri = data.getData();
            imgv_up_cloth.setImageURI(image_uri);
        }
    }
}
