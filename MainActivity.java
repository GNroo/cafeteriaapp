package com.example.cafeteria;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements ProductAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> productList = new ArrayList<>();
    private OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerViewProducts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ProductAdapter(productList);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(this);

        fetchProducts();

        findViewById(R.id.fabAddProduct).setOnClickListener(view -> showAddProductDialog());
    }



    @Override
    public void onItemClick(Product product) {

        showEditProductDialog(product);
    }

    @Override
    public void onItemLongClick(Product product) {

        showDeleteConfirmationDialog(product);
    }

    @Override
    public void onBuyClick(Product product) {

        recordPurchaseInDatabase(product, 1);
    }



    private void fetchProducts() {
        String url = "http://10.0.2.2/cafeteria_api/get_products.php";
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) { Log.e("MainActivity", "Error al obtener productos", e); }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseData = response.body().string();
                    try {
                        JSONArray jsonArray = new JSONArray(responseData);
                        productList.clear();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            Product product = new Product();
                            product.setId(jsonObject.getInt("id"));
                            product.setName(jsonObject.getString("name"));
                            product.setDescription(jsonObject.getString("description"));
                            product.setPrice(jsonObject.getDouble("price"));
                            productList.add(product);
                        }
                        runOnUiThread(() -> adapter.notifyDataSetChanged());
                    } catch (JSONException e) { Log.e("MainActivity", "Error al parsear JSON", e); }
                }
            }
        });
    }

    private void showAddProductDialog() {
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_product, null);
        final EditText editTextName = dialogView.findViewById(R.id.editTextProductName);
        final EditText editTextDescription = dialogView.findViewById(R.id.editTextProductDescription);
        final EditText editTextPrice = dialogView.findViewById(R.id.editTextProductPrice);
        new AlertDialog.Builder(this)
                .setView(dialogView).setTitle("Nuevo Producto").setPositiveButton("Agregar", (dialog, which) -> {
                    String name = editTextName.getText().toString().trim();
                    String description = editTextDescription.getText().toString().trim();
                    String priceStr = editTextPrice.getText().toString().trim();
                    if (name.isEmpty() || priceStr.isEmpty()) { Toast.makeText(this, "El nombre y el precio son obligatorios", Toast.LENGTH_SHORT).show(); return; }
                    try { double price = Double.parseDouble(priceStr); addProductToDatabase(name, description, price); } catch (NumberFormatException e) { Toast.makeText(this, "Por favor, introduce un precio válido", Toast.LENGTH_SHORT).show(); }
                }).setNegativeButton("Cancelar", null).show();
    }

    private void addProductToDatabase(String name, String description, double price) {
        String url = "http://10.0.2.2/cafeteria_api/add_product.php";
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        JSONObject jsonObject = new JSONObject();
        try { jsonObject.put("name", name); jsonObject.put("description", description); jsonObject.put("price", price); } catch (JSONException e) { e.printStackTrace(); return; }
        RequestBody requestBody = RequestBody.create(jsonObject.toString(), JSON);
        Request request = new Request.Builder().url(url).post(requestBody).build();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) { runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error de red", Toast.LENGTH_SHORT).show()); }
            @Override public void onResponse(Call call, Response response) { if (response.isSuccessful()) { runOnUiThread(() -> { Toast.makeText(MainActivity.this, "Producto agregado", Toast.LENGTH_SHORT).show(); fetchProducts(); }); } else { runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error del servidor", Toast.LENGTH_SHORT).show()); } }
        });
    }



    private void showEditProductDialog(final Product product) {
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_product, null);
        final EditText editTextName = dialogView.findViewById(R.id.editTextProductName);
        final EditText editTextDescription = dialogView.findViewById(R.id.editTextProductDescription);
        final EditText editTextPrice = dialogView.findViewById(R.id.editTextProductPrice);
        editTextName.setText(product.getName()); editTextDescription.setText(product.getDescription()); editTextPrice.setText(String.valueOf(product.getPrice()));
        new AlertDialog.Builder(this)
                .setView(dialogView).setTitle("Editar Producto").setPositiveButton("Guardar Cambios", (dialog, which) -> {
                    String name = editTextName.getText().toString().trim();
                    String description = editTextDescription.getText().toString().trim();
                    String priceStr = editTextPrice.getText().toString().trim();
                    if (name.isEmpty() || priceStr.isEmpty()) { Toast.makeText(this, "El nombre y el precio son obligatorios", Toast.LENGTH_SHORT).show(); return; }
                    try { double price = Double.parseDouble(priceStr); updateProductInDatabase(product.getId(), name, description, price); } catch (NumberFormatException e) { Toast.makeText(this, "Por favor, introduce un precio válido", Toast.LENGTH_SHORT).show(); }
                }).setNegativeButton("Cancelar", null).show();
    }

    private void updateProductInDatabase(int id, String name, String description, double price) {
        String url = "http://10.0.2.2/cafeteria_api/update_product.php";
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        JSONObject jsonObject = new JSONObject();
        try { jsonObject.put("id", id); jsonObject.put("name", name); jsonObject.put("description", description); jsonObject.put("price", price); } catch (JSONException e) { e.printStackTrace(); return; }
        RequestBody requestBody = RequestBody.create(jsonObject.toString(), JSON);
        Request request = new Request.Builder().url(url).post(requestBody).build();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) { runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error de red al actualizar", Toast.LENGTH_SHORT).show()); }
            @Override public void onResponse(Call call, Response response) { if (response.isSuccessful()) { runOnUiThread(() -> { Toast.makeText(MainActivity.this, "Producto actualizado", Toast.LENGTH_SHORT).show(); fetchProducts(); }); } else { runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error del servidor al actualizar", Toast.LENGTH_SHORT).show()); } }
        });
    }



    private void showDeleteConfirmationDialog(final Product product) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Producto")
                .setMessage("¿Estás seguro de que deseas eliminar '" + product.getName() + "'? Esta acción no se puede deshacer.")
                .setPositiveButton("Sí, eliminar", (dialog, which) -> deleteProductFromDatabase(product))
                .setNegativeButton("No, cancelar", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteProductFromDatabase(Product product) {
        String url = "http://10.0.2.2/cafeteria_api/delete_product.php";
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", product.getId());
        } catch (JSONException e) { e.printStackTrace(); return; }

        RequestBody requestBody = RequestBody.create(jsonObject.toString(), JSON);
        Request request = new Request.Builder().url(url).post(requestBody).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error de red al eliminar", Toast.LENGTH_SHORT).show());
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Producto eliminado", Toast.LENGTH_SHORT).show();
                        fetchProducts();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error del servidor al eliminar", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void recordPurchaseInDatabase(Product product, int quantity) {
        String url = "http://10.0.2.2/cafeteria_api/record_purchase.php";
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("product_id", product.getId());
            jsonObject.put("quantity", quantity);
            jsonObject.put("purchase_price", product.getPrice());
        } catch (JSONException e) { e.printStackTrace(); return; }

        RequestBody requestBody = RequestBody.create(jsonObject.toString(), JSON);
        Request request = new Request.Builder().url(url).post(requestBody).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error de red al registrar compra", Toast.LENGTH_SHORT).show());
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "¡Compra registrada!", Toast.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error al registrar compra", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}