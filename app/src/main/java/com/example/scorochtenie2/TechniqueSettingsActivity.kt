package com.example.scorochtenie2

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.widget.Toast

class TechniqueSettingsActivity : AppCompatActivity() {

    private lateinit var techniqueTitle: TextView
    private lateinit var techniqueDescription: TextView
    private lateinit var speedSlider: SeekBar
    private lateinit var speedLabel: TextView
    private lateinit var fontSizeSlider: SeekBar
    private lateinit var fontSizeLabel: TextView
    private lateinit var shortTextBtn: Button
    private lateinit var mediumTextBtn: Button
    private lateinit var longTextBtn: Button
    private lateinit var startBtn: Button
    
    // Кнопки выбора цвета
    private lateinit var colorYellowBtn: ImageButton
    private lateinit var colorGreenBtn: ImageButton
    private lateinit var colorBlueBtn: ImageButton
    private lateinit var colorPinkBtn: ImageButton
    private lateinit var colorOrangeBtn: ImageButton
    
    private lateinit var sharedPreferences: SharedPreferences

    private val techniqueDescriptions = mapOf(
        "Чтение блоками" to "Увеличивает скорость чтения путем группировки слов в смысловые блоки",
        "Чтение по диагонали" to "Помогает быстро найти ключевую информацию в тексте",
        "Метод указки" to "Концентрирует внимание и повышает скорость чтения",
        "Предложения наоборот" to "Развивает гибкость мышления и понимание контекста",
        "Слова наоборот" to "Тренирует быстрое распознавание слов и улучшает концентрацию",
        "Зашумленный текст" to "Заставляет читать быстрее, не возвращаясь к уже прочитанному",
        "Частично скрытые строки" to "Развивает навык предугадывания и быстрого чтения"
    )

    private var selectedTextLength: String = "Средний"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_technique_settings)

        sharedPreferences = getSharedPreferences("technique_settings", MODE_PRIVATE)
        
        initViews()
        setupTechnique()
        setupSliders()
        setupColorButtons()
        setupTextLengthButtons()
        setupStartButton()
    }

    private fun initViews() {
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener { finish() }

        techniqueTitle = findViewById(R.id.technique_title)
        techniqueDescription = findViewById(R.id.technique_description)
        speedSlider = findViewById(R.id.speed_slider)
        speedLabel = findViewById(R.id.speed_label)
        fontSizeSlider = findViewById(R.id.font_size_slider)
        fontSizeLabel = findViewById(R.id.font_size_label)
        shortTextBtn = findViewById(R.id.btn_short_text)
        mediumTextBtn = findViewById(R.id.btn_medium_text)
        longTextBtn = findViewById(R.id.btn_long_text)
        startBtn = findViewById(R.id.btn_start)
        
        // Цветовые кнопки
        colorYellowBtn = findViewById(R.id.btn_color_yellow)
        colorGreenBtn = findViewById(R.id.btn_color_green)
        colorBlueBtn = findViewById(R.id.btn_color_blue)
        colorPinkBtn = findViewById(R.id.btn_color_pink)
        colorOrangeBtn = findViewById(R.id.btn_color_orange)
    }

    private fun setupTechnique() {
        val technique = intent.getStringExtra("technique_name") ?: "Чтение блоками"
        techniqueTitle.text = "Техника \"$technique\""
        techniqueDescription.text = techniqueDescriptions[technique] ?: "Улучшает навыки чтения"
    }

    private fun setupSliders() {
        speedSlider.max = 2
        speedSlider.progress = 1
        speedLabel.text = SpeedConfig.speedLabels[1]

        speedSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                speedLabel.text = SpeedConfig.speedLabels[progress]
                updateDescriptionPreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        fontSizeSlider.max = 2
        fontSizeSlider.progress = 1
        fontSizeLabel.text = FontConfig.fontSizeLabels[1]

        fontSizeSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                fontSizeLabel.text = FontConfig.fontSizeLabels[progress]
                updateDescriptionPreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupTextLengthButtons() {
        val buttons = listOf(shortTextBtn, mediumTextBtn, longTextBtn)
        val texts = listOf("Короткий", "Средний", "Длинный")

        // Устанавливаем средний размер текста по умолчанию
        selectTextLengthButton(mediumTextBtn, "Средний")

        buttons.forEachIndexed { index, button ->
            button.setOnClickListener {
                selectTextLengthButton(button, texts[index])
                selectedTextLength = texts[index]
            }
        }
    }

    private fun selectTextLengthButton(selectedButton: Button, text: String) {
        val buttons = listOf(shortTextBtn, mediumTextBtn, longTextBtn)

        buttons.forEach { button ->
            if (button == selectedButton) {
                button.isSelected = true
                button.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            } else {
                button.isSelected = false
                val attrs = intArrayOf(android.R.attr.textColorSecondary)
                val typedArray = obtainStyledAttributes(attrs)
                val textColorSecondary = typedArray.getColor(0, ContextCompat.getColor(this, R.color.text_secondary))
                typedArray.recycle()
                button.setTextColor(textColorSecondary)
            }
        }
    }

    private fun setupColorButtons() {
        val colorButtons = listOf(colorYellowBtn, colorGreenBtn, colorBlueBtn, colorPinkBtn, colorOrangeBtn)
        
        // Получаем сохраненный цвет или устанавливаем желтый по умолчанию
        val savedColorIndex = sharedPreferences.getInt("highlight_color_index", 0)
        selectColorButton(colorButtons[savedColorIndex], savedColorIndex)
        
        colorButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                selectColorButton(button, index)
                // Сохраняем выбранный цвет
                sharedPreferences.edit().putInt("highlight_color_index", index).apply()
            }
        }
    }
    
    private fun selectColorButton(selectedButton: ImageButton, colorIndex: Int) {
        val colorButtons = listOf(colorYellowBtn, colorGreenBtn, colorBlueBtn, colorPinkBtn, colorOrangeBtn)
        
        colorButtons.forEach { button ->
            // ImageButton использует drawable селектор для отображения выбранного состояния
            button.isSelected = (button == selectedButton)
        }
    }

    private fun updateDescriptionPreview() {
        val fontSizeMultiplier = FontConfig.getFontSizeMultiplier(fontSizeSlider.progress)
        techniqueDescription.textSize = FontConfig.BASE_TEXT_SIZE * fontSizeMultiplier
    }

    private fun setupStartButton() {
        startBtn.setOnClickListener {
            val technique = intent.getStringExtra("technique_name") ?: "Чтение блоками"
            val speed = speedSlider.progress
            val fontSize = fontSizeSlider.progress

            val intent = when (technique) {
                "Метод указки" -> Intent(this, PointerMethodActivity::class.java)
                "Чтение блоками" -> Intent(this, BlockReadingActivity::class.java)
                "Предложения наоборот" -> Intent(this, SentenceReverseActivity::class.java)
                "Слова наоборот" -> Intent(this, WordReverseActivity::class.java)
                "Чтение по диагонали" -> Intent(this, DiagonalReadingActivity::class.java)
                "Частично скрытые строки" -> Intent(this, PartiallyHiddenLinesActivity::class.java)
                "Зашумленный текст" -> Intent(this, CurtainTextCurtainActivity::class.java)
                else -> null
            }

            intent?.apply {
                val selectedColorIndex = sharedPreferences.getInt("highlight_color_index", 0)
                putExtra("technique_name", technique)
                putExtra("speed", speed)
                putExtra("font_size", fontSize)
                putExtra("text_length", selectedTextLength)
                putExtra("highlight_color_index", selectedColorIndex)
                startActivity(this)
            } ?: run {
                Toast.makeText(
                    this,
                    "Запуск: $technique\nСкорость: ${SpeedConfig.speedLabels[speed]}\nШрифт: ${FontConfig.fontSizeLabels[fontSize]}\nТекст: $selectedTextLength",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}