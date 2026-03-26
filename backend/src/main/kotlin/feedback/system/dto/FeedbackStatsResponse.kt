package feedback.system.dto

data class FeedbackStatsResponse(
    val enterpriseId: String,
    val totalCount: Long,
    val pendingCount: Long,
    val respondedCount: Long,
    val expiredCount: Long,
    val averageRating: Double?,
    val ratingDistribution: Map<Int, Long>
)
