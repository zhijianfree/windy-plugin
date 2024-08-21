package com.zj.gyl.windy.services

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.zj.gyl.windy.MyBundle
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.reflect.Type
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread


@Service(Service.Level.PROJECT)
class MyProjectService(project: Project) {
    var demandPage: PageModel<DemandDto>? = PageModel(0, ArrayList())
    var bugPage: PageModel<BugDto>? = PageModel(0, ArrayList())
    var workPage: PageModel<WorkTaskDto>? = PageModel(0, ArrayList())

    init {
        thisLogger().info(MyBundle.message("projectService", project.name))
    }

    fun asyncDemandData(listener: DataLoadListener) {
        thread {
            demandPage = requestDemandList()
            thisLogger().warn("加载完成数据了" + demandPage!!.total)
            listener.load()
        }
    }

    fun asyncBugData(listener: DataLoadListener) {
        thread {
            bugPage = requestBugList()
            thisLogger().warn("加载完成数据了" + bugPage!!.total)
            listener.load()
        }
    }
    fun asyncWorkData(listener: DataLoadListener) {
        thread {
            workPage = requestWorkList()
            thisLogger().warn("加载完成数据了" + workPage!!.total)
            listener.load()
        }
    }

    fun createWork(work: WorkTask): Boolean {
        val gson = Gson()
        val urlString = "http://10.202.244.79:9768/v1/devops/work/tasks"
        var connection: HttpURLConnection? = null
        try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection?
            connection!!.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.doOutput = true
            val requestBody = gson.toJson(work)
            connection.outputStream.use { os ->
                os.write(requestBody.toByteArray(charset("UTF-8")))
                os.flush()
            }
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                    val result = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        result.append(line)
                    }
                    val responseModel =
                        gson.fromJson(result.toString(), ResponseModel::class.java)
                    val dataString = gson.toJson(responseModel.data)
                    val task = gson.fromJson(dataString, WorkTaskDto::class.java)
                    return task?.taskId != null
                }
            } else {
                System.err.println("Request failed with response code: $responseCode")
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            System.err.println("create work task error")
        } finally {
            connection?.disconnect()
        }
        return false

//        val gson = Gson()
//        val requestBody = gson.toJson(work)
//        val url = "http://10.202.244.79:9768/v1/devops/work/tasks"
//
//        return try {
//            val client = HttpClient()
//            val request = HttpRequest.Builder()
//                .url(url)
//                .post(requestBody.toByteArray()) // Convert JSON string to ByteArray
//                .header("Content-Type", "application/json; charset=utf-8")
//                .build()
//
//            val response: HttpResponse = client.newCall(request).execute()
//
//            val result = response.body?.string()
//            val responseModel = gson.fromJson(result, ResponseModel::class.java)
//            val dataString = gson.toJson(responseModel.data)
//            val task = gson.fromJson(dataString, WorkTaskDto::class.java)
//            task?.taskId != null
//        } catch (e: Exception) {
//            thisLogger().warn("create work task error", e)
//            false
//        }
    }

    private fun requestDemandList(): PageModel<DemandDto>? {
        val urlString = "http://10.202.244.79:9768/v1/devops/user/demands?size=100"
        var connection: HttpURLConnection? = null
        val gson = Gson()

        try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection!!.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val result = java.lang.StringBuilder()
                BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        result.append(line)
                    }
                }
                val responseJson = result.toString()
                val responseModel = gson.fromJson(responseJson, ResponseModel::class.java)
                val dataString = gson.toJson(responseModel.data)
                val type: Type = object : TypeToken<PageModel<DemandDto?>?>() {}.type
                return gson.fromJson(dataString, type)
            } else {
                System.err.println("Request failed with response code: $responseCode")
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            System.err.println("get demand list error")
        } finally {
            connection?.disconnect()
        }
        return null
    }

    private fun requestBugList(): PageModel<BugDto>? {
        val urlString = "http://10.202.244.79:9768/v1/devops/user/bugs?size=100"
        var connection: HttpURLConnection? = null
        val gson = Gson()

        try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection!!.requestMethod = "GET"
            connection!!.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            val responseCode = connection!!.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val result = java.lang.StringBuilder()
                BufferedReader(InputStreamReader(connection!!.inputStream)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        result.append(line)
                    }
                }
                val responseJson = result.toString()
                val responseModel = gson.fromJson(responseJson, ResponseModel::class.java)
                val dataString = gson.toJson(responseModel.data)
                val type = object : TypeToken<PageModel<BugDto?>?>() {}.type
                return gson.fromJson<Any>(dataString, type) as PageModel<BugDto>?
            } else {
                System.err.println("Request failed with response code: $responseCode")
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            System.err.println("get bug list error")
        } finally {
            connection?.disconnect()
        }
        return null
    }

    private fun requestWorkList(): PageModel<WorkTaskDto>? {
        val urlString = "http://10.202.244.79:9768/v1/devops/work/tasks?size=100"
        var connection: HttpURLConnection? = null
        val gson = Gson()

        try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection!!.requestMethod = "GET"
            connection!!.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            val responseCode = connection!!.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val result = java.lang.StringBuilder()
                BufferedReader(InputStreamReader(connection!!.inputStream)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        result.append(line)
                    }
                }
                val responseJson = result.toString()
                // Log the result (similar to `thisLogger().warn("get work list = $result")`)
                println("get work list = $responseJson")
                val responseModel = gson.fromJson(responseJson, ResponseModel::class.java)
                val dataString = gson.toJson(responseModel.data)
                val type = object : TypeToken<PageModel<WorkTaskDto?>?>() {}.type
                return gson.fromJson<Any>(dataString, type) as PageModel<WorkTaskDto>?
            } else {
                System.err.println("Request failed with response code: $responseCode")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            System.err.println("get work list error")
        } finally {
            connection?.disconnect()
        }
        return null
    }


    fun getRandomNumber() = "windy" + (1..100).random()
}
