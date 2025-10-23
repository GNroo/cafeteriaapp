package com.example.cafeteria;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {


    public interface OnItemClickListener {
        void onItemClick(Product product);
        void onItemLongClick(Product product);
        void onBuyClick(Product product);
    }

    private OnItemClickListener listener;
    private List<Product> productList;


    public ProductAdapter(List<Product> productList) {
        this.productList = productList;
    }


    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.name.setText(product.getName());
        holder.description.setText(product.getDescription());
        holder.price.setText(String.format(Locale.getDefault(), "$%.2f", product.getPrice()));


        if (listener != null) {

            holder.itemView.setOnClickListener(v -> listener.onItemClick(product));


            holder.itemView.setOnLongClickListener(v -> {
                listener.onItemLongClick(product);
                return true;
            });


            holder.buyButton.setOnClickListener(v -> listener.onBuyClick(product));
        }
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }


    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView name, description, price;
        Button buyButton;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textViewProductName);
            description = itemView.findViewById(R.id.textViewProductDescription);
            price = itemView.findViewById(R.id.textViewProductPrice);
            buyButton = itemView.findViewById(R.id.buttonBuy);
        }
    }
}