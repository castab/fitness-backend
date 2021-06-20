package com.supernet.fitnesstracker.util

object Constants {
    enum class UnitTypeEnum {
        NONE, MASS, DISTANCE, TIME
    }

    enum class UnitMeasureEnum {
        LBS, KGS, SECONDS, MINUTES, HOURS, MILES, KMS, METERS
    }

    fun UnitMeasureEnum?.getType() =
        when (this) {
            UnitMeasureEnum.LBS, UnitMeasureEnum.KGS -> UnitTypeEnum.MASS
            UnitMeasureEnum.SECONDS, UnitMeasureEnum.MINUTES, UnitMeasureEnum.HOURS -> UnitTypeEnum.TIME
            UnitMeasureEnum.METERS, UnitMeasureEnum.KMS, UnitMeasureEnum.MILES -> UnitTypeEnum.DISTANCE
            else -> UnitTypeEnum.NONE
        }
}