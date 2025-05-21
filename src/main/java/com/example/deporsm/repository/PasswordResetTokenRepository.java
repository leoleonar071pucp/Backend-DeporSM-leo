package com.example.deporsm.repository;

import com.example.deporsm.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {

    /**
     * Busca un token de restablecimiento de contraseña por su valor
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Busca tokens válidos (no expirados y no usados) para un usuario específico
     */
    @Query("SELECT t FROM PasswordResetToken t WHERE t.usuario.id = :usuarioId AND t.expiracion > :ahora AND t.usado = false")
    Optional<PasswordResetToken> findValidTokenByUsuarioId(@Param("usuarioId") Integer usuarioId, @Param("ahora") Timestamp ahora);

    /**
     * Elimina los tokens expirados
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiracion <= :ahora OR t.usado = true")
    void deleteExpiredTokens(@Param("ahora") Timestamp ahora);

    /**
     * Invalida todos los tokens existentes para un usuario
     */
    @Modifying
    @Transactional
    @Query("UPDATE PasswordResetToken t SET t.usado = true WHERE t.usuario.id = :usuarioId AND t.usado = false")
    void invalidateAllTokensByUsuarioId(@Param("usuarioId") Integer usuarioId);
}
