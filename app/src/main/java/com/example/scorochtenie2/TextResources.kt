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

data class KeywordTextData(
    val text: String,
    val keyWords: List<String>,
    val questionsAndAnswers: List<Pair<String, List<String>>>
)

object TextResources {
    private var diagonalTexts: List<DiagonalTextData> = emptyList()
    private var keywordTexts: List<KeywordTextData> = emptyList()
    private var otherTexts: Map<String, List<TextData>> = emptyMap()
    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) {
            Log.d("TextResources", "Already initialized, skipping...")
            return
        }
        
        try {
            Log.d("TextResources", "Initializing TextResources...")
            val parser = context.resources.getXml(R.xml.texts)
            val diagonalList = mutableListOf<DiagonalTextData>()
            val keywordList = mutableListOf<KeywordTextData>()
            val otherMap = mutableMapOf<String, MutableList<TextData>>()

            var currentTechnique: String? = null
            var currentText: StringBuilder? = null
            var currentBreakWords: MutableList<String>? = null
            var currentKeyWords: MutableList<String>? = null
            var currentQuestions: MutableList<Pair<String, List<String>>>? = null
            var currentQuestionText: StringBuilder? = null
            var currentAnswers: MutableList<String>? = null

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "technique" -> {
                                currentTechnique = parser.getAttributeValue(null, "name")
                                otherMap[currentTechnique] = mutableListOf()
                                Log.d("TextResources", "Found technique: $currentTechnique")
                            }
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
                            "keyWords" -> {
                                currentKeyWords = mutableListOf()
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
                                if (currentTechnique != null && currentText != null && currentQuestions != null) {
                                    when (currentTechnique) {
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
                                        "Поиск ключевых слов" -> {
                                            keywordList.add(
                                                KeywordTextData(
                                                    text = currentText.toString(),
                                                    keyWords = currentKeyWords ?: emptyList(),
                                                    questionsAndAnswers = currentQuestions.toList()
                                                )
                                            )
                                            Log.d("TextResources", "Added keyword text with ${currentQuestions.size} questions")
                                        }
                                        else -> {
                                            otherMap[currentTechnique]?.add(
                                                TextData(
                                                    text = currentText.toString(),
                                                    questionsAndAnswers = currentQuestions.toList()
                                                )
                                            )
                                            Log.d("TextResources", "Added other text for '$currentTechnique' with ${currentQuestions.size} questions")
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

            diagonalTexts = diagonalList
            keywordTexts = keywordList
            otherTexts = otherMap
            
            Log.d("TextResources", "Initialization complete:")
            Log.d("TextResources", "Diagonal texts: ${diagonalTexts.size}")
            Log.d("TextResources", "Keyword texts: ${keywordTexts.size}")
            Log.d("TextResources", "Other texts: ${otherTexts.size}")
            Log.d("TextResources", "Other texts keys: ${otherTexts.keys}")
            
            isInitialized = true
        } catch (e: Exception) {
            Log.e("TextResources", "Error initializing TextResources", e)
            e.printStackTrace()
            diagonalTexts = emptyList()
            keywordTexts = emptyList()
            otherTexts = emptyMap()
            isInitialized = false
        }
    }

    fun getDiagonalTexts(): List<DiagonalTextData> = diagonalTexts
    fun getKeywordTexts(): List<KeywordTextData> = keywordTexts
    fun getOtherTexts(): Map<String, List<TextData>> = otherTexts
    
    fun reset() {
        isInitialized = false
        diagonalTexts = emptyList()
        keywordTexts = emptyList()
        otherTexts = emptyMap()
        Log.d("TextResources", "Reset TextResources")
    }
}