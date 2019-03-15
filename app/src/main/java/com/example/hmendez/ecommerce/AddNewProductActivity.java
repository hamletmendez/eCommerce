package com.example.hmendez.ecommerce;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class AddNewProductActivity extends AppCompatActivity {

private  String categoryName,saveCurrentDate, saveCurrentTime,
         productRandomKey, downLoadImageUrl, productDescription, productPrice, productName;

private Button addNewProduct;

private EditText inputProductName, inputProductDescription, inputProductPrice;
private ImageView inputProductImage;

private final static  int galleryPick = 1;
private Uri imageUri;
private StorageReference productImageRef;
private DatabaseReference productsRef;
private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_product);

        categoryName = getIntent().getExtras().get("category").toString();
        productImageRef = FirebaseStorage.getInstance().getReference().child("Product Images");
        productsRef = FirebaseDatabase.getInstance().getReference().child("products");

        addNewProduct = findViewById(R.id.add_new_product);

        inputProductName = findViewById(R.id.product_name);
        inputProductDescription = findViewById(R.id.product_description);
        inputProductPrice = findViewById(R.id.product_price);

        loadingBar = new ProgressDialog(this);

        inputProductImage = findViewById(R.id.select_product_image);

        inputProductImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenGallery();
            }
        });

        addNewProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ValidateProductData();
            }
        });

    }

    private void ValidateProductData() {
        productDescription = inputProductDescription.getText().toString();
        productPrice = inputProductPrice.getText().toString();
        productName = inputProductName.getText().toString();

        if (imageUri == null ){
            Toast.makeText(this, "Product image is mandatory...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(productDescription)){
            Toast.makeText(this, "Please enter a product description...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(productPrice)){
            Toast.makeText(this, "Please enter a product price...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(productName)){
            Toast.makeText(this, "Please enter a product name...", Toast.LENGTH_SHORT).show();
        }
        else{
            StoreProductInformation();
        }
    }

    private void StoreProductInformation() {

        loadingBar.setTitle("Add New Product");
        loadingBar.setMessage("Please wait, while we are adding the new product...");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();


        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        productRandomKey = saveCurrentDate + saveCurrentTime;

        final StorageReference filePath = productImageRef.child(imageUri.getLastPathSegment()+ productRandomKey+".jpg");

        final UploadTask uploadTask = filePath.putFile(imageUri);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddNewProductActivity.this, "Error: " + e.toString(), Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(AddNewProductActivity.this, "Product image uploaded successfully", Toast.LENGTH_SHORT).show();

                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()){
                            throw  task.getException();

                        }
                        downLoadImageUrl = filePath.getDownloadUrl().toString();
                        return  filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){

                            downLoadImageUrl = task.getResult().toString();

                            Toast.makeText(AddNewProductActivity.this, "Product image loaded to database successfully...", Toast.LENGTH_SHORT).show();

                            SaveProductInfoToDatabase();
                        }
                    }
                });
            }


        });

    }

    private void SaveProductInfoToDatabase() {
        HashMap<String, Object> productMap = new HashMap<>();
        productMap.put("pid",productRandomKey);
        productMap.put("date",saveCurrentDate);
        productMap.put("time",saveCurrentTime);
        productMap.put("description", productDescription);
        productMap.put("image",downLoadImageUrl);
        productMap.put("category",categoryName);
        productMap.put("price", productPrice);
        productMap.put("productname", productName);

        productsRef.child(productRandomKey).updateChildren(productMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            loadingBar.dismiss();

                            Intent intent = new Intent(AddNewProductActivity.this,CategoryActivity.class);
                            Toast.makeText(AddNewProductActivity.this, "Product added successfully...", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            loadingBar.dismiss();
                            String message = task.getException().toString();
                            Toast.makeText(AddNewProductActivity.this, "Error: " + message , Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void OpenGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,galleryPick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == galleryPick && resultCode == RESULT_OK && data != null){
            imageUri = data.getData();
            inputProductImage.setImageURI(imageUri);
        }
    }
}
