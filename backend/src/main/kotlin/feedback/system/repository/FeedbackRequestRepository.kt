package feedback.system.repository

import feedback.system.model.FeedbackRequest
import org.springframework.data.mongodb.repository.MongoRepository

interface FeedbackRequestRepository : MongoRepository<FeedbackRequest, String> {
    fun findByEnterpriseId(enterpriseId: String): List<FeedbackRequest>
    fun countByEnterpriseIdAndStatus(enterpriseId: String, status: feedback.system.model.FeedbackStatus): Long
}
