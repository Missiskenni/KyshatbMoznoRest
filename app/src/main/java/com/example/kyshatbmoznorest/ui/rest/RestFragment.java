package com.example.kyshatbmoznorest.ui.rest;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.kyshatbmoznorest.Models.Restaurant;
import com.example.kyshatbmoznorest.Models.User;
import com.example.kyshatbmoznorest.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class RestFragment extends Fragment {

    ImageView ivRestPhoto;
    TextView tvNameRest, tvDescRest;
    Button btnChangeRestInfo, btnChangeRestPhoto;
    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference restRef, userRef;
    FirebaseUser curUser;
    Uri photoUriFood;
    String idRest;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_rest, container, false);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        idRest = "";

        auth = FirebaseAuth.getInstance();
        curUser = auth.getCurrentUser();
        db = FirebaseDatabase.getInstance();
        restRef = db.getReference("Restaurant");
        userRef = db.getReference("Users").child(curUser.getUid());

        ivRestPhoto = view.findViewById(R.id.ivRestPhoto);
        tvNameRest = view.findViewById(R.id.tvNameRest);
        tvDescRest = view.findViewById(R.id.tvDescRest);
        btnChangeRestInfo = view.findViewById(R.id.btnChangeRestInfo);
        btnChangeRestInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChangeWindow();
            }
        });
        btnChangeRestPhoto = view.findViewById(R.id.btnChangeRestPhoto);
        btnChangeRestPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getImage();
            }
        });

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                assert user != null;
                idRest = user.getIdRest();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        restRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds:snapshot.getChildren()){
                    Restaurant restaurant = ds.getValue(Restaurant.class);
                    assert restaurant != null;
                    if (restaurant.getId().equals(idRest)) {
                        tvNameRest.setText(restaurant.getName());
                        tvDescRest.setText(restaurant.getDescription());
                        Picasso.get().load(restaurant.getPhotoUriRest()).into(ivRestPhoto);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void showChangeWindow(){

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle("Изменение данных");

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View changeInfoWindow = inflater.inflate(R.layout.change_restdata, null);
        dialog.setView(changeInfoWindow);

        final EditText changeName = changeInfoWindow.findViewById(R.id.changeName);
        final EditText changeDisc = changeInfoWindow.findViewById(R.id.changeDisc);

        restRef.child(idRest).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Restaurant restaurant = snapshot.getValue(Restaurant.class);

                assert restaurant != null;
                changeName.setText(restaurant.getName());
                changeDisc.setText(restaurant.getDescription());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        dialog.setNegativeButton("Отменить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.setPositiveButton("Изменить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String name = changeName.getText().toString();
                String disc = changeDisc.getText().toString();

                if (name.isEmpty()){
                    Toast.makeText(getActivity(), "Введите название ресторана", Toast.LENGTH_SHORT).show();
                } else if (disc.isEmpty()) {
                    Toast.makeText(getActivity(), "Введите описание ресторана", Toast.LENGTH_SHORT).show();
                }else {
                    restRef.child(idRest).child("name").setValue(name);
                    restRef.child(idRest).child("description").setValue(disc);
                    Toast.makeText(getActivity(), "Данные изменены!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && data != null && data.getData() != null){
            photoUriFood = data.getData();
            StorageReference storageReference = FirebaseStorage.getInstance()
                    .getReference().child("Restaurants/"+idRest+"/photoOfRest.jpg");
            storageReference.putFile(photoUriFood).addOnCompleteListener(command -> {
                storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    restRef.child(idRest).child("photoUriRest").setValue(uri.toString());
                    Toast.makeText(getView().getContext(), "Фото изменено!", Toast.LENGTH_SHORT).show();
                });
            });

        }
    }

    public void getImage(){
        Intent intentChooser = new Intent();
        intentChooser.setType("image/");
        intentChooser.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intentChooser, 1);
    }

}