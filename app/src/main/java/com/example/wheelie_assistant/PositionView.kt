//package com.app.wheelie_assistant
//
//import android.content.Context
//import android.opengl.GLES20
//import android.opengl.GLSurfaceView
//import android.opengl.Matrix
//import android.util.AttributeSet
//import javax.microedition.khronos.egl.EGLConfig
//import javax.microedition.khronos.opengles.GL10
//
//class AttitudeView(context: Context, attrs: AttributeSet?) : GLSurfaceView(context, attrs) {
//
//    private var renderer: AttitudeRenderer
//
//    var roll: Float = 0f
//        set(value) {
//            field = value
//            renderer.roll = value
//        }
//
//    var pitch: Float = 0f
//        set(value) {
//            field = value
//            renderer.pitch = value
//        }
//
//    init {
//        setEGLContextClientVersion(2)
//        renderer = AttitudeRenderer()
//        setRenderer(renderer)
//        renderMode = RENDERMODE_CONTINUOUSLY
//    }
//}
//
//class AttitudeRenderer : GLSurfaceView.Renderer {
//
//    var roll: Float = 0f
//    var pitch: Float = 0f
//
//    private val vPMatrix = FloatArray(16)
//    private val projectionMatrix = FloatArray(16)
//    private val viewMatrix = FloatArray(16)
//    private val rotationMatrix = FloatArray(16)
//
//    private lateinit var aircraftArrow: AircraftTriangle
//    private lateinit var crosshair: Crosshair
//
//    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
//        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
//        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
//
//        aircraftArrow = AircraftTriangle()
//        crosshair = Crosshair()
//    }
//
//    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
//        GLES20.glViewport(0, 0, width, height)
//        val ratio = width.toFloat() / height.toFloat()
//        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 1f, 10f)
//    }
//
//    override fun onDrawFrame(gl: GL10?) {
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
//
//        // Камера смотрит строго сзади (по оси Z)
//        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
//        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
//
//        // Сначала рисуем статическое перекрестие
//        crosshair.draw(vPMatrix)
//
//        // Затем рисуем стрелку самолета, которая вращается
//        Matrix.setIdentityM(rotationMatrix, 0)
//        Matrix.rotateM(rotationMatrix, 0, roll, 0f, 0f, 1f) // Крен вокруг Z
//        Matrix.rotateM(rotationMatrix, 0, pitch, 1f, 0f, 0f) // Тангаж вокруг X
//
//        aircraftArrow.draw(vPMatrix, rotationMatrix)
//    }
//}
//
//// Класс для статического перекрестия (указателей горизонта/вертикали)
//class Crosshair {
//    private var program: Int
//    private var positionHandle: Int
//    private var colorHandle: Int
//    private var mvpMatrixHandle: Int
//
//    private val vertexBuffer: java.nio.FloatBuffer
//    private val colorBuffer: java.nio.FloatBuffer
//
//    // Перекрестие в виде линий горизонта и вертикали БЕЗ отметок
//    // Линии не доходят до центра, оставляя место для стрелки (радиус 0.6)
//    private val vertices = floatArrayOf(
//        // Горизонтальная линия - левая часть (от -1.2 до -0.8)
//        -1.6f, 0.0f, 0.0f,   -0.8f, 0.0f, 0.0f,
//
//        // Горизонтальная линия - правая часть (от 0.8 до 1.2)
//        0.8f, 0.0f, 0.0f,    1.6f, 0.0f, 0.0f,
//
//        // Вертикальная линия - нижняя часть (от -1.2 до -0.8)
//        0.0f, -1.6f, 0.0f,   0.0f, -0.8f, 0.0f,
//
//        // Вертикальная линия - верхняя часть (от 0.8 до 1.2)
//        0.0f, 0.8f, 0.0f,    0.0f, 1.6f, 0.0f
//    )
//
//    // Белый цвет для всего перекрестия
//    private val colors = floatArrayOf(
//        1.0f, 1.0f, 1.0f, 1.0f,  // начало левой горизонтали
//        1.0f, 1.0f, 1.0f, 1.0f,  // конец левой горизонтали
//        1.0f, 1.0f, 1.0f, 1.0f,  // начало правой горизонтали
//        1.0f, 1.0f, 1.0f, 1.0f,  // конец правой горизонтали
//        1.0f, 1.0f, 1.0f, 1.0f,  // начало нижней вертикали
//        1.0f, 1.0f, 1.0f, 1.0f,  // конец нижней вертикали
//        1.0f, 1.0f, 1.0f, 1.0f,  // начало верхней вертикали
//        1.0f, 1.0f, 1.0f, 1.0f   // конец верхней вертикали
//    )
//
//    init {
//        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, """
//            attribute vec4 vPosition;
//            uniform mat4 uMVPMatrix;
//            attribute vec4 aColor;
//            varying vec4 vColor;
//            void main() {
//                gl_Position = uMVPMatrix * vPosition;
//                vColor = aColor;
//            }
//        """.trimIndent())
//
//        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, """
//            precision mediump float;
//            varying vec4 vColor;
//            void main() {
//                gl_FragColor = vColor;
//            }
//        """.trimIndent())
//
//        program = GLES20.glCreateProgram()
//        GLES20.glAttachShader(program, vertexShader)
//        GLES20.glAttachShader(program, fragmentShader)
//        GLES20.glLinkProgram(program)
//
//        positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
//        colorHandle = GLES20.glGetAttribLocation(program, "aColor")
//        mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
//
//        vertexBuffer = java.nio.ByteBuffer.allocateDirect(vertices.size * 4)
//            .order(java.nio.ByteOrder.nativeOrder())
//            .asFloatBuffer()
//            .apply {
//                put(vertices)
//                position(0)
//            }
//
//        colorBuffer = java.nio.ByteBuffer.allocateDirect(colors.size * 4)
//            .order(java.nio.ByteOrder.nativeOrder())
//            .asFloatBuffer()
//            .apply {
//                put(colors)
//                position(0)
//            }
//    }
//
//    fun draw(vPMatrix: FloatArray) {
//        GLES20.glUseProgram(program)
//
//        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, vPMatrix, 0)
//
//        GLES20.glEnableVertexAttribArray(positionHandle)
//        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)
//
//        GLES20.glEnableVertexAttribArray(colorHandle)
//        GLES20.glVertexAttribPointer(colorHandle, 4, GLES20.GL_FLOAT, false, 0, colorBuffer)
//
//        // Рисуем все линии перекрестия
//        GLES20.glLineWidth(2.0f)
//        GLES20.glDrawArrays(GLES20.GL_LINES, 0, vertices.size / 3)
//
//        GLES20.glDisableVertexAttribArray(positionHandle)
//    }
//
//    private fun loadShader(type: Int, shaderCode: String): Int {
//        val shader = GLES20.glCreateShader(type)
//        GLES20.glShaderSource(shader, shaderCode)
//        GLES20.glCompileShader(shader)
//        return shader
//    }
//}
//
//class AircraftTriangle {
//    private var program: Int
//    private var positionHandle: Int
//    private var colorHandle: Int
//    private var mvpMatrixHandle: Int
//
//    private val vertexBuffer: java.nio.FloatBuffer
//    private val colorBuffer: java.nio.FloatBuffer
//
//    // Равнобедренный треугольник, направленный вперед (по оси Z)
//    // При roll=0, pitch=0 виден как горизонтальная линия (вид сзади)
//    private val vertices = floatArrayOf(
//        // Вершины треугольника
//        0.0f, 0.0f, 1.0f,   // Нос (верхняя точка) - вперед по Z
//        -0.5f, 0.0f, 0.0f,  // Левое крыло
//        0.5f, 0.0f, 0.0f    // Правое крыло
//    )
//
//    // Белый цвет для всего треугольника
//    private val colors = floatArrayOf(
//        1.0f, 1.0f, 1.0f, 1.0f,  // нос
//        1.0f, 1.0f, 1.0f, 1.0f,  // левое крыло
//        1.0f, 1.0f, 1.0f, 1.0f   // правое крыло
//    )
//
//    init {
//        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
//        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
//
//        program = GLES20.glCreateProgram()
//        GLES20.glAttachShader(program, vertexShader)
//        GLES20.glAttachShader(program, fragmentShader)
//        GLES20.glLinkProgram(program)
//
//        positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
//        colorHandle = GLES20.glGetAttribLocation(program, "aColor")
//        mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
//
//        vertexBuffer = java.nio.ByteBuffer.allocateDirect(vertices.size * 4)
//            .order(java.nio.ByteOrder.nativeOrder())
//            .asFloatBuffer()
//            .apply {
//                put(vertices)
//                position(0)
//            }
//
//        colorBuffer = java.nio.ByteBuffer.allocateDirect(colors.size * 4)
//            .order(java.nio.ByteOrder.nativeOrder())
//            .asFloatBuffer()
//            .apply {
//                put(colors)
//                position(0)
//            }
//    }
//
//    fun draw(vPMatrix: FloatArray, rotationMatrix: FloatArray) {
//        GLES20.glUseProgram(program)
//
//        val mvpMatrix = FloatArray(16)
//        Matrix.multiplyMM(mvpMatrix, 0, vPMatrix, 0, rotationMatrix, 0)
//
//        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
//
//        GLES20.glEnableVertexAttribArray(positionHandle)
//        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)
//
//        GLES20.glEnableVertexAttribArray(colorHandle)
//        GLES20.glVertexAttribPointer(colorHandle, 4, GLES20.GL_FLOAT, false, 0, colorBuffer)
//
//        // Рисуем треугольник
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3)
//
//        // Рисуем контур треугольника (линии)
//        GLES20.glLineWidth(2.0f)
//        GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, 3)
//
//        GLES20.glDisableVertexAttribArray(positionHandle)
//    }
//
//    private fun loadShader(type: Int, shaderCode: String): Int {
//        val shader = GLES20.glCreateShader(type)
//        GLES20.glShaderSource(shader, shaderCode)
//        GLES20.glCompileShader(shader)
//        return shader
//    }
//
//    companion object {
//        const val vertexShaderCode = """
//            attribute vec4 vPosition;
//            uniform mat4 uMVPMatrix;
//            attribute vec4 aColor;
//            varying vec4 vColor;
//            void main() {
//                gl_Position = uMVPMatrix * vPosition;
//                vColor = aColor;
//            }
//        """
//
//        const val fragmentShaderCode = """
//            precision mediump float;
//            varying vec4 vColor;
//            void main() {
//                gl_FragColor = vColor;
//            }
//        """
//    }
//}
package com.app.wheelie_assistant

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.AttributeSet
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class PositionView(context: Context, attrs: AttributeSet?) : GLSurfaceView(context, attrs) {

  private var renderer: PositionRenderer

  var roll: Float = 0f
    set(value) {
      field = value
      renderer.roll = value
    }

  var pitch: Float = 0f
    set(value) {
      field = value
      renderer.pitch = value
    }

  init {
    setEGLContextClientVersion(2)
    renderer = PositionRenderer()
    setRenderer(renderer)
    renderMode = RENDERMODE_CONTINUOUSLY
  }
}

class PositionRenderer : GLSurfaceView.Renderer {

  var roll: Float = 0f
  var pitch: Float = 0f

  private val vPMatrix = FloatArray(16)
  private val projectionMatrix = FloatArray(16)
  private val viewMatrix = FloatArray(16)
  private val rotationMatrix = FloatArray(16)

  private lateinit var aircraftArrow: ObjectTriangle
  private lateinit var crosshair: Crosshair

  override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
    GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
    GLES20.glEnable(GLES20.GL_DEPTH_TEST)

    aircraftArrow = ObjectTriangle()
    crosshair = Crosshair()
  }

  override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
    GLES20.glViewport(0, 0, width, height)
    val ratio = width.toFloat() / height.toFloat()
    Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 1f, 10f)
  }

  override fun onDrawFrame(gl: GL10?) {
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

    // Камера смотрит строго сзади (по оси Z)
    Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
    Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

    // Сначала рисуем статическое перекрестие
    crosshair.draw(vPMatrix)

    // Затем рисуем стрелку самолета, которая вращается
    Matrix.setIdentityM(rotationMatrix, 0)
    Matrix.rotateM(rotationMatrix, 0, roll, 0f, 0f, 1f) // Крен вокруг Z
    Matrix.rotateM(rotationMatrix, 0, -pitch, 1f, 0f, 0f) // Тангаж вокруг X

    aircraftArrow.draw(vPMatrix, rotationMatrix)
  }
}

// Класс для цифровых обозначений углов
class AngleLabels {
  private var program: Int
  private var positionHandle: Int
  private var colorHandle: Int
  private var mvpMatrixHandle: Int

  private val vertexBuffer: java.nio.FloatBuffer
  private val colorBuffer: java.nio.FloatBuffer

  init {
    val vertexShader = loadShader(
      GLES20.GL_VERTEX_SHADER, """
            attribute vec4 vPosition;
            uniform mat4 uMVPMatrix;
            attribute vec4 aColor;
            varying vec4 vColor;
            void main() {
                gl_Position = uMVPMatrix * vPosition;
                vColor = aColor;
            }
        """.trimIndent()
    )

    val fragmentShader = loadShader(
      GLES20.GL_FRAGMENT_SHADER, """
            precision mediump float;
            varying vec4 vColor;
            void main() {
                gl_FragColor = vColor;
            }
        """.trimIndent()
    )

    program = GLES20.glCreateProgram()
    GLES20.glAttachShader(program, vertexShader)
    GLES20.glAttachShader(program, fragmentShader)
    GLES20.glLinkProgram(program)

    positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
    colorHandle = GLES20.glGetAttribLocation(program, "aColor")
    mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")

    // Создаем упрощенные цифры (просто точки для демонстрации)
    val vertices = createSimpleDigitVertices()

    vertexBuffer =
      java.nio.ByteBuffer.allocateDirect(vertices.size * 4).order(java.nio.ByteOrder.nativeOrder())
        .asFloatBuffer().apply {
          put(vertices)
          position(0)
        }

    // Белый цвет для всех цифр
    val colors = FloatArray(vertices.size / 3 * 4) {
      1.0f; 1.0f; 1.0f; 1.0f
    }

    colorBuffer =
      java.nio.ByteBuffer.allocateDirect(colors.size * 4).order(java.nio.ByteOrder.nativeOrder())
        .asFloatBuffer().apply {
          put(colors)
          position(0)
        }
  }

  fun draw(vPMatrix: FloatArray, roll: Float, pitch: Float) {
    GLES20.glUseProgram(program)
    GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, vPMatrix, 0)

    GLES20.glEnableVertexAttribArray(positionHandle)
    GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)

    GLES20.glEnableVertexAttribArray(colorHandle)
    GLES20.glVertexAttribPointer(colorHandle, 4, GLES20.GL_FLOAT, false, 0, colorBuffer)

    // Рисуем обозначения углов
    GLES20.glLineWidth(1.5f)
    GLES20.glDrawArrays(GLES20.GL_LINES, 0, vertexBuffer.capacity() / 3)

    GLES20.glDisableVertexAttribArray(positionHandle)
  }

  private fun createSimpleDigitVertices(): FloatArray {
    val vertices = mutableListOf<Float>()

    // Упрощенные обозначения - просто линии для демонстрации
    // Угол крена справа (горизонтально)
    addSimpleText(vertices, "ROLL", 1.3f, 0.0f, 0.08f)

    // Угол тангажа сверху (горизонтально)
    addSimpleText(vertices, "PITCH", 0.0f, 1.3f, 0.08f)

    return vertices.toFloatArray()
  }

  private fun addSimpleText(
    vertices: MutableList<Float>, text: String, x: Float, y: Float, size: Float
  ) {
    // Упрощенная реализация - просто горизонтальная линия с текстом
    when (text) {
      "ROLL" -> {
        // Горизонтальная линия для ROLL
        addLine(vertices, x, y, x + size * 3, y)
      }

      "PITCH" -> {
        // Горизонтальная линия для PITCH
        addLine(vertices, x, y, x + size * 3, y)
      }
    }
  }

  private fun addLine(vertices: MutableList<Float>, x1: Float, y1: Float, x2: Float, y2: Float) {
    vertices.addAll(listOf(x1, y1, 0.0f, x2, y2, 0.0f))
  }

  private fun loadShader(type: Int, shaderCode: String): Int {
    val shader = GLES20.glCreateShader(type)
    GLES20.glShaderSource(shader, shaderCode)
    GLES20.glCompileShader(shader)
    return shader
  }
}

// Класс для статического перекрестия (указателей горизонта/вертикали)
class Crosshair {
  private var program: Int
  private var positionHandle: Int
  private var colorHandle: Int
  private var mvpMatrixHandle: Int

  private val vertexBuffer: java.nio.FloatBuffer
  private val colorBuffer: java.nio.FloatBuffer

  // Перекрестие в виде линий горизонта и вертикали БЕЗ отметок
  private val vertices = floatArrayOf(
    // Горизонтальная линия - левая часть (от -1.6 до -0.8)
    -2.7f, 0.0f, 0.0f, -1.7f, 0.0f, 0.0f,

    // Горизонтальная линия - правая часть (от 0.8 до 1.6)
    1.7f, 0.0f, 0.0f, 2.7f, 0.0f, 0.0f,

    // Вертикальная линия - нижняя часть (от -1.6 до -0.8)
    0.0f, -2.7f, 0.0f, 0.0f, -1.7f, 0.0f,

    // Вертикальная линия - верхняя часть (от 0.8 до 1.6)
    0.0f, 1.7f, 0.0f, 0.0f, 2.7f, 0.0f
  )

  // Белый цвет для всего перекрестия
  private val colors = floatArrayOf(
    1.0f, 1.0f, 1.0f, 1.0f,  // начало левой горизонтали
    1.0f, 1.0f, 1.0f, 1.0f,  // конец левой горизонтали
    1.0f, 1.0f, 1.0f, 1.0f,  // начало правой горизонтали
    1.0f, 1.0f, 1.0f, 1.0f,  // конец правой горизонтали
    1.0f, 1.0f, 1.0f, 1.0f,  // начало нижней вертикали
    1.0f, 1.0f, 1.0f, 1.0f,  // конец нижней вертикали
    1.0f, 1.0f, 1.0f, 1.0f,  // начало верхней вертикали
    1.0f, 1.0f, 1.0f, 1.0f   // конец верхней вертикали
  )

  init {
    val vertexShader = loadShader(
      GLES20.GL_VERTEX_SHADER, """
            attribute vec4 vPosition;
            uniform mat4 uMVPMatrix;
            attribute vec4 aColor;
            varying vec4 vColor;
            void main() {
                gl_Position = uMVPMatrix * vPosition;
                vColor = aColor;
            }
        """.trimIndent()
    )

    val fragmentShader = loadShader(
      GLES20.GL_FRAGMENT_SHADER, """
            precision mediump float;
            varying vec4 vColor;
            void main() {
                gl_FragColor = vColor;
            }
        """.trimIndent()
    )

    program = GLES20.glCreateProgram()
    GLES20.glAttachShader(program, vertexShader)
    GLES20.glAttachShader(program, fragmentShader)
    GLES20.glLinkProgram(program)

    positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
    colorHandle = GLES20.glGetAttribLocation(program, "aColor")
    mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")

    vertexBuffer =
      java.nio.ByteBuffer.allocateDirect(vertices.size * 4).order(java.nio.ByteOrder.nativeOrder())
        .asFloatBuffer().apply {
          put(vertices)
          position(0)
        }

    colorBuffer =
      java.nio.ByteBuffer.allocateDirect(colors.size * 4).order(java.nio.ByteOrder.nativeOrder())
        .asFloatBuffer().apply {
          put(colors)
          position(0)
        }
  }

  fun draw(vPMatrix: FloatArray) {
    GLES20.glUseProgram(program)

    GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, vPMatrix, 0)

    GLES20.glEnableVertexAttribArray(positionHandle)
    GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)

    GLES20.glEnableVertexAttribArray(colorHandle)
    GLES20.glVertexAttribPointer(colorHandle, 4, GLES20.GL_FLOAT, false, 0, colorBuffer)

    // Рисуем все линии перекрестия
    GLES20.glLineWidth(2.0f)
    GLES20.glDrawArrays(GLES20.GL_LINES, 0, vertices.size / 3)

    GLES20.glDisableVertexAttribArray(positionHandle)
  }

  private fun loadShader(type: Int, shaderCode: String): Int {
    val shader = GLES20.glCreateShader(type)
    GLES20.glShaderSource(shader, shaderCode)
    GLES20.glCompileShader(shader)
    return shader
  }
}

class ObjectTriangle {
  private var program: Int
  private var positionHandle: Int
  private var colorHandle: Int
  private var mvpMatrixHandle: Int

  private val vertexBuffer: java.nio.FloatBuffer
  private val colorBuffer: java.nio.FloatBuffer

  // Равнобедренный треугольник, направленный вперед (по оси Z)
  // При roll=0, pitch=0 виден как горизонтальная линия (вид сзади)

//    private val vertices = floatArrayOf(
//        // Вершины треугольника
//        0.0f, 0.0f, 1.0f,   // Нос (верхняя точка) - вперед по Z
//        -0.5f, 0.0f, 0.0f,  // Левое крыло
//        0.5f, 0.0f, 0.0f    // Правое крыло
//    )

  private val vertices = floatArrayOf(
    // Вершины треугольника
    0.0f, 0.0f, 1.5f,   // Нос (верхняя точка) - вперед по Z
    -1.0f, 0.0f, 0.0f,  // Левое крыло
    1.0f, 0.0f, 0.0f    // Правое крыло
  )

  // Белый цвет для всего треугольника
  private val colors = floatArrayOf(
    1.0f, 1.0f, 1.0f, 1.0f,  // нос
    1.0f, 1.0f, 1.0f, 1.0f,  // левое крыло
    1.0f, 1.0f, 1.0f, 1.0f   // правое крыло
  )

  init {
    val vertexShader = loadShader(
      GLES20.GL_VERTEX_SHADER, """
            attribute vec4 vPosition;
            uniform mat4 uMVPMatrix;
            attribute vec4 aColor;
            varying vec4 vColor;
            void main() {
                gl_Position = uMVPMatrix * vPosition;
                vColor = aColor;
            }
        """.trimIndent()
    )

    val fragmentShader = loadShader(
      GLES20.GL_FRAGMENT_SHADER, """
            precision mediump float;
            varying vec4 vColor;
            void main() {
                gl_FragColor = vColor;
            }
        """.trimIndent()
    )

    program = GLES20.glCreateProgram()
    GLES20.glAttachShader(program, vertexShader)
    GLES20.glAttachShader(program, fragmentShader)
    GLES20.glLinkProgram(program)

    positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
    colorHandle = GLES20.glGetAttribLocation(program, "aColor")
    mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")

    vertexBuffer =
      java.nio.ByteBuffer.allocateDirect(vertices.size * 4).order(java.nio.ByteOrder.nativeOrder())
        .asFloatBuffer().apply {
          put(vertices)
          position(0)
        }

    colorBuffer =
      java.nio.ByteBuffer.allocateDirect(colors.size * 4).order(java.nio.ByteOrder.nativeOrder())
        .asFloatBuffer().apply {
          put(colors)
          position(0)
        }
  }

  fun draw(vPMatrix: FloatArray, rotationMatrix: FloatArray) {
    GLES20.glUseProgram(program)

    val mvpMatrix = FloatArray(16)
    Matrix.multiplyMM(mvpMatrix, 0, vPMatrix, 0, rotationMatrix, 0)

    GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)

    GLES20.glEnableVertexAttribArray(positionHandle)
    GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)

    GLES20.glEnableVertexAttribArray(colorHandle)
    GLES20.glVertexAttribPointer(colorHandle, 4, GLES20.GL_FLOAT, false, 0, colorBuffer)

    // Рисуем треугольник
    GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3)

    // Рисуем контур треугольника (линии)
    GLES20.glLineWidth(2.0f)
    GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, 3)

    GLES20.glDisableVertexAttribArray(positionHandle)
  }

  private fun loadShader(type: Int, shaderCode: String): Int {
    val shader = GLES20.glCreateShader(type)
    GLES20.glShaderSource(shader, shaderCode)
    GLES20.glCompileShader(shader)
    return shader
  }
}