package com.example.addressbook.utils;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;


/**
 * author : lipeng
 * date   : 2019-10-23 13:53
 * desc   : 获取联系人
 */
public class PermissionAddressBookUtils {
    private static final int REQUEST_PERMISSION_ADDRESS_BOOK = 10;
    private static final int REQUEST_ADDRESS_BOOK = 11;

    public interface OnNativeAddressBookListener {
        void onNativeAddress(Map<String, Object> addressBookMap);
    }

    private OnNativeAddressBookListener onNativeAddressBookListener;
    private static String packageName = "com.example.addressbook";
    private Activity activity;

    public PermissionAddressBookUtils(Activity activity) {
        this.activity = activity;
    }

    /**
     * 请求通讯录权限
     *
     * @param onNativeAddressBookListener
     */
    public void getNativeAddressBook(OnNativeAddressBookListener onNativeAddressBookListener) {
        this.onNativeAddressBookListener = onNativeAddressBookListener;
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_PERMISSION_ADDRESS_BOOK);
            } else {
                alertDialog();
            }
        } else {
            goContacts();
        }
    }

    /**
     * @param requestCode
     * @param resultCode
     * @param data        选择联系人的回调
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ADDRESS_BOOK) {
            try {
                if (data != null) {
                    Uri uri = data.getData();
                    String phoneNum = null;
                    String contactName = null;
                    // 创建内容解析者
                    ContentResolver contentResolver = activity.getContentResolver();
                    Cursor cursor = null;
                    if (uri != null) {
                        cursor = contentResolver.query(uri, null, null, null, null);
                    }
                    while (cursor.moveToNext()) {
                        contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        phoneNum = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    }
                    cursor.close();
                    if (phoneNum != null) {
                        phoneNum = phoneNum.replaceAll("-", " ");
                        phoneNum = phoneNum.replaceAll(" ", "");
                    }

                    callbackNativeAddressBook(0, contactName, phoneNum);
                } else {
                    callbackNativeAddressBook(-2, null, null); //用户未选择联系人
                }
            } catch (Exception e) {
                callbackNativeAddressBook(-3, null, null);
            } //异常
        }
    }

    /**
     * @param requestCode
     * @param permissions
     * @param grantResults 通讯录权限回调
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_ADDRESS_BOOK) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                goContacts();
            } else {
                alertDialog();
            }
        }
    }

    /**
     * @param code
     * @param name
     * @param phone 回调数据处理
     */
    private void callbackNativeAddressBook(int code, String name, String phone) {
        Map<String, Object> addressBookMap = new HashMap<>();
        addressBookMap.put("name", name);
        addressBookMap.put("phone", phone);
        addressBookMap.put("errorCode", code);
        if (code == 0) {
            if (onNativeAddressBookListener != null) {
                onNativeAddressBookListener.onNativeAddress(addressBookMap);
            }
        }
    }

    /**
     * 跳转到联系人界面
     */
    private void goContacts() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        activity.startActivityForResult(intent, REQUEST_ADDRESS_BOOK);
    }


    /**
     * 对话框
     */
    private void alertDialog() {
        callbackNativeAddressBook(-1, null, null);
        new AlertDialog.Builder(activity)
                .setTitle("通讯录未授权")
                .setMessage("请在手机的设置-隐私-通讯录选项中，允许美菜商城访问你的通讯录")
                .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        PermissionAddressBookUtils.this.jumpPermissionPage();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 手机设置
     */
    private void jumpPermissionPage() {
        String name = Build.MANUFACTURER;
        switch (name) {
            case "HUAWEI":
                goHuaWeiManager();
                break;
            case "vivo":
                goVivoManager();
                break;
            case "OPPO":
                goOppoManager();
                break;
            case "Coolpad":
                goCoolpadManager();
                break;
            case "Meizu":
                goMeiZuManager();
                break;
            case "Xiaomi":
                goXiaoMiManager();
                break;
            case "samsung":
                goSangXinManager();
                break;
            case "Sony":
                goSonyManager();
                break;
            case "LG":
                goLGManager();
                break;
            default:
                goIntentSetting();
                break;
        }
    }

    private void goLGManager() {
        try {
            Intent intent = new Intent(packageName);
            ComponentName comp = new ComponentName("com.android.settings", "com.android.settings.Settings$AccessLockSummaryActivity");
            intent.setComponent(comp);
            activity.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(activity,"跳转失败",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            goIntentSetting();
        }
    }

    private void goSonyManager() {
        try {
            Intent intent = new Intent(packageName);
            ComponentName comp = new ComponentName("com.sonymobile.cta", "com.sonymobile.cta.SomcCTAMainActivity");
            intent.setComponent(comp);
            activity.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(activity,"跳转失败",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            goIntentSetting();
        }
    }

    private void goHuaWeiManager() {
        try {
            Intent intent = new Intent(packageName);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName comp = new ComponentName("com.huawei.systemmanager", "com.huawei.permissionmanager.ui.MainActivity");
            intent.setComponent(comp);
            activity.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(activity,"跳转失败",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            goIntentSetting();
        }
    }

    private String getMiUiVersion() {
        String propName = "ro.miui.ui.version.name";
        String line;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(
                    new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return line;
    }

    private void goXiaoMiManager() {
        String rom = getMiUiVersion();
        Intent intent = new Intent();
        if ("V6".equals(rom) || "V7".equals(rom)) {
            intent.setAction("miui.intent.action.APP_PERM_EDITOR");
            intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
            intent.putExtra("extra_pkgname", packageName);
        } else if ("V8".equals(rom) || "V9".equals(rom)) {
            intent.setAction("miui.intent.action.APP_PERM_EDITOR");
            intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
            intent.putExtra("extra_pkgname", packageName);
        } else {
            goIntentSetting();
        }
        activity.startActivity(intent);
    }

    private void goMeiZuManager() {
        try {
            Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.putExtra("packageName", packageName);
            activity.startActivity(intent);
        } catch (ActivityNotFoundException localActivityNotFoundException) {
            localActivityNotFoundException.printStackTrace();
            goIntentSetting();
        }
    }

    private void goSangXinManager() {
        //三星4.3可以直接跳转
        goIntentSetting();
    }

    private void goIntentSetting() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        try {
            activity.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void goOppoManager() {
        doStartApplicationWithPackageName("com.coloros.safecenter");
    }


    private void goCoolpadManager() {
        doStartApplicationWithPackageName("com.yulong.android.security:remote");
    }

    private void goVivoManager() {
        doStartApplicationWithPackageName("com.bairenkeji.icaller");
    }

    /**
     * 此方法在手机各个机型设置中已经失效
     *
     * @return
     */
    private Intent getAppDetailSettingIntent() {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", activity.getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", activity.getPackageName());
        }
        return localIntent;
    }

    private void doStartApplicationWithPackageName(String packagename) {
        // 通过包名获取此APP详细信息，包括Activities、services、versioncode、name等等
        PackageInfo packageinfo = null;
        try {
            packageinfo = activity.getPackageManager().getPackageInfo(packagename, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageinfo == null) {
            return;
        }
        // 创建一个类别为CATEGORY_LAUNCHER的该包名的Intent
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(packageinfo.packageName);
        // 通过getPackageManager()的queryIntentActivities方法遍历
        List<ResolveInfo> resolveinfoList = activity.getPackageManager()
                .queryIntentActivities(resolveIntent, 0);
        ResolveInfo resolveinfo = resolveinfoList.iterator().next();
        if (resolveinfo != null) {
            // packageName参数2 = 参数 packname
            String packageName = resolveinfo.activityInfo.packageName;
            // 这个就是我们要找的该APP的LAUNCHER的Activity[组织形式：packageName参数2.mainActivityname]
            String className = resolveinfo.activityInfo.name;
            // LAUNCHER Intent
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            // 设置ComponentName参数1:packageName参数2:MainActivity路径
            ComponentName cn = new ComponentName(packageName, className);
            intent.setComponent(cn);
            try {
                activity.startActivity(intent);
            } catch (Exception e) {
                goIntentSetting();
                e.printStackTrace();
            }
        }
    }

}
