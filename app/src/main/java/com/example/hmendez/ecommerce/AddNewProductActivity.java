package com.example.hmendez.ecommerce;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.hmendez.ecommerce.R;

public class AddNewProductActivity extends AppCompatActivity {

private  String categoryName;
private Button addNewProduct;
private EditText inputProductName, inputProductDescription, inputProductPrice;
private ImageView inputProductImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_product);

        categoryName = getIntent().getExtras().get("category").toString();

        addNewProduct = findViewById(R.id.add_new_product);

        inputProductName = findViewById(R.id.product_name);
        inputProductDescription = findViewById(R.id.product_description);
        inputProductPrice = findViewById(R.id.product_price);

        inputProductImage = findViewById(R.id.select_product_image);


    }
}
