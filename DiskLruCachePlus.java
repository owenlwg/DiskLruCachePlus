package com.owen.photogallery.utils.LruCache;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * DiskLruCache wrapper
 */
public class DiskLruCachePlus {

    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
    private static final String DISK_CACHE_SUBDIR = "thumbnails";

    private DiskLruCache mDiskLruCache;


    public DiskLruCachePlus(Context context) {
        File cacheDir = getDiskCacheDir(context, DISK_CACHE_SUBDIR);
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        try {
            mDiskLruCache = DiskLruCache.open(cacheDir, getAppVersion(context), 1, 10 * 1024 * 1024);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void put(String url, byte[] bitmapBytes) {
        final String realKey = getPicName(url);
        DiskLruCache.Snapshot snapShot = null;
        OutputStream out = null;
        try {
            snapShot = mDiskLruCache.get(realKey);
            if (snapShot == null) {
                DiskLruCache.Editor editor = mDiskLruCache.edit(realKey);
                if (editor != null) {
                    out = editor.newOutputStream(0);
                    out.write(bitmapBytes);
                    out.flush();
                    editor.commit();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public Bitmap get(String url) {
        FileDescriptor fileDescriptor = null;
        FileInputStream fileInputStream = null;
        Bitmap bitmap = null;
        try {
            final String realKey = getPicName(url);
            DiskLruCache.Snapshot snapShot = mDiskLruCache.get(realKey);
            if (snapShot != null) {
                fileInputStream = (FileInputStream) snapShot.getInputStream(0);
                fileDescriptor = fileInputStream.getFD();
                if (fileDescriptor != null) {
                    bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileDescriptor == null && fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                }
            }
        }
        return bitmap;
    }


    private String getPicName(String url) {
        String[] urls = url.split("/");
        String picName = urls[urls.length - 1];
        return picName;
    }

    private File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                    || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }

    private int getAppVersion(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }

}
