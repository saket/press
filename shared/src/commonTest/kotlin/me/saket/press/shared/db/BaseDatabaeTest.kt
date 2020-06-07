package me.saket.press.shared.db

import com.squareup.sqldelight.db.SqlDriver
import me.saket.press.PressDatabase
import me.saket.press.shared.RobolectricTest
import kotlin.test.AfterTest

expect fun inMemorySqlDriver(): SqlDriver

/**
 * Creates an in-memory database and closes it before and after each test.
 * This class exists because JUnit rules aren't a thing (yet) in Kotlin tests.
 * The name of this class is not a typo.
 */
open class BaseDatabaeTest : RobolectricTest() {

  private val sqlDriver: SqlDriver = inMemorySqlDriver()
  protected val database: PressDatabase = sqlDriver.createPressDatabase()

  @AfterTest
  fun closeDb() {
    sqlDriver.close()
  }
}
