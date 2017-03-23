package ml.puredark.hviewer.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;

/**
 * Created by PureDark on 2017/3/24.
 */

public class MyThumbnailUtils extends ThumbnailUtils {
    public static final int TARGET_SIZE_MICRO_THUMBNAIL = 96;


    public static Bitmap createVideoThumbnail(Context context, Uri uri, int kind) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            FileDescriptor fd = context.getContentResolver().openFileDescriptor(uri, "r").getFileDescriptor();
            retriever.setDataSource(fd, 0, 0x7ffffffffffffffL);
            bitmap = retriever.getFrameAtTime(-1);
        } catch (IllegalArgumentException ex) {
            // Assume this is a corrupt video file
        } catch (RuntimeException ex) {
            // Assume this is a corrupt video file.
        } catch (FileNotFoundException fileEx) {
            // Assume this is a corrupt video file.
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                // Ignore failures while cleaning up.
            }
        }

        if (bitmap == null) return null;

        if (kind == MediaStore.Images.Thumbnails.MINI_KIND) {
            // Scale down the bitmap if it's too large.
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int max = Math.max(width, height);
            if (max > 512) {
                float scale = 512f / max;
                int w = Math.round(scale * width);
                int h = Math.round(scale * height);
                bitmap = Bitmap.createScaledBitmap(bitmap, w, h, true);
            }
        } else if (kind == MediaStore.Images.Thumbnails.MICRO_KIND) {
            bitmap = extractThumbnail(bitmap,
                    TARGET_SIZE_MICRO_THUMBNAIL,
                    TARGET_SIZE_MICRO_THUMBNAIL,
                    OPTIONS_RECYCLE_INPUT);
        }
        return bitmap;
    }
}
