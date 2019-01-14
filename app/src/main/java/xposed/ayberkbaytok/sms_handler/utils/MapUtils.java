package xposed.ayberkbaytok.sms_handler.utils;

import android.content.ContentValues;
import java.util.HashMap;

public final class MapUtils {
    private MapUtils() { }

    public static int capacityForSize(int size) {
        // HashMap 3/4 kapasiteye ulasinca resize olduğu için
        //Initialize ederken 4/3 katında oluşturmak mantikli.
        return (size * 4 + 2) / 3;
    }

    public static ContentValues contentValuesForSize(int size) {
        return new ContentValues(capacityForSize(size));
    }

    public static <K, V> HashMap<K, V> hashMapForSize(int size) {
        return new HashMap<>(capacityForSize(size));
    }
}
