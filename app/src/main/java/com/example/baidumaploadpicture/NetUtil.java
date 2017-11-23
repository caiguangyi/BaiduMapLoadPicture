package com.example.baidumaploadpicture;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.Random;

/**
 * Created by kuosanyun on 16/9/29.
 */
public class NetUtil {
    private static DecimalFormat df1 = new DecimalFormat("0.00000000000000");//格式化小数

    /**
     * 没有连接网络
     */
    public static final int NETWORK_NONE = -1;
    /**
     * 移动网络
     */
    public static final int NETWORK_MOBILE = 0;
    /**
     * 无线网络
     */
    public static final int NETWORK_WIFI = 1;

    public static int getNetWrokState(Context context) {
        // 得到连接管理器对象
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {

            if (activeNetworkInfo.getType() == (ConnectivityManager.TYPE_WIFI)) {
                return NETWORK_WIFI;
            } else if (activeNetworkInfo.getType() == (ConnectivityManager.TYPE_MOBILE)) {
                return NETWORK_MOBILE;
            }
        } else {
            return NETWORK_NONE;
        }
        return NETWORK_NONE;
    }

    public static void initGPS(final Context context, final Activity activity) {
        LocationManager locManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // 未打开位置开关，可能导致定位失败或定位不准，提示用户或做相应处理
            Toast.makeText(context, "请打开GPS",
                    Toast.LENGTH_SHORT).show();
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setMessage("请打开GPS");
            dialog.setPositiveButton("确定",
                    new android.content.DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                            // 转到手机设置界面，用户设置GPS
                            Intent intent = new Intent(
                                    Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            activity.startActivityForResult(intent, Constant.OPEN_GPS_SUCCESS); // 设置完成后返回到原来的界面

                        }
                    });
            dialog.setNeutralButton("取消", new android.content.DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    arg0.dismiss();
                }
            });
            dialog.show();

        }
    }

    public static String getLagLng(String vir_point){
        String[] spt = vir_point.split(",");
        Double latitude = Double.parseDouble(spt[0]);
        Double longitude = Double.parseDouble(spt[1]);

        Random random = new Random();
        int a = random.nextInt(10);
        Double fff = Double.parseDouble(df1.format(a));
        double fdf = fff / 4000;//专门给纬度用

        int a1 = random.nextInt(10);
        Double fff1 = Double.parseDouble(df1.format(a1));
        double fdf1 = fff1 / 4000;//专门给经度用

        int nextInt = random.nextInt(2) + 1;
        if (nextInt % 2 == 0) {
            latitude = latitude + fdf;

        } else {
            latitude = latitude - fdf;
        }

        int anInt = random.nextInt(2) + 1;
        if (anInt % 2 == 0) {
            longitude = longitude + fdf1;
        } else {
            longitude = longitude - fdf1;
        }


        String draw_vir_point = latitude + "," + longitude;

        return draw_vir_point;
    }
}

