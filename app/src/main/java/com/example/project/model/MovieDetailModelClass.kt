package com.example.project.model

import java.io.Serializable

data class MovieDetailModelClass(
   var index: String,
   var movieName: String,
   var movieEpisode: String,
   var movieDescription: String,
   var moviePoster: String
):Serializable{
   constructor():this("", "","","","")
}
