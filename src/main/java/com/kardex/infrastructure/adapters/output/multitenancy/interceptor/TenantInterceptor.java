package com.kardex.infrastructure.adapters.output.multitenancy.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;

import com.kardex.infrastructure.adapters.output.messageBroker.aspect.JwtTokenService;
import com.kardex.infrastructure.adapters.output.multitenancy.utils.TenantContext;

@Component
public class TenantInterceptor implements WebRequestInterceptor {

    @Autowired
    private JwtTokenService jwtTokenService;

    /**
     * Este método se llama antes de que se llame al controlador, y establece el
     * identificador de inquilino desde el JWT en el TenantContext.
     * Utiliza el servicio unificado que maneja tanto contexto HTTP como RabbitMQ.
     * 
     * @param request La solicitud web
     * @throws Exception Si no se pudo establecer el identificador de inquilino
     *                   desde el JWT
     */
    @Override
    public void preHandle(WebRequest request) throws Exception {
        try {
            String tenantId = jwtTokenService.getTenantId();
            TenantContext.setTenantId(tenantId);
        } catch (Exception e) {
            // En caso de error, no establecer el tenant context
            // Esto permitirá que la aplicación funcione sin contexto de tenant si es necesario
            throw new Exception("No se pudo establecer el contexto del tenant desde el JWT", e);
        }
    }

    /**  
     * Este metodo se llama después de que se llama al controlador. Borra el
     * identificador de inquilino del TenantContext.
     */
    @Override
    public void postHandle(WebRequest request, ModelMap model) throws Exception {
        TenantContext.clear();
    }

    /**
     * Este metodo se llama después de que se llama al controlador y
     * después de que se llama al método postHandle. No hace nada en este
     * caso, pero se declara para implementar la interfaz WebRequestInterceptor.
     * 
     * @param request La solicitud web
     * @param ex      La excepcion lanzada por el controlador, si es que se
     *               lanza, o null si no se lanzó ninguna excepción
     * @throws Exception Si se produce un error inesperado
     */
    @Override
    public void afterCompletion(WebRequest request, Exception ex) throws Exception {
        // No hay nada que hacer aquí
    }
}