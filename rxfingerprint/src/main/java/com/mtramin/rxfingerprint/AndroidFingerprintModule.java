package com.mtramin.rxfingerprint;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;

import static android.Manifest.permission.USE_FINGERPRINT;

/**
 * Created by hieuxit on 6/28/17.
 */

public class AndroidFingerprintModule implements FingerprintModule {

    private static AndroidFingerprintModule instance;

    // Do not public constructor
    private AndroidFingerprintModule() {
    }

    public static AndroidFingerprintModule getInstance() {
        if (instance == null) {
            instance = new AndroidFingerprintModule();
        }
        return instance;
    }

    @Override
    @RequiresApi(Build.VERSION_CODES.M)
    public boolean fingerprintPermissionGranted(Context context) {
        return context.checkSelfPermission(USE_FINGERPRINT) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    public CancellationSignal createCancellationSignal() {
        return new CancellationSignal();
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressWarnings("MissingPermission")
    public boolean isHardwareDetected(Context context) {
        FingerprintManager fingerprintManager = getFingerprintManager(context);
        if (fingerprintManager == null) {
            return false;
        }
        return fingerprintPermissionGranted(context) && fingerprintManager.isHardwareDetected();
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressWarnings("MissingPermission")
    public boolean hasEnrolledFingerprints(@NonNull Context context) {
        FingerprintManager fingerprintManager = getFingerprintManager(context);
        if (fingerprintManager == null) {
            return false;
        }
        return fingerprintPermissionGranted(context) && fingerprintManager.hasEnrolledFingerprints();
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressWarnings("MissingPermission")
    public void authenticate(Context context, @Nullable CryptoObjectWrapper crypto, @Nullable CancellationSignal cancel, int flags, @NonNull AuthenticationCallbackWrapper callback, @Nullable Handler handler) {
        FingerprintManager fingerprintManager = getFingerprintManager(context);
        FingerprintManager.CryptoObject cryptoImpl = null;
        if (crypto != null) {
            cryptoImpl = new FingerprintManager.CryptoObject(crypto.getCipher());
        }
        fingerprintManager.authenticate(cryptoImpl, cancel, 0, new AuthenticationCallbackImpl(callback), handler);
    }

    @Nullable
    @RequiresApi(Build.VERSION_CODES.M)
    static FingerprintManager getFingerprintManager(Context context) {
        try {
            return (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
        } catch (Exception | NoClassDefFoundError e) {
            Log.e("RxFingerprint", "Device with SDK < 23 doesn't provide Fingerprint APIs", e);
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    static class AuthenticationCallbackImpl extends FingerprintManager.AuthenticationCallback {

        private AuthenticationCallbackWrapper wrapper;

        public AuthenticationCallbackImpl(AuthenticationCallbackWrapper wrapper) {
            this.wrapper = wrapper;
        }

        public void onAuthenticationError(int errorCode, CharSequence errString) {
            if (wrapper != null) {
                wrapper.onAuthenticationError(errorCode, errString);
            }
        }

        /**
         * Called when a recoverable error has been encountered during authentication. The help
         * string is provided to give the user guidance for what went wrong, such as
         * "Sensor dirty, please clean it."
         *
         * @param helpCode   An integer identifying the error message
         * @param helpString A human-readable string that can be shown in UI
         */
        public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
            if (wrapper != null) {
                wrapper.onAuthenticationHelp(helpCode, helpString);
            }
        }

        /**
         * Called when a fingerprint is recognized.
         *
         * @param result An object containing authentication-related data
         */
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
            if (wrapper != null) {
                FingerprintManager.CryptoObject cryptoObject = result.getCryptoObject();
                if (cryptoObject == null) {
                    wrapper.onAuthenticationSucceeded(null);
                } else {
                    wrapper.onAuthenticationSucceeded(new CryptoObjectWrapper(cryptoObject.getCipher()));
                }
            }
        }

        /**
         * Called when a fingerprint is valid but not recognized.
         */
        public void onAuthenticationFailed() {
            if (wrapper != null) {
                wrapper.onAuthenticationFailed();
            }
        }

        /**
         * Called when a fingerprint image has been acquired, but wasn't processed yet.
         *
         * @param acquireInfo one of FINGERPRINT_ACQUIRED_* constants
         * @hide
         */
        public void onAuthenticationAcquired(int acquireInfo) {
            if (wrapper != null) {
                wrapper.onAuthenticationAcquired(acquireInfo);
            }
        }
    }
}
