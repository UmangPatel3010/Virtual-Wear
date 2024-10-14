package com.example.virtualwear;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AllClothesAdapter extends RecyclerView.Adapter<AllClothesAdapter.MyViewHolder> {
    private Context context;
    private ArrayList<AllClothesModel> img_list;


    public AllClothesAdapter(Context context, ArrayList<AllClothesModel> list_name) {
        this.context = context;
        this.img_list = list_name;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.each_cloth,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Picasso.get().load(img_list.get(position).img_url).into(holder.img_cloth);
        holder.txt_name.setText(img_list.get(position).img_name);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextActivity(position);
            }
        });

        holder.imgb_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(context)
                        .setMessage("Are you sure you want to delete?")
                        .setTitle("Warning!")
                        .setIcon(R.drawable.baseline_warning_24)
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ProgressDialog loading = ProgressDialog.show(context,"Deleting your Image","Deleting",true,true);
                                FirebaseStorage.getInstance().getReference().child(img_list.get(position).file_name)
                                        .delete().addOnCompleteListener(new OnCompleteListener<Void>() {

                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    FirebaseFirestore.getInstance().collection("Clothes")
                                                            .whereEqualTo("file_name",img_list.get(position).file_name)
                                                            .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

                                                                @Override
                                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                    if(task.isSuccessful() && !task.getResult().isEmpty()){
                                                                        String docID = task.getResult().getDocuments().get(0).getId();
                                                                        FirebaseFirestore.getInstance().collection("Clothes")
                                                                                .document(docID).delete()
                                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void unused) {
                                                                                        loading.dismiss();
                                                                                        Toast.makeText(context, "Deleted Sucessfully", Toast.LENGTH_SHORT).show();
                                                                                        Intent i = new Intent(context,AddClothActivity.class);
                                                                                        i.putExtra("pass","true");
                                                                                        context.startActivity(i);
                                                                                    }
                                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                                    @Override
                                                                                    public void onFailure(@NonNull Exception e) {
                                                                                        loading.dismiss();
                                                                                        Toast.makeText(context, "Error while Deleting", Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                });
                                                                    }else{
                                                                        loading.dismiss();
                                                                        Toast.makeText(context, "Deteting Failed", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                            });
                                                }else{
                                                    loading.dismiss();
                                                    Toast.makeText(context, "Deteting Failed", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        }).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return img_list.size();
    }

    public void nextActivity(int position){
        Intent intent = new Intent(context, ClothView.class);
        intent.putExtra("img_url",img_list.get(position).img_url);
        context.startActivity(intent);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView img_cloth;
        ImageButton imgb_del;
        TextView txt_name;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            img_cloth = itemView.findViewById(R.id.item_image);
            txt_name = itemView.findViewById(R.id.item_name);
            imgb_del = itemView.findViewById(R.id.imgb_del);
        }
    }

}
