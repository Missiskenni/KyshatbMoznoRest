package com.example.kyshatbmoznorest;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kyshatbmoznorest.Adapter.OrderCartAdapter;
import com.example.kyshatbmoznorest.Models.FoodInCart;
import com.example.kyshatbmoznorest.Models.Order;
import com.example.kyshatbmoznorest.Models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class OneOrderActivity extends AppCompatActivity {

    String idOrder;
    String idUserOrder, idUser;
    TextView tvAddress, tvDate, tvStatus, tvPhone, tvTotalPrice, tvUserNameOrder;
    RecyclerView rvCartInOrder;
    FirebaseDatabase db;
    DatabaseReference ordRef, usersRef;

    FirebaseAuth auth;
    FirebaseUser curUser;
    List<FoodInCart> foodInCartList = new ArrayList<>();
    OrderCartAdapter orderCartAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_order);

        auth = FirebaseAuth.getInstance();
        curUser = auth.getCurrentUser();
        idUser = curUser.getUid();

        idOrder = getIntent().getStringExtra("id");

        tvAddress = findViewById(R.id.tvAddress);
        tvUserNameOrder = findViewById(R.id.tvUserNameOrder);
        tvDate = findViewById(R.id.tvDate);
        tvStatus = findViewById(R.id.tvStatus);
        rvCartInOrder = findViewById(R.id.rvCartInOrder);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvPhone = findViewById(R.id.tvPhone);

        rvCartInOrder.setLayoutManager(new LinearLayoutManager(this));
        orderCartAdapter = new OrderCartAdapter(this, foodInCartList);
        rvCartInOrder.setAdapter(orderCartAdapter);

        db = FirebaseDatabase.getInstance();
        ordRef = db.getReference("Orders").child(idOrder);
        usersRef = db.getReference("Users");

        ordRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Order order = snapshot.getValue(Order.class);

                assert order != null;
                tvAddress.setText(order.getAddress());
                tvDate.setText(order.getDate());
                tvStatus.setText(order.getStatus());
                tvTotalPrice.setText(order.getPrice());
                idUserOrder = order.getIdUser();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    User user = ds.getValue(User.class);
                    assert user != null;
                    if (user.getId().equals(idUserOrder)) {
                        tvUserNameOrder.setText(user.getName());
                        tvPhone.setText(user.getPhoneNumber());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        ordRef.child("orderList").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!foodInCartList.isEmpty()) foodInCartList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    FoodInCart foodInCart = ds.getValue(FoodInCart.class);
                    assert foodInCart!=null;
                    foodInCartList.add(foodInCart);
                }
                orderCartAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}