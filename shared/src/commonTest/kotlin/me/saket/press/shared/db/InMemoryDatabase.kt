package me.saket.press.shared.db

import com.squareup.sqldelight.db.SqlDriver

expect fun inMemorySqlDriver(): SqlDriver

@Suppress("TestFunctionName")
fun TestDatabase() = inMemorySqlDriver().createPressDatabase()
