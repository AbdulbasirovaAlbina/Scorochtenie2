package com.example.scorochtenie2

import android.content.Context
import android.util.Log
import com.example.scorochtenie2.R
import org.xmlpull.v1.XmlPullParser

data class TextData(
    val text: String,
    val questionsAndAnswers: List<Pair<String, List<String>>>
)

data class DiagonalTextData(
    val text: String,
    val breakWords: List<String>,
    val questionsAndAnswers: List<Pair<String, List<String>>>
)


object TextResources {
    private var diagonalTexts: List<DiagonalTextData> = emptyList()

    private var otherTexts: Map<String, List<TextData>> = emptyMap()
    private var demoText: TextData? = null
    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) {
            Log.d("TextResources", "Already initialized, skipping...")
            return
        }
        
        try {
            Log.d("TextResources", "Initializing TextResources...")
            val diagonalList = mutableListOf<DiagonalTextData>()
            val otherMap = mutableMapOf<String, MutableList<TextData>>()

            // Mapping technique names to XML resource IDs
            val techniqueFiles = mapOf(
                "Чтение по диагонали" to R.xml.diagonal_reading,
                "Чтение блоками" to R.xml.block_reading,
                "Предложения наоборот" to R.xml.reverse_sentences,
                "Слова наоборот" to R.xml.reverse_words,
                "Метод указки" to R.xml.pointer_method,
                "Частично скрытые строки" to R.xml.partially_hidden_lines,
                "Зашумленный текст" to R.xml.noisy_text
            )

            // Load each technique from its separate XML file
            for ((techniqueName, xmlResourceId) in techniqueFiles) {
                try {
                    val parser = context.resources.getXml(xmlResourceId)
                    loadTechniqueFromParser(parser, techniqueName, diagonalList, otherMap)
                    Log.d("TextResources", "Successfully loaded technique: $techniqueName")
                } catch (e: Exception) {
                    Log.e("TextResources", "Error loading technique $techniqueName", e)
                }
            }

            // Load demo text for learning/demonstration
            try {
                val demoParser = context.resources.getXml(R.xml.demo_text)
                demoText = loadDemoText(demoParser)
                Log.d("TextResources", "Successfully loaded demo text")
            } catch (e: Exception) {
                Log.e("TextResources", "Error loading demo text", e)
            }

            diagonalTexts = diagonalList
            otherTexts = otherMap
            
            Log.d("TextResources", "Initialization complete:")
            Log.d("TextResources", "Diagonal texts: ${diagonalTexts.size}")
            Log.d("TextResources", "Other texts: ${otherTexts.size}")
            Log.d("TextResources", "Other texts keys: ${otherTexts.keys}")
            
            isInitialized = true
        } catch (e: Exception) {
            Log.e("TextResources", "Error initializing TextResources", e)
            e.printStackTrace()
            diagonalTexts = emptyList()
            otherTexts = emptyMap()
            isInitialized = false
        }
    }

    private fun loadTechniqueFromParser(
        parser: XmlPullParser,
        techniqueName: String,
        diagonalList: MutableList<DiagonalTextData>,
        otherMap: MutableMap<String, MutableList<TextData>>
    ) {
        var currentText: StringBuilder? = null
        var currentBreakWords: MutableList<String>? = null
        var currentKeyWords: MutableList<String>? = null
        var currentQuestions: MutableList<Pair<String, List<String>>>? = null
        var currentQuestionText: StringBuilder? = null
        var currentAnswers: MutableList<String>? = null

        if (!otherMap.containsKey(techniqueName)) {
            otherMap[techniqueName] = mutableListOf()
        }

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "text" -> {
                            currentText = StringBuilder()
                            currentBreakWords = mutableListOf()
                            currentKeyWords = mutableListOf()
                            currentQuestions = mutableListOf()
                        }
                        "content" -> {
                            currentText?.append(parser.nextText().trim())
                        }
                        "breakWords" -> {
                            currentBreakWords = mutableListOf()
                        }
                        "word" -> {
                            val word = parser.nextText().trim()
                            currentBreakWords?.add(word)
                            currentKeyWords?.add(word)
                        }
                        "questions" -> {
                            currentQuestions = mutableListOf()
                        }
                        "question" -> {
                            currentQuestionText = StringBuilder(parser.getAttributeValue(null, "text"))
                            currentAnswers = mutableListOf()
                        }
                        "answer" -> {
                            val answer = parser.nextText().trim()
                            currentAnswers?.add(answer)
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    when (parser.name) {
                        "text" -> {
                            if (currentText != null && currentQuestions != null) {
                                when (techniqueName) {
                                    "Чтение по диагонали" -> {
                                        diagonalList.add(
                                            DiagonalTextData(
                                                text = currentText.toString(),
                                                breakWords = currentBreakWords ?: emptyList(),
                                                questionsAndAnswers = currentQuestions.toList()
                                            )
                                        )
                                        Log.d("TextResources", "Added diagonal text with ${currentQuestions.size} questions")
                                    }
                                    else -> {
                                        otherMap[techniqueName]?.add(
                                            TextData(
                                                text = currentText.toString(),
                                                questionsAndAnswers = currentQuestions.toList()
                                            )
                                        )
                                        Log.d("TextResources", "Added text for '$techniqueName' with ${currentQuestions.size} questions")
                                    }
                                }
                            }
                            currentText = null
                            currentBreakWords = null
                            currentKeyWords = null
                            currentQuestions = null
                        }
                        "question" -> {
                            if (currentQuestionText != null && currentAnswers != null) {
                                currentQuestions?.add(
                                    currentQuestionText.toString() to currentAnswers.toList()
                                )
                            }
                            currentQuestionText = null
                            currentAnswers = null
                        }
                    }
                }
            }
            eventType = parser.next()
        }
    }

    private fun loadDemoText(parser: XmlPullParser): TextData? {
        var currentText: StringBuilder? = null
        var currentQuestions: MutableList<Pair<String, List<String>>>? = null
        var currentQuestionText: StringBuilder? = null
        var currentAnswers: MutableList<String>? = null

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "text" -> {
                            currentText = StringBuilder()
                            currentQuestions = mutableListOf()
                        }
                        "content" -> {
                            currentText?.append(parser.nextText().trim())
                        }
                        "questions" -> {
                            currentQuestions = mutableListOf()
                        }
                        "question" -> {
                            currentQuestionText = StringBuilder(parser.getAttributeValue(null, "text"))
                            currentAnswers = mutableListOf()
                        }
                        "answer" -> {
                            val answer = parser.nextText().trim()
                            currentAnswers?.add(answer)
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    when (parser.name) {
                        "text" -> {
                            if (currentText != null && currentQuestions != null) {
                                return TextData(
                                    text = currentText.toString(),
                                    questionsAndAnswers = currentQuestions.toList()
                                )
                            }
                        }
                        "question" -> {
                            if (currentQuestionText != null && currentAnswers != null) {
                                currentQuestions?.add(
                                    currentQuestionText.toString() to currentAnswers.toList()
                                )
                            }
                            currentQuestionText = null
                            currentAnswers = null
                        }
                    }
                }
            }
            eventType = parser.next()
        }
        return null
    }

    fun getDiagonalTexts(): List<DiagonalTextData> = diagonalTexts
    fun getOtherTexts(): Map<String, List<TextData>> = otherTexts
    fun getDemoText(): TextData? = demoText
    
    // Специальная функция для демонстрации техник
    fun getDemoTextForTechnique(techniqueName: String): String {
        return demoText?.text ?: "Демонстрационный текст недоступен"
    }
    
    fun reset() {
        isInitialized = false
        diagonalTexts = emptyList()
        otherTexts = emptyMap()
        demoText = null
        Log.d("TextResources", "Reset TextResources")
    }
}