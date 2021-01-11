package ch.ethz.ikg.rideshare.comm

import android.content.Intent
import android.os.Handler
import android.util.Log
import android.widget.Toast
import ch.ethz.ikg.rideshare.Constants
import ch.ethz.ikg.rideshare.data.repos.UserRepository
import ch.ethz.ikg.rideshare.util.Result
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

object ServerComm {

    val JSONMediaType = "application/json; charset=utf-8".toMediaType()

    fun getServer(url: String, userRepository: UserRepository): Result<JSONObject> {
        return try {
            val client = OkHttpClient()

            val request = Request.Builder()
                .url(url)
                .get()
            val authRequest = wrapRequestWithTokenAuth(request, userRepository)

            val response = client.newCall(authRequest.build()).execute()

            // Handle common error cases.
            if (response.code == 401) {
                Result.Error(IOException("User not authorized. Should redirect to login."))
            } else {
                Result.Success(JSONObject(response.body!!.string()))
            }
        } catch (e: Throwable) {
            Result.Error(IOException("Error requesting $url.", e))
        }
    }

    fun postServer(url: String, jsonData: String, userRepository: UserRepository): Result<JSONObject> {
        return try {
            val client = OkHttpClient()

            val body = jsonData.toRequestBody(JSONMediaType)
            val request = Request.Builder()
                .url(url)
                .post(body)
            val authRequest = wrapRequestWithTokenAuth(request, userRepository)

            val response = client.newCall(authRequest.build()).execute()

            // Handle common error cases.
            if (response.code == 401) {
                Result.Error(IOException("User not authorized. Should redirect to login."))
            } else {
                Result.Success(JSONObject(response.body!!.string()))
            }
        } catch (e: Throwable) {
            Result.Error(IOException("Error requesting $url.", e))
        }
    }

    fun wrapRequestWithTokenAuth(requestBuilder: Request.Builder, userRepository: UserRepository): Request.Builder {
        // Get user from in-memory repository.
        if (userRepository.user.value != null) {
            userRepository.user.value?.token.let { token ->
                requestBuilder.addHeader("Authorization", "Token $token")
            }
        }
        return requestBuilder
    }
}
