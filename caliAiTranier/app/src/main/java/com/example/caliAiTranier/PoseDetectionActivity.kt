package com.example.caliAiTranier

import android.widget.ImageView
import android.content.Intent
import android.graphics.*
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.example.caliAiTrainer.R
import com.example.caliAiTranier.model.TargetActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import java.io.File
import java.io.FileOutputStream

import java.io.IOException
import java.io.InputStream

class PoseDetectionActivity : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 1
    private lateinit var imageView: ImageView
    private lateinit var btnSelectImage: Button
    private var poseResultBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pose_detection)

        imageView = findViewById(R.id.imageView)
        btnSelectImage = findViewById(R.id.btnSelectImage)

        btnSelectImage.setOnClickListener {
            openImagePicker()
        }
    }

    private fun openImagePicker() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            val imageUri: Uri = data.data!!
            try {
                val imageStream: InputStream? = contentResolver.openInputStream(imageUri)
                val selectedImage: Bitmap = BitmapFactory.decodeStream(imageStream)
                imageView.setImageBitmap(selectedImage)
                runPoseDetection(selectedImage)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun runPoseDetection(imageBitmap: Bitmap) {
        imageView.setImageBitmap(imageBitmap)

        val options = PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            .build()

        val poseDetector = PoseDetection.getClient(options)

        val image = InputImage.fromBitmap(imageBitmap, 0)

        poseDetector.process(image)
            .addOnSuccessListener { pose ->
                drawPoseOnCanvas(imageBitmap, pose)
                showPoseResult()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Pose detection failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun drawPoseOnCanvas(imageBitmap: Bitmap, pose: Pose) {
        val mutableBitmap = imageBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)
        val paint = Paint()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f

        // Sol ve sağ omuz noktalarını bul
        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)

        // Sol ve sağ dirsek noktalarını bul
        val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
        val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)

        // Sol ve sağ diz noktalarını bul
        val leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)
        val rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE)

        // Sol ve sağ ayak bileği noktalarını bul
        val leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)
        val rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)

        // Sol ve sağ el bileği noktalarını bul
        val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
        val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)

        // Sağ ve sol kalça noktalarını bul
        val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)
        val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)

        // Sol ve sağ kulak noktalarını bul
        val leftEar = pose.getPoseLandmark(PoseLandmark.LEFT_EAR)
        val rightEar = pose.getPoseLandmark(PoseLandmark.RIGHT_EAR)

        // Baş pozisyonunu bul
        val nose = pose.getPoseLandmark(PoseLandmark.NOSE)

        // Başın önde olup olmadığını kontrol et
        val isHeadForward = nose?.position?.y ?: 0f > leftShoulder?.position?.y ?: 0f &&
                nose?.position?.y ?: 0f > rightShoulder?.position?.y ?: 0f

        // Boynun duruşunu belirle
        val neck = pose.getPoseLandmark(PoseLandmark.NOSE)?.position?.y ?: 0f > pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)?.position?.y ?: 0f &&
                pose.getPoseLandmark(PoseLandmark.NOSE)?.position?.y ?: 0f > pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)?.position?.y ?: 0f

        // Boynun duruşuna göre çizgi rengini belirle
        paint.color = if (neck) Color.GREEN else Color.RED


        // Başın önde olup olmadığına bağlı olarak çizgi rengini belirle
        paint.color = if (isHeadForward) Color.GREEN else Color.RED

        // Sol omuzla başı birleştir
        leftShoulder?.let {
            canvas.drawLine(it.position.x, it.position.y, nose?.position?.x ?: 0f, nose?.position?.y ?: 0f, paint)
        }

        // Sağ omuzla başı birleştir
        rightShoulder?.let {
            canvas.drawLine(it.position.x, it.position.y, nose?.position?.x ?: 0f, nose?.position?.y ?: 0f, paint)
        }

        // Sol omuzla boynu birleştir
        pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)?.let {
            canvas.drawLine(it.position.x, it.position.y, pose.getPoseLandmark(PoseLandmark.NOSE)?.position?.x ?: 0f, pose.getPoseLandmark(PoseLandmark.NOSE)?.position?.y ?: 0f, paint)
        }

        // Sağ omuzla boynu birleştir
        pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)?.let {
            canvas.drawLine(it.position.x, it.position.y, pose.getPoseLandmark(PoseLandmark.NOSE)?.position?.x ?: 0f, pose.getPoseLandmark(PoseLandmark.NOSE)?.position?.y ?: 0f, paint)
        }

        leftElbow?.let {
            canvas.drawLine(leftShoulder!!.position.x, leftShoulder.position.y, it.position.x, it.position.y, paint)
        }

        rightElbow?.let {
            canvas.drawLine(rightShoulder!!.position.x, rightShoulder.position.y, it.position.x, it.position.y, paint)
        }

        leftKnee?.let {
            canvas.drawLine(leftHip!!.position.x, leftHip.position.y, it.position.x, it.position.y, paint)
        }

        rightKnee?.let {
            canvas.drawLine(rightHip!!.position.x, rightHip.position.y, it.position.x, it.position.y, paint)
        }

        leftAnkle?.let {
            canvas.drawLine(leftKnee!!.position.x, leftKnee.position.y, it.position.x, it.position.y, paint)
        }

        rightAnkle?.let {
            canvas.drawLine(rightKnee!!.position.x, rightKnee.position.y, it.position.x, it.position.y, paint)
        }

        leftWrist?.let {
            canvas.drawLine(leftElbow!!.position.x, leftElbow.position.y, it.position.x, it.position.y, paint)
        }

        rightWrist?.let {
            canvas.drawLine(rightElbow!!.position.x, rightElbow.position.y, it.position.x, it.position.y, paint)
        }

        leftShoulder?.let {
            rightShoulder?.let {
                canvas.drawLine(it.position.x, it.position.y, leftShoulder.position.x, leftShoulder.position.y, paint)
            }
        }

        rightHip?.let {
            leftHip?.let {
                canvas.drawLine(it.position.x, it.position.y, rightHip.position.x, rightHip.position.y, paint)
            }
        }


        poseResultBitmap = mutableBitmap
        imageView.post {
            imageView.setImageBitmap(mutableBitmap)
        }
    }



    private fun showPoseResult() {
        poseResultBitmap?.let {
            try {
                val tempFile = File.createTempFile("pose_result", ".png", cacheDir)
                val outputStream = FileOutputStream(tempFile)
                it.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()

                val intent = Intent(this, TargetActivity::class.java)
                intent.putExtra("poseResultUri", Uri.fromFile(tempFile))
                startActivity(intent)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

}
