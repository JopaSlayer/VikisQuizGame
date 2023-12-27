package com.alex.vikis

import QuizDatabaseManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alex.vikis.ui.theme.VikisTheme

val cates = mapOf("География" to geographyQuestions, "Игры" to gamingQuestions, "Спорт" to sportsQuestions, "Фильмы" to filmsQuestions, "Анекдоты от нейросети" to comedianQuestions, "Компьютеры" to computerPartsQuestions, "Авто" to autoQuestions, "Биология" to bioQuestions, "Еда" to foodQuestions, "Физика" to physicsQuestions)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            VikisTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                        StartButton()
                    }
                }
            }
        }
    }
}

@Composable
fun StartButton() {
    var isStarted by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Vikis",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        if (!isStarted) {
            Button(
                onClick = { isStarted = !isStarted },
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = "Начать играть!", fontSize = 30.sp)
            }
        }

        if (isStarted) {
            PickACategory()
        }
    }
}

@Composable
fun PickACategory() {
    var choosenCategory = remember {
        mutableStateOf(emptyQuestions)
    }
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (choosenCategory.value == emptyQuestions) {
            for (currentCategory in cates.keys) {
                Button(
                    onClick = { choosenCategory.value = cates.get(currentCategory)!! },
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxWidth(0.90f)
                ) {
                    Text(text = currentCategory, fontSize = 22.sp)
                }
            }
        } else {
            Column {
                StartQuiz(category = choosenCategory.value)
            }
        }
    }
}

@Composable
fun StartQuiz(category: Array<quizQuestions>) {
    val allQuestions = category.toMutableList()
    val shuffledQuestions = remember { allQuestions.shuffled() }

    val usedIndices = remember { mutableSetOf<Int>() }
    var currentQuestionIndex = remember { mutableStateOf(0) }
    var pickedAnswer = remember { mutableStateOf("") }
    var totalAnswers = remember { mutableStateOf(0) }
    var rightAnswers = remember { mutableStateOf(0) }
    var missedAnswers = remember { mutableStateOf(0) }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (currentQuestionIndex.value < 10) {
            while (usedIndices.contains(currentQuestionIndex.value)) {
                currentQuestionIndex.value = (0 until shuffledQuestions.size).random()
            }

            val currentQuestion = shuffledQuestions[currentQuestionIndex.value]
            usedIndices.add(currentQuestionIndex.value)

            Text(
                modifier = Modifier.align(CenterHorizontally),
                fontSize = 25.sp,
                text = currentQuestion.questionCategory
            )
            Text(
                modifier = Modifier
                    .padding(horizontal = 15.dp)
                    .align(CenterHorizontally),
                fontSize = 25.sp,
                text = currentQuestion.questionText
            )

            val shuffledAnswerOptions = currentQuestion.qustionAnswers.toMutableList()
            shuffledAnswerOptions.shuffle()

            for (quizCurrentAnswer in shuffledAnswerOptions) {
                Button(
                    onClick = {
                        pickedAnswer.value = quizCurrentAnswer
                        ++totalAnswers.value
                        if (pickedAnswer.value == currentQuestion.rightAnswer) {
                            ++rightAnswers.value
                        } else {
                            ++missedAnswers.value
                        }
                        ++currentQuestionIndex.value
                    },
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxWidth(0.90f)
                ) {
                    Text(text = quizCurrentAnswer, fontSize = 25.sp)
                }
            }
        } else {
            EndQuiz(totalAnswers = totalAnswers.value, rightAnswers = rightAnswers.value, missedAnswers = missedAnswers.value, category = category)
        }
    }
}


@Composable
fun EndQuiz(totalAnswers: Int, rightAnswers: Int, missedAnswers: Int, category: Array<quizQuestions>) {
    var wannaReturnToStart by remember { mutableStateOf(false) }
    var showStatistics by remember { mutableStateOf(false) }

    val categoryTitle = category[0].questionCategory
    val quizDatabaseManager = QuizDatabaseManager(LocalContext.current)

    Column {
        if (wannaReturnToStart) {
            PickACategory()
        } else {
            Box (Modifier.padding(start = 10.dp)){
                Column {
                    Text(text = "Всего дано ответов: $totalAnswers")
                    Text(text = "Правильных из них: $rightAnswers")
                    Text(text = "Процент выполнения: ${rightAnswers.toFloat() / totalAnswers.toFloat() * 100}%")
                }
            }

            Column (verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = {
                        quizDatabaseManager.insertQuizResult(categoryTitle, 1, totalAnswers, rightAnswers)
                        wannaReturnToStart = !wannaReturnToStart
                    }
                ) {
                    Text(text = "Вернуться на экран выбора темы", fontSize = 17.sp)
                }
                Button(
                    onClick = {
                        showStatistics = !showStatistics
                    }
                ) {
                    Text(text = if (showStatistics) "Скрыть статистику" else "Показать статистику", fontSize = 17.sp)
                }

                if (showStatistics) {
                    StatisticsScreen(category = categoryTitle)
                }


            }


        }
    }
}


@Composable
fun StatisticsScreen(category: String) {
    val quizDatabaseManager = QuizDatabaseManager(LocalContext.current)
    val statistics = quizDatabaseManager.getAllQuizResults(category)

    Column {
        Text(text = "Статистика для категории: $category")
        Text(text = "Всего сыграно игр: ${statistics.sumBy { it.totalGames }}")
        Text(text = "Общее количество ответов: ${statistics.sumBy { it.totalAnswers }}")
        Text(text = "Общее количество правильных ответов: ${statistics.sumBy { it.rightAnswers }}")
        Text(text = "Процент правильных ответов ${statistics.sumBy { (( it.rightAnswers.toFloat()) * 100 / it.totalAnswers.toFloat()).toInt()}}%")

    }
}
