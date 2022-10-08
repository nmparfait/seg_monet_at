package com.example.notesapp.model

data class Notes(
    val notesTitle: String = "",
    val notesDetail: String = "",
    val notesDate: String = "",
    val notesImagePath: String? = ""
)