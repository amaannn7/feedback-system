package feedback.system.repository

import feedback.system.model.FeedbackFormConfig
import org.springframework.data.mongodb.repository.MongoRepository

interface FeedbackFormConfigRepository : MongoRepository<FeedbackFormConfig, String> {
    fun findByEnterpriseId(enterpriseId: String): FeedbackFormConfig?
}
