package com.dertefter.neticlient.di

import com.dertefter.neticlient.data.repository.ScheduleRepository
import com.dertefter.neticlient.data.repository.SettingsRepository
import com.dertefter.neticlient.data.repository.UserRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetDependencies {
    val scheduleRepository: ScheduleRepository
    val settingsRepository: SettingsRepository
    val userRepository: UserRepository
}