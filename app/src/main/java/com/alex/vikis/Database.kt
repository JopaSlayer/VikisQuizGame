import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

const val DATABASE_NAME = "QuizStats.db"
const val DATABASE_VERSION = 1

const val TABLE_NAME = "quiz_stats"
const val COLUMN_ID = "id"
const val COLUMN_CATEGORY = "category"
const val COLUMN_TOTAL_GAMES = "total_games"
const val COLUMN_TOTAL_ANSWERS = "total_answers"
const val COLUMN_RIGHT_ANSWERS = "right_answers"

class QuizDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val SQL_CREATE_ENTRIES =
        "CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_CATEGORY TEXT," +
                "$COLUMN_TOTAL_GAMES INTEGER," +
                "$COLUMN_TOTAL_ANSWERS INTEGER," +
                "$COLUMN_RIGHT_ANSWERS INTEGER)"

    private val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS $TABLE_NAME"

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }
}

class QuizDatabaseManager(context: Context) {

    private val dbHelper = QuizDatabaseHelper(context)

    fun insertQuizResult(
        category: String,
        totalGames: Int,
        totalAnswers: Int,
        rightAnswers: Int
    ): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CATEGORY, category)
            put(COLUMN_TOTAL_GAMES, totalGames)
            put(COLUMN_TOTAL_ANSWERS, totalAnswers)
            put(COLUMN_RIGHT_ANSWERS, rightAnswers)
        }
        return db.insert(TABLE_NAME, null, values)
    }

    fun getAllQuizResults(category: String): List<QuizStatistics> {
        val db = dbHelper.readableDatabase
        val projection = arrayOf(
            COLUMN_ID,
            COLUMN_CATEGORY,
            COLUMN_TOTAL_GAMES,
            COLUMN_TOTAL_ANSWERS,
            COLUMN_RIGHT_ANSWERS
        )

        val selection = "$COLUMN_CATEGORY = ?"
        val selectionArgs = arrayOf(category)

        val cursor: Cursor = db.query(
            TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        val statisticsList = mutableListOf<QuizStatistics>()
        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow(COLUMN_ID))
                val totalGames = getInt(getColumnIndexOrThrow(COLUMN_TOTAL_GAMES))
                val totalAnswers = getInt(getColumnIndexOrThrow(COLUMN_TOTAL_ANSWERS))
                val rightAnswers = getInt(getColumnIndexOrThrow(COLUMN_RIGHT_ANSWERS))

                statisticsList.add(
                    QuizStatistics(
                        id,
                        category,
                        totalGames,
                        totalAnswers,
                        rightAnswers
                    )
                )
            }
        }
        return statisticsList
    }
}
data class QuizStatistics(
    val id: Long,
    val category: String,
    val totalGames: Int,
    val totalAnswers: Int,
    val rightAnswers: Int
)
