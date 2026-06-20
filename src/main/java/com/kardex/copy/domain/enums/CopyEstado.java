package com.kardex.copy.domain.enums;

/**
 * Estados posibles de un trabajo de copia del módulo kardex (weighted-average).
 * Replica el contrato uniforme del orquestador (REQ-INVWA-01).
 */
public enum CopyEstado {

    EN_PROCESO,
    COMPLETADO,
    COMPLETADO_CON_ADVERTENCIAS,
    FALLIDO,
    ERROR_NO_REINTENTABLE,
    CANCELADO
}
