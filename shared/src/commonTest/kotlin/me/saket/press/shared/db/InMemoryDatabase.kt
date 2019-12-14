package me.saket.press.shared.db

import com.squareup.sqldelight.db.SqlDriver

expect fun inMemorySqlDriver(): SqlDriver

/**
 * TODO: Add a base test class that will delete the DB after each test.
 */
@Suppress("TestFunctionName")
fun TestDatabase() = inMemorySqlDriver().createPressDatabase()
