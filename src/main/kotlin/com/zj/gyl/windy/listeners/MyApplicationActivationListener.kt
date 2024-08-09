package com.zj.gyl.windy.listeners

import com.intellij.openapi.application.ApplicationActivationListener
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.wm.IdeFrame
import com.zj.gyl.windy.services.MyProjectService
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.concurrent.thread

internal class MyApplicationActivationListener : ApplicationActivationListener {

    override fun applicationActivated(ideFrame: IdeFrame) {
        thisLogger().warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.")
//        // 获取当前打开的项目
//        val project: Project? = ProjectManager.getInstance().openProjects.firstOrNull()
//
//        project?.let {
//            // 获取 MyProjectService 实例
//            val myService = it.service<MyProjectService>()
//            myService.asyncInitData()
//        }
    }
}
