package sk.hutta.casnik

import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET

interface ServerApi {
    @GET("api/ping")
    suspend fun ping(): String
}

val serverApi: ServerApi = Retrofit.Builder()
    .baseUrl("http://10.0.2.2:8080/")
    .addConverterFactory(ScalarsConverterFactory.create())
    .build()
    .create(ServerApi::class.java)