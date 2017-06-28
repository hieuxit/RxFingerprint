package com.mtramin.rxfingerprint;

import javax.crypto.Cipher;

/**
 * Created by hieuxit on 6/28/17.
 */

public class CryptoObjectWrapper {
    private Cipher cipher;

    public CryptoObjectWrapper(Cipher cipher) {
        this.cipher = cipher;
    }

    public Cipher getCipher() {
        return cipher;
    }
}
