package com.ys.nearbypaint.presentation.view.view

enum class ElementMode(val rawValue :Int)  {
    MODE_ERASER(-1),
    MODE_LINE(0),
    MODE_STAMP_SQUARE(1),
    MODE_STAMP_RECTANGLE(2),
    MODE_STAMP_TRIANGLE(3),
    MODE_STAMP_STAR(4),
    UNKNOWN(99);

    companion object {
        fun from(findValue: Int): ElementMode = ElementMode.values().first { it.rawValue == findValue }
    }
}