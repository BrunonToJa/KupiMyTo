package pl.kupimyto.app

import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pl.kupimyto.app.data.remote.KupimytoApi
import pl.kupimyto.app.data.remote.RemoteKupimytoRepository
import pl.kupimyto.app.data.remote.TokenHolder
import pl.kupimyto.app.data.repository.FakeKupimytoRepository
import pl.kupimyto.app.data.repository.KupimytoRepository
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import android.content.Context
import pl.kupimyto.app.data.SettingsStore

/** Ręczne DI. Wybór źródła danych: BuildConfig.USE_FAKE_DATA (app/build.gradle.kts). */
class AppContainer(context: Context) {
    val settings = SettingsStore(context.applicationContext)
    val repository: KupimytoRepository =
        if (BuildConfig.USE_FAKE_DATA) FakeKupimytoRepository() else createRemote()

    private fun createRemote(): KupimytoRepository {
        val tokens = TokenHolder()
        val client = OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                val req = chain.request().newBuilder()
                tokens.token?.let { req.header("Authorization", "Bearer $it") }
                chain.proceed(req.build())
            })
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
            .build()
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            encodeDefaults = true
        }
        val api = Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(KupimytoApi::class.java)
        return RemoteKupimytoRepository(api, tokens)
    }
}
