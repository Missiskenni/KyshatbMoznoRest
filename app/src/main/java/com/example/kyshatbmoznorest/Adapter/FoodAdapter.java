package com.example.kyshatbmoznorest.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kyshatbmoznorest.Models.Cart;
import com.example.kyshatbmoznorest.Models.Food;
import com.example.kyshatbmoznorest.R;
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

public class FoodAdapter extends RecyclerView.Adapter<FoodViewHolder>{

    Context context;
    List<Food> foodList;

    public FoodAdapter(Context context, List<Food> foodList) {
        this.context = context;
        this.foodList = foodList;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FoodViewHolder(LayoutInflater.from(context).inflate(R.layout.recycle_item_food, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser curUser = auth.getCurrentUser();
        DatabaseReference cartRef = db.getReference("Carts");

        holder.foodName.setText(foodList.get(position).getName());
        holder.foodPrice.setText(foodList.get(position).getPrice()+"р.");
        holder.foodWeight.setText(foodList.get(position).getWeight()+"г.");
        Picasso.get().load(foodList.get(position).getPhotoUriFood()).into(holder.photoOfFood);

        List<Cart> carts = new ArrayList<>();


        cartRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (carts.size()>0) carts.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    Cart cart = ds.getValue(Cart.class);
                    if (cart.getIdUser().equals(curUser.getUid())) carts.add(cart);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }
}

class FoodViewHolder extends RecyclerView.ViewHolder{

    TextView foodPrice, foodName, foodWeight;
    ImageView photoOfFood;

    public FoodViewHolder(@NonNull View itemView) {
        super(itemView);

        foodPrice = itemView.findViewById(R.id.foodPrice);
        foodName = itemView.findViewById(R.id.foodName);
        foodWeight = itemView.findViewById(R.id.foodWeight);
        photoOfFood = itemView.findViewById(R.id.photoOfFood);
    }
}
