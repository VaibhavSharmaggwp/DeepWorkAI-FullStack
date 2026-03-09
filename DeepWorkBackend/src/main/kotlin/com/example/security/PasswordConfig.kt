package com.example.security
import org.mindrot.jbcrypt.BCrypt


object PasswordConfig{
    // Turns a password into a secure hash
    fun hashPassword(password: String): String{
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

    // Checks if a typed password matches the stored hash
    fun checkPassword(password: String, hash: String): Boolean {
        return BCrypt.checkpw(password, hash)
    }
}