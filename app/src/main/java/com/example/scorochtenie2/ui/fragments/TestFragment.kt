package com.example.scorochtenie2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment


class TestFragment : Fragment() {
    
    companion object {
        private const val ARG_TEXT_INDEX = "textIndex"
        private const val ARG_TECHNIQUE_NAME = "techniqueName"
        private const val ARG_DURATION_PER_WORD = "durationPerWord"

        fun newInstance(textIndex: Int, techniqueName: String, durationPerWord: Long): TestFragment {
            return TestFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_TEXT_INDEX, textIndex)
                    putString(ARG_TECHNIQUE_NAME, techniqueName)
                    putLong(ARG_DURATION_PER_WORD, durationPerWord)
                }
            }
        }
    }

    private var score = 0
    private var currentTextIndex = 0
    private var techniqueName: String = ""
    private var durationPerWord: Long = 400L
    private var currentQuestionIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_test, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentTextIndex = arguments?.getInt(ARG_TEXT_INDEX, 0) ?: 0
        techniqueName = arguments?.getString(ARG_TECHNIQUE_NAME) ?: ""
        durationPerWord = arguments?.getLong(ARG_DURATION_PER_WORD) ?: 400L


        TextResources.initialize(requireContext())
        
        displayQuestion(0)
    }

    private fun displayQuestion(index: Int) {
        val questionHeader = view?.findViewById<TextView>(R.id.tv_question_header)
        val questionText = view?.findViewById<TextView>(R.id.questionText)
        val radioGroup = view?.findViewById<RadioGroup>(R.id.radioGroup)
        val submitButton = view?.findViewById<Button>(R.id.btnSubmit)


        val questions = getQuestionsForTechnique(techniqueName, currentTextIndex)



        if (questions.isNullOrEmpty()) {

            questionHeader?.visibility = View.GONE
            questionText?.text = "Ошибка: вопросы для этой техники недоступны."
            radioGroup?.visibility = View.GONE
            submitButton?.visibility = View.GONE
            return
        }

        if (index < questions.size) {
            val questionPair = questions[index]
            questionText?.text = questionPair.first
            questionText?.tag = Pair(index, questionPair.second[0])

            questionHeader?.text = "Вопрос ${index + 1}"

            radioGroup?.removeAllViews()
            val options = questionPair.second.shuffled()
            options.forEach { option ->
                val radioButton = RadioButton(context).apply {
                    text = option
                    id = View.generateViewId()
                    textSize = 16f

                    val textColor = if (context?.resources?.configuration?.uiMode?.and(android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
                        android.graphics.Color.WHITE
                    } else {
                        android.graphics.Color.BLACK
                    }
                    setTextColor(textColor)
                }
                radioGroup?.addView(radioButton)
            }

            submitButton?.setOnClickListener {
                checkAnswer()
            }
        } else {
            showResult()
        }
    }

    private fun getQuestionsForTechnique(techniqueName: String, textIndex: Int): List<Pair<String, List<String>>>? {

        val result = when (techniqueName) {
            "Чтение по диагонали" -> {
                val texts = TextResources.getTexts()

                texts["Чтение по диагонали"]?.getOrNull(textIndex)?.questionsAndAnswers
            }
            "Чтение блоками" -> {
                val texts = TextResources.getTexts()

                texts[techniqueName]?.getOrNull(textIndex)?.questionsAndAnswers
            }
            "Метод указки" -> {
                val texts = TextResources.getTexts()

                texts[techniqueName]?.getOrNull(textIndex)?.questionsAndAnswers
            }
            "Предложения наоборот" -> {
                val texts = TextResources.getTexts()

                texts[techniqueName]?.getOrNull(textIndex)?.questionsAndAnswers
            }
            "Слова наоборот" -> {
                val texts = TextResources.getTexts()

                texts[techniqueName]?.getOrNull(textIndex)?.questionsAndAnswers
            }
            "Зашумленный текст" -> {
                val texts = TextResources.getTexts()

                texts[techniqueName]?.getOrNull(textIndex)?.questionsAndAnswers
            }
            "Частично скрытые строки" -> {
                val texts = TextResources.getTexts()
                texts[techniqueName]?.getOrNull(textIndex)?.questionsAndAnswers
            }
            else -> {

                getDefaultQuestions(textIndex)
            }
        }
        

        return result
    }

    private fun getDefaultQuestions(textIndex: Int): List<Pair<String, List<String>>> {
        return listOf(
            "О чем был текст?" to listOf("О чтении", "О путешествии", "О работе", "О любви"),
            "Сколько персонажей было?" to listOf("Один", "Два", "Три", "Четыре"),
            "Какое было настроение?" to listOf("Хорошее", "Плохое", "Нейтральное", "Неизвестно")
        )
    }

    private fun checkAnswer() {
        val radioGroup = view?.findViewById<RadioGroup>(R.id.radioGroup)
        val questionText = view?.findViewById<TextView>(R.id.questionText)

        val selectedRadioButtonId = radioGroup?.checkedRadioButtonId ?: -1
        if (selectedRadioButtonId == -1) {
            Toast.makeText(context, "Выберите ответ", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedRadioButton = radioGroup?.findViewById<RadioButton>(selectedRadioButtonId)
        val userAnswer = selectedRadioButton?.text.toString().lowercase()
        val correctAnswer = (questionText?.tag as? Pair<*, *>)?.second as? String ?: ""
        
        if (userAnswer == correctAnswer.lowercase()) {
            score++
        }

        radioGroup?.clearCheck()
        currentQuestionIndex++
        displayQuestion(currentQuestionIndex)
    }

    private fun showResult() {
        val questionHeader = view?.findViewById<TextView>(R.id.tv_question_header)
        val questionText = view?.findViewById<TextView>(R.id.questionText)
        val radioGroup = view?.findViewById<RadioGroup>(R.id.radioGroup)
        val submitButton = view?.findViewById<Button>(R.id.btnSubmit)

        val resultContainer = view?.findViewById<View>(R.id.resultContainer)
        val tvResultTitle = view?.findViewById<TextView>(R.id.tvResultTitle)
        val tvResultPercent = view?.findViewById<TextView>(R.id.tvResultPercent)
        val progressComprehension = view?.findViewById<android.widget.ProgressBar>(R.id.progressComprehension)
        val tvResultDetails = view?.findViewById<TextView>(R.id.tvResultDetails)
        val btnDone = view?.findViewById<Button>(R.id.btnDone)

        val totalQuestions = getQuestionsForTechnique(techniqueName, currentTextIndex)?.size ?: 0
        val comprehensionPercentage = if (totalQuestions > 0) {
            (score * 100) / totalQuestions
        } else {
            0
        }


        questionHeader?.visibility = View.GONE
        questionText?.visibility = View.GONE
        radioGroup?.visibility = View.GONE
        submitButton?.visibility = View.GONE


        tvResultTitle?.text = "Тест завершён!"
        tvResultPercent?.text = "$comprehensionPercentage%"
        progressComprehension?.progress = 0
        tvResultDetails?.text = "Верно $score из $totalQuestions"


        val sharedPreferences = requireContext().getSharedPreferences("TechniqueTimes", Context.MODE_PRIVATE)
        val readingTimeMillis = sharedPreferences.getLong(techniqueName, 0L)
        val readingTimeSeconds = (readingTimeMillis / 1000).toInt()

        resultContainer?.alpha = 0f
        resultContainer?.visibility = View.VISIBLE
        resultContainer?.animate()?.alpha(1f)?.setDuration(250)?.start()


        progressComprehension?.animate()?.setDuration(600)?.withStartAction {
            progressComprehension.progress = 0
        }?.withEndAction {

        }?.start()
        progressComprehension?.postDelayed({
            progressComprehension.progress = comprehensionPercentage
        }, 100)


        TestResultManager.saveTestResult(requireContext(), techniqueName, comprehensionPercentage, readingTimeSeconds, currentTextIndex)

        btnDone?.setOnClickListener {

            val intent = Intent(requireContext(), MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra("tab", 2)
            }
            startActivity(intent)
            activity?.finish()
        }
    }
} 