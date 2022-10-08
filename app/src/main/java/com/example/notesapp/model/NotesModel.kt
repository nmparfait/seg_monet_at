package com.example.notesapp.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class NotesModel(
    val name: String? = null,
    val email: String? = null,
    val date: String? = null,
    val premiumAccess: Boolean? = null,
    val currentLocation: String? = null,
    val notesTitle: String? = null,
    val notesDetail: String? = null,
    val notesDate: String? = null,
    val notesImagePath: String? = null
)
