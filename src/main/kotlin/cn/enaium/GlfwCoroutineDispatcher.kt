package cn.enaium

import kotlinx.coroutines.CoroutineDispatcher
import org.lwjgl.glfw.GLFW
import kotlin.coroutines.CoroutineContext

class GlfwCoroutineDispatcher : CoroutineDispatcher() {
    private val tasks = mutableListOf<Runnable>()

    fun emitTasks() {
        val toRun = synchronized(tasks) {
            val list = tasks.toList()
            tasks.clear()
            list
        }
        toRun.forEach { it.run() }
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        synchronized(tasks) {
            tasks.add(block)
        }
        GLFW.glfwPostEmptyEvent()
    }
}