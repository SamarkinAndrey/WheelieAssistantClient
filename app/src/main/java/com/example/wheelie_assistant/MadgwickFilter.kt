import kotlin.math.*

class MadgwickFilter(var beta: Float = 0.1f) {
  private var q0: Float = 1.0f
  private var q1: Float = 0.0f
  private var q2: Float = 0.0f
  private var q3: Float = 0.0f

  private var lastUpdate: Long = System.nanoTime()

  private var accelScale: Float = 1.0f / 16384.0f
  private var gyroScale: Float = 1.0f / 131.0f

  var pitch: Float = 0.0f
  var roll: Float = 0.0f
  var yaw: Float = 0.0f

  fun update(ax: Int, ay: Int, az: Int, gx: Int, gy: Int, gz: Int) {
    val axFloat = ax * accelScale
    val ayFloat = ay * accelScale
    val azFloat = az * accelScale
    val gxFloat = gx * gyroScale * (PI.toFloat() / 180.0f)
    val gyFloat = gy * gyroScale * (PI.toFloat() / 180.0f)
    val gzFloat = gz * gyroScale * (PI.toFloat() / 180.0f)

    updateFloat(axFloat, ayFloat, azFloat, gxFloat, gyFloat, gzFloat)
  }

  fun updateFloat(ax: Float, ay: Float, az: Float, gx: Float, gy: Float, gz: Float) {
    val currentTime = System.nanoTime()
    val deltaTime = (currentTime - lastUpdate) / 1_000_000_000.0f
    lastUpdate = currentTime

    val dt = if (deltaTime > 0.001f) deltaTime else 0.001f

    val q0 = this.q0
    val q1 = this.q1
    val q2 = this.q2
    val q3 = this.q3

    val norm = sqrt(ax * ax + ay * ay + az * az)
    val axNorm = if (norm > 0.0f) ax / norm else 0.0f
    val ayNorm = if (norm > 0.0f) ay / norm else 0.0f
    val azNorm = if (norm > 0.0f) az / norm else 0.0f

    val f1 = 2.0f * (q1 * q3 - q0 * q2) - axNorm
    val f2 = 2.0f * (q0 * q1 + q2 * q3) - ayNorm
    val f3 = 2.0f * (0.5f - q1 * q1 - q2 * q2) - azNorm

    val j11 = -2.0f * q2
    val j12 = 2.0f * q3
    val j13 = -2.0f * q0
    val j14 = 2.0f * q1
    val j21 = 2.0f * q1
    val j22 = 2.0f * q0
    val j23 = 2.0f * q3
    val j24 = 2.0f * q2
    val j31 = 0.0f
    val j32 = -4.0f * q1
    val j33 = -4.0f * q2
    val j34 = 0.0f

    val grad1 = j11 * f1 + j21 * f2 + j31 * f3
    val grad2 = j12 * f1 + j22 * f2 + j32 * f3
    val grad3 = j13 * f1 + j23 * f2 + j33 * f3
    val grad4 = j14 * f1 + j24 * f2 + j34 * f3

    val gradNorm = sqrt(grad1 * grad1 + grad2 * grad2 + grad3 * grad3 + grad4 * grad4)

    val grad1Norm = if (gradNorm > 0.0f) grad1 / gradNorm else 0.0f
    val grad2Norm = if (gradNorm > 0.0f) grad2 / gradNorm else 0.0f
    val grad3Norm = if (gradNorm > 0.0f) grad3 / gradNorm else 0.0f
    val grad4Norm = if (gradNorm > 0.0f) grad4 / gradNorm else 0.0f

    val qDot1 = 0.5f * (-q1 * gx - q2 * gy - q3 * gz) - beta * grad1Norm
    val qDot2 = 0.5f * (q0 * gx + q2 * gz - q3 * gy) - beta * grad2Norm
    val qDot3 = 0.5f * (q0 * gy - q1 * gz + q3 * gx) - beta * grad3Norm
    val qDot4 = 0.5f * (q0 * gz + q1 * gy - q2 * gx) - beta * grad4Norm

    this.q0 += qDot1 * dt
    this.q1 += qDot2 * dt
    this.q2 += qDot3 * dt
    this.q3 += qDot4 * dt

    val qNorm = sqrt(this.q0 * this.q0 + this.q1 * this.q1 + this.q2 * this.q2 + this.q3 * this.q3)
    this.q0 /= qNorm
    this.q1 /= qNorm
    this.q2 /= qNorm
    this.q3 /= qNorm

    // Обновление углов Эйлера
    this.pitch = Math.toDegrees(asin(2.0f * (this.q0 * this.q2 - this.q3 * this.q1)).toDouble()).toFloat()
    this.roll = Math.toDegrees(atan2(this.q0 * this.q1 + this.q2 * this.q3, 0.5f - this.q1 * this.q1 - this.q2 * this.q2).toDouble()).toFloat()
    this.yaw = Math.toDegrees(atan2(this.q0 * this.q3 + this.q1 * this.q2, 0.5f - this.q2 * this.q2 - this.q3 * this.q3).toDouble()).toFloat()
  }

  fun setAccelScale(range: Float) {
    this.accelScale = 1.0f / (32768.0f / range)
  }

  fun setGyroScale(range: Float) {
    this.gyroScale = 1.0f / (32768.0f / range)
  }

  fun reset() {
    q0 = 1.0f
    q1 = 0.0f
    q2 = 0.0f
    q3 = 0.0f
    pitch = 0.0f
    roll = 0.0f
    yaw = 0.0f
    lastUpdate = System.nanoTime()
  }
}