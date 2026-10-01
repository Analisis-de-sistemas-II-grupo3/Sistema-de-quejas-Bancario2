package com.umg.quejasbancario.repository;

import com.umg.quejasbancario.entity.CuentaBancaria;
import com.umg.quejasbancario.entity.enums.EstadoCuenta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CuentaBancariaRepository extends JpaRepository<CuentaBancaria, Integer> {
    Optional<CuentaBancaria> findByNumeroCuentaAndEstado(String numeroCuenta, EstadoCuenta estado);
    List<CuentaBancaria> findByUsuario_IdUsuarioAndEstado(Integer idUsuario, EstadoCuenta estado);
    boolean existsByUsuario_IdUsuarioAndEstado(Integer idUsuario, EstadoCuenta estado);
}
