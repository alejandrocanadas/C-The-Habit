package com.example.cthehabit.utils

import com.example.cthehabit.data.entity.UserMission
import java.util.UUID

object MissionMapper {

    fun toUserMissions(
        missions: List<Mission>,
        dateAssigned: String
    ): List<UserMission> {
        return missions.map { mission ->
            UserMission(
                id = UUID.randomUUID().toString(),
                activity = mission.activity,
                text = mission.text,
                dateAssigned = dateAssigned,
                completed = false,
                cancelled = false
            )
        }
    }
}