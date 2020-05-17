package me.saket.press.shared.db

import com.squareup.sqldelight.db.SqlDriver
import me.saket.press.PressDatabase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

expect fun inMemorySqlDriver(): SqlDriver

/**
 * Creates an in-memory database and closes it before and after each test.
 * This class exists because JUnit rules aren't a thing (yet) in Kotlin tests.
 * The name of this class is not a typo.
 */
open class BaseDatabaeTest {

  private lateinit var sqlDriver: SqlDriver
  private lateinit var _database: PressDatabase

  fun database(): PressDatabase {
    if (::_database.isInitialized) {
      return _database
    } else {
      throw IllegalStateException(
          "Test database isn't created because @BeforeTest hasn't been " +
              "called yet. Avoid storing the database as a class property."
      )
    }
  }

  @BeforeTest
  fun initDb() {
    sqlDriver = inMemorySqlDriver()
    _database = sqlDriver.createPressDatabase()
  }

  @AfterTest
  fun closeDb() {
    sqlDriver.close()
  }
}
