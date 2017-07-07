package com.mtramin.rxfingerprint;

import android.content.Context;
import android.os.CancellationSignal;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by hieuxit on 6/28/17.
 */

interface FingerprintModule {

    boolean needAuthenticate();

    boolean fingerprintPermissionGranted(Context context);

    CancellationSignal createCancellationSignal();

    boolean isHardwareDetected(Context context);

    boolean hasEnrolledFingerprints(@NonNull Context context);

    void authenticate(Context context, @Nullable CryptoObjectWrapper crypto, @Nullable CancellationSignal cancel,
                      int flags, @NonNull AuthenticationCallbackWrapper callback, @Nullable Handler handler);
}
