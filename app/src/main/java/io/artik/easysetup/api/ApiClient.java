package io.artik.easysetup.api;

import io.artik.easysetup.BuildConfig;

import java.util.Collections;

import okhttp3.CertificatePinner;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * ApiClient for Artik Cloud API Calls.
 */
public class ApiClient
{

        public static Retrofit retrofit;



    public static Retrofit getApiClient(String BASE_URL){

            if(retrofit == null) {

                HttpLoggingInterceptor logging = new HttpLoggingInterceptor();

                if (BuildConfig.DEBUG)
                    logging.setLevel(HttpLoggingInterceptor.Level.BODY);
                else
                    logging.setLevel(HttpLoggingInterceptor.Level.NONE);

                OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder();


                /**
                 * Force the connection to use only TLS v.1.2 avoiding the fallback to older version to avoid vulnerabilities
                 *
                 */
                final ConnectionSpec.Builder connectionSpec =
                        new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS);
                connectionSpec.tlsVersions(TlsVersion.TLS_1_2).build();

                /*  Use this command to get the certificate hash
                    openssl s_client -connect api.artik.cloud:443 | openssl x509 -pubkey -noout | openssl rsa -pubin -outform der | openssl dgst -sha256 -binary | openssl enc -base64
                 */
                CertificatePinner certificatePinner = new CertificatePinner.Builder()
                        .add("api.artik.cloud", "sha256/oVxrc3kTTtXyYm256w1RCNdTBawGZhu7nCugK5HsBs0=")
                        .build();

                final OkHttpClient client = httpBuilder.certificatePinner(certificatePinner)
                        .connectionSpecs(Collections.singletonList(connectionSpec.build()))
                        .addInterceptor(logging)
                        .build();

                retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(client)
                        .build();


            }
            return retrofit;
        }
}
