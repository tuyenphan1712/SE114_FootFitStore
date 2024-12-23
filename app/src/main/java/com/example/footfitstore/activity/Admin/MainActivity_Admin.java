package com.example.footfitstore.activity.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.footfitstore.R;
import com.example.footfitstore.activity.LoginActivity;
import com.example.footfitstore.fragment.Admin.CategoriesAndBannersFragment;
import com.example.footfitstore.fragment.Admin.ProductsFragmentAdmin;
import com.example.footfitstore.fragment.Admin.PromotionFragmentAdmin;
import com.example.footfitstore.fragment.Admin.StatisticsFragmentAdmin;
import com.example.footfitstore.fragment.Admin.UsersFragmentAdmin;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity_Admin extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_admin);
        bottomNavigationView = findViewById(R.id.bottomNavigationAdmin);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_users) {
                    setFragment(new UsersFragmentAdmin());
                    return true;
                } else if (itemId == R.id.nav_discount) {
                    setFragment(new PromotionFragmentAdmin());
                    return true;
                } else if (itemId == R.id.nav_product) {
                    setFragment(new ProductsFragmentAdmin());
                    return true;
                } else if (itemId == R.id.nav_category) {
                    setFragment(new CategoriesAndBannersFragment());
                    return true;
                } else if (itemId == R.id.nav_order) {
                    setFragment(new StatisticsFragmentAdmin());
                    return true;
                } else {
                    return false;
                }
            }
        });
        setFragment(new UsersFragmentAdmin());
    }
    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment);
        fragmentTransaction.commit();
    }

    public void setSelectedNavItem(int itemId) {
        bottomNavigationView.setSelectedItemId(itemId);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frame);
        if (fragment instanceof UsersFragmentAdmin) {
            if (((UsersFragmentAdmin) fragment).onBackPressed()) {
                return;
            }
        }
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signOut();
        Intent intent = new Intent(MainActivity_Admin.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}