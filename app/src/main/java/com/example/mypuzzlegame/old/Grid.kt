package com.example.klotskigame

import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun Grid(columns: Int, modifier: Modifier = Modifier) {

}

fun items(
    count: Int,
    key: ((index: Int) -> Any)? = null,
    span: (LazyGridItemSpanScope.(index: Int) -> GridItemSpan)? = null,
    contentType: (index: Int) -> Any? = { null },
    itemContent: @Composable LazyGridItemScope.(index: Int) -> Unit
) {

}