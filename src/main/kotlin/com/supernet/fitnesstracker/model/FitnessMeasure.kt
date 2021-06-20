package com.supernet.fitnesstracker.model

import com.supernet.fitnesstracker.util.Constants

data class FitnessMeasure(
    val type: Constants.UnitTypeEnum = Constants.UnitTypeEnum.MASS,
    val unit: Constants.UnitMeasureEnum? = Constants.UnitMeasureEnum.LBS
) {
    // TODO: We'll get to this sometime
//    init {
//        if (type == UnitTypeEnum.NONE && unit != null) throw Exception("No good combo")
//    }
}