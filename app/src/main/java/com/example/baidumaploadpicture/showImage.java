package com.example.baidumaploadpicture;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by shaoweifeng on 15/12/19.
 */
public class showImage {
    private static String base64;
    //    private static String api = com.ksytech.kuosanyun.activitys.Common.SERVER_IP;
    private static String imagePath;

    private static String share_imageUrl;
    private static String share_url;
    private static String share_descption;
    private static String share_title;


    //ImageLoader 显示图片;设置默认图片;内存缓存;SD缓存;
    public static void show(String url, ImageView view, boolean isMemoryCache, boolean isDiscCache, int defaultImage) {
        DisplayImageOptions options = getDisplayImageOptions(isMemoryCache, isDiscCache, defaultImage);
        ImageLoader.getInstance().displayImage(url, view, options);
    }


    //加载图片，返回Bitmap;
    public static Bitmap loadBitmap(String url, boolean isMemoryCache, boolean isDiscCache, int defaultImage) {
        DisplayImageOptions options = getDisplayImageOptions(isMemoryCache, isDiscCache, defaultImage);
        return ImageLoader.getInstance().loadImageSync(url, options);
    }


    public static DisplayImageOptions getDisplayImageOptions(boolean isMemoryCache, boolean isDiscCache, int defaultImage) {
        return new DisplayImageOptions.Builder()
//                .showImageOnLoading(defaultImage)            //加载图片时的图片
                .showImageForEmptyUri(defaultImage)         //没有图片资源时的默认图片
                .showImageOnFail(defaultImage)              //加载失败时的图片
                .cacheInMemory(isMemoryCache)                               //启用内存缓存
                .cacheOnDisc(isDiscCache)                                 //启用外存缓存
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }


    public static void show_round(String url, ImageView view, boolean isMemoryCache, boolean isDiscCache, int defaultImage) {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(defaultImage)            //加载图片时的图片
                .showImageForEmptyUri(defaultImage)         //没有图片资源时的默认图片
                .showImageOnFail(defaultImage)              //加载失败时的图片
                .cacheInMemory(isMemoryCache)                               //启用内存缓存
                .cacheOnDisc(isDiscCache)                                 //启用外存缓存
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new RoundedBitmapDisplayer(20))         //设置显示风格这里是圆角
                .build();
        String imgurl = url;
        ImageLoader.getInstance().displayImage(imgurl, view, options);

    }

    //ImageLoader 显示图片;设置默认图片;内存缓存;SD缓存;
    public static void shows(String url, ImageView view, boolean isMemoryCache, boolean isDiscCache, int defaultImage) {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
//                .showImageOnLoading(defaultImage)            //加载图片时的图片
                .showImageForEmptyUri(defaultImage)         //没有图片资源时的默认图片
                .showImageOnFail(defaultImage)              //加载失败时的图片
                .cacheInMemory(isMemoryCache)                               //启用内存缓存
                .cacheOnDisc(isDiscCache)                                 //启用外存缓存
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        String imgurl = url;
        ImageLoader.getInstance().displayImage(imgurl, view, options);
    }


    public static long getBitmapsize(Bitmap bitmap) {

        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
        // return bitmap.getByteCount();
        // }
        // Pre HC-MR1
        return bitmap.getRowBytes() * bitmap.getHeight();

    }

    public static String bitmapToBase64(Bitmap bitmap) {

        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                baos.flush();
                baos.close();

                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }


    //添加水印文字
    public static Bitmap oldWordBitmap(Context context, Bitmap res, String word) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Bitmap newb = Bitmap.createBitmap(res.getWidth(), res.getHeight(), Bitmap.Config.RGB_565);// 创建一个新的和SRC长度宽度一样的位图
        Canvas cv = new Canvas(newb);
        cv.drawBitmap(res, 0, 0, null);// 在 0，0坐标开始画入src
        Paint paint = new Paint();
        String familyName = "宋体";
        Typeface font = Typeface.create(familyName, Typeface.NORMAL);
        TextPaint textPaint = new TextPaint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTypeface(font);
        textPaint.setShadowLayer(2, 2, 2, Color.WHITE);
        textPaint.setTextSize(35);
        StaticLayout layout = null;
        int proportion = sp.getInt("device_proportion", 3);
        layout = new StaticLayout(word, textPaint, res.getWidth(), Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, true);
        float cv_width = 10 * proportion; //起始位置
        cv.translate(cv_width, newb.getHeight() - 20 * proportion - 15);
        layout.draw(cv);
        res.recycle();
        cv.save(Canvas.ALL_SAVE_FLAG);// 保存
        cv.restore();// 存储
        return newb;
    }


    /**
     * 重新绘制图片 返回图片
     */
    public static Bitmap resizeQRcode(Bitmap map, int w, int h, Context context) {
        int width = map.getWidth();
        int height = map.getHeight();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        float scaleWight = ((float) w) / map.getWidth();
        float scaleHeight = ((float) h) / map.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWight, scaleHeight);
        Bitmap res = Bitmap.createBitmap(map, 0, 0, width, height, matrix, true);
        return res;
    }


    /**
     * 生成裁剪图片
     *
     * @param real       裁剪的view
     * @param proportion
     * @param context
     * @param width      宽
     * @param height     高
     * @param type       判断是否带水印 1 不带水印
     * @return 存放图片地址
     */

    public static String getPic(View real, int proportion, Context context, int width, int height, int type) {
        imagePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Camera/" + System.currentTimeMillis() + ".jpg";
        Log.i("real---", real + "real");
        real.setDrawingCacheEnabled(true);
        Bitmap map = Bitmap.createBitmap(real.getDrawingCache());
        map = Bitmap.createBitmap(map, 0, 0, map.getWidth(), map.getHeight());
        real.setDrawingCacheEnabled(false);
        File screenshot_image = new File(imagePath);
        if (map != null) {
            if (type == 1) {  //不带水印
//                map = showImage.resizeQRcode(map, width, height, context);
            } else {
//                map = showImage.resizeBitmap(map, width, height, proportion, context, 1);
            }
            System.out.println("bitmap got!");
            try {
                FileOutputStream outputStream = null;
                try {
                    File destDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Camera");
                    if (!destDir.exists()) {
                        destDir.mkdirs();
                        if (!screenshot_image.exists()) {
                            screenshot_image.createNewFile();
                            // 创建文件
                        }
                    }
                    outputStream = new FileOutputStream(screenshot_image);
                    map.compress(Bitmap.CompressFormat.JPEG, 75, outputStream);
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri uri = Uri.fromFile(screenshot_image);
                    intent.setData(uri);
                    context.sendBroadcast(intent);
                    outputStream.flush();
                    outputStream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return imagePath;

    }


}
