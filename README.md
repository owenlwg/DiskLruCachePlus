# DiskLruCachePlus
Android DiskLruCache的一个包装，增加了put和get方法，方便调用

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
