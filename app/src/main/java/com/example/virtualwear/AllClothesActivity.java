package com.example.virtualwear;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class AllClothesActivity extends AppCompatActivity  {
    RecyclerView recyclerView;
    FloatingActionButton fab_add;
    AllClothesAdapter allClothesAdapter;
    ArrayList<AllClothesModel> img_list;
    FirebaseFirestore db;
    FirebaseAuth auth;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_clothes);
        auth = FirebaseAuth.getInstance();

        fab_add = findViewById(R.id.fab_add);
        recyclerView = findViewById(R.id.recycv_all_clothes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        img_list = new ArrayList<>();
        allClothesAdapter = new AllClothesAdapter(this,img_list);
        recyclerView.setAdapter(allClothesAdapter);
        db = FirebaseFirestore.getInstance();
        db.collection("Clothes").whereEqualTo("email","common")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document: task.getResult())
                            {
                                AllClothesModel allClothesModel = document.toObject(AllClothesModel.class);
                                img_list.add(allClothesModel);
                                allClothesAdapter.notifyDataSetChanged();
                            }
                        }else {
                            Toast.makeText(AllClothesActivity.this, "not successful", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        db.collection("Clothes").whereEqualTo("email",FirebaseAuth.getInstance().getCurrentUser().getEmail())
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document: task.getResult())
                            {
                                AllClothesModel allClothesModel = document.toObject(AllClothesModel.class);
                                img_list.add(allClothesModel);
                                allClothesAdapter.notifyDataSetChanged();
                            }
                        }else {
                            Toast.makeText(AllClothesActivity.this, "not successful", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AllClothesActivity.this,AddClothActivity.class);
                startActivity(intent);
            }
        });
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_logout){
            auth.signOut();
            startActivity(new Intent(AllClothesActivity.this,LoginActivity.class));
            finish();
        }
        return true;
    }
}
