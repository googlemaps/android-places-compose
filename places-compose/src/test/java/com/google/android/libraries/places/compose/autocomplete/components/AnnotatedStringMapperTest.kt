package com.google.android.libraries.places.compose.autocomplete.components

import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import com.google.common.truth.Truth.assertThat
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
class AutoCompleteTextFieldTest {
    @Test
    fun toAnnotatedStringTest_bold_first_word() {
        val words = listOf(
            "word1",
            "word2",
            "word3"
        )

        val text = words.joinToString(" ")

        val ranges = listOf(
            0 to 5,
            6 to 11,
            12 to 17,
        )

        ranges.zip(words).forEach { (range, word) ->
            testAnnotation(text, range.first, range.second, word)
        }
    }

    private fun testAnnotation(text: String, start: Int, end: Int, word: String) {
        val annotatedText = SpannableStringBuilder(text).apply {
            setSpan(
                /* what = */ StyleSpan(Typeface.BOLD),
                /* start = */ start,
                /* end = */ end,
                /* flags = */ SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }.toAnnotatedString(predictionsHighlightStyle)

        annotatedText.getStringAnnotations(0, annotatedText.length)

        val annotations = annotatedText.spanStyles
        assertThat(annotations).hasSize(1)
        assertThat(annotations[0].start).isEqualTo(start)
        assertThat(annotations[0].end).isEqualTo(end)
        assertThat(annotatedText.substring(start, end)).isEqualTo(word)
    }
}
