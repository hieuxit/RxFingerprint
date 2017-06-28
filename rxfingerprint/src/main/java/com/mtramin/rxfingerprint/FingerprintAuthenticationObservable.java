/*
 * Copyright 2015 Marvin Ramin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mtramin.rxfingerprint;

import android.content.Context;
import android.support.annotation.Nullable;

import com.mtramin.rxfingerprint.data.FingerprintAuthenticationResult;
import com.mtramin.rxfingerprint.data.FingerprintResult;

import rx.Observable;
import rx.Subscriber;

/**
 * Authenticates the user with his fingerprint.
 */
class FingerprintAuthenticationObservable extends FingerprintObservable<FingerprintAuthenticationResult> {

    /**
     * Creates an Observable that will enable the fingerprint scanner of the device and listen for
     * the users fingerprint for authentication
     *
     * @param context context to use
     * @return Observable {@link FingerprintAuthenticationResult}
     */
    static Observable<FingerprintAuthenticationResult> create(Context context, FingerprintModule fingerprintModule) {
        return Observable.create(new FingerprintAuthenticationObservable(context, fingerprintModule));
    }

    private FingerprintAuthenticationObservable(Context context, FingerprintModule fingerprintModule) {
        super(context, fingerprintModule);
    }

    @Nullable
    @Override
    protected CryptoObjectWrapper initCryptoObject(Subscriber<FingerprintAuthenticationResult> subscriber) {
        // Simple authentication does not need CryptoObject
        return null;
    }

    @Override
    protected void onAuthenticationSucceeded(Subscriber<FingerprintAuthenticationResult> subscriber, CryptoObjectWrapper cryptoObject) {
        subscriber.onNext(new FingerprintAuthenticationResult(FingerprintResult.AUTHENTICATED, null));
        subscriber.onCompleted();
    }

    @Override
    protected void onAuthenticationHelp(Subscriber<FingerprintAuthenticationResult> subscriber, int helpMessageId, String helpString) {
        subscriber.onNext(new FingerprintAuthenticationResult(FingerprintResult.HELP, helpString));
    }

    @Override
    protected void onAuthenticationFailed(Subscriber<FingerprintAuthenticationResult> subscriber) {
        subscriber.onNext(new FingerprintAuthenticationResult(FingerprintResult.FAILED, null));
    }
}
