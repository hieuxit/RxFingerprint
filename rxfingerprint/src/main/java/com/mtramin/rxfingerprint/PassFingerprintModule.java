package com.mtramin.rxfingerprint;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;

import com.samsung.android.sdk.pass.Spass;
import com.samsung.android.sdk.pass.SpassFingerprint;

/**
 * Created by hieuxit on 6/29/17.
 */

public class PassFingerprintModule implements FingerprintModule {

    private static final String SPASS_PERMISSION = "com.samsung.android.providers.context.permission.WRITE_USE_APP_FEATURE_SURVEY";
    private static PassFingerprintModule instance;

    public static PassFingerprintModule getInstance(Context context) {
        if (instance == null) {
            instance = new PassFingerprintModule(context);
        }
        return instance;
    }

    private Context context;
    private Spass spass;
    private SpassFingerprint spassFingerprint;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    private PassFingerprintModule(Context context) {
        this.context = context;
        Spass s;
        try {
            s = new Spass();
            s.initialize(this.context);
        } catch (SecurityException e) {
            // Rethrow security exceptions, which happen when the manifest permission is missing.
            throw e;
        } catch (Exception ignored) {
            // The awful spass sdk throws an exception on non-samsung devices, so swallow it here.
            s = null;
        }
        spass = s;
    }

    @Override
    public boolean fingerprintPermissionGranted(Context context) {
        return ContextCompat.checkSelfPermission(context, SPASS_PERMISSION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public CancellationSignal createCancellationSignal() {
        return new CancellationSignal();
    }

    @Override
    public boolean isHardwareDetected(Context context) {
        try {
            return spass != null && spass.isFeatureEnabled(Spass.DEVICE_FINGERPRINT);
        } catch (Exception ignored) {
            return false;
        }
    }

    @Override
    public boolean hasEnrolledFingerprints(@NonNull Context context) {
        try {
            if (isHardwareDetected(context)) {
                if (spassFingerprint == null) {
                    spassFingerprint = new SpassFingerprint(context);
                }
                return spassFingerprint.hasRegisteredFinger();
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void authenticate(final Context context, @Nullable final CryptoObjectWrapper cryptoData,
                             @Nullable final CancellationSignal cancellationSignal, final int flags,
                             @NonNull final AuthenticationCallbackWrapper callback, @Nullable final Handler handler) {
        if (spassFingerprint == null) {
            spassFingerprint = new SpassFingerprint(context);
        }

        cancelFingerprintRequest(spassFingerprint);

        try {
            spassFingerprint.startIdentify(new SpassFingerprint.IdentifyListener() {

                boolean fatal = false;

                @Override
                public void onFinished(int status) {
                    switch (status) {
                        case SpassFingerprint.STATUS_AUTHENTIFICATION_SUCCESS:
                        case SpassFingerprint.STATUS_AUTHENTIFICATION_PASSWORD_SUCCESS:
                            callback.onAuthenticationSucceeded(cryptoData);
                            return;
                        case SpassFingerprint.STATUS_QUALITY_FAILED:
                            callback.onAuthenticationHelp(FingerprintManager.FINGERPRINT_ERROR_UNABLE_TO_PROCESS,
                                    context.getString(R.string.fingerprint_acquired_partial));
                            break;
                        case SpassFingerprint.STATUS_SENSOR_FAILED:
                            callback.onAuthenticationHelp(FingerprintManager.FINGERPRINT_ERROR_UNABLE_TO_PROCESS,
                                    context.getString(R.string.fingerprint_acquired_insufficient));
                            break;
                        case SpassFingerprint.STATUS_AUTHENTIFICATION_FAILED:
                            callback.onAuthenticationFailed();
                            break;
                        case SpassFingerprint.STATUS_TIMEOUT_FAILED:
                            callback.onAuthenticationHelp(FingerprintManager.FINGERPRINT_ERROR_TIMEOUT,
                                    context.getString(R.string.fingerprint_error_timeout));
                            fatal = true;
                            break;
                        default:
                            callback.onAuthenticationError(FingerprintManager.FINGERPRINT_ERROR_CANCELED,
                                    context.getString(R.string.fingerprint_error_hw_not_available));
                            fatal = true;
                            break;
                        case SpassFingerprint.STATUS_USER_CANCELLED:
                            // Don't send a cancelled message.
                            fatal = true;
                            break;
                    }
                }

                @Override
                public void onReady() {

                }

                @Override
                public void onStarted() {

                }

                @Override
                public void onCompleted() {
                    if (!fatal) {
                        mainHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                authenticate(context, cryptoData, cancellationSignal, flags, callback, handler);
                            }
                        }, 100);
                    }
                }
            });
        } catch (Throwable t) {
            callback.onAuthenticationError(FingerprintManager.FINGERPRINT_ERROR_LOCKOUT,
                    context.getString(R.string.fingerprint_error_lockout));
            return;
        }

        cancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
            @Override
            public void onCancel() {
                cancelFingerprintRequest(spassFingerprint);
            }
        });
    }

    private static void cancelFingerprintRequest(SpassFingerprint spassFingerprint) {
        try {
            spassFingerprint.cancelIdentify();
        } catch (Throwable t) {
            // There's no way to query if there's an active identify request,
            // so just try to cancel and ignore any exceptions.
            t.printStackTrace();
        }
    }
}
