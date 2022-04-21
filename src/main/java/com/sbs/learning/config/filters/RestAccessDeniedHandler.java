package com.sbs.learning.config.filters;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sbs.learning.model.http.response.ApiResponse;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
	    AccessDeniedException accessDeniedException) throws IOException, ServletException {
		ApiResponse apiResponse = new ApiResponse(403, "Forbiden: Access Denied");
		response.setStatus(apiResponse.getStatus());
		PrintWriter out = response.getWriter();
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(out, apiResponse);
		out.close();
		
	}

}
