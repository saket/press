package me.saket.press.shared.db

import androidx.test.core.app.ApplicationProvider
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import me.saket.press.PressDatabase

/**
 * Passing an empty name creates an in-memory DB. The advantage of using this
 * over a JdbcDriver is that manual creation of the tables isn't required.
 * Robolectric takes care of it.
 */
actual fun inMemorySqlDriver(): SqlDriver = AndroidSqliteDriver(
    schema = PressDatabase.Schema,
    context = ApplicationProvider.getApplicationContext(),
    name = null
)