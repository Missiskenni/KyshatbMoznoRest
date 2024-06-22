package com.example.kyshatbmoznorest.ui.orders;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.kyshatbmoznorest.Adapter.OrdersAdapter;
import com.example.kyshatbmoznorest.Models.Order;
import com.example.kyshatbmoznorest.Models.User;
import com.example.kyshatbmoznorest.R;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrdersFragment extends Fragment {
    TextView ordersNow, ordersPast, tvGetDate, ordersOnTheWay;
    LinearLayout llord2;
    RecyclerView rvOrders;
    OrdersAdapter ordersAdapter;
    OrdersAdapter ordersOnWayAdapter;
    OrdersAdapter orderPastsAdapter;
    OrdersAdapter orderSortedPastsAdapter;
    List<Order> orderList = new ArrayList<>();
    List<Order> pastOrderList = new ArrayList<>();
    List<Order> onWayOrderList = new ArrayList<>();
    List<Order> sortedPastOrderList = new ArrayList<>();
    String idRest;
    FirebaseAuth auth;
    FirebaseUser curUser;
    FirebaseDatabase db;
    DatabaseReference ordRef, userRef;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_orders, container, false);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        idRest = "";
        auth = FirebaseAuth.getInstance();
        curUser = auth.getCurrentUser();
        db = FirebaseDatabase.getInstance();
        ordRef = db.getReference("Orders");
        userRef = db.getReference("Users").child(curUser.getUid());

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

        ordersNow = view.findViewById(R.id.ordersNow);
        ordersPast = view.findViewById(R.id.ordersPast);
        ordersOnTheWay = view.findViewById(R.id.ordersOnTheWay);
        rvOrders = view.findViewById(R.id.rvOrders);
        llord2 = view.findViewById(R.id.llord2);
        tvGetDate = view.findViewById(R.id.tvGetDate);

        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        ordersAdapter = new OrdersAdapter(getContext(), orderList);
        orderPastsAdapter = new OrdersAdapter(getContext(), pastOrderList);
        ordersOnWayAdapter = new OrdersAdapter(getContext(), onWayOrderList);
        orderSortedPastsAdapter = new OrdersAdapter(getContext(), sortedPastOrderList);

        rvOrders.setAdapter(ordersAdapter);

        tvGetDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog();
            }
        });

        ordRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!orderList.isEmpty()) orderList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    Order order = ds.getValue(Order.class);
                    assert order != null;
                    if (order.getIdRest().equals(idRest)){
                        if (order.getStatus().equals("Принят")) orderList.add(order);
                        else if (order.getStatus().equals("В пути")) onWayOrderList.add(order);
                        else pastOrderList.add(order);
                    }
                }
                ordersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ordersNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rvOrders.setAdapter(ordersAdapter);
                ordersNow.setBackgroundColor(getResources().getColor(R.color.yellow));
                ordersPast.setBackgroundColor(getResources().getColor(R.color.white));
                ordersOnTheWay.setBackgroundColor(getResources().getColor(R.color.white));
                llord2.setVisibility(View.GONE);
            }
        });
        ordersOnTheWay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rvOrders.setAdapter(ordersOnWayAdapter);
                ordersNow.setBackgroundColor(getResources().getColor(R.color.white));
                ordersPast.setBackgroundColor(getResources().getColor(R.color.white));
                ordersOnTheWay.setBackgroundColor(getResources().getColor(R.color.yellow));
                llord2.setVisibility(View.GONE);
            }
        });
        ordersPast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rvOrders.setAdapter(orderPastsAdapter);
                ordersNow.setBackgroundColor(getResources().getColor(R.color.white));
                ordersPast.setBackgroundColor(getResources().getColor(R.color.yellow));
                ordersOnTheWay.setBackgroundColor(getResources().getColor(R.color.white));
                tvGetDate.setText("Выберите диапозон дат");
                llord2.setVisibility(View.VISIBLE);
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void datePickerDialog() {
        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText("Выберите диапозон дат");

        MaterialDatePicker<Pair<Long, Long>> datePicker = builder.build();
        datePicker.addOnPositiveButtonClickListener(selection -> {

            if (!sortedPastOrderList.isEmpty()) sortedPastOrderList.clear();

            Long startDate = selection.first;
            Long endDate = selection.second;

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String startDateString = sdf.format(new Date(startDate));
            String endDateString = sdf.format(new Date(endDate));

            String selectedDateRange = startDateString + " - " + endDateString;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate start = LocalDate.parse(startDateString, formatter);
            LocalDate end = LocalDate.parse(endDateString, formatter);

            for (int i=0;i<pastOrderList.size();i++){
                String orderDate = pastOrderList.get(i).getDate();
                int indEnd = orderDate.indexOf(" ");
                String date = orderDate.substring(0, indEnd);
                LocalDate localDateOrder = LocalDate.parse(date, formatter);
                if (localDateOrder.isAfter(start)&&localDateOrder.isBefore(end)
                        ||localDateOrder.isEqual(start)||localDateOrder.isEqual(end)) sortedPastOrderList.add(pastOrderList.get(i));
            }

            rvOrders.setAdapter(orderSortedPastsAdapter);
            tvGetDate.setText(selectedDateRange);
        });

        // Showing the date picker dialog
        datePicker.show(getParentFragmentManager(), "DATE_PICKER");
    }
}