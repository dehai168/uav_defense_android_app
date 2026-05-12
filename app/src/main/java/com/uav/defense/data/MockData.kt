package com.uav.defense.data

import com.uav.defense.data.models.Camera
import com.uav.defense.data.models.PadTarget

object MockData {
    val targets = listOf(
        PadTarget("T001", "danger", "大疆 Phantom 4", "2.4GHz", 45f, 0.8f, 120f, "向雷达中心逼近", "pending", "14:21:33", 114.0628935, 22.5358332, 15f, "无人机", "hostile", "约2分钟", -5f, -0.5f),
        PadTarget("T002", "warning", "御 Mini 3", "5.8GHz", 120f, 1.2f, 80f, "匀速直线飞行", "pending", "14:24:15", 114.0675122, 22.5253315, 10f, "无人机", "hostile", "约5分钟", -3f, -0.3f),
        PadTarget("T003", "warning", "巡检机 A1", "433MHz", 200f, 1.1f, 60f, "预设巡检航线", "pending", "14:18:00", 114.0537066, 22.5214246, 8f, "无人机", "friendly", "约4分钟", -5f, -0.3f),
        PadTarget("T004", "warning", "苍鹭", "-", 280f, 0.9f, 40f, "自然飞行", "pending", "14:20:45", 114.0487314, 22.5321449, 20f, "鸟类", "friendly", "约3分钟", -5f, -0.3f),
        PadTarget("T005", "warning", "轻型飞行器", "-", 320f, 1.4f, 200f, "固定航线", "pending", "14:22:10", 114.0485989, 22.5403987, 30f, "飞行器", "friendly", "约6分钟", -5f, -0.3f)
    )

    val cameras = listOf(
        Camera("CAM-1", "光电跟踪仪-01", "tracking", "大疆 Phantom 4"),
        Camera("CAM-2", "光电跟踪仪-02", "searching", "-"),
        Camera("CAM-3", "热成像仪-01", "locked", "御 Mini 3"),
        Camera("CAM-4", "可见光仪-01", "searching", "-")
    )
}
