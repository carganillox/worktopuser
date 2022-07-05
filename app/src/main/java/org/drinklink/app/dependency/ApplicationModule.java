/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.dependency;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.drinklink.app.BuildConfig;
import org.drinklink.app.api.ApiAuthService;
import org.drinklink.app.api.ApiCache;
import org.drinklink.app.api.ApiService;
import org.drinklink.app.api.AuthorizationHeaderInterceptor;
import org.drinklink.app.api.AutomaticSignUpInterceptorToken;
import org.drinklink.app.api.InterceptorToken;
import org.drinklink.app.api.V1Initializer;
import org.drinklink.app.notifications.NotificationsTokenUpdateService;
import org.drinklink.app.persistence.DataStorage;
import org.drinklink.app.persistence.PreferencesStorage;
import org.drinklink.app.persistence.model.AnnotationInternalExclusionStrategy;
import org.drinklink.app.workflow.IOrderProcessor;
import org.drinklink.app.workflow.OrderProcessor;

import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 *
 */
@Module
public class ApplicationModule {

    private static final String MERCHANT_URL = "http://10.0.2.2:3000/";

    //http://slowwly.robertomurray.co.uk/delay/1000/url/http://google.co.uk
//    public static final String BASE_URL = "http://drinklinkdev-001-site1.itempurl.com/api/";
    public static final String BASE_URL_WITHOUT_SLASH = "https://drinklink-prod-be.azurewebsites.net/api";
//    public static final String BASE_URL_WITHOUT_SLASH = "https://drinklink-preprod-be.azurewebsites.net/api";
    public static final String BASE_URL = BASE_URL_WITHOUT_SLASH + "/";
//    public static final String BASE_URL = "http://drinklink.aspifyhost.cz/api/";
//    public static final String BASE_URL = "45.58.142.26";
//    public static final String BASE_URL = "http://private-2400e9-mgun.apiary-mock.com";
//    public static final String BASE_URL = "http://slowwly.robertomurray.co.uk/delay/3000/url/http://private-2400e9-mgun.apiary-mock.com/";
    public static final String API_SERVICE_CACHE = "api_service_cache";
    public static final String API_SERVICE = "api_service";
    public static final String API_OK_CLIENT = "api_ok_client";
    public static final String AUTH_API_OK_CLIENT = "auth_api_ok_client";

    private static Context contex;

    public ApplicationModule(Context application) {
        contex = application.getApplicationContext();
    }

//    @Singleton
//    @Provides
//    Application providesApplication(){
//        return application;
//    }

    @Singleton
    @Provides
    Context providesContext(){
        return contex;
    }

    @Provides
    @Singleton
    @Named(AUTH_API_OK_CLIENT)
    public OkHttpClient providesOkHttpClient() {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY :
                HttpLoggingInterceptor.Level.NONE);

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
//                .addNetworkInterceptor(new OkHttp3StethoInterceptor())
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(logging);

//        if (BuildConfig.DEBUG) {
//            builder = builder.addInterceptor(chain -> {
//                try {
//                    Thread.sleep(TimeUnit.SECONDS.toMillis(HTTP_DELAY));
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                return chain.proceed(chain.request());
//            });
//        }

        return builder.build();
    }

    @Provides
    @Singleton
    @Named(API_OK_CLIENT)
    public OkHttpClient providesAuthOkHttpClient(AuthorizationHeaderInterceptor authInterceptor) {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY :
                HttpLoggingInterceptor.Level.NONE);

        OkHttpClient.Builder builder = new OkHttpClient.Builder()

//                .connectTimeout(30, TimeUnit.SECONDS)
//                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)

//                .connectTimeout(1, TimeUnit.MILLISECONDS)
//                .writeTimeout(1, TimeUnit.MILLISECONDS)
//                .readTimeout(1, TimeUnit.MILLISECONDS)

//                .addNetworkInterceptor(new OkHttp3StethoInterceptor())
                .addInterceptor(logging)
                .addInterceptor(authInterceptor)
                .authenticator(authInterceptor);

//        if (BuildConfig.DEBUG) {
//            builder = builder.addInterceptor(chain -> {
//                try {
//                    Thread.sleep(TimeUnit.SECONDS.toMillis(HTTP_DELAY));
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                return chain.proceed(chain.request());
//            });
//        }

        return builder.build();
    }

    @Provides
    @Singleton
    public Retrofit providesRetrofit(@Named(API_OK_CLIENT) OkHttpClient client, Gson gson) {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
    }

    @Provides
    @Singleton
    @Named(API_SERVICE_CACHE)
    public ApiService providesApiServiceCache(@Named(API_SERVICE) ApiService apiService ) {
        return new ApiCache(apiService);
    }

    @Provides
    @Singleton
    @Named(API_SERVICE)
    public ApiService providesApiService(Retrofit retrofit) {
        return retrofit.create(ApiService.class);

    }

    @Provides
    @Singleton
    public ApiAuthService providesApiAuthService(@Named(AUTH_API_OK_CLIENT) OkHttpClient client, Gson gson) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        return retrofit.create(ApiAuthService.class);
    }

    @Provides
    @Singleton
    public Gson providesGson() {
        return new GsonBuilder().setExclusionStrategies(new AnnotationInternalExclusionStrategy()).create();
    }

    @Provides
    @Singleton
    public IOrderProcessor providesOrderProcessor(DataStorage dataStorage) {
        return new OrderProcessor(dataStorage);
    }

    @Provides
    @Singleton
    public AuthorizationHeaderInterceptor provideAuthorizationInterceptor(InterceptorToken interceptorToken) {
        return new AuthorizationHeaderInterceptor(interceptorToken);
    }

    @Provides
    @Singleton
    public V1Initializer bindInterceptor(InterceptorToken interceptorToken, @Named(API_SERVICE) ApiService apiService,
                                         NotificationsTokenUpdateService notificationsTokenUpdateService) {
        interceptorToken.setNotificationsService(apiService);
        interceptorToken.setNotificationsTokenUpdateService(notificationsTokenUpdateService);
        return new V1Initializer();
    }

    @Provides
    @Singleton
    public InterceptorToken provideV1Interceptor(ApiAuthService service, PreferencesStorage preferencesStorage, Context context) {
        return new AutomaticSignUpInterceptorToken(service, preferencesStorage, context);
    }
}
