package feedback.system.repository

import feedback.system.model.FeedbackRequest
import feedback.system.model.FeedbackStatus
import org.springframework.data.mongodb.repository.MongoRepository

interface FeedbackRequestRepository : MongoRepository<FeedbackRequest, String> {
    fun findByEnterpriseId(enterpriseId: String): List<FeedbackRequest>
    fun countByEnterpriseIdAndStatus(enterpriseId: String, status: FeedbackStatus): Long
    fun findByEnterpriseIdAndStatus(enterpriseId: String, status: FeedbackStatus): List<FeedbackRequest>
}
