package com.msxf.sso.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.client.authentication.AuthenticationFilter;
import org.jasig.cas.client.session.SingleSignOutFilter;
import org.jasig.cas.client.util.AbstractConfigurationFilter;
import org.jasig.cas.client.util.AssertionThreadLocalFilter;
import org.jasig.cas.client.util.HttpServletRequestWrapperFilter;
import org.jasig.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter;
import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;

/**
 * 复合Filter
 * @create 2015-7-21 下午4:30:30
 * @author 玄玉<http://blog.csdn.net/jadyer>
 */
public class MSCompositeFilter extends AbstractConfigurationFilter {
	private Filter[] filters = new Filter[5];

	public void init(FilterConfig filterConfig) throws ServletException {
		String casServerLoginUrl = "http://sso.jadyer.com:8080/cas-server-web/login";
		String casServerUrlPrefix = "http://sso.jadyer.com:8080/cas-server-web";
		String casClientServerName = "http://boss.jadyer.com:8080";
		AuthenticationFilter casAuthenticationFilter = new AuthenticationFilter();
		casAuthenticationFilter.setCasServerLoginUrl(casServerLoginUrl);
		casAuthenticationFilter.setServerName(casClientServerName);
		Cas20ProxyReceivingTicketValidationFilter casTicketValidationFilter = new Cas20ProxyReceivingTicketValidationFilter();
		casTicketValidationFilter.setServerName(casClientServerName);
		casTicketValidationFilter.setTicketValidator(new Cas20ServiceTicketValidator(casServerUrlPrefix));
		filters[0] = new SingleSignOutFilter();
//		filters[1] = new AuthenticationFilter();
//		filters[2] = new Cas20ProxyReceivingTicketValidationFilter();
		filters[1] = casAuthenticationFilter;
		filters[2] = casTicketValidationFilter;
		filters[3] = new HttpServletRequestWrapperFilter();
		filters[4] = new AssertionThreadLocalFilter();
		for(Filter obj : filters){
			System.out.println("[复合Filter]-->Initializing Filter defined in ApplicationContext: '" + obj.toString() + "'");
			obj.init(filterConfig);
		}
	}

	public void destroy() {
		for(Filter obj : filters){
			System.out.println("[复合Filter]-->Destroying Filter defined in ApplicationContext: '" + obj.toString() + "'");
			obj.destroy();
		}
	}

	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest)req;
		HttpServletResponse response = (HttpServletResponse)resp;
		VirtualFilterChain virtualFilterChain = new VirtualFilterChain(chain, filters);
		System.out.println("[复合Filter]-->11");
		virtualFilterChain.doFilter(request, response);
		System.out.println("[复合Filter]-->22");
	}
	
	private static class VirtualFilterChain implements FilterChain {
		private final FilterChain originalChain;
		private final Filter[] additionalFilters;
		private int currentPosition = 0;
		private VirtualFilterChain(FilterChain chain, Filter[] additionalFilters) {
			this.originalChain = chain;
			this.additionalFilters = additionalFilters;
		}
		@Override
		public void doFilter(final ServletRequest request, final ServletResponse response) throws IOException, ServletException {
			System.out.println("[复合Filter]-->33");
			if(currentPosition == additionalFilters.length){
				System.out.println("[复合Filter]-->44");
				originalChain.doFilter(request, response);
				System.out.println("[复合Filter]-->55");
			}else{
				currentPosition++;
				Filter nextFilter = additionalFilters[currentPosition - 1];
				System.out.println("[复合Filter]-->66");
				nextFilter.doFilter(request, response, this);
				System.out.println("[复合Filter]-->77");
			}
		}
	}
}