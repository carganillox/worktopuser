package org.drinklink.app.utils;

import com.bumptech.glide.load.Key;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.concurrent.TimeUnit;

public class TimeSignature implements Key {
    private int signature;

    public TimeSignature() {
        long hours = System.currentTimeMillis() / TimeUnit.HOURS.toMillis(6);
        // images are cached in time windows of 6 hours
        this.signature = Long.valueOf(hours).hashCode();
    }
   
    @Override
    public boolean equals(Object o) {
        if (o instanceof TimeSignature) {
            TimeSignature other = (TimeSignature) o;
            return signature == other.signature;
        }
        return false;
    }
 
    @Override
    public int hashCode() {
        return signature;
    }

    @Override
    public void updateDiskCacheKey(MessageDigest md) {
        md.update(ByteBuffer.allocate(Integer.SIZE).putInt(signature).array());
    }
}