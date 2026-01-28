package com.edulingua.core.models

/**
 * System roles with hierarchical permissions.
 * Users = Admin roles, Consumers = Test-taker roles
 */
enum class Role(
    val code: String,
    val description: String,
    val type: RoleType,
    val permissions: Set<Permission>
) {
    // Admin role - Users type
    ADMIN(
        code = "A",
        description = "Administrator",
        type = RoleType.USER,
        permissions = setOf(
            Permission.MANAGE_USERS,
            Permission.MANAGE_CONSUMERS,
            Permission.VIEW_REPORTS,
            Permission.GENERATE_REPORTS,
            Permission.MANAGE_TESTS,
            Permission.MANAGE_QUESTIONS,
            Permission.VIEW_ALL_DATA,
            Permission.EXPORT_DATA,
            Permission.MANAGE_ROLES
        )
    ),

    // Consumer roles - hierarchical structure
    STATE(
        code = "S",
        description = "State Level",
        type = RoleType.CONSUMER,
        permissions = setOf(
            Permission.VIEW_STATE_DATA,
            Permission.MANAGE_DISTRICTS,
            Permission.VIEW_REPORTS,
            Permission.VIEW_STATE_TESTS
        )
    ),

    DISTRICT(
        code = "D",
        description = "District Level",
        type = RoleType.CONSUMER,
        permissions = setOf(
            Permission.VIEW_DISTRICT_DATA,
            Permission.MANAGE_SCHOOLS,
            Permission.VIEW_REPORTS,
            Permission.VIEW_DISTRICT_TESTS
        )
    ),

    SCHOOL(
        code = "SC",
        description = "School Level",
        type = RoleType.CONSUMER,
        permissions = setOf(
            Permission.VIEW_SCHOOL_DATA,
            Permission.MANAGE_CLASSES,
            Permission.VIEW_SCHOOL_REPORTS,
            Permission.ASSIGN_TESTS
        )
    ),

    CLASS(
        code = "CL",
        description = "Class Level",
        type = RoleType.CONSUMER,
        permissions = setOf(
            Permission.VIEW_CLASS_DATA,
            Permission.VIEW_CLASS_REPORTS,
            Permission.MANAGE_STUDENTS
        )
    ),

    STUDENT(
        code = "ST",
        description = "Student",
        type = RoleType.CONSUMER,
        permissions = setOf(
            Permission.TAKE_TEST,
            Permission.VIEW_OWN_RESULTS,
            Permission.VIEW_OWN_PROFILE
        )
    );

    companion object {
        fun fromCode(code: String): Role? {
            return values().find { it.code == code }
        }

        fun getUserRoles(): List<Role> {
            return values().filter { it.type == RoleType.USER }
        }

        fun getConsumerRoles(): List<Role> {
            return values().filter { it.type == RoleType.CONSUMER }
        }
    }

    fun hasPermission(permission: Permission): Boolean {
        return permissions.contains(permission)
    }
}

/**
 * Type of role - differentiates between admin users and consumers
 */
enum class RoleType {
    USER,      // Admin users who manage the system
    CONSUMER   // Test takers with hierarchical access
}

/**
 * Granular permissions that can be assigned to roles
 */
enum class Permission(val description: String) {
    // Admin permissions
    MANAGE_USERS("Manage admin users"),
    MANAGE_CONSUMERS("Manage consumer accounts"),
    MANAGE_ROLES("Manage roles and permissions"),
    VIEW_ALL_DATA("View all system data"),
    EXPORT_DATA("Export system data"),
    GENERATE_REPORTS("Generate comprehensive reports"),

    // Test management
    MANAGE_TESTS("Create and manage tests"),
    MANAGE_QUESTIONS("Create and manage questions"),
    ASSIGN_TESTS("Assign tests to students"),

    // Hierarchical consumer permissions - State
    VIEW_STATE_DATA("View state level data"),
    MANAGE_DISTRICTS("Manage districts under state"),
    VIEW_STATE_TESTS("View state level test results"),

    // District level
    VIEW_DISTRICT_DATA("View district level data"),
    MANAGE_SCHOOLS("Manage schools under district"),
    VIEW_DISTRICT_TESTS("View district level test results"),

    // School level
    VIEW_SCHOOL_DATA("View school level data"),
    MANAGE_CLASSES("Manage classes in school"),
    VIEW_SCHOOL_REPORTS("View school reports"),

    // Class level
    VIEW_CLASS_DATA("View class level data"),
    VIEW_CLASS_REPORTS("View class reports"),
    MANAGE_STUDENTS("Manage students in class"),

    // Student level
    TAKE_TEST("Take assigned tests"),
    VIEW_OWN_RESULTS("View own test results"),
    VIEW_OWN_PROFILE("View own profile"),

    // Common
    VIEW_REPORTS("View reports")
}
