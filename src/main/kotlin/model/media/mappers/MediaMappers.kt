package io.github.krisalord.model.media.mappers

import io.github.krisalord.model.media.MediaModel
import io.github.krisalord.model.media.dto.CreateMediaResponse
import io.github.krisalord.model.media.dto.SearchMediaResponse

fun MediaModel.toCreateMediaResponse() = CreateMediaResponse(
    id = id.toHexString(),
    title = title,
    genres = genres,
    rating = rating,
    status = status
)

fun MediaModel.toSearchMediaResponse() = SearchMediaResponse(
    id = id.toHexString(),
    title = title,
    genres = genres,
    rating = rating,
    status = status
)