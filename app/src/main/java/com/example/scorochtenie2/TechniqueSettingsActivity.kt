package com.example.scorochtenie2

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

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

    private val speedLabels = arrayOf("Медленно", "Средне", "Быстро")
    private val fontSizeLabels = arrayOf("Маленький", "Средний", "Большой")
    
    // Описания для каждой техники
    private val techniqueDescriptions = mapOf(
        "Чтение блоками" to "Увеличивает скорость чтения путем группировки слов в смысловые блоки",
        "Чтение по диагонали" to "Помогает быстро найти ключевую информацию в тексте", 
        "Метод указки" to "Концентрирует внимание и повышает скорость чтения",
        "Предложения наоборот" to "Развивает гибкость мышления и понимание контекста",
        "Слова наоборот" to "Тренирует быстрое распознавание слов и улучшает концентрацию",
        "Текст за шторкой" to "Заставляет читать быстрее, не возвращаясь к уже прочитанному",
        "Зашумленный текст" to "Повышает концентрацию и способность выделять важную информацию",
        "Частично скрытые строки" to "Развивает навык предугадывания и быстрого чтения"
    )

    private var selectedTextLength = "Средний"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_technique_settings)

        initViews()
        setupTechnique()
        setupSliders()
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
    }

    private fun setupTechnique() {
        val technique = intent.getStringExtra("technique_name") ?: "Чтение блоками"
        techniqueTitle.text = "Техника \"$technique\""
        techniqueDescription.text = techniqueDescriptions[technique] ?: "Улучшает навыки чтения"
    }

    private fun setupSliders() {
        // Настройка слайдера скорости (3 позиции: 0, 1, 2)
        speedSlider.max = 2
        speedSlider.progress = 1 // По умолчанию средняя скорость
        speedLabel.text = speedLabels[1]
        
        speedSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                speedLabel.text = speedLabels[progress]
                updateDescriptionPreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Настройка слайдера размера шрифта (3 позиции: 0, 1, 2)
        fontSizeSlider.max = 2
        fontSizeSlider.progress = 1 // По умолчанию средний размер
        fontSizeLabel.text = fontSizeLabels[1]
        
        fontSizeSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                fontSizeLabel.text = fontSizeLabels[progress]
                updateDescriptionPreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupTextLengthButtons() {
        val buttons = listOf(shortTextBtn, mediumTextBtn, longTextBtn)
        val texts = listOf("Короткий", "Средний", "Длинный")
        
        // По умолчанию выбран средний текст
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
                // Получение цвета из темы
                val attrs = intArrayOf(android.R.attr.textColorSecondary)
                val typedArray = obtainStyledAttributes(attrs)
                val textColorSecondary = typedArray.getColor(0, ContextCompat.getColor(this, R.color.text_secondary))
                typedArray.recycle()
                button.setTextColor(textColorSecondary)
            }
        }
    }

    private fun updateDescriptionPreview() {
        // Динамически изменяем размер текста описания как в настройках телефона
        val fontSizeMultiplier = when (fontSizeSlider.progress) {
            0 -> 0.8f
            1 -> 1.0f
            2 -> 1.2f
            else -> 1.0f
        }
        
        val baseTextSize = 14f
        techniqueDescription.textSize = baseTextSize * fontSizeMultiplier
    }

    private fun setupStartButton() {
        startBtn.setOnClickListener {
            // Здесь будет запуск упражнения с выбранными настройками
            val technique = intent.getStringExtra("technique_name") ?: "Чтение блоками"
            val speed = speedLabels[speedSlider.progress]
            val fontSize = fontSizeLabels[fontSizeSlider.progress]
            
            Toast.makeText(this, 
                "Запуск: $technique\nСкорость: $speed\nШрифт: $fontSize\nТекст: $selectedTextLength", 
                Toast.LENGTH_LONG).show()
            
            // TODO: Запустить соответствующую активность упражнения
        }
    }
}
