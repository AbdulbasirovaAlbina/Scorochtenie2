package com.example.scorochtenie2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment

import kotlin.random.Random

class HomeFragment : Fragment() {

    private lateinit var quoteText: TextView
    private lateinit var quoteAuthor: TextView
    private lateinit var sessionsCount: TextView
    private lateinit var totalTime: TextView
    private lateinit var tipText: TextView
    
    private val quotes = listOf(
        Pair("«Чтение — это окошко, через которое дети видят и познают мир и самих себя»", "— В. А. Сухомлинский"),
        Pair("«Чтение хороших книг — это разговор с самыми лучшими людьми прошедших времен»", "— Рене Декарт"),
        Pair("«Книга — это друг, который никогда не предаст»", "— Александр Дюма"),
        Pair("«Читать — это еще ничего не значит; что читать и как понимать читаемое — вот в чем главное дело»", "— К. Д. Ушинский"),
        Pair("«Люди перестают мыслить, когда перестают читать»", "— Дени Дидро"),
        Pair("«Чтение делает человека знающим, беседа — находчивым, а привычка записывать — точным»", "— Фрэнсис Бэкон"),
        Pair("«Скорочтение — это навык, который поможет вам успевать больше в меньшее время»", "— Тони Бьюзен"),
        Pair("«Читающий человек — мыслящий человек»", "— Русская пословица")
    )
    
    private val tips = listOf(
        "Начинайте тренировки с 10-15 минут в день. Регулярность важнее продолжительности!",
        "Устраните отвлекающие факторы во время чтения — выключите уведомления и найдите тихое место.",
        "Практикуйте чтение в разное время дня, чтобы найти свой пик концентрации.",
        "Не возвращайтесь к уже прочитанному тексту — это замедляет скорость чтения.",
        "Используйте указатель (палец или ручку) для ведения взгляда по строкам.",
        "Читайте каждый день хотя бы по 20-30 минут для поддержания навыка.",
        "Перед чтением определите цель — что именно вы хотите узнать из текста.",
        "Делайте короткие перерывы каждые 25-30 минут чтения для отдыха глаз."
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        
        initializeViews(view)
        setupQuoteAndTip()
        setupStatistics()
        setupQuickActions(view)
        
        return view
    }
    
    private fun initializeViews(view: View) {
        quoteText = view.findViewById(R.id.quote_text)
        quoteAuthor = view.findViewById(R.id.quote_author)
        sessionsCount = view.findViewById(R.id.sessions_count)
        totalTime = view.findViewById(R.id.total_time)
        tipText = view.findViewById(R.id.tip_text)
    }
    
    private fun setupQuoteAndTip() {
        // Устанавливаем случайную цитату
        val randomQuote = quotes[Random.nextInt(quotes.size)]
        quoteText.text = randomQuote.first
        quoteAuthor.text = randomQuote.second
        
        // Устанавливаем случайный совет
        val randomTip = tips[Random.nextInt(tips.size)]
        tipText.text = randomTip
    }
    
    private fun setupStatistics() {
        val sharedPreferences = requireContext().getSharedPreferences("TechniqueTimes", Context.MODE_PRIVATE)
        val allPrefs = sharedPreferences.all
        
        // Подсчитываем количество сессий (количество сохраненных времен)
        val sessions = allPrefs.size
        sessionsCount.text = sessions.toString()
        
        // Подсчитываем общее время (сумма всех времен в минутах)
        val totalTimeMs = allPrefs.values.sumOf { 
            when (it) {
                is Long -> it
                else -> 0L
            }
        }
        val totalMinutes = (totalTimeMs / 60000).toInt()
        
        totalTime.text = when {
            totalMinutes < 60 -> "$totalMinutes мин"
            totalMinutes < 1440 -> "${totalMinutes / 60}ч ${totalMinutes % 60}м"
            else -> "${totalMinutes / 1440}д ${(totalMinutes % 1440) / 60}ч"
        }
    }
    
    private fun setupQuickActions(view: View) {
        val practiceCard = view.findViewById<CardView>(R.id.quick_practice_card)
        val learningCard = view.findViewById<CardView>(R.id.quick_learning_card)
        
        practiceCard.setOnClickListener {
            // Переходим на вкладку практики
            (activity as? MainActivity)?.switchToTab(2) // Практика - индекс 2
        }
        
        learningCard.setOnClickListener {
            // Переходим на вкладку обучения
            (activity as? MainActivity)?.switchToTab(1) // Обучение - индекс 1
        }
    }
}
