package com.example.virtualwear;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;

public class ClothView extends AppCompatActivity {
    ImageView imgv_cloth_view ;
    Button btn_go,btn_back;
    String img_url;
    FirebaseFirestore db;
    String fileName;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.clothes_view);

        imgv_cloth_view = findViewById(R.id.imgv_cloth_view);
        btn_back = findViewById(R.id.btn_back);
        btn_go = findViewById(R.id.btn_go);
        Intent intent = getIntent();
        img_url = intent.getStringExtra("img_url");
        db = FirebaseFirestore.getInstance();
        db.collection("Clothes").whereEqualTo("img_url",img_url)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document: task.getResult())
                            {
                                AllClothesModel allClothesModel = document.toObject(AllClothesModel.class);
                                fileName = allClothesModel.file_name;
                            }
                        }else {
                            Toast.makeText(ClothView.this, "not successful", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        Picasso.get().load(img_url).into(imgv_cloth_view);

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btn_go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                next_activity();
            }
        });
    }
    protected void next_activity(){
        Intent intent = new Intent(getApplicationContext(), LiveFeeding.class);
        intent.putExtra("file_name",fileName);
        startActivity(intent);
    }
}
