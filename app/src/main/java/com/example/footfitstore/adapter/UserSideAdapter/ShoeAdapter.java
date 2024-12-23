package com.example.footfitstore.adapter.UserSideAdapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.footfitstore.R;
import com.example.footfitstore.Utils.CustomDialog;
import com.example.footfitstore.activity.User.ProductDetailActivity;
import com.example.footfitstore.fragment.User.FavouriteFragment;
import com.example.footfitstore.model.Promotion;
import com.example.footfitstore.model.Shoe;
import com.example.footfitstore.fragment.User.ExploreFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ShoeAdapter extends RecyclerView.Adapter<ShoeAdapter.ItemViewHolder> {
    private List<Shoe> popularShoesList;
    private List<Shoe> allShoesList;
    private Context context;
    private String viewType;
    private ExploreFragment exploreFragment;
    private BottomSheetListener listener;
    private FavouriteFragment favouriteFragment;
    public interface BottomSheetListener{
        void showBottomSheetDialog(Shoe shoe);
    }
    public ShoeAdapter(Context context, List<Shoe> popularShoesList, List<Shoe> allShoesList, String viewType, ExploreFragment exploreFragment,BottomSheetListener listener) {
        this.context = context;
        this.popularShoesList = popularShoesList;
        this.allShoesList = allShoesList;
        this.viewType = viewType;
        this.exploreFragment = exploreFragment;
        this.listener=listener;
    }
    public ShoeAdapter(Context context, List<Shoe> popularShoesList, List<Shoe> allShoesList, String viewType, FavouriteFragment favouriteFragment,BottomSheetListener listener) {
        this.context = context;
        this.popularShoesList = popularShoesList;
        this.allShoesList = allShoesList;
        this.viewType = viewType;
        this.favouriteFragment = favouriteFragment;
        this.listener=listener;
    }
    public ShoeAdapter(Context context, List<Shoe> popularShoesList, List<Shoe> allShoesList, String viewType) {
        this.context = context;
        this.popularShoesList = popularShoesList;
        this.allShoesList = allShoesList;
        this.viewType = viewType;
    }
    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_shoe, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Shoe shoe;
        if (viewType.equals("popular")) {
            shoe = popularShoesList.get(position);
        } else {
            shoe = allShoesList.get(position);
        }

        double finalPrice = shoe.getPrice();
        Promotion promotion = shoe.getPromotion();

        if (promotion != null && isPromotionActive(promotion)) {
            finalPrice = shoe.getPrice() * (1 - promotion.getDiscount() / 100.0);
        }

        holder.titleTextView.setText(shoe.getTitle());
        holder.priceTextView.setText("$" + String.format("%.2f", finalPrice));

        if (shoe.getPicUrl() != null && !shoe.getPicUrl().isEmpty()) {
            Picasso.get().load(shoe.getPicUrl().get(0)).into(holder.itemImageView);
        }
        if (shoe.isFavourite()) {
            holder.heartIcon.setImageResource(R.drawable.ic_heart_filled);
        } else {
            holder.heartIcon.setImageResource(R.drawable.ic_heart);
        }

        holder.heartIcon.setOnClickListener(v -> {
            toggleFavourite(shoe, holder);
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("productId", shoe.getProductId());
            context.startActivity(intent);
        });
        holder.btnAddCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.showBottomSheetDialog(shoe);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if ("popular".equals(viewType) && popularShoesList != null) {
            return popularShoesList.size();
        } else if (allShoesList != null) {
            return allShoesList.size();
        } else {
            return 0;
        }
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImageView, heartIcon;
        TextView titleTextView, priceTextView;
        ImageButton btnAddCart;
        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImageView = itemView.findViewById(R.id.itemImage);
            titleTextView = itemView.findViewById(R.id.itemTitle);
            priceTextView = itemView.findViewById(R.id.itemPrice);
            heartIcon = itemView.findViewById(R.id.heartIcon);
            btnAddCart=itemView.findViewById(R.id.addToCartButton);
        }
    }

    private void toggleFavourite(Shoe shoe, ItemViewHolder holder) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            new CustomDialog(context)
                    .setTitle("Failed")
                    .setMessage("Failed To Get Data.")
                    .setIcon(R.drawable.error)
                    .setPositiveButton("OK", null)
                    .hideNegativeButton()
                    .show();
            return;
        }

        String uid = user.getUid();
        DatabaseReference userFavouriteRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(uid)
                .child("favourite");

        if (shoe.isFavourite()) {
            userFavouriteRef.child(shoe.getProductId()).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        shoe.setFavourite(false);
                        holder.heartIcon.setImageResource(R.drawable.ic_heart);
                        syncFavouriteStatus(shoe);
                    });
        } else {
            Map<String, Object> favouriteItem = new HashMap<>();
            favouriteItem.put("productId", shoe.getProductId());
            favouriteItem.put("title", shoe.getTitle());

            userFavouriteRef.child(shoe.getProductId()).setValue(favouriteItem)
                    .addOnSuccessListener(aVoid -> {
                        shoe.setFavourite(true);
                        holder.heartIcon.setImageResource(R.drawable.ic_heart_filled);
                        syncFavouriteStatus(shoe);
                    });
        }
    }


    private void syncFavouriteStatus(Shoe shoe) {
        if (allShoesList != null) {
            for (Shoe item : allShoesList) {
                if (item.getProductId().equals(shoe.getProductId())) {
                    item.setFavourite(shoe.isFavourite());
                    break;
                }
            }
        }

        if (popularShoesList != null) {
            for (Shoe item : popularShoesList) {
                if (item.getProductId().equals(shoe.getProductId())) {
                    item.setFavourite(shoe.isFavourite());
                    break;
                }
            }
        }

        if (exploreFragment != null) {
            exploreFragment.updateAdapters();
        }
    }

    private boolean isPromotionActive(Promotion promotion) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date startDate = sdf.parse(promotion.getStartDate());
            Date endDate = sdf.parse(promotion.getEndDate());
            Date today = new Date();
            today = resetTime(today);
            return (today.after(startDate) || today.equals(startDate)) && (today.before(endDate) || today.equals(endDate));
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }
    private static Date resetTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}
