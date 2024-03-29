package com.example.tetrisai.model

enum class TetrisBlock(val shapes: Array<Array<BooleanArray>>) {
    I(arrayOf(
        arrayOf(
            booleanArrayOf(false, false, false, false),
            booleanArrayOf(true, true, true, true),
            booleanArrayOf(false, false, false, false),
            booleanArrayOf(false, false, false, false)
        ),
        arrayOf(
            booleanArrayOf(false, false, true, false),
            booleanArrayOf(false, false, true, false),
            booleanArrayOf(false, false, true, false),
            booleanArrayOf(false, false, true, false)
        )
    )),
    J(arrayOf(
        arrayOf(
            booleanArrayOf(true, false, false),
            booleanArrayOf(true, true, true),
            booleanArrayOf(false, false, false)
        ),
        arrayOf(
            booleanArrayOf(false, true, true),
            booleanArrayOf(false, true, false),
            booleanArrayOf(false, true, false)
        ),
        arrayOf(
            booleanArrayOf(false, false, false),
            booleanArrayOf(true, true, true),
            booleanArrayOf(false, false, true)
        ),
        arrayOf(
            booleanArrayOf(false, true, false),
            booleanArrayOf(false, true, false),
            booleanArrayOf(true, true, false)
        )
    )),
    L(arrayOf(
        arrayOf(
            booleanArrayOf(false, false, true),
            booleanArrayOf(true, true, true),
            booleanArrayOf(false, false, false)
        ),
        arrayOf(
            booleanArrayOf(false, true, false),
            booleanArrayOf(false, true, false),
            booleanArrayOf(false, true, true)
        ),
        arrayOf(
            booleanArrayOf(false, false, false),
            booleanArrayOf(true, true, true),
            booleanArrayOf(true, false, false)
        ),
        arrayOf(
            booleanArrayOf(true, true, false),
            booleanArrayOf(false, true, false),
            booleanArrayOf(false, true, false)
        )
    )),
    O(arrayOf(
        arrayOf(
            booleanArrayOf(true, true),
            booleanArrayOf(true, true)
        )
    )),
    S(arrayOf(
        arrayOf(
            booleanArrayOf(false, true, true),
            booleanArrayOf(true, true, false),
            booleanArrayOf(false, false, false)
        ),
        arrayOf(
            booleanArrayOf(false, true, false),
            booleanArrayOf(false, true, true),
            booleanArrayOf(false, false, true)
        )
    )),
    T(arrayOf(
        arrayOf(
            booleanArrayOf(false, true, false),
            booleanArrayOf(true, true, true),
            booleanArrayOf(false, false, false)
        ),
        arrayOf(
            booleanArrayOf(false, true, false),
            booleanArrayOf(false, true, true),
            booleanArrayOf(false, true, false)
        ),
        arrayOf(
            booleanArrayOf(false, false, false),
            booleanArrayOf(true, true, true),
            booleanArrayOf(false, true, false)
        ),
        arrayOf(
            booleanArrayOf(false, true, false),
            booleanArrayOf(true, true, false),
            booleanArrayOf(false, true, false)
        )
    )),
    Z(arrayOf(
        arrayOf(
            booleanArrayOf(true, true, false),
            booleanArrayOf(false, true, true),
            booleanArrayOf(false, false, false)
        ),
        arrayOf(
            booleanArrayOf(false, false, true),
            booleanArrayOf(false, true, true),
            booleanArrayOf(false, true, false)
        )
    )),
    EMPTY(arrayOf(arrayOf(booleanArrayOf(false))));

    fun getShape(rotation: Int = 0): Array<BooleanArray> {
        return shapes[rotation % shapes.size]
    }

    fun getNumberOfRotations() : Int {
        return shapes.size
    }

    fun getBlockName(): String {
        return this.name
    }

}