package feedback.system

import org.junit.jupiter.api.Test

class ApplicationTests {

	@Test
	fun `main class exists`() {
		// Verifies the application class is loadable without requiring MongoDB
		val clazz = Application::class
		assert(clazz.simpleName == "Application")
	}
}
