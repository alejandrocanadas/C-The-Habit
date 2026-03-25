package com.example.cthehabit.utils

import com.example.cthehabit.data.entity.UserMission
import java.util.UUID

object DailyMissionPlanner {

    fun buildTodayMissions(
        pending: List<UserMission>,
        generated: List<Mission>,
        today: String
    ): List<UserMission> {

        val carried = pending.take(3).map {
            it.copy(
                id = UUID.randomUUID().toString(),
                dateAssigned = today,
                completed = false,
                cancelled = false
            )
        }

        val usedActivities = carried.map { it.activity }.toSet()

        val fresh = generated
            .filterNot { it.activity in usedActivities }
            .take(3 - carried.size)
            .map {
                UserMission(
                    id = UUID.randomUUID().toString(),
                    activity = it.activity,
                    text = it.text,
                    dateAssigned = today,
                    completed = false,
                    cancelled = false
                )
            }

        return (carried + fresh).take(3)
    }
}