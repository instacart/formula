package com.examples.todoapp.tasks

import com.instacart.formula.fragment.FragmentKey
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TaskListKey(
    override val tag: String = "task list",
) : FragmentKey
