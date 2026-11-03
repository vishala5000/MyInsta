
package com.myinsta.app

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
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
        requestStoragePermission()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(40, 40, 40, 40)
            setBackgroundColor(Color.WHITE)
        }

        val title = TextView(this).apply {
            text = "My Insta"
            textSize = 32f
            setTextColor(Color.parseColor("#E1306C"))
            gravity = Gravity.CENTER
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        root.addView(title, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { bottomMargin = 60 })

        val btnPrompts = Button(this).apply {
            text = "Prompts"
            textSize = 18f
            setOnClickListener { showPromptsDialog() }
        }
        root.addView(btnPrompts, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { bottomMargin = 20 })

        val btnMake = Button(this).apply {
            text = "Make Prompt"
            textSize = 18f
            setOnClickListener { showMakePromptScreen() }
        }
        root.addView(btnMake, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

        setContentView(root)
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    startActivity(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                        Uri.parse("package:$packageName")))
                } catch (e: Exception) {
                    startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
                }
            }
        }
    }

    private fun showPromptsDialog() {
        val dialog = AlertDialog.Builder(this).setTitle("Prompts").create()
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 30, 30, 30)
        }
        val b1 = Button(this).apply {
            text = "Copy Photo Prompt"
            setOnClickListener {
                copyToClipboard(PHOTO_PROMPT)
                Toast.makeText(this@MainActivity, "Photo prompt copied!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }
        val b2 = Button(this).apply {
            text = "Copy Caption Prompt"
            setOnClickListener {
                copyToClipboard(CAPTION_PROMPT)
                Toast.makeText(this@MainActivity, "Caption prompt copied!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }
        layout.addView(b1)
        layout.addView(b2)
        dialog.setView(layout)
        dialog.show()
    }

    private fun copyToClipboard(text: String) {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("prompt", text))
    }

    private fun showMakePromptScreen() {
        val root = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 30, 30, 30)
        }

        val hint = TextView(this).apply {
            text = "Enter your prompt below:"
            textSize = 16f
        }
        layout.addView(hint)

        val input = EditText(this).apply {
            hint = "Type prompt here..."
            minLines = 4
            gravity = Gravity.TOP
            setPadding(20, 20, 20, 20)
            setBackgroundColor(Color.parseColor("#F0F0F0"))
        }
        layout.addView(input, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = 10; bottomMargin = 20 })

        val previewImg = ImageView(this).apply {
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
            setBackgroundColor(Color.LTGRAY)
        }
        layout.addView(previewImg, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { bottomMargin = 20 })

        val btnGen = Button(this).apply {
            text = "Generate Image"
            textSize = 16f
            setOnClickListener {
                val txt = input.text.toString()
                if (txt.isBlank()) { Toast.makeText(this@MainActivity, "Enter prompt first", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
                val bmp = generateImage(txt)
                previewImg.setImageBitmap(bmp)
                previewImg.tag = bmp
            }
        }
        layout.addView(btnGen, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { bottomMargin = 10 })

        val btnDl = Button(this).apply {
            text = "Download to Gallery"
            textSize = 16f
            setOnClickListener {
                val bmp = previewImg.tag as? Bitmap
                if (bmp == null) { Toast.makeText(this@MainActivity, "Generate image first", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
                val path = saveImage(bmp)
                Toast.makeText(this@MainActivity, "Saved: $path", Toast.LENGTH_LONG).show()
                sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(File(path))))
            }
        }
        layout.addView(btnDl)

        root.addView(layout)
        setContentView(root)
    }

    private fun generateImage(text: String): Bitmap {
        val W = 1080; val H = 1350
        val bmp = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(Color.WHITE)

        val paint = TextPaint(TextPaint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 52f
            typeface = android.graphics.Typeface.DEFAULT
            isAntiAlias = true
        }

        val textWidth = 980f
        val textHeight = 1250f
        val left = (W - textWidth) / 2f
        val top = (H - textHeight) / 2f

        val sl = StaticLayout.Builder.obtain(text, 0, text.length, paint, textWidth.toInt())
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0f, 1.2f)
            .setIncludePad(false)
            .build()

        canvas.save()
        canvas.translate(left, top)
        sl.draw(canvas)
        canvas.restore()

        return bmp
    }

    private fun saveImage(bmp: Bitmap): String {
        val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyInsta")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "insta_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        return file.absolutePath
    }
}
