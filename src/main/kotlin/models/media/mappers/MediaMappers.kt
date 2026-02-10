package io.github.krisalord.models.media.mappers

import io.github.krisalord.models.media.MediaModel
import io.github.krisalord.models.media.dto.CreateMediaResponse
import io.github.krisalord.models.media.dto.SearchMediaResponse

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