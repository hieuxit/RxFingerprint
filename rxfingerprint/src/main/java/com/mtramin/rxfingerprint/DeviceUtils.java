package com.mtramin.rxfingerprint;

import com.samsung.android.sdk.SsdkVendorCheck;

/**
 * Created by hieuxit on 6/29/17.
 */

public class DeviceUtils {
    public static boolean isSamsungDevice() {
        return SsdkVendorCheck.isSamsungDevice();
    }
}
