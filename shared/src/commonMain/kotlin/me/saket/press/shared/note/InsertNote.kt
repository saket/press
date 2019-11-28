package me.saket.press.shared.note

import com.benasher44.uuid.Uuid

data class InsertNote internal constructor(val uuid: Uuid, val content: String)
