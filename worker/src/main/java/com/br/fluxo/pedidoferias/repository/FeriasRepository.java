package com.br.fluxo.pedidoferias.repository;

import com.br.fluxo.pedidoferias.model.Ferias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeriasRepository extends JpaRepository<Ferias, Long> {

}
