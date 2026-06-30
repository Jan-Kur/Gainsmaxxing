package com.gainsmaxxing.domain

object CalendarCycle {
    fun nextTypeId(current: Long?, orderedIds: List<Long>): Long? {
        if (orderedIds.isEmpty()) return null
        if (current == null) return orderedIds.first()
        val index = orderedIds.indexOf(current)
        if (index < 0) return orderedIds.first()
        return if (index >= orderedIds.lastIndex) null else orderedIds[index + 1]
    }
}
