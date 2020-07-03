package com.sandoval.simplesmilecamera.overlay

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import com.sandoval.simplesmilecamera.camera.CameraConfiguration


class GraphicOverlay(
    context: Context?,
    attrs: AttributeSet?
) :
    View(context, attrs) {
    private val lock = Any()
    private var previewWidth = 0
    private var previewHeight = 0
    var widthScaleValue = 1.0f
        private set
    var heightScaleValue = 1.0f
        private set
    var cameraFacing = CameraConfiguration.CAMERA_FACING_BACK
        private set
    private val graphics: MutableList<BaseGraphic> = ArrayList()
    fun clear() {
        synchronized(lock) { graphics.clear() }
        this.postInvalidate()
    }

    fun addGraphic(graphic: BaseGraphic) {
        synchronized(lock) { graphics.add(graphic) }
    }

    fun removeGraphic(graphic: BaseGraphic?) {
        synchronized(lock) { graphics.remove(graphic) }
        this.postInvalidate()
    }

    fun setCameraInfo(width: Int, height: Int, facing: Int) {
        synchronized(lock) {
            previewWidth = width
            previewHeight = height
            cameraFacing = facing
        }
        this.postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        synchronized(lock) {
            if (previewWidth != 0 && previewHeight != 0) {
                widthScaleValue =
                    canvas.width.toFloat() / previewWidth.toFloat()
                heightScaleValue =
                    canvas.height.toFloat() / previewHeight.toFloat()
            }
            for (graphic in graphics) {
                graphic.draw(canvas)
            }
        }
    }
}