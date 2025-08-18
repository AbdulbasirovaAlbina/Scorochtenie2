package com.example.scorochtenie2

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

class ProgressFragment : Fragment() {

    private lateinit var techniqueSelector: RecyclerView
    private lateinit var progressContainer: ViewGroup
    private lateinit var techniqueNameText: TextView
    private lateinit var usesCountText: TextView
    private lateinit var avgComprehensionText: TextView
    private lateinit var totalTimeText: TextView
    private lateinit var avgTimeText: TextView
    private lateinit var daysProgressContainer: ViewGroup
    private lateinit var selectedPeriodText: TextView
    private lateinit var selectPeriodButton: MaterialButton
    private lateinit var techniqueSelectorAdapter: TechniqueSelectorAdapter
    private var selectedStartDate: Calendar? = null
    private var selectedTechnique: String = "Все техники"
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    private val techniques = listOf(
        TechniqueItem("Все техники", R.drawable.ic_all_techniques),
        TechniqueItem("Чтение блоками", R.drawable.ic_block_reading),
        TechniqueItem("Чтение по диагонали", R.drawable.ic_diagonal_reading),
        TechniqueItem("Метод указки", R.drawable.ic_pointer_method),
        TechniqueItem("Предложения наоборот", R.drawable.ic_sentence_reverse),
        TechniqueItem("Слова наоборот", R.drawable.ic_word_reverse),
        TechniqueItem("Зашумленный текст", R.drawable.ic_noisy_text),
        TechniqueItem("Частично скрытые строки", R.drawable.ic_partially_hidden_lines)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("ProgressFragment", "onCreateView called")
        val view = inflater.inflate(R.layout.fragment_progress, container, false)

        initViews(view)
        setupTechniqueSelector()
        setupPeriodSelector()
        loadTechniqueProgress(selectedTechnique)

        return view
    }

    override fun onResume() {
        super.onResume()
        Log.d("ProgressFragment", "onResume called")
        loadTechniqueProgress(selectedTechnique)
        techniqueSelectorAdapter.notifyDataSetChanged()
    }

    private fun initViews(view: View) {
        Log.d("ProgressFragment", "initViews called")
        techniqueSelector = view.findViewById(R.id.technique_selector)
        progressContainer = view.findViewById(R.id.progress_container)

        val progressItemView = layoutInflater.inflate(R.layout.item_technique_progress, progressContainer, false)
        techniqueNameText = progressItemView.findViewById(R.id.technique_name)
        usesCountText = progressItemView.findViewById(R.id.uses_count)
        avgComprehensionText = progressItemView.findViewById(R.id.avg_comprehension)
        totalTimeText = progressItemView.findViewById(R.id.total_time)
        avgTimeText = progressItemView.findViewById(R.id.avg_time)
        daysProgressContainer = progressItemView.findViewById(R.id.days_progress_container)
        selectedPeriodText = progressItemView.findViewById(R.id.selected_period)
        selectPeriodButton = progressItemView.findViewById(R.id.select_period_button)

        progressContainer.addView(progressItemView)
    }

    private fun setupTechniqueSelector() {
        Log.d("ProgressFragment", "setupTechniqueSelector called")
        techniqueSelectorAdapter = TechniqueSelectorAdapter(techniques) { technique ->
            selectedTechnique = technique.title
            loadTechniqueProgress(technique.title)
        }
        techniqueSelector.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        techniqueSelector.adapter = techniqueSelectorAdapter
    }

    private fun setupPeriodSelector() {
        Log.d("ProgressFragment", "setupPeriodSelector called")
        selectPeriodButton.setOnClickListener {
            Log.d("ProgressFragment", "select_period_button clicked")
            showDatePicker()
        }
        updatePeriodDisplay(null)
    }

    private fun showDatePicker() {
        Log.d("ProgressFragment", "showDatePicker called")

        // Ограничение: даты от 1 января 2025 до конца текущего дня
        val startDate = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2025, Calendar.JANUARY, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endDate = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2025, Calendar.AUGUST, 18, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        Log.d("ProgressFragment", "Date constraints: start=${dateFormat.format(startDate.time)} (${startDate.timeInMillis}), end=${dateFormat.format(endDate.time)} (${endDate.timeInMillis})")

        // Создаём CalendarView
        val calendarView = CalendarView(requireContext()).apply {
            minDate = startDate.timeInMillis
            maxDate = endDate.timeInMillis
            setDate(selectedStartDate?.timeInMillis ?: endDate.timeInMillis, false, true)
        }

        // Переменная для хранения выбранной даты
        var selectedDateMillis: Long = selectedStartDate?.timeInMillis ?: endDate.timeInMillis

        // TextView для отображения диапазона дат
        val rangeTextView = TextView(requireContext()).apply {
            val initialDate = selectedStartDate?.time ?: endDate.time
            val rangeEnd = Calendar.getInstance().apply {
                time = initialDate
                add(Calendar.DAY_OF_YEAR, 6)
            }
            text = "Диапазон: ${dateFormat.format(initialDate)} - ${dateFormat.format(rangeEnd.time)}"
            setPadding(90, 0, 16, 24)
            textSize = 16f
        }

        // Обработчик выбора даты
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                set(year, month, dayOfMonth, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            selectedDateMillis = selectedCalendar.timeInMillis
            val rangeEnd = Calendar.getInstance().apply {
                timeInMillis = selectedDateMillis
                add(Calendar.DAY_OF_YEAR, 6)
            }
            rangeTextView.text = "Диапазон: ${dateFormat.format(selectedCalendar.time)} - ${dateFormat.format(rangeEnd.time)}"
            Log.d("ProgressFragment", "Date selected in CalendarView: ${dateFormat.format(selectedCalendar.time)} (${selectedDateMillis})")
        }

        // Создаём кастомный заголовок
        val titleTextView = TextView(requireContext()).apply {
            text = "Укажите дату начала"
            textSize = 20f
            setPadding(16, 50, 16, 0)
            gravity = Gravity.CENTER
        }

        // Создаём кастомный макет для диалога
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            addView(calendarView)
            addView(rangeTextView)
        }

        // Создаём диалог
        val dialog = AlertDialog.Builder(requireContext())
            .setCustomTitle(titleTextView)
            .setView(container)
            .setPositiveButton("OK") { _, _ ->
                val selectedCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                    timeInMillis = selectedDateMillis
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val currentDate = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                    set(2025, Calendar.AUGUST, 18, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                if (selectedCalendar.after(currentDate)) {
                    Toast.makeText(requireContext(), "Нельзя выбирать даты после текущей даты!", Toast.LENGTH_SHORT).show()
                    Log.d("ProgressFragment", "Rejected date: ${dateFormat.format(selectedCalendar.time)} is after ${dateFormat.format(currentDate.time)}")
                    return@setPositiveButton
                }
                selectedStartDate = selectedCalendar
                updatePeriodDisplay(selectedCalendar)
                loadTechniqueProgress(selectedTechnique)
                Log.d("ProgressFragment", "Confirmed date: ${dateFormat.format(selectedCalendar.time)} (${selectedDateMillis})")
            }
            .setNegativeButton("Отмена", null)
            .create()

        dialog.show()
        Log.d("ProgressFragment", "CalendarView dialog shown")
    }

    private fun updatePeriodDisplay(startDate: Calendar?) {
        if (startDate == null) {
            selectedPeriodText.text = "Текущая неделя"
        } else {
            val endDate = Calendar.getInstance().apply {
                timeInMillis = startDate.timeInMillis
                add(Calendar.DAY_OF_YEAR, 6)
            }
            selectedPeriodText.text = "${dateFormat.format(startDate.time)} - ${dateFormat.format(endDate.time)}"
        }
    }

    private fun loadTechniqueProgress(techniqueName: String) {
        val stats = if (techniqueName == "Все техники") {
            TestResultManager.getAllTechniquesStats(requireContext(), selectedStartDate)
        } else {
            TestResultManager.getTechniqueStats(requireContext(), techniqueName, selectedStartDate)
        }

        updateProgressDisplay(techniqueName, stats)
    }

    private fun updateProgressDisplay(techniqueName: String, stats: TechniqueStats) {
        techniqueNameText.text = techniqueName
        usesCountText.text = stats.usesCount.toString()
        avgComprehensionText.text = "${stats.avgComprehension}%"
        totalTimeText.text = formatTime(stats.totalReadingTimeSeconds)
        avgTimeText.text = formatTime(stats.avgReadingTimeSeconds)
        updateDaysProgress(stats.dailyComprehension)
    }

    private fun formatTime(seconds: Int): String {
        return when {
            seconds < 60 -> "${seconds} сек"
            seconds < 3600 -> "${seconds / 60} мин ${seconds % 60} сек"
            else -> "${seconds / 3600} ч ${(seconds % 3600) / 60} мин"
        }
    }

    private fun updateDaysProgress(dailyComprehension: List<Int>) {
        daysProgressContainer.removeAllViews()

        // Генерация списка дней недели
        val dayNames = mutableListOf<String>()
        val calendar = Calendar.getInstance()
        val baseDayNames = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")

        if (selectedStartDate == null) {
            // Текущая неделя: начинается с понедельника
            dayNames.addAll(baseDayNames)
        } else {
            // Выбранный период: начинаем с дня недели selectedStartDate
            calendar.timeInMillis = selectedStartDate!!.timeInMillis
            for (i in 0..6) {
                val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                val index = when (dayOfWeek) {
                    Calendar.SUNDAY -> 6
                    Calendar.MONDAY -> 0
                    Calendar.TUESDAY -> 1
                    Calendar.WEDNESDAY -> 2
                    Calendar.THURSDAY -> 3
                    Calendar.FRIDAY -> 4
                    Calendar.SATURDAY -> 5
                    else -> 0
                }
                dayNames.add(baseDayNames[index])
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        // Определяем текущий день для выделения (только для текущей недели)
        val currentDayIndex = if (selectedStartDate == null) {
            val currentDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
            when (currentDayOfWeek) {
                Calendar.SUNDAY -> 6
                Calendar.MONDAY -> 0
                Calendar.TUESDAY -> 1
                Calendar.WEDNESDAY -> 2
                Calendar.THURSDAY -> 3
                Calendar.FRIDAY -> 4
                Calendar.SATURDAY -> 5
                else -> 0
            }
        } else {
            -1 // Не выделяем текущий день для выбранного периода
        }

        dailyComprehension.forEachIndexed { index, comprehension ->
            val dayView = layoutInflater.inflate(R.layout.item_day_progress, daysProgressContainer, false)

            val tvDayName = dayView.findViewById<TextView>(R.id.tv_day_name)
            val tvDayPercentage = dayView.findViewById<TextView>(R.id.tv_day_percentage)
            val dayProgressBar = dayView.findViewById<View>(R.id.day_progress_bar)

            tvDayName.text = dayNames[index]

            if (index == currentDayIndex) {
                tvDayName.setTextColor(resources.getColor(R.color.primary_color, null))
                tvDayName.setTypeface(null, android.graphics.Typeface.BOLD)
            }

            tvDayPercentage.text = "${comprehension}%"

            val progressPercent = comprehension.toFloat() / 100f
            dayProgressBar.post {
                val height = (dayProgressBar.parent as View).height
                val progressHeight = (height * progressPercent).toInt()

                dayProgressBar.animate()
                    .setDuration(1)
                    .setInterpolator(android.view.animation.AccelerateDecelerateInterpolator())
                    .withEndAction {
                        dayProgressBar.layoutParams.height = progressHeight
                        dayProgressBar.requestLayout()
                    }
                    .start()
            }

            daysProgressContainer.addView(dayView)
        }
    }
}