package android.collab.bestfood;

import android.Manifest;
import android.collab.bestfood.lib.MyToast;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

// 앱을 실행할 때 필요한 권한을 처리하기 위한 액티비티
public class PermissionActivity  extends AppCompatActivity {
    private static final int PERMISSION_MULTI_CODE = 100;
    // 화면을 구성하고 SDK 버전과 권한에 따른 처리를 한다. @param savedInstanceState 액티비티가 새로 생성 되어을 경우 ,이전 상태를 값을 가지는 개체
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        if(Build.VERSION.SDK_INT < 23) {
            goIndexActivity();
        } else {
            if(checkAndRequestPerPermissions()) {
                goIndexActivity();

            }
        }
    }

    //권한 확인 권한 요청 @return 필요한 권한이 모두 부여 되어 있을 시 t nor f
    private boolean checkAndRequestPerPermissions(){
        String [] permissions = new String[] {
                Manifest.permission.CAMERA,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_NETWORK_STATE
        };

        List<String> listPermissionsNeeded = new ArrayList<>();

        for(String permission:permissions){
            if(ContextCompat.checkSelfPermission(this, permission)
            != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(permission);
            }
        }

        if(!listPermissionsNeeded.isEmpty()){
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),
                    PERMISSION_MULTI_CODE);

            return false;
        }

        return true;
    }

    //@param 권한 결과
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(grantResults.length == 0) return ;

        switch (requestCode){
            case PERMISSION_MULTI_CODE:
                checkPermissionResult(permissions, grantResults);

                break;
        }
    }

    private  void checkPermissionResult(String[] permissions, int[] grantResults) {
        boolean isAllGranted = true;

        for(int i = 0; i<permissions.length; i++){
            if(grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                isAllGranted = false;
            }
        }

        //권한이 부여되었다면
        if(isAllGranted){
            goIndexActivity();
        } else {
            showPermissionDialog();
        }
    }

    private void goIndexActivity() {
        Intent intent = new Intent(this, IndexActivity.class);
        startActivity(intent);

        finish();
    }

    private void showPermissionDialog(){
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.permission_setting_title);
        dialog.setMessage(R.string.permission_setting_message);
        dialog.setPositiveButton(R.string.setting, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialog.setCancelable(true);
                MyToast.s(PermissionActivity.this, R.string.permission_setting_restart);

                PermissionActivity.this.finish();
                goAppSettingActivity();
            }
        });
        dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialog.setCancelable(true);
                PermissionActivity.this.finish();
            }
        });

    }

    private void goAppSettingActivity(){
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }
}
