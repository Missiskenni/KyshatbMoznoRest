package com.example.kyshatbmoznorest.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kyshatbmoznorest.Models.Order;
import com.example.kyshatbmoznorest.Models.Restaurant;
import com.example.kyshatbmoznorest.Models.User;
import com.example.kyshatbmoznorest.OneOrderActivity;
import com.example.kyshatbmoznorest.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class OrdersAdapter extends RecyclerView.Adapter<OrderViewHolder>{

    Context context;
    List<Order> orderList;

    public OrdersAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new OrderViewHolder(LayoutInflater.from(context).inflate(R.layout.recycle_item_order, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference userRef = db.getReference("Users");

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    User user = ds.getValue(User.class);
                    assert user != null;
                    if (orderList.get(position).getIdUser().equals(user.getId())) holder.tvUserOrder.setText(user.getName());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.tvPriceOrder.setText(orderList.get(position).getPrice());
        holder.tvTimeOrder.setText(orderList.get(position).getDate());

        holder.rlOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, OneOrderActivity.class);
                intent.putExtra("id",orderList.get(position).getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }
}

class OrderViewHolder extends RecyclerView.ViewHolder{

    TextView tvUserOrder, tvPriceOrder, tvTimeOrder;
    RelativeLayout rlOrder;

    public OrderViewHolder(@NonNull View itemView) {
        super(itemView);

        tvUserOrder = itemView.findViewById(R.id.tvUserOrder);
        tvPriceOrder = itemView.findViewById(R.id.tvPriceOrder);
        tvTimeOrder = itemView.findViewById(R.id.tvTimeOrder);
        rlOrder = itemView.findViewById(R.id.rlOrder);
    }


}