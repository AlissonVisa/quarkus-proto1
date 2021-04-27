package com.alissonvisa.exception;

import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class MyApplicationExceptionHandler implements ExceptionMapper<Exception>
{
        @Override
        public Response toResponse(Exception exception) {
                String stackTrace = ExceptionUtils.getStackTrace(exception);
                return Response.status(Response.Status.BAD_REQUEST).entity(stackTrace).build();
        }
}
