package com.mtramin.rxfingerprint;

/**
 * Created by hieuxit on 6/28/17.
 */

public abstract class AuthenticationCallbackWrapper {
    /**
     * Called when an unrecoverable error has been encountered and the operation is complete.
     * No further callbacks will be made on this object.
     * @param errorCode An integer identifying the error message
     * @param errString A human-readable error string that can be shown in UI
     */
    public void onAuthenticationError(int errorCode, CharSequence errString){}

    /**
     * Called when a recoverable error has been encountered during authentication. The help
     * string is provided to give the user guidance for what went wrong, such as
     * "Sensor dirty, please clean it."
     * @param helpCode An integer identifying the error message
     * @param helpString A human-readable string that can be shown in UI
     */
    public void onAuthenticationHelp(int helpCode, CharSequence helpString){}

    /**
     * Called when a fingerprint is recognized.
     * @param result An object containing authentication-related data
     */
    public void onAuthenticationSucceeded(CryptoObjectWrapper cryptoObject){}

    /**
     * Called when a fingerprint is valid but not recognized.
     */
    public void onAuthenticationFailed(){}

    /**
     * Called when a fingerprint image has been acquired, but wasn't processed yet.
     *
     * @param acquireInfo one of FINGERPRINT_ACQUIRED_* constants
     * @hide
     */
    public void onAuthenticationAcquired(int acquireInfo){}
}
