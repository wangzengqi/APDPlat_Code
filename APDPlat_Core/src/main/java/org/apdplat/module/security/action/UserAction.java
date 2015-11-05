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

package org.apdplat.module.security.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apdplat.module.security.model.User;
import org.apdplat.module.security.service.UserReportService;
import org.apdplat.module.security.service.UserService;
import org.apdplat.module.system.service.PropertyHolder;
import org.apdplat.platform.action.ExtJSSimpleAction;
import org.apdplat.platform.criteria.Property;
import org.apdplat.platform.criteria.PropertyCriteria;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Scope("prototype")
@Controller
@RequestMapping("/security/user/")
public class UserAction extends ExtJSSimpleAction<User> {
    @Resource
    private UserReportService userReportService;
    @Resource
    private UserService userService;
    
    @Override
    @ResponseBody
    public String report(){
        byte[] report = userReportService.getReport(servletContext, getRequest());
        return new String(report);
    }
    @Override
    protected void checkModel(User model) throws Exception{
        userService.checkModel(model);
    }
    
    @Override
    public PropertyCriteria buildPropertyCriteria(){
        String roleId = getRequest().getParameter("roleId");
        if (StringUtils.isNotEmpty(roleId))
        {
        	return userService.buildPropertyCriteria(super.buildPropertyCriteria(), Integer.parseInt(roleId));
        }
        return super.buildPropertyCriteria();
    }
    
    @Override
    public String query() {
    	 String roleId = getRequest().getParameter("roleId");
    	 page = userService.getUsersForRole(getPageCriteria(), Integer.parseInt(roleId));
         Map map = new HashMap();
         map.put("totalProperty", page.getTotalRecords());
         List<Map> result = new ArrayList<>();
         renderJsonForQuery(result);
         map.put("root", result);
         this.getPage().getModels().clear();
         this.setPage(null);
         return toJson(map);
    }
    
    @ResponseBody
    @RequestMapping("reset.action")
    public String reset(@RequestParam String ids,
                        @RequestParam String password){
        super.setIds(ids);
        String result = userService.reset(getIds(), password);
        return result;
    }

    @ResponseBody
    @RequestMapping("online.action")
    public String online(@RequestParam(required=false) Integer start,
                         @RequestParam(required=false) Integer limit,
                         @RequestParam(required=false) String org,
                         @RequestParam(required=false) String role){
        super.setStart(start);
        super.setLimit(limit);
        page = userService.getOnlineUsers(getStart(), getLimit(), role);
        Map map = new HashMap();
        map.put("totalProperty", page.getTotalRecords());
        List<Map> result = new ArrayList<>();
        renderJsonForQuery(result);
        map.put("root", result);
        return toJson(map);
    }

    @ResponseBody
    @RequestMapping("store.action")
    public String store(@RequestParam(required=false) String select){
        if("true".equals(select)){
            return super.query();
        }
        List<User> users=getService().query(User.class).getModels();
        List<Map<String,String>> map=new ArrayList<>();
        users.forEach(user -> {
            Map<String,String> temp=new HashMap<>();
            temp.put("value", user.getUsername());
            temp.put("text", user.getUsername());
            map.add(temp);
        });
        return toJson(map);
    }
    @Override
    public void assemblyModelForCreate(User model) {
        String roleId = getRequest().getParameter("roleId");
        userService.assemblyModelForCreate(model, Integer.parseInt(roleId));
    }
    @Override
    public void prepareForDelete(Integer[] ids){
        userService.prepareForDelete(ids);
    }   
    @Override
    protected void old(User model) {
        if(PropertyHolder.getBooleanProperty("demo")){
            if(model.getUsername().equals("admin")){
                throw new RuntimeException("演示版本不能修改admin用户");
            }
        }
    }
    @ResponseBody
    @RequestMapping("modify-password.action")
    public String modifyPassword(@RequestParam String oldPassword,
                                 @RequestParam String newPassword){
        Map map = userService.modifyPassword(oldPassword, newPassword);
        return toJson(map);
    }
    
    // 在更新一个特定的部分的Model之前对Model添加需要修改的属性
    @Override
    protected void assemblyModelForPartUpdate(List<Property> properties) {
        userService.assemblyModelForPartUpdate(properties, model);
    }
    @Override
    protected void assemblyModelForUpdate(User model){
        userService.assemblyModelForUpdate(model);
    }
    @Override
    protected void renderJsonForRetrieve(Map map) {
        render(map,model);
        map.put("roles", model.getRoleStrs());
    }
    
    @Override
    protected void render(Map map,User model){
        map.put("id", model.getId());
        map.put("version", model.getVersion());
        map.put("username", model.getUsername());
        map.put("realName", model.getRealName());
        map.put("phone", model.getPhone());
        map.put("enabled", model.isEnabled()==true?"启用":"停用");
        map.put("des", model.getDes());
    }
}