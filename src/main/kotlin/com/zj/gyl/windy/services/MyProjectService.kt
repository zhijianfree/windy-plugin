package com.zj.gyl.windy.services

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.zj.gyl.windy.MyBundle
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
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

    fun createWork(work : WorkTask) :Boolean{
        try {
            val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            val gson = Gson()
            val requestBody = gson.toJson(work).toRequestBody(mediaType)

            val client = OkHttpClient()
            val request = Request.Builder()
                .post(requestBody)
                .url("http://10.202.244.79:9768/v1/devops/work/tasks")
                .build()
            var result = client.newCall(request).execute().body?.string()
            val responseModel = gson.fromJson(result, ResponseModel::class.java)
            var dataString = gson.toJson(responseModel.data)
            val task = gson.fromJson(dataString, WorkTaskDto::class.java)
            return task?.taskId != null
        }catch (e:Exception){
            thisLogger().warn("create work task error", e)
        }
        return false
    }

    private fun requestDemandList(): PageModel<DemandDto>? {
        try {
            val client = OkHttpClient()
            val request = Request.Builder()
                .get()
                .url("http://10.202.244.79:9768/v1/devops/user/demands?size=100")
                .build()
            var result = client.newCall(request).execute().body?.string()
            val gson = Gson()
            val responseModel = gson.fromJson(result, ResponseModel::class.java)
            var dataString = gson.toJson(responseModel.data)
            val type = object : TypeToken<PageModel<DemandDto>>() {}.type
            return gson.fromJson(dataString, type)
        } catch (e: Exception) {
            thisLogger().warn("get demand list error", e)
        }
        return null
    }

    private fun requestBugList(): PageModel<BugDto>? {
        try {
            val client = OkHttpClient()
            val request = Request.Builder()
                .get()
                .url("http://10.202.244.79:9768/v1/devops/user/bugs?size=100")
                .build()
            val result = client.newCall(request).execute().body?.string()
            val gson = Gson()
            val responseModel = gson.fromJson(result, ResponseModel::class.java)
            val dataString = gson.toJson(responseModel.data)
            val type = object : TypeToken<PageModel<BugDto>>() {}.type
            return gson.fromJson(dataString, type)
        } catch (e: Exception) {
            thisLogger().warn("get bug list error", e)
        }
        return null
    }

    private fun requestWorkList(): PageModel<WorkTaskDto>? {
        try {
            val client = OkHttpClient()
            val request = Request.Builder()
                .get()
                .url("http://10.202.244.79:9768/v1/devops/work/tasks?size=100")
                .build()
            val result = client.newCall(request).execute().body?.string()
            thisLogger().warn("get work list = $result")
            val gson = Gson()
            val responseModel = gson.fromJson(result, ResponseModel::class.java)
            val dataString = gson.toJson(responseModel.data)
            val type = object : TypeToken<PageModel<WorkTaskDto>>() {}.type
            return gson.fromJson(dataString, type)
        } catch (e: Exception) {
            thisLogger().warn("get work list error", e)
        }
        return null
    }


    fun getRandomNumber() = "windy" + (1..100).random()
}
