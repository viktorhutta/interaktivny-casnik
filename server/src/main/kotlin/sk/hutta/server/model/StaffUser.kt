package sk.hutta.server.model

import jakarta.persistence.*

enum class Role { WAITER, KITCHEN, ADMIN }

@Entity
class StaffUser(
    var username: String,
    var passwordHash: String,
    @Enumerated(EnumType.STRING)
    var role: Role,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
)