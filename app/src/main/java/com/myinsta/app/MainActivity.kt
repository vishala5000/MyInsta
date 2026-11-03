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
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private var previewImageView: ImageView? = null
    private var currentBitmap: Bitmap? = null

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

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics).toInt()
    }

    private fun createGradientDrawable(colors: IntArray, orientation: GradientDrawable.Orientation = GradientDrawable.Orientation.TOP_BOTTOM, cornerRadius: Int = 16): GradientDrawable {
        return GradientDrawable(orientation, colors).apply {
            this.cornerRadius = dpToPx(cornerRadius).toFloat()
        }
    }

    private fun createCardDrawable(): GradientDrawable {
        return GradientDrawable().apply {
            setColor(Color.WHITE)
            cornerRadius = dpToPx(20).toFloat()
        }
    }

    private fun setupMainScreen() {
        previewImageView = null
        currentBitmap = null

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#FAFAFA"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dpToPx(32), dpToPx(48), dpToPx(32), dpToPx(48))
            background = createGradientDrawable(
                intArrayOf(
                    Color.parseColor("#833AB4"),
                    Color.parseColor("#E1306C"),
                    Color.parseColor("#FCAF45")
                )
            )
        }

        val emoji = TextView(this).apply {
            setText("📸✨")
            textSize = 48f
            gravity = Gravity.CENTER
        }
        header.addView(emoji)

        val title = TextView(this).apply {
            setText("My Insta")
            textSize = 40f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setTypeface(null, Typeface.BOLD)
        }
        header.addView(title, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dpToPx(8) })

        val subtitle = TextView(this).apply {
            setText("Create Viral Instagram Content")
            textSize = 15f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            alpha = 0.9f
        }
        header.addView(subtitle, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dpToPx(6) })

        root.addView(header, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))

        val scrollView = ScrollView(this).apply {
            isVerticalScrollBarEnabled = true
            overScrollMode = View.OVER_SCROLL_NEVER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(20), dpToPx(24), dpToPx(20), dpToPx(32))
        }

        val promptsCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dpToPx(24), dpToPx(28), dpToPx(24), dpToPx(28))
            background = createCardDrawable()
            elevation = dpToPx(6).toFloat()
        }

        val promptsIcon = TextView(this).apply {
            setText("💡")
            textSize = 36f
            gravity = Gravity.CENTER
        }
        promptsCard.addView(promptsIcon)

        val promptsTitle = TextView(this).apply {
            setText("Quick Prompts")
            textSize = 22f
            setTextColor(Color.parseColor("#2D2D2D"))
            gravity = Gravity.CENTER
            setTypeface(null, Typeface.BOLD)
        }
        promptsCard.addView(promptsTitle, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dpToPx(8) })

        val promptsDesc = TextView(this).apply {
            setText("Copy ready-to-use prompts for photos and captions")
            textSize = 14f
            setTextColor(Color.parseColor("#666666"))
            gravity = Gravity.CENTER
        }
        promptsCard.addView(promptsDesc, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dpToPx(6) })

        val btnPrompts = Button(this).apply {
            setText("📋  View Prompts")
            textSize = 16f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            setPadding(dpToPx(16), dpToPx(14), dpToPx(16), dpToPx(14))
            background = createGradientDrawable(
                intArrayOf(Color.parseColor("#667EEA"), Color.parseColor("#764BA2")),
                GradientDrawable.Orientation.LEFT_RIGHT
            )
            elevation = dpToPx(4).toFloat()
            stateListAnimator = null
            setOnClickListener { showPromptsDialog() }
        }
        promptsCard.addView(btnPrompts, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dpToPx(20) })

        content.addView(promptsCard, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = dpToPx(16) })

        val makeCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dpToPx(24), dpToPx(28), dpToPx(24), dpToPx(28))
            background = createCardDrawable()
            elevation = dpToPx(6).toFloat()
        }

        val makeIcon = TextView(this).apply {
            setText("🎨")
            textSize = 36f
            gravity = Gravity.CENTER
        }
        makeCard.addView(makeIcon)

        val makeTitle = TextView(this).apply {
            setText("Create Custom Image")
            textSize = 22f
            setTextColor(Color.parseColor("#2D2D2D"))
            gravity = Gravity.CENTER
            setTypeface(null, Typeface.BOLD)
        }
        makeCard.addView(makeTitle, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dpToPx(8) })

        val makeDesc = TextView(this).apply {
            setText("Generate perfect 4:5 Instagram images with your text")
            textSize = 14f
            setTextColor(Color.parseColor("#666666"))
            gravity = Gravity.CENTER
        }
        makeCard.addView(makeDesc, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dpToPx(6) })

        val btnMake = Button(this).apply {
            setText("✨  Start Creating")
            textSize = 16f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            setPadding(dpToPx(16), dpToPx(14), dpToPx(16), dpToPx(14))
            background = createGradientDrawable(
                intArrayOf(Color.parseColor("#F093FB"), Color.parseColor("#F5576C")),
                GradientDrawable.Orientation.LEFT_RIGHT
            )
            elevation = dpToPx(4).toFloat()
            stateListAnimator = null
            setOnClickListener { setupMakePromptScreen() }
        }
        makeCard.addView(btnMake, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dpToPx(20) })

        content.addView(makeCard)

        scrollView.addView(content)
        root.addView(scrollView)

        setContentView(root)
    }

    private fun showPromptsDialog() {
        val dialog = AlertDialog.Builder(this).create()
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(28), dpToPx(28), dpToPx(28), dpToPx(28))
            setBackgroundColor(Color.WHITE)
        }

        val title = TextView(this).apply {
            setText("📋 Copy Prompts")
            textSize = 24f
            setTextColor(Color.parseColor("#2D2D2D"))
            gravity = Gravity.CENTER
            setTypeface(null, Typeface.BOLD)
        }
        layout.addView(title, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = dpToPx(24) })

        val b1 = Button(this).apply {
            setText("📷  Copy Photo Prompt")
            textSize = 16f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
            background = createGradientDrawable(
                intArrayOf(Color.parseColor("#4FACFE"), Color.parseColor("#00F2FE")),
                GradientDrawable.Orientation.LEFT_RIGHT
            )
            elevation = dpToPx(4).toFloat()
            stateListAnimator = null
            setOnClickListener {
                copyToClipboard(PHOTO_PROMPT)
                Toast.makeText(this@MainActivity, "✅ Photo prompt copied!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }
        layout.addView(b1, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = dpToPx(12) })

        val b2 = Button(this).apply {
            setText("💬  Copy Caption Prompt")
            textSize = 16f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
            background = createGradientDrawable(
                intArrayOf(Color.parseColor("#FA709A"), Color.parseColor("#FEE140")),
                GradientDrawable.Orientation.LEFT_RIGHT
            )
            elevation = dpToPx(4).toFloat()
            stateListAnimator = null
            setOnClickListener {
                copyToClipboard(CAPTION_PROMPT)
                Toast.makeText(this@MainActivity, "✅ Caption prompt copied!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }
        layout.addView(b2)

        dialog.setView(layout)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun copyToClipboard(text: String) {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("prompt", text))
    }

    private fun setupMakePromptScreen() {
        previewImageView = null
        currentBitmap = null

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#FAFAFA"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
            background = createGradientDrawable(
                intArrayOf(Color.parseColor("#833AB4"), Color.parseColor("#E1306C")),
                GradientDrawable.Orientation.LEFT_RIGHT,
                0
            )
        }

        val btnBack = Button(this).apply {
            setText("← Back")
            textSize = 14f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            setPadding(dpToPx(14), dpToPx(8), dpToPx(14), dpToPx(8))
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#40000000"))
                cornerRadius = dpToPx(12).toFloat()
            }
            stateListAnimator = null
            setOnClickListener { setupMainScreen() }
        }
        header.addView(btnBack)

        val headerTitle = TextView(this).apply {
            setText("✨ Create Image")
            textSize = 20f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
        }
        header.addView(headerTitle, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { leftMargin = dpToPx(12) })

        root.addView(header, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))

        val scrollView = ScrollView(this).apply {
            isVerticalScrollBarEnabled = true
            overScrollMode = View.OVER_SCROLL_NEVER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(32))
        }

        val inputCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(20))
            background = createCardDrawable()
            elevation = dpToPx(4).toFloat()
        }

        val inputLabel = TextView(this).apply {
            setText("📝 Enter Your Prompt")
            textSize = 18f
            setTextColor(Color.parseColor("#2D2D2D"))
            setTypeface(null, Typeface.BOLD)
        }
        inputCard.addView(inputLabel)

        val input = EditText(this).apply {
            setHint("Type your creative prompt here...")
            setHintTextColor(Color.parseColor("#AAAAAA"))
            minLines = 4
            maxLines = 8
            gravity = Gravity.TOP or Gravity.START
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
            textSize = 15f
            setTextColor(Color.BLACK)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#F8F9FA"))
                cornerRadius = dpToPx(12).toFloat()
                setStroke(dpToPx(2), Color.parseColor("#E0E0E0"))
            }
        }
        inputCard.addView(input, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dpToPx(12) })

        content.addView(inputCard, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = dpToPx(16) })

        val btnGen = Button(this).apply {
            setText("🎨 Generate Image")
            textSize = 17f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
            background = createGradientDrawable(
                intArrayOf(Color.parseColor("#667EEA"), Color.parseColor("#764BA2")),
                GradientDrawable.Orientation.LEFT_RIGHT
            )
            elevation = dpToPx(6).toFloat()
            stateListAnimator = null
            setOnClickListener {
                val txt = input.text.toString().trim()
                if (txt.isBlank()) {
                    Toast.makeText(this@MainActivity, "⚠️ Please enter a prompt first", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val bmp = generateImage(txt)
                currentBitmap = bmp
                previewImageView?.setImageBitmap(bmp)
                Toast.makeText(this@MainActivity, "✅ Image generated!", Toast.LENGTH_SHORT).show()
                
                previewImageView?.let { img ->
                    scrollView.post {
                        scrollView.smoothScrollTo(0, img.top - dpToPx(20))
                    }
                }
            }
        }
        content.addView(btnGen, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = dpToPx(16) })

        val previewCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
            background = createCardDrawable()
            elevation = dpToPx(4).toFloat()
        }

        val previewLabel = TextView(this).apply {
            setText("🖼️ Preview")
            textSize = 18f
            setTextColor(Color.parseColor("#2D2D2D"))
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
        }
        previewCard.addView(previewLabel, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = dpToPx(12) })

        val screenWidth = resources.displayMetrics.widthPixels - dpToPx(20 + 20 + 16 + 16 + 8 + 8)
        val previewHeight = (screenWidth * 1350) / 1080

        val previewImg = ImageView(this).apply {
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
            setBackgroundColor(Color.parseColor("#F0F0F0"))
            setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#F0F0F0"))
                cornerRadius = dpToPx(12).toFloat()
            }
        }
        previewImageView = previewImg
        
        previewCard.addView(previewImg, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            previewHeight
        ))

        content.addView(previewCard, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = dpToPx(16) })

        val btnDl = Button(this).apply {
            setText("💾 Download to Gallery")
            textSize = 17f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
            background = createGradientDrawable(
                intArrayOf(Color.parseColor("#F093FB"), Color.parseColor("#F5576C")),
                GradientDrawable.Orientation.LEFT_RIGHT
            )
            elevation = dpToPx(6).toFloat()
            stateListAnimator = null
            setOnClickListener {
                val bmp = currentBitmap
                if (bmp == null) {
                    Toast.makeText(this@MainActivity, "⚠️ Generate image first", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val path = saveImage(bmp)
                Toast.makeText(this@MainActivity, "✅ Saved to $path", Toast.LENGTH_LONG).show()
                sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(File(path))))
            }
        }
        content.addView(btnDl, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))

        scrollView.addView(content)
        root.addView(scrollView)

        setContentView(root)
    }

    // ===== FIXED: Text STRICTLY inside 980x1250 box, zero cutoff =====
    private fun generateImage(text: String): Bitmap {
        val W = 1080
        val H = 1350
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

        // Step 1: Try shrinking font from 80f down to 10f
        var currentTextSize = 80f
        var displayText = text
        var sl: StaticLayout? = null

        while (currentTextSize >= 10f) {
            paint.textSize = currentTextSize
            val testSl = StaticLayout.Builder.obtain(displayText, 0, displayText.length, paint, textWidth.toInt())
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(0f, 1.2f)
                .setIncludePad(false)
                .build()
            
            if (testSl.height <= maxTextHeight) {
                sl = testSl
                break
            }
            currentTextSize -= 1f
        }

        // Step 2: If still too long at 10f, truncate word-by-word with "..."
        if (sl == null) {
            paint.textSize = 10f
            val words = displayText.split(" ")
            var truncated = ""
            
            for (word in words) {
                val test = if (truncated.isEmpty()) word else "$truncated $word"
                val testSl = StaticLayout.Builder.obtain(test, 0, test.length, paint, textWidth.toInt())
                    .setAlignment(Layout.Alignment.ALIGN_CENTER)
                    .setLineSpacing(0f, 1.2f)
                    .setIncludePad(false)
                    .build()
                
                if (testSl.height <= maxTextHeight) {
                    truncated = test
                } else {
                    // Add "..." to indicate truncation
                    truncated = truncated.trimEnd()
                    if (truncated.isNotEmpty()) {
                        truncated = truncated.substring(0, truncated.length.coerceAtLeast(1)) + "..."
                    }
                    break
                }
            }
            
            sl = StaticLayout.Builder.obtain(truncated, 0, truncated.length, paint, textWidth.toInt())
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(0f, 1.2f)
                .setIncludePad(false)
                .build()
        }

        // Step 3: Center the text block perfectly in the 1080x1350 canvas
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
