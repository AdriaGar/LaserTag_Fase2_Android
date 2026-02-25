package com.adrig.lasertag.A3_Partida

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.RectF
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.adrig.lasertag.R
import com.adrig.lasertag.data.PlayerPosition
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.sqrt

class F_Mapa : Fragment(), SensorEventListener {

    private val viewModel: VM_Mapa by viewModels()

    private lateinit var mapImageView: ImageView
    private lateinit var parent: FrameLayout
    private var ownMarker: View? = null

    private val PERMISSION_REQUEST = 100

    private val otherMarkers = mutableMapOf<String, View>()

    private val imageMatrix = Matrix()
    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private lateinit var gestureDetector: GestureDetector
    private var isInitialMatrixSet = false
    private val matrixValues = FloatArray(9)

    private var minScale = 1.0f
    private val MAX_SCALE = 20.0f

    // For manual rotation gesture
    private var lastAngle = 0f

    // For auto-recenter
    private val recenterHandler = Handler(Looper.getMainLooper())
    private var recenterRunnable: Runnable? = null
    private val RECENTER_DELAY_MS = 5000L
    private var isAutoCentering = true

    // For sensor rotation
    private lateinit var sensorManager: SensorManager
    private var rotationVectorSensor: Sensor? = null
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    private var currentDeviceAngle = 0f // The angle currently shown on map
    private var smoothedDeviceAngle = 0f // The smoothed angle from sensor
    private val SMOOTHING_FACTOR = 0.2f
    private val ROTATION_UPDATE_THRESHOLD = 1f // Update only if change is > 1 degree

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_mapa, container, false)
        mapImageView = view.findViewById(R.id.mapImageView)
        parent = view.findViewById(R.id.mapContainer)

        mapImageView.scaleType = ImageView.ScaleType.MATRIX

        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        ownMarker = View(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(40, 40)
            setBackgroundColor(Color.RED)
            visibility = View.INVISIBLE
        }
        parent.addView(ownMarker)

        setupGestureDetectors()

        mapImageView.setOnTouchListener { _, event ->
            isAutoCentering = false
            resetRecenterTimer()

            scaleGestureDetector.onTouchEvent(event)
            gestureDetector.onTouchEvent(event)
            handleRotationGesture(event)

            if (event.actionMasked == MotionEvent.ACTION_MOVE || event.actionMasked == MotionEvent.ACTION_UP) {
                checkBounds()
                mapImageView.imageMatrix = imageMatrix
                updateAllMarkersPositions()
            }
            true
        }

        mapImageView.viewTreeObserver.addOnGlobalLayoutListener {
            if (mapImageView.drawable != null && mapImageView.width > 0 && mapImageView.height > 0) {
                if (!isInitialMatrixSet) {
                    setupInitialMatrix()
                    isInitialMatrixSet = true
                }
                updateAllMarkersPositions()
            }
        }

        return view
    }

    private fun setupInitialMatrix() {
        val imageWidth = mapImageView.drawable.intrinsicWidth.toFloat()
        val imageHeight = mapImageView.drawable.intrinsicHeight.toFloat()
        val viewWidth = mapImageView.width.toFloat()
        val viewHeight = mapImageView.height.toFloat()

        val scale = max(viewWidth / imageWidth, viewHeight / imageHeight)
        minScale = scale

        val redundantXSpace = viewWidth - (imageWidth * scale)
        val redundantYSpace = viewHeight - (imageHeight * scale)
        val dx = redundantXSpace / 2
        val dy = redundantYSpace / 2

        imageMatrix.setScale(scale, scale)
        imageMatrix.postTranslate(dx, dy)
        mapImageView.imageMatrix = imageMatrix
    }

    private fun setupGestureDetectors() {
        scaleGestureDetector = ScaleGestureDetector(
            requireContext(),
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    val scaleFactor = detector.scaleFactor
                    val currentScale = getCurrentScale()
                    var newScale = currentScale * scaleFactor
                    newScale = newScale.coerceIn(minScale, MAX_SCALE)
                    val actualScaleFactor = newScale / currentScale

                    imageMatrix.postScale(
                        actualScaleFactor,
                        actualScaleFactor,
                        detector.focusX,
                        detector.focusY
                    )
                    return true
                }
            })

        gestureDetector =
            GestureDetector(requireContext(), object : GestureDetector.SimpleOnGestureListener() {
                override fun onScroll(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    distanceX: Float,
                    distanceY: Float
                ): Boolean {
                    imageMatrix.postTranslate(-distanceX, -distanceY)
                    return true
                }

                override fun onDown(e: MotionEvent): Boolean = true
            })
    }

    private fun handleRotationGesture(event: MotionEvent) {
        if (event.pointerCount == 2) {
            when (event.actionMasked) {
                MotionEvent.ACTION_POINTER_DOWN -> {
                    lastAngle = getAngle(event)
                }
                MotionEvent.ACTION_MOVE -> {
                    val angle = getAngle(event)
                    val deltaAngle = angle - lastAngle
                    val focusX = (event.getX(0) + event.getX(1)) / 2
                    val focusY = (event.getY(0) + event.getY(1)) / 2

                    imageMatrix.postRotate(deltaAngle, focusX, focusY)
                    lastAngle = angle
                }
            }
        }
    }

    private fun getAngle(event: MotionEvent): Float {
        val dx = event.getX(0) - event.getX(1)
        val dy = event.getY(0) - event.getY(1)
        return (atan2(dy.toDouble(), dx.toDouble()) * 180 / Math.PI).toFloat()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_GAME)
        if (hasLocationPermission()) {
            viewModel.startLocationUpdates()
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST)
        }
        resetRecenterTimer()
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        viewModel.stopLocationUpdates()
        recenterRunnable?.let { recenterHandler.removeCallbacks(it) }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            var azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
            azimuth = (azimuth + 360) % 360

            var delta = azimuth - smoothedDeviceAngle
            if (delta > 180) delta -= 360
            if (delta < -180) delta += 360
            smoothedDeviceAngle += SMOOTHING_FACTOR * delta
            smoothedDeviceAngle = (smoothedDeviceAngle + 360) % 360

            var rotationChange = smoothedDeviceAngle - currentDeviceAngle
            if (rotationChange > 180) rotationChange -= 360
            if (rotationChange < -180) rotationChange += 360

            if (abs(rotationChange) > ROTATION_UPDATE_THRESHOLD) {
                currentDeviceAngle = smoothedDeviceAngle
                if (isAutoCentering) {
                    updateMapTransform()
                }
            }
        }
    }

    private fun observeViewModel() {
        viewModel.ownPlayerPosition.observe(viewLifecycleOwner) { position ->
            if (isAutoCentering) {
                updateMapTransform()
            } else {
                updateMarkerPosition(ownMarker, position, true)
            }
        }
        viewModel.otherPlayers.observe(viewLifecycleOwner) { players ->
            updateOtherPlayersMarkers(players)
        }
    }

    private fun updateAllMarkersPositions() {
        viewModel.ownPlayerPosition.value?.let { updateMarkerPosition(ownMarker, it, true) }
        viewModel.otherPlayers.value?.let { updateOtherPlayersMarkers(it) }
    }

    private fun updateMarkerPosition(marker: View?, position: PlayerPosition?, isOwnPlayer: Boolean = false) {
        if (marker == null || position == null || mapImageView.drawable == null) return

        val point = floatArrayOf(
            (position.relX * mapImageView.drawable.intrinsicWidth).toFloat(),
            (position.relY * mapImageView.drawable.intrinsicHeight).toFloat()
        )
        imageMatrix.mapPoints(point)

        marker.x = mapImageView.left + point[0] - marker.width / 2f
        marker.y = mapImageView.top + point[1] - marker.height / 2f
        marker.visibility = View.VISIBLE

        val currentScale = getCurrentScale()
        val markerScale = 1.0f + (2.0f * ((currentScale - minScale) / (MAX_SCALE - minScale)))
        marker.scaleX = markerScale
        marker.scaleY = markerScale
        marker.rotation = if (isOwnPlayer) 0f else -getCurrentRotation()
    }

    private fun updateOtherPlayersMarkers(players: List<PlayerPosition>) {
        val currentPlayerIds = players.map { it.id }.toSet()
        val markersToRemove = otherMarkers.keys.filter { it !in currentPlayerIds }
        markersToRemove.forEach { id ->
            otherMarkers[id]?.let { parent.removeView(it) }
            otherMarkers.remove(id)
        }

        for (player in players) {
            val markerView = otherMarkers[player.id] ?: run {
                val v = View(requireContext()).apply {
                    layoutParams = FrameLayout.LayoutParams(35, 35)
                    setBackgroundColor(Color.BLUE)
                }
                parent.addView(v)
                otherMarkers[player.id] = v
                v
            }
            updateMarkerPosition(markerView, player)
        }
    }

    private fun resetRecenterTimer() {
        recenterRunnable?.let { recenterHandler.removeCallbacks(it) }
        recenterRunnable = Runnable {
            isAutoCentering = true
            updateMapTransform()
        }
        recenterHandler.postDelayed(recenterRunnable!!, RECENTER_DELAY_MS)
    }

    private fun updateMapTransform() {
        if (!isAutoCentering || mapImageView.drawable == null || !isInitialMatrixSet) return
        val position = viewModel.ownPlayerPosition.value ?: return

        val imageWidth = mapImageView.drawable.intrinsicWidth
        val imageHeight = mapImageView.drawable.intrinsicHeight
        val viewWidth = mapImageView.width.toFloat()
        val viewHeight = mapImageView.height.toFloat()

        val playerX = position.relX * imageWidth
        val playerY = position.relY * imageHeight

        val targetRotation = -currentDeviceAngle + 180f

        val scale = getCurrentScale().coerceAtLeast(minScale)

        val newMatrix = Matrix()
        newMatrix.setTranslate(-playerX.toFloat(), -playerY.toFloat())
        newMatrix.postScale(scale, scale)
        newMatrix.postRotate(targetRotation)
        newMatrix.postTranslate(viewWidth / 2f, viewHeight / 2f)

        imageMatrix.set(newMatrix)
        mapImageView.imageMatrix = imageMatrix

        updateAllMarkersPositions()
    }

    private fun getDisplayedImageRect(imageView: ImageView): RectF? {
        val d = imageView.drawable ?: return null
        val matrix = imageView.imageMatrix
        val rect = RectF(0f, 0f, d.intrinsicWidth.toFloat(), d.intrinsicHeight.toFloat())
        matrix.mapRect(rect)
        return rect
    }

    private fun getCurrentScale(): Float {
        imageMatrix.getValues(matrixValues)
        val scaleX = matrixValues[Matrix.MSCALE_X]
        val skewY = matrixValues[Matrix.MSKEW_Y]
        return sqrt(scaleX * scaleX + skewY * skewY)
    }

    private fun getCurrentRotation(): Float {
        imageMatrix.getValues(matrixValues)
        val scaleX = matrixValues[Matrix.MSCALE_X]
        val skewY = matrixValues[Matrix.MSKEW_Y]
        return (atan2(skewY.toDouble(), scaleX.toDouble()) * 180 / Math.PI).toFloat()
    }

    private fun checkBounds() {
        val rect = getDisplayedImageRect(mapImageView) ?: return
        var deltaX = 0f
        var deltaY = 0f
        val viewWidth = mapImageView.width.toFloat()
        val viewHeight = mapImageView.height.toFloat()

        when {
            rect.width() > viewWidth -> {
                if (rect.left > 0) deltaX = -rect.left
                else if (rect.right < viewWidth) deltaX = viewWidth - rect.right
            }
            else -> deltaX = viewWidth / 2 - (rect.left + rect.width() / 2)
        }

        when {
            rect.height() > viewHeight -> {
                if (rect.top > 0) deltaY = -rect.top
                else if (rect.bottom < viewHeight) deltaY = viewHeight - rect.bottom
            }
            else -> deltaY = viewHeight / 2 - (rect.top + rect.height() / 2)
        }

        if (deltaX != 0f || deltaY != 0f) {
            imageMatrix.postTranslate(deltaX, deltaY)
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            viewModel.startLocationUpdates()
        } else {
            Log.e("F_Mapa", "Permiso de ubicación denegado")
        }
    }
}