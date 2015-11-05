/**
 * 
 * APDPlat - Application Product Development Platform
 * Copyright (c) 2013, 杨尚川, yang-shangchuan@qq.com
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package org.apdplat.module.security.service;

import org.apdplat.module.module.model.Command;
import org.apdplat.module.module.service.ModuleService;
import org.apdplat.module.system.service.PropertyHolder;
import org.apdplat.platform.log.APDPlatLogger;
import org.apdplat.platform.service.ServiceFacade;
import org.apdplat.platform.util.FileUtils;

import java.util.*;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.apdplat.platform.log.APDPlatLoggerFactory;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.access.intercept.DefaultFilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Service;

/**
 * Spring Security授权服务
 * 
 * @author 杨尚川
 */
@Service
public class SpringSecurityService {
	private static final APDPlatLogger LOG = APDPlatLoggerFactory.getAPDPlatLogger(SpringSecurityService.class);
	@Resource(name = "filterSecurityInterceptor")
	private FilterSecurityInterceptor filterSecurityInterceptor;
	@Resource(name = "serviceFacade")
	protected ServiceFacade serviceFacade;

	/**
	 *
	 * @return 系统是否启用安全机制
	 */
	public static boolean isSecurity() {
		String security = PropertyHolder.getProperty("security");
		if (security != null && "true".equals(security.trim())) {
			return true;
		}
		return false;
	}

	/**
	 * 初始化系统安全拦截信息
	 */
	@PostConstruct
	public void initSecurityConfigInfo() {
		String security = PropertyHolder.getProperty("security");
		if (security == null || !"true".equals(security.trim())) {
			LOG.info("当前系统禁用安全机制");
			return;
		}

		LOG.info("开始初始化权限子系统...");
		LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> requestMap = new LinkedHashMap<>();

		SecurityConfig manager = new SecurityConfig("ROLE_MANAGER");
		SecurityConfig superManager = new SecurityConfig("ROLE_SUPERMANAGER");
		SecurityConfig teacherManager = new SecurityConfig("ROLE_TEACHER");
		Collection<ConfigAttribute> value = new ArrayList<>();
		value.add(manager);
		value.add(teacherManager);
		value.add(superManager);

		Collection<String> urls = new LinkedHashSet<>();
		String[] urlFiles = PropertyHolder.getProperty("manager.default.url").split(",");
		for (String urlFile : urlFiles) {
			Collection<String> url = FileUtils.getClassPathTextFileContent(urlFile);
			urls.addAll(url);
		}
		urls.forEach(url -> {
			if (url.contains("=")) {
				String[] attr = url.split("=");
				url = attr[0];
				String[] roles = attr[1].split(",");
				Collection<ConfigAttribute> v = new ArrayList<>();
				for (String role : roles) {
					v.add(new SecurityConfig(role));
				}
				RequestMatcher key = new AntPathRequestMatcher(url, "POST");
				requestMap.put(key, v);
				key = new AntPathRequestMatcher(url, "GET");
				requestMap.put(key, v);
			}
			else {
				RequestMatcher key = new AntPathRequestMatcher(url, "POST");
				requestMap.put(key, value);
				key = new AntPathRequestMatcher(url, "GET");
				requestMap.put(key, value);
			}
		});

		serviceFacade.query(Command.class).getModels().forEach(command -> {
			List<String> paths = ModuleService.getCommandPath(command);
			Map<String, String> map = ModuleService.getCommandPathToRole(command);
			paths.forEach(path -> {
				RequestMatcher key = new AntPathRequestMatcher(path.toString().toLowerCase() + ".action*", "POST");
				List val = new ArrayList<>();
				val.add(new SecurityConfig("ROLE_MANAGER" + map.get(path)));
				val.add(manager);
				val.add(teacherManager);
				val.add(superManager);
				requestMap.put(key, val);
				key = new AntPathRequestMatcher(path.toString().toLowerCase() + ".action*", "GET");
				requestMap.put(key, val);
			});
		});

		RequestMatcher key = new AntPathRequestMatcher("/**", "POST");
		requestMap.put(key, Arrays.asList(superManager));

		key = new AntPathRequestMatcher("/**", "GET");
		requestMap.put(key, value);

		DefaultFilterInvocationSecurityMetadataSource source = new DefaultFilterInvocationSecurityMetadataSource(
				requestMap);

		filterSecurityInterceptor.setSecurityMetadataSource(source);

		LOG.debug("system privilege info:\n");
		requestMap.entrySet().forEach(entry -> {
			LOG.debug(entry.getKey().toString());
			entry.getValue().forEach(att -> {
				LOG.debug("\t" + att.toString());
			});
		});
		LOG.info("完成初始化权限子系统...");
	}
}