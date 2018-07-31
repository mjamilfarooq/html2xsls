package com.h2e.webservice.aspect;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletResponse;

@Aspect
@Configuration
public class ConverterControllerAspect {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @AfterReturning( value = "execution(* com.h2e.webservice.controller.ConverterController.converter(..))", returning = "result")
    public void cleanUp(JoinPoint joinPoint, ResponseEntity<Resource> result)
    {
        try {
            logger.info("in cleanup " + result.getBody().getFilename());
          //  result.getBody().getFile().delete();
        }
        catch(Exception ex)
        {
            logger.error("Unabe to delete file " + result.getBody().getFilename());
        }
    }
}
