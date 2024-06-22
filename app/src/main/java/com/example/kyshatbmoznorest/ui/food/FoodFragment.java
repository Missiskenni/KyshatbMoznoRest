package com.example.kyshatbmoznorest.ui.food;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.kyshatbmoznorest.Models.Food;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FoodFragment extends Fragment {

    Button btnCreateFood, btnChangeFood, btnDeleteFood;
    String idRest, idFood, strUri;
    List<Food> foods = new ArrayList<>();
    List<String> namesFood = new ArrayList<>();
    FirebaseDatabase db;
    FirebaseAuth auth;
    FirebaseUser curUser;
    DatabaseReference food_ref, user_ref;
    Uri photoUriFood;
    ImageView photoFood;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_food, container, false);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        idRest = "";

        btnCreateFood = view.findViewById(R.id.btnCreateFood);
        btnCreateFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddFood();
            }
        });
        btnChangeFood = view.findViewById(R.id.btnChangeFood);
        btnChangeFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChangeFood();
            }
        });
        btnDeleteFood = view.findViewById(R.id.btnDeleteFood);
        btnDeleteFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteFood();
            }
        });

        db = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        curUser = auth.getCurrentUser();
        food_ref = db.getReference("Food");
        user_ref = db.getReference("Users").child(curUser.getUid());

        user_ref.addValueEventListener(new ValueEventListener() {
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

        food_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!foods.isEmpty()) foods.clear();
                if (!namesFood.isEmpty()) namesFood.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    Food food = ds.getValue(Food.class);

                    assert food != null;
                    if (food.getIdRest().equals(idRest)) {
                        foods.add(food);
                        namesFood.add(food.getName());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void showAddFood(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("Добавить еду");

        photoUriFood = Uri.parse("android.resource://com.example.kyshatbmoznorest/" + R.drawable.food_def);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View addFood = inflater.inflate(R.layout.add_food, null);
        dialog.setView(addFood);

        final EditText nameOfFood = addFood.findViewById(R.id.nameOfFood);
        final EditText compOfFood = addFood.findViewById(R.id.compOfFood);
        final EditText priceOfFood = addFood.findViewById(R.id.priceOfFood);
        final EditText weightOfFood = addFood.findViewById(R.id.weightOfFood);
        final EditText catOfFood = addFood.findViewById(R.id.catOfFood);
        photoFood = addFood.findViewById(R.id.photoFood);
        final Button btnPhotoOfFood  = addFood.findViewById(R.id.btnPhotoOfFood);

        btnPhotoOfFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getImage();
            }
        });

        dialog.setNegativeButton("Отменить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.setPositiveButton("Добавить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Food newFood = new Food();
                String id, name, price, composition, weight, category;

                id = food_ref.push().getKey();
                name = nameOfFood.getText().toString();
                price = priceOfFood.getText().toString();
                composition = compOfFood.getText().toString();
                weight = weightOfFood.getText().toString();
                category = catOfFood.getText().toString();

                if (name.isEmpty()){
                    Toast.makeText(getActivity(), "Укажите название еды", Toast.LENGTH_SHORT).show();
                } else if (price.isEmpty()) {
                    Toast.makeText(getActivity(), "Укажите цену еды", Toast.LENGTH_SHORT).show();
                } else if (composition.isEmpty()) {
                    Toast.makeText(getActivity(), "Укажите состав еды", Toast.LENGTH_SHORT).show();
                }else if (weight.isEmpty()){
                    Toast.makeText(getActivity(), "Укажите вес еды", Toast.LENGTH_SHORT).show();
                } else if (category.isEmpty()) {
                    Toast.makeText(getActivity(), "Укажите категорию еды", Toast.LENGTH_SHORT).show();
                }else {
                    StorageReference storageReference = FirebaseStorage.getInstance()
                            .getReference().child("Food/"+id+"/photoOfFood.jpg");
                    storageReference.putFile(photoUriFood).addOnCompleteListener(command -> {
                        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            newFood.setId(id);
                            newFood.setIdRest(idRest);
                            newFood.setName(name);
                            newFood.setPrice(price);
                            newFood.setComposition(composition);
                            newFood.setWeight(weight);
                            newFood.setCategory(category);
                            newFood.setPhotoUriFood(uri.toString());

                            food_ref.child(id).setValue(newFood);
                            Toast.makeText(getView().getContext(), "Еда добавлена!", Toast.LENGTH_SHORT).show();
                        });
                    });
                }
            }
        });

        dialog.show();

    }
    private void showChangeFood(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("Изменить еду");

        photoUriFood = null;

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View changeFood = inflater.inflate(R.layout.change_food, null);
        dialog.setView(changeFood);

        final Spinner spinFood = changeFood.findViewById(R.id.spinFood);
        final EditText nameOfFood = changeFood.findViewById(R.id.nameOfFoodChange);
        final EditText compOfFood = changeFood.findViewById(R.id.compOfFoodChange);
        final EditText priceOfFood = changeFood.findViewById(R.id.priceOfFoodChange);
        final EditText weightOfFood = changeFood.findViewById(R.id.weightOfFoodChange);
        final EditText catOfFood = changeFood.findViewById(R.id.catOfFoodChange);
        photoFood = changeFood.findViewById(R.id.photoFoodChange);
        final Button btnPhotoOfFood  = changeFood.findViewById(R.id.btnPhotoOfFoodChange);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, namesFood);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinFood.setAdapter(adapter);

        spinFood.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                long pos = spinFood.getSelectedItemId();
                idFood = foods.get(Math.toIntExact(pos)).getId();
                nameOfFood.setText(foods.get(Math.toIntExact(pos)).getName());
                compOfFood.setText(foods.get(Math.toIntExact(pos)).getComposition());
                priceOfFood.setText(foods.get(Math.toIntExact(pos)).getPrice());
                weightOfFood.setText(foods.get(Math.toIntExact(pos)).getWeight());
                catOfFood.setText(foods.get(Math.toIntExact(pos)).getCategory());
                strUri = foods.get(Math.toIntExact(pos)).getPhotoUriFood();
                Picasso.get().load(strUri).into(photoFood);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btnPhotoOfFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getImage();
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
                Food newFood = new Food();
                String id, name, price, composition, weight, category;

                id = idFood;
                name = nameOfFood.getText().toString();
                price = priceOfFood.getText().toString();
                composition = compOfFood.getText().toString();
                weight = weightOfFood.getText().toString();
                category = catOfFood.getText().toString();
                if (name.isEmpty()){
                    Toast.makeText(getActivity(), "Укажите название еды", Toast.LENGTH_SHORT).show();
                } else if (price.isEmpty()) {
                    Toast.makeText(getActivity(), "Укажите цену еды", Toast.LENGTH_SHORT).show();
                } else if (composition.isEmpty()) {
                    Toast.makeText(getActivity(), "Укажите состав еды", Toast.LENGTH_SHORT).show();
                }else if (weight.isEmpty()){
                    Toast.makeText(getActivity(), "Укажите вес еды", Toast.LENGTH_SHORT).show();
                } else if (category.isEmpty()) {
                    Toast.makeText(getActivity(), "Укажите категорию еды", Toast.LENGTH_SHORT).show();
                }else{
                    if (photoUriFood!=null&&!photoUriFood.toString().isEmpty()){
                        StorageReference storageReference = FirebaseStorage.getInstance()
                                .getReference().child("Food/"+id+"/photoOfFood.jpg");
                        storageReference.putFile(photoUriFood).addOnCompleteListener(command -> {
                            storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                                newFood.setId(id);
                                newFood.setIdRest(idRest);
                                newFood.setName(name);
                                newFood.setPrice(price);
                                newFood.setComposition(composition);
                                newFood.setWeight(weight);
                                newFood.setCategory(category);
                                newFood.setPhotoUriFood(uri.toString());

                                food_ref.child(id).setValue(newFood);
                                Toast.makeText(getView().getContext(), "Еда изменена!", Toast.LENGTH_SHORT).show();
                            });
                        });
                    }
                    else {
                        newFood.setId(id);
                        newFood.setIdRest(idRest);
                        newFood.setName(name);
                        newFood.setPrice(price);
                        newFood.setComposition(composition);
                        newFood.setWeight(weight);
                        newFood.setCategory(category);
                        newFood.setPhotoUriFood(strUri);

                        food_ref.child(id).setValue(newFood);
                        Toast.makeText(getView().getContext(), "Еда изменена!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        dialog.show();

    }
    private void showDeleteFood(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("Удалить еду");

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View deleteFood = inflater.inflate(R.layout.delete_food, null);
        dialog.setView(deleteFood);

        final Spinner spinFoodDelete = deleteFood.findViewById(R.id.spinFoodDelete);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, namesFood);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinFoodDelete.setAdapter(adapter);

        dialog.setNegativeButton("Отменить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                long pos = spinFoodDelete.getSelectedItemId();
                Food selFood = foods.get((int) pos);
                food_ref.child(selFood.getId()).removeValue();
                Toast.makeText(getView().getContext(), "Еда удалена!", Toast.LENGTH_SHORT).show();
                spinFoodDelete.setSelection(0);
            }
        });

        dialog.show();

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && data != null && data.getData() != null){
            photoUriFood = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), photoUriFood);
                photoFood.setImageBitmap(bitmap);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Toast.makeText(getView().getContext(), "Фото выбрано!", Toast.LENGTH_SHORT).show();
        }
    }

    public void getImage(){
        Intent intentChooser = new Intent();
        intentChooser.setType("image/");
        intentChooser.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intentChooser, 1);
    }

}