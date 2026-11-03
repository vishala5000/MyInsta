package com.myinsta.app

import android.Manifest
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private val PHOTO_PROMPT = """Create man unique instagram post prompt
Face should 100% locked same reference face image
Include this full in prompt
An extremely unremarkable, casual iPhone snapshot of [SUBJECT/SCENE]. Careless framing, accidental composition, mild overexposure, and natural uneven indoor lighting. Looks like an unedited camera roll photo."""

    private val CAPTION_PROMPT = "Viral instagram ready to paste caption for this post and also alt text"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequestPermissions()
        setupMainScreen()
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    startActivity(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:$packageName")))
                } catch (e: Exception) {
                    startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
                }
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 100)
            }
        }
    }

    private fun setupMainScreen() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(60, 60, 60, 60)
            setBackgroundColor(Color.WHITE)
        }

        val title = TextView(this).apply {
            text = "My Insta"
            textSize = 36f
            setTextColor(Color.parseColor("#E1306C"))
            gravity = Gravity.CENTER
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        root.addView(title, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { bottomMargin = 80 })

        val btnPrompts = Button(this).apply {
            text = "Prompts"
            textSize = 20f
            setPadding(0, 30, 0, 30)
            setOnClickListener { showPromptsDialog() }
        }
        root.addView(btnPrompts, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { bottomMargin = 30 })

        val btnMake = Button(this).apply {
            text = "Make Prompt"
            textSize = 20f
            setPadding(0, 30, 0, 30)
            setOnClickListener { setupMakePromptScreen() }
        }
        root.addView(btnMake, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

        setContentView(root)
    }

    private fun showPromptsDialog() {
        val dialog = AlertDialog.Builder(this).setTitle("Copy Prompts").create()
        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(40, 40, 40, 40) }
        
        val b1 = Button(this).apply {
            text = "Copy Photo Prompt"
            textSize = 16f
            setOnClickListener { copyToClipboard(PHOTO_PROMPT); Toast.makeText(this@MainActivity, "Photo prompt copied!", Toast.LENGTH_SHORT).show(); dialog.dismiss() }
        }
        val b2 = Button(this).apply {
            text = "Copy Caption Prompt"
            textSize = 16f
            setOnClickListener { copyToClipboard(CAPTION_PROMPT); Toast.makeText(this@MainActivity, "Caption prompt copied!", Toast.LENGTH_SHORT).show(); dialog.dismiss() }
        }
        
        layout.addView(b1); layout.addView(b2)
        dialog.setView(layout)
        dialog.show()
    }

    private fun copyToClipboard(text: String) {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("prompt", text))
    }

    private fun setupMakePromptScreen() {
        val scrollView = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
            setBackgroundColor(Color.WHITE)
        }

        val btnBack = Button(this).apply {
            text = "← Back to Main"
            setOnClickListener { setupMainScreen() }
        }
        layout.addView(btnBack, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { bottomMargin = 20 })

        val hint = TextView(this).apply { text = "Enter your prompt below:"; textSize = 18f; setTypeface(null, android.graphics.Typeface.BOLD) }
        layout.addView(hint)

        val input = EditText(this).apply {
            hint = "Type prompt here..."
            minLines = 5
            gravity = Gravity.TOP
            setPadding(30, 30, 30, 30)
            textSize = 16f
            setBackgroundColor(Color.parseColor("#F5F5F5"))
        }
        layout.addView(input, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = 15; bottomMargin = 30 })

        val previewImg = ImageView(this).apply {
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
            setBackgroundColor(Color.LTGRAY)
            setPadding(10, 10, 10, 10)
        }
        layout.addView(previewImg, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { bottomMargin = 30 })

        val btnGen = Button(this).apply {
            text = "Generate Image"
            textSize = 18f
            setPadding(0, 30, 0, 30)
            setOnClickListener {
                val txt = input.text.toString().trim()
                if (txt.isBlank()) { Toast.makeText(this@MainActivity, "Please enter a prompt first", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
                val bmp = generateImage(txt)
                previewImg.setImageBitmap(bmp)
                previewImg.tag = bmp
                Toast.makeText(this@MainActivity, "Image generated!", Toast.LENGTH_SHORT).show()
            }
        }
        layout.addView(btnGen, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { bottomMargin = 20 })

        val btnDl = Button(this).apply {
            text = "Download to Gallery"
            textSize = 18f
            setPadding(0, 30, 0, 30)
            setBackgroundColor(Color.parseColor("#E1306C"))
            setTextColor(Color.WHITE)
            setOnClickListener {
                val bmp = previewImg.tag as? Bitmap
                if (bmp == null) { Toast.makeText(this@MainActivity, "Generate image first", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
                val path = saveImage(bmp)
                Toast.makeText(this@MainActivity, "Saved: $path", Toast.LENGTH_LONG).show()
                sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(File(path))))
            }
        }
        layout.addView(btnDl)

        scrollView.addView(layout)
        setContentView(scrollView)
    }

    private fun generateImage(text: String): Bitmap {
        val W = 1080; val H = 1350
        val bmp = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(Color.WHITE)

        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val textWidth = 980f
        val maxTextHeight = 1250f
        
        // Dynamically scale text size to guarantee NO cutoff and perfect fit within 1250px height
        var currentTextSize = 72f
        var sl: StaticLayout
        while (true) {
            paint.textSize = currentTextSize
            sl = StaticLayout.Builder.obtain(text, 0, text.length, paint, textWidth.toInt())
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(0f, 1.3f)
                .setIncludePad(false)
                .build()
            
            if (sl.height <= maxTextHeight || currentTextSize <= 24f) break
            currentTextSize -= 2f
        }

        // Perfectly center the text block in the 1080x1350 canvas
        val left = (W - textWidth) / 2f
        val top = (H - sl.height.toFloat()) / 2f

        canvas.save()
        canvas.translate(left, top)
        sl.draw(canvas)
        canvas.restore()

        return bmp
    }

    private fun saveImage(bmp: Bitmap): String {
        val filename = "MyInsta_${System.currentTimeMillis()}.png"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MyInsta")
            }
            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                }
                return "Pictures/MyInsta/$filename"
            }
        } else {
            val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyInsta")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, filename)
            FileOutputStream(file).use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
            return file.absolutePath
        }
        return "Failed to save"
    }
}
