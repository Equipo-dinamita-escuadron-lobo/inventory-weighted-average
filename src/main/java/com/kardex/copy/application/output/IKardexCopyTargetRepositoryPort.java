package com.kardex.copy.application.output;

import com.kardex.infrastructure.adapters.output.jpa.entity.KardexEntity;

/**
 * Puerto de salida: escritura de KardexEntity en destino durante copia.
 * REQ-INVWA-01.
 */
public interface IKardexCopyTargetRepositoryPort {

    KardexEntity guardar(KardexEntity entity);
}
