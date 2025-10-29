package com.example.wheelie_assistant

import kotlin.math.*

class KalmanFilter {
  private var x = Matrix(4, 1)
  private var P = Matrix(4, 4)
  private var F = Matrix(4, 4)
  private var H = Matrix(2, 4)
  private var Q = Matrix(4, 4)
  private var R = Matrix(2, 2)
  private var I = Matrix(4, 4)

  private var lastUpdate: Long = System.nanoTime()

  private var accelScale: Float = 1.0f / 16384.0f
  private var gyroScale: Float = 1.0f / 131.0f

  var pitch: Float = 0.0f
  var roll: Float = 0.0f

  init {
    initializeMatrices()
  }

  private fun initializeMatrices() {
    x.set(0, 0, 0.0)
    x.set(1, 0, 0.0)
    x.set(2, 0, 0.0)
    x.set(3, 0, 0.0)

    P.set(0, 0, 1.0)
    P.set(1, 1, 1.0)
    P.set(2, 2, 1.0)
    P.set(3, 3, 1.0)

    F.set(0, 0, 1.0)
    F.set(1, 1, 1.0)
    F.set(2, 2, 1.0)
    F.set(3, 3, 1.0)

    H.set(0, 0, 1.0)
    H.set(1, 1, 1.0)

    Q.set(0, 0, 0.01)
    Q.set(1, 1, 0.01)
    Q.set(2, 2, 0.1)
    Q.set(3, 3, 0.1)

    R.set(0, 0, 0.05)
    R.set(1, 1, 0.05)

    I.set(0, 0, 1.0)
    I.set(1, 1, 1.0)
    I.set(2, 2, 1.0)
    I.set(3, 3, 1.0)
  }

  fun update(ax: Int, ay: Int, az: Int, gx: Int, gy: Int, gz: Int) {
    val currentTime = System.nanoTime()
    val deltaTime = (currentTime - lastUpdate) / 1_000_000_000.0
    lastUpdate = currentTime

    val dt = max(deltaTime, 0.001)

    val axFloat = ax * accelScale
    val ayFloat = ay * accelScale
    val azFloat = az * accelScale
    val gxFloat = gx * gyroScale * (PI.toFloat() / 180.0f)
    val gyFloat = gy * gyroScale * (PI.toFloat() / 180.0f)
    val gzFloat = gz * gyroScale * (PI.toFloat() / 180.0f)

    F.set(0, 2, dt)
    F.set(1, 3, dt)

    predict()

    x.set(2, 0, gyFloat.toDouble())
    x.set(3, 0, gxFloat.toDouble())

    val accelMagnitude = sqrt(axFloat * axFloat + ayFloat * ayFloat + azFloat * azFloat)
    if (abs(accelMagnitude - 1.0f) < 0.3f) {
      val measuredPitch = calculatePitchFromAccel(axFloat, ayFloat, azFloat)
      val measuredRoll = calculateRollFromAccel(axFloat, ayFloat, azFloat)

      correct(measuredPitch, measuredRoll)
    }

    this.pitch = Math.toDegrees(x.get(0, 0)).toFloat()
    this.roll = Math.toDegrees(x.get(1, 0)).toFloat()
  }

  private fun calculatePitchFromAccel(ax: Float, ay: Float, az: Float): Float {
    return atan2(ay, sqrt(ax * ax + az * az))
  }

  private fun calculateRollFromAccel(ax: Float, ay: Float, az: Float): Float {
    return atan2(-ax, sqrt(ay * ay + az * az))
  }

  private fun predict() {
    x = F.multiply(x)
    val F_T = F.transpose()
    P = F.multiply(P).multiply(F_T).add(Q)
  }

  private fun correct(measuredPitch: Float, measuredRoll: Float) {
    val z = Matrix(2, 1)
    z.set(0, 0, measuredPitch.toDouble())
    z.set(1, 0, measuredRoll.toDouble())

    val H_x = H.multiply(x)
    val y = z.subtract(H_x)

    val H_T = H.transpose()
    val P_H_T = P.multiply(H_T)
    val S = H.multiply(P_H_T).add(R)

    val K = if (S.rows == 2 && S.cols == 2) {
      val S_inv = S.inverse2x2()
      P_H_T.multiply(S_inv)
    } else {
      P_H_T.multiply(S.scalarMultiply(1.0 / S.get(0, 0)))
    }

    val K_y = K.multiply(y)
    x = x.add(K_y)

    val K_H = K.multiply(H)
    val I_K_H = I.subtract(K_H)
    P = I_K_H.multiply(P)
  }

  fun setAccelScale(range: Float) {
    this.accelScale = 1.0f / (32768.0f / range)
  }

  fun setGyroScale(range: Float) {
    this.gyroScale = 1.0f / (32768.0f / range)
  }

  fun setProcessNoise(angleNoise: Double, rateNoise: Double) {
    Q.set(0, 0, angleNoise)
    Q.set(1, 1, angleNoise)
    Q.set(2, 2, rateNoise)
    Q.set(3, 3, rateNoise)
  }

  fun setMeasurementNoise(noise: Double) {
    R.set(0, 0, noise)
    R.set(1, 1, noise)
  }

  fun getPitchRate(): Float = Math.toDegrees(x.get(2, 0)).toFloat()
  fun getRollRate(): Float = Math.toDegrees(x.get(3, 0)).toFloat()

  fun reset() {
    initializeMatrices()
    pitch = 0.0f
    roll = 0.0f
    lastUpdate = System.nanoTime()
  }
}

class Matrix(val rows: Int, val cols: Int) {
  val data: Array<DoubleArray> = Array(rows) { DoubleArray(cols) }

  fun set(row: Int, col: Int, value: Double) {
    data[row][col] = value
  }

  fun get(row: Int, col: Int): Double = data[row][col]

  fun multiply(other: Matrix): Matrix {
    require(cols == other.rows) { "Matrix dimensions don't match" }
    val result = Matrix(rows, other.cols)
    for (i in 0 until rows) {
      for (j in 0 until other.cols) {
        var sum = 0.0
        for (k in 0 until cols) {
          sum += data[i][k] * other.data[k][j]
        }
        result.set(i, j, sum)
      }
    }
    return result
  }

  fun add(other: Matrix): Matrix {
    require(rows == other.rows && cols == other.cols) { "Matrix dimensions don't match" }
    val result = Matrix(rows, cols)
    for (i in 0 until rows) {
      for (j in 0 until cols) {
        result.set(i, j, data[i][j] + other.data[i][j])
      }
    }
    return result
  }

  fun subtract(other: Matrix): Matrix {
    require(rows == other.rows && cols == other.cols) { "Matrix dimensions don't match" }
    val result = Matrix(rows, cols)
    for (i in 0 until rows) {
      for (j in 0 until cols) {
        result.set(i, j, data[i][j] - other.data[i][j])
      }
    }
    return result
  }

  fun transpose(): Matrix {
    val result = Matrix(cols, rows)
    for (i in 0 until rows) {
      for (j in 0 until cols) {
        result.set(j, i, data[i][j])
      }
    }
    return result
  }

  fun scalarMultiply(scalar: Double): Matrix {
    val result = Matrix(rows, cols)
    for (i in 0 until rows) {
      for (j in 0 until cols) {
        result.set(i, j, data[i][j] * scalar)
      }
    }
    return result
  }

  fun inverse2x2(): Matrix {
    require(rows == 2 && cols == 2) { "Matrix must be 2x2" }
    val det = data[0][0] * data[1][1] - data[0][1] * data[1][0]
    require(det != 0.0) { "Matrix is not invertible" }

    val result = Matrix(2, 2)
    result.set(0, 0, data[1][1] / det)
    result.set(0, 1, -data[0][1] / det)
    result.set(1, 0, -data[1][0] / det)
    result.set(1, 1, data[0][0] / det)
    return result
  }
}
