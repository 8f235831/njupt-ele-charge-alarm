package pers.u8f23.njupt_ele_poller;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import org.jetbrains.annotations.NotNull;
import pers.u8f23.njupt_ele_poller.entity.config.RootConfig;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author 8f23
 * @create 2023/7/2-17:09
 */
public class HttpUtils
{
	public static final int TIME_OUT = 3;
	public static final String BASE_URL = "http://wxcard.njupt.edu.cn/";
	private static final Retrofit SERVICE_CREATOR;

	static
	{
		// 日志拦截器
		HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
		logging.setLevel(HttpLoggingInterceptor.Level.NONE);
		// cookie JSESSIONID
		Interceptor cookieInterceptor = new Interceptor()
		{
			@NotNull
			@Override
			public Response intercept(@NotNull Chain chain) throws IOException
			{
				final Request.Builder builder = chain.request().newBuilder();
				builder.addHeader("Cookie",
					"JSESSIONID=" + RootConfig.getReference().getJSessionId());
				return chain.proceed(builder.build());
			}
		};

		OkHttpClient homeSiteClient = new OkHttpClient.Builder()
			.addInterceptor(logging)
			.addInterceptor(cookieInterceptor)
			.connectTimeout(TIME_OUT, TimeUnit.SECONDS)
			.build();
		SERVICE_CREATOR = new Retrofit.Builder()
			.addConverterFactory(GsonConverterFactory.create())
			.baseUrl(BASE_URL)
			.addCallAdapterFactory(RxJava3CallAdapterFactory.create())
			.client(homeSiteClient)
			.build();
	}

	public static <S> S buildService(Class<S> clazz)
	{
		return SERVICE_CREATOR.create(clazz);
	}

}
