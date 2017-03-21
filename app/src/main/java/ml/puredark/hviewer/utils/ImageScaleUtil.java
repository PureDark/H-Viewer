package ml.puredark.hviewer.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * Created by PureDark on 2016/5/11.
 */
public class ImageScaleUtil {

    /**
     * 压缩图片宽高
     */
    public static Bitmap getScaledImage(String srcPath, int width, int height) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);//此时返回bm为空

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        float hh = height;
        float ww = width;
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {
            //如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {
            //如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        return compressImage(bitmap, 512);//压缩好比例大小后再进行质量压缩
    }

    /**
     * 压缩图片质量
     */
    public static Bitmap compressImage(Bitmap image, int size) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int options = 100;
        //循环判断如果压缩后图片是否大于 size kb且质量比例大于50%,大于继续压缩
        while (baos.toByteArray().length / 1024 > size && options > 50) {
            //重置baos即清空baos
            baos.reset();
            //这里压缩options%，把压缩后的数据存放到baos中
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);
            options -= 10;//每次都减少10
        }
        //把压缩后的数据baos存放到ByteArrayInputStream中
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        //把ByteArrayInputStream数据生成图片
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
        return bitmap;
    }

    /**
     * 按正方形裁切图片
     */
    public static Bitmap cropToSquare(Bitmap bitmap) {
        int w = bitmap.getWidth(); // 得到图片的宽，高
        int h = bitmap.getHeight();

        int wh = w > h ? h : w;// 裁切后所取的正方形区域边长

        int retX = w > h ? (w - h) / 2 : 0;//基于原图，取正方形左上角x坐标
        int retY = w > h ? 0 : (h - w) / 2;

        //下面这句是关键
        return Bitmap.createBitmap(bitmap, retX, retY, wh, wh, null, false);
    }

    public static void saveToFile(Context context, Bitmap bitmap, String destPath) throws IOException {
        ImageView iv = new ImageView(context);
        iv.setImageBitmap(bitmap);
        File file = new File(destPath);
        FileInputStream fis;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int count;
        FileOutputStream out = new FileOutputStream(file);
        if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
            out.flush();
            out.close();
        }
        fis = new FileInputStream(destPath);
        while ((count = fis.read(buffer)) >= 0) {
            baos.write(buffer, 0, count);
        }
    }

    public static String getFileMd5(File file) {
        try {
            String md5 = null;
            InputStream in = new FileInputStream(file);
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            ByteArrayOutputStream out = new ByteArrayOutputStream((int) file.length());
            byte[] cache = new byte[1048576];
            for (int i = in.read(cache); i != -1; i = in.read(cache)) {
                out.write(cache, 0, i);
            }
            in.close();
            out.close();
            messageDigest.update(out.toByteArray());
            BigInteger bi = new BigInteger(1, messageDigest.digest());
            md5 = bi.toString(16);
            return md5;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
