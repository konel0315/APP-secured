package com.example.basicapp;

import android.content.Context;
import android.os.Build;
import androidx.annotation.RequiresApi;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class SSLUtill {

    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static SSLContext createSSLContext(Context context) {
        try {
            // 1. 인증서를 읽어옵니다.
            InputStream caInput = context.getResources().openRawResource(R.raw.server_cert); // res/raw/server_cert.crt

            // 2. 인증서를 X509Certificate로 변환합니다.
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate ca = (X509Certificate) cf.generateCertificate(caInput);

            // 3. 인증서를 KeyStore에 추가합니다.
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);  // 빈 KeyStore 생성
            keyStore.setCertificateEntry("server_cert", ca); // 인증서를 저장

            // 4. TrustManagerFactory를 사용하여 인증서 검증을 위한 TrustManager를 만듭니다.
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);

            // 5. SSLContext를 생성하고, 생성된 TrustManager를 설정합니다.
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);

            return sslContext;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
