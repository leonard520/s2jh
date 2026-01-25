package lab.s2jh.core.web.mvc.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import lab.s2jh.core.exception.DuplicateTokenException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * Spring MVC Token Interceptor - replacement for Struts ExtTokenInterceptor
 * 
 * Prevents duplicate form submission by validating tokens.
 * Only validates if token parameter is present (lenient mode).
 */
@Component
public class TokenHandlerInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(TokenHandlerInterceptor.class);

    /** Token name field parameter */
    public static final String TOKEN_NAME_FIELD = "struts.token.name";
    
    /** Session attribute for counter token */
    public static final String TOKEN_COUNTER = "struts.form.submit.counter.token";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) 
            throws Exception {
        
        // Only check POST requests
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // Check if token name field is present
        String tokenNameField = request.getParameter(TOKEN_NAME_FIELD);
        if (StringUtils.isBlank(tokenNameField)) {
            // No token parameter, skip validation (lenient mode)
            return true;
        }

        logger.debug("Token name field found: {}", tokenNameField);
        
        // Get the actual token value
        String token = request.getParameter(tokenNameField);
        if (StringUtils.isBlank(token)) {
            logger.warn("Token value is empty for field: {}", tokenNameField);
            return true;
        }

        // Validate against session
        HttpSession session = request.getSession(true);
        synchronized (session) {
            String counterToken = (String) session.getAttribute(TOKEN_COUNTER);
            if (counterToken != null && token.equals(counterToken)) {
                logger.warn("Duplicate token detected: {}", token);
                throw new DuplicateTokenException("The form has already been processed");
            }
            // Store token to prevent resubmission
            session.setAttribute(TOKEN_COUNTER, token);
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
        // No post-processing needed
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        // No cleanup needed
    }
}
