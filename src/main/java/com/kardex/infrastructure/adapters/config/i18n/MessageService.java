package com.kardex.infrastructure.adapters.config.i18n;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.kardex.domain.port.IMessageServicePort;

import lombok.RequiredArgsConstructor;

/**
 * Servicio para la gestión de mensajes internacionalizados.
 * Proporciona métodos para obtener mensajes en diferentes idiomas.
 */
@Service
@RequiredArgsConstructor
public class MessageService implements IMessageServicePort {
    
    private final MessageSource messageSource;

    /**
     * Obtiene un mensaje usando el locale actual
     * @param key la clave del mensaje
     * @param args argumentos para formatear el mensaje
     * @return el mensaje formateado
     */
    @Override
    public String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

    /**
     * Obtiene un mensaje con un valor por defecto si no se encuentra la clave
     * @param key la clave del mensaje
     * @param defaultMessage mensaje por defecto
     * @param args argumentos para formatear el mensaje
     * @return el mensaje formateado o el mensaje por defecto
     */
    @Override
    public String getMessage(String key, String defaultMessage, Object... args) {
        return messageSource.getMessage(key, args, defaultMessage, LocaleContextHolder.getLocale());
    }
}
