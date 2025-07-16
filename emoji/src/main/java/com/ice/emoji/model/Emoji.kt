package com.ice.emoji.model

data class Emoji(
    val codes: String,
    val char: String,
    val name: String,
    val category: String,
    val group: String,
    val subgroup: String
)
