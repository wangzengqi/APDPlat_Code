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

import org.apdplat.module.security.model.Role;
import org.apdplat.module.security.model.User;
import org.apdplat.platform.log.APDPlatLogger;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import org.apdplat.platform.log.APDPlatLoggerFactory;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;

/**
 * 在线用户服务
 * @author 杨尚川
 */
@Service
public class OnlineUserService{
    private static final APDPlatLogger LOG = APDPlatLoggerFactory.getAPDPlatLogger(OnlineUserService.class);
    
    @Resource(name="sessionRegistry")
    private SessionRegistry sessionRegistry;
    
    /**
     * 根据会话ID获取在线用户的用户名
     * @param sessionID 会话ID
     * @return 用户名
     */
    public String getUsername(String sessionID) {
        User user = getUser(sessionID);
        String username = "匿名用户";
        if (user != null) {
            username = user.getUsername();
        }
        LOG.debug("获取会话为："+sessionID+" 的用户名："+username);
        return username;
    }
    /**
     * 根据会话ID获取在线用户
     * @param sessionID
     * @return 用户
     */
    public User getUser(String sessionID) {
        SessionInformation info=sessionRegistry.getSessionInformation(sessionID);
        if(info == null){
            LOG.debug("没有获取到会话ID为："+sessionID+" 的在线用户");
            return null;
        }
        User user = (User)info.getPrincipal();
        LOG.debug("获取到会话ID为："+sessionID+" 的在线用户 "+user.getUsername());
        
        return user;
    }
    /**
     * 获取所有在线用户
     * @return 在线用户列表
     */
    public List<User> getUsers(){
        return getUsers(null);
    }
    /**
     * 根据用户的组织机构和角色来筛选在线用户
     * @param org 组织机构
     * @param role 角色
     * @return 在线用户列表
     */
    public List<User> getUsers(Role role){
        LOG.debug("获取在线用户, role: "+role);
        if(role == null ){
            //返回所有在线用户
            return getAllUsers();
        }
        if(role != null){
            //返回属于特定角色及其所有子角色的在线用户
            return getUsersForRole(role);
        }
        return null;
    }
    /**
     * 获取所有在线用户
     * @return 
     */
    private List<User> getAllUsers(){
        List<User> result=new ArrayList<>();
        List<Object> users = sessionRegistry.getAllPrincipals();
        users.forEach(obj -> {
            User user = (User)obj;
            result.add(user);
            LOG.debug("获取到会话ID为："+sessionRegistry.getAllSessions(obj, false).get(0).getSessionId() +" 的在线用户");
        });
        return result;
    }
    
    /**
     * 筛选出属于role的用户
     * @param role
     * @return 
     */
    private List<User> getUsersForRole(Role role){
        List<User> result=new ArrayList<>();
//        if(role == null){
//            return result;
//        }
//        List<Object> users = sessionRegistry.getAllPrincipals();
//        List<Integer> roleIds=RoleService.getChildIds(role);
//        roleIds.add(role.getId());
//        for(Object obj : users){
//            User user=(User)obj;
////            for(Role r : user.getRoles()){
////                if(roleIds.contains(r.getId())){
////                    result.add(user);
////                    LOG.info("获取到会话ID为："+sessionRegistry.getAllSessions(obj, false).get(0).getSessionId() +" 的在线用户");
////                    break;
////                }
////            }
//        }        
        return result;
    }
}