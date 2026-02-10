package concurrency

import io.github.krisalord.config.JwtConfig
import io.github.krisalord.config.JwtSettings
import io.github.krisalord.errors.UserAlreadyExistsException
import io.github.krisalord.models.user.UserModel
import io.github.krisalord.models.user.dto.RegisterRequest
import io.github.krisalord.repositories.UserRepository
import io.github.krisalord.security.PasswordHashing
import io.github.krisalord.services.AuthService
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import java.util.*
import kotlin.test.assertEquals


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthServiceConcurrentRegistrationTest {

    private lateinit var userRepository: UserRepository
    private lateinit var authService: AuthService

    @BeforeAll
    fun setup() = runBlocking {
        val client = KMongo.createClient("mongodb+srv://admin:admin@secure-ai-notes-api.3kpghgz.mongodb.net/?appName=secure-ai-notes-api").coroutine
        val database = client.getDatabase("secure-ai-notes-api")
        val userCollection = database.getCollection<UserModel>("users")

        userRepository = UserRepository(userCollection)
        val passwordHashing = PasswordHashing()
        val jwtConfig = JwtConfig(
            JwtSettings("test-secret", "test-issuer", 1000 * 60 * 60)
        )
        authService = AuthService(userRepository, passwordHashing, jwtConfig)
    }


    @Test
    fun `two concurrent registrations with same email results in one created and one conflict`() = runBlocking {
        val email = "concurrent2@test.com"
        val password = "password123"
        val results = Collections.synchronizedList(mutableListOf<String>())

        coroutineScope {
            repeat(2) {
                launch {
                    try {
                        authService.register(RegisterRequest(email, password))
                        results.add("CREATED")
                    } catch (e: UserAlreadyExistsException) {
                        results.add("CONFLICT")
                    } catch (e: Exception) {
                        e.printStackTrace()
                        results.add("ERROR")
                    }
                }
            }
        }

        val createdCount = results.count { it == "CREATED" }
        val conflictCount = results.count { it == "CONFLICT" }

        println("Created: $createdCount, Conflicts: $conflictCount")

        assertEquals(1, createdCount, "One user should be created")
        assertEquals(1, conflictCount, "One registration should fail because of duplicate email")
    }
}