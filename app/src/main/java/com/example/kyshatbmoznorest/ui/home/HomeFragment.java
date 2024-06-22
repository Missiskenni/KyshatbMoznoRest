package com.example.kyshatbmoznorest.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kyshatbmoznorest.Adapter.FoodAdapter;
import com.example.kyshatbmoznorest.Models.Feedback;
import com.example.kyshatbmoznorest.Models.Food;
import com.example.kyshatbmoznorest.Models.Restaurant;
import com.example.kyshatbmoznorest.Models.User;
import com.example.kyshatbmoznorest.R;
import com.example.kyshatbmoznorest.databinding.FragmentHomeBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    ImageView headImgRest;
    TextView nameRest, tvClickRating;
    String idRest, nameOfRest, photoRest;
    RatingBar ratingRestFood;
    List<Food> listFood = new ArrayList<>();
    FirebaseDatabase db;
    FloatingActionButton fab;
    DatabaseReference food_ref;
    DatabaseReference feed_ref;
    DatabaseReference user_ref;
    DatabaseReference rest_ref;
    FirebaseAuth auth;
    FirebaseUser curUser;
    FoodAdapter foodAdapter;
    RecyclerView rvFood;
    List<Float> rating = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        idRest = "";

        nameRest = view.findViewById(R.id.nameRest);
        ratingRestFood = view.findViewById(R.id.ratingRestFood);
        headImgRest = view.findViewById(R.id.headImgRest);
        tvClickRating = view.findViewById(R.id.tvClickRating);
        rvFood = view.findViewById(R.id.rvFood);

        db = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        curUser = auth.getCurrentUser();
        food_ref = db.getReference("Food");
        rest_ref = db.getReference("Restaurant");
        feed_ref = db.getReference("Feedbacks");
        user_ref = db.getReference("Users").child(curUser.getUid());

        user_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                assert user!=null;

                idRest = user.getIdRest();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        rest_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    Restaurant restaurant = ds.getValue(Restaurant.class);
                    assert restaurant != null;
                    if (restaurant.getId().equals(idRest)) {
                        nameOfRest = restaurant.getName();
                        photoRest = restaurant.getPhotoUriRest();
                        nameRest.setText(nameOfRest);
                        Picasso.get().load(photoRest).into(headImgRest);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        feed_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                float rt = 0;
                if (!rating.isEmpty()) rating.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    Feedback feedback = ds.getValue(Feedback.class);
                    assert feedback!=null;
                    if (feedback.getIdRest().equals(idRest)) rating.add(Float.valueOf(feedback.getRating()));
                }
                rt = countAverage(rating);
                ratingRestFood.setRating(rt);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        rvFood.setLayoutManager(new GridLayoutManager(getContext(), 2));
        foodAdapter = new FoodAdapter(getContext(), listFood);
        rvFood.setAdapter(foodAdapter);

        getDataFromDB();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void getDataFromDB(){
        ValueEventListener vListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!listFood.isEmpty()) listFood.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    Food food = ds.getValue(Food.class);
                    assert food != null;
                    if (food.getIdRest().equals(idRest)) listFood.add(food);
                }
                foodAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        food_ref.addValueEventListener(vListener);
    }

    private float countAverage(List<Float> list){
        float average = 0;
        float total = 0;

        for (int i = 0; i<list.size(); i++){
            total = total + list.get(i);
        }

        average = total/list.size();

        return average;
    }

}