package cn.enaium

import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.graphics.asComposeCanvas
import androidx.compose.ui.scene.CanvasLayersComposeScene
import androidx.compose.ui.scene.ComposeScene
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import org.jetbrains.skia.*
import org.jetbrains.skia.FramebufferFormat.Companion.GR_GL_RGBA8
import org.jetbrains.skiko.FrameDispatcher
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL30.GL_FRAMEBUFFER
import org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_BINDING
import org.lwjgl.opengl.GL30.glBindFramebuffer
import org.lwjgl.system.MemoryUtil.NULL

/**
 * @author Enaium
 */
@OptIn(InternalComposeUiApi::class)
fun main() {
    GLFWErrorCallback.createPrint(System.err).set()
    if (!glfwInit())
        throw IllegalStateException("Unable to initialize GLFW")

    glfwDefaultWindowHints() // optional, the current window hints are already the default
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE) // the window will stay hidden after creation
    glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE) // the window will be resizable

    var width = 800
    var height = 600
    val window = glfwCreateWindow(width, height, "compose multiplatform lwjgl!", NULL, NULL)

    // Make the OpenGL context current
    glfwMakeContextCurrent(window)
    // Enable v-sync
    glfwSwapInterval(1)

    GL.createCapabilities()

    val context = DirectContext.makeGL()
    var surface = createSurface(width, height, context)

    val glfwDispatcher = GlfwCoroutineDispatcher()
    lateinit var composeScene: ComposeScene

    fun render() {
        surface?.canvas?.clear(Color.WHITE)
        composeScene.size = IntSize(width, height)
        surface?.canvas?.asComposeCanvas()?.also {
            composeScene.render(it, System.nanoTime())
        }
        context.flush()
    }

    val density = Density(1f)
    composeScene =
        CanvasLayersComposeScene(
            density = density,
            coroutineContext = glfwDispatcher
        )

    glfwSetWindowSizeCallback(window) { _, windowWidth, windowHeight ->
        width = windowWidth
        height = windowHeight
        surface?.close()
        surface = createSurface(width, height, context)
    }

    composeScene.subscribeToGLFWEvents(window)
    composeScene.setContent { App() }

    // Make the window visible
    glfwShowWindow(window)

    // Set the clear color
    glClearColor(0.0f, 0.0f, 0.0f, 0.0f)

    while (!glfwWindowShouldClose(window)) {

        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        render()

        glfwSwapBuffers(window) // swap the color buffers

        // Poll for window events. The key callback above will only be
        // invoked during this call.
        glfwPollEvents()
    }

    composeScene.close()
    glfwDestroyWindow(window)
}


private fun createSurface(width: Int, height: Int, context: DirectContext): Surface? {
    val fbId = glGetInteger(GL_FRAMEBUFFER_BINDING)
    val renderTarget = BackendRenderTarget.makeGL(width, height, 0, 8, fbId, GR_GL_RGBA8)
    return Surface.makeFromBackendRenderTarget(
        context, renderTarget, SurfaceOrigin.BOTTOM_LEFT, SurfaceColorFormat.RGBA_8888, ColorSpace.sRGB
    )
}