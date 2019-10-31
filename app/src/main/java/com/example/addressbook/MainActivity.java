package com.example.addressbook;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.addressbook.utils.PermissionAddressBookUtils;

import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private Button button;
    private TextView tv;
    private PermissionAddressBookUtils permissionAddressBookUtils;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button=findViewById(R.id.btn);
        tv=findViewById(R.id.tv);
        permissionAddressBookUtils = new PermissionAddressBookUtils(this);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permissionAddressBookUtils.getNativeAddressBook(new PermissionAddressBookUtils.OnNativeAddressBookListener() {
                    @Override
                    public void onNativeAddress(Map<String, Object> addressBookMap) {
                        tv.setText(addressBookMap.get("name").toString()+":"+addressBookMap.get("phone").toString());
                    }
                });
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        permissionAddressBookUtils.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionAddressBookUtils.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
