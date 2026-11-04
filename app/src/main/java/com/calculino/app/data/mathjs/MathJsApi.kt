package com.calculino.app.data.mathjs

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface MathJsApi {
    @FormUrlEncoded
    @POST("v4/")
    suspend fun evaluate(
        @Field("expr") expression: String,
        @Field("precision") precision: Int,
    ): Response<ResponseBody>
}
