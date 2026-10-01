package com.umg.quejasbancario.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utilidad de linea de comandos para generar un hash BCrypt valido, util
 * para crear manualmente el usuario Administrador inicial (o cualquier
 * otro) directamente en la base de datos de Neon, sin tener que levantar
 * el backend completo.
 *
 * Uso (desde la raiz del proyecto, con Maven):
 *   mvn compile exec:java -Dexec.mainClass="com.umg.quejasbancario.util.PasswordHashGenerator" -Dexec.args="TuContrasenaSegura"
 *
 * O simplemente ejecuta esta clase desde tu IDE (Run) pasando la
 * contrasena como argumento del programa.
 */
public class PasswordHashGenerator {
    public static void main(String[] args) {
        String contrasena = args.length > 0 ? args[0] : "Admin*2026";
        String hash = new BCryptPasswordEncoder().encode(contrasena);
        System.out.println("Contraseña: " + contrasena);
        System.out.println("Hash BCrypt: " + hash);
        System.out.println("\nUsa este valor en la columna contrasena_hash del INSERT del usuario Administrador (database/postgres_schema.sql o extensions.sql).");
    }
}
