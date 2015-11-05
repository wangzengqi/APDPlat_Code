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

package org.apdplat.module.security.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apdplat.module.module.model.Command;
import org.apdplat.module.module.model.Module;
import org.apdplat.platform.annotation.Database;
import org.apdplat.platform.annotation.ModelAttr;
import org.apdplat.platform.annotation.ModelCollRef;
import org.apdplat.platform.criteria.Operator;
import org.apdplat.platform.criteria.PageCriteria;
import org.apdplat.platform.criteria.PropertyCriteria;
import org.apdplat.platform.criteria.PropertyEditor;
import org.apdplat.platform.criteria.PropertyType;
import org.apdplat.platform.generator.ActionGenerator;
import org.apdplat.platform.model.SimpleModel;
import org.apdplat.platform.search.annotations.Index;
import org.apdplat.platform.search.annotations.Searchable;
import org.apdplat.platform.search.annotations.SearchableComponent;
import org.apdplat.platform.search.annotations.SearchableProperty;
import org.apdplat.platform.service.ServiceFacade;
import org.apdplat.platform.util.SpringContextUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Entity
@Scope("prototype")
@Component
@Searchable
@Table(name = "UserTable",
uniqueConstraints = {
    @UniqueConstraint(columnNames = {"username"})})
@XmlRootElement
@XmlType(name = "User")
@Database
public class User extends SimpleModel  implements UserDetails{
    //用户名不分词
    @SearchableProperty(index= Index.NOT_ANALYZED)
    @ModelAttr("用户名")
    protected String username;

    @SearchableProperty
    @ModelAttr("姓名")
    protected String realName;

    @ModelAttr("密码")
    protected String password;

    @SearchableProperty
    @ModelAttr("备注")
    protected String des;
    
    @SearchableProperty
    @ModelAttr("联系方式")
    protected String phone;
    
    @ManyToOne
    @SearchableComponent(prefix="role_")
    @ModelAttr("角色")
    @ModelCollRef("roleName")
    protected Role role;
    
    @ModelAttr("账号过期")
    protected boolean accountexpired = false;
    @ModelAttr("账户锁定")
    protected boolean accountlocked = false;
    @ModelAttr("信用过期")
    protected boolean credentialsexpired = false;
    @ModelAttr("账户可用")
    protected boolean enabled = true;
    
    /**
     * 用户登录验证
     * 具体的验证规则就写在这里
     * 
     * @return 验证结果，null为验证通过，非null则为验证未通过的原因
     */
    public String loginValidate(){
        String message = null;
        if(!isEnabled()){
            message = "用户账号被禁用";
        }
        if(!isAccountNonExpired()){
            message = "用户帐号已过期";
        }
        if(!isAccountNonLocked()){
            message = "用户帐号已被锁定";
        }
        if(!isCredentialsNonExpired()){
            message = "用户凭证已过期";
        }
        if(getAuthorities() == null){
            message = "用户帐号未被授予任何权限";
        }
        return message;
    }
    
    /**
     * 用户是否为超级管理员
     * @return
     */
    public boolean isSuperManager(){
        return "超级管理员".equals(role.getRoleName());
    }
    
    public boolean isManager(){
    	return "管理员".equals(role.getRoleName());
    }
    
    public boolean isTeacher()
    {
    	return "教师".equals(role.getRoleName());
    }
    
    public boolean isStudent()
    {
    	return "学员".equals(role.getRoleName());
    }
    
    public String getRoleStrs(){
        return role.getRoleName();
    }
    
    public List<Command> getCommand() {
		if (isSuperManager()) {
			return getAllCommand();
		}
        return getCommandForRole();
    }

    private List<Command> getAllCommand(){
    	ServiceFacade serviceFacade = SpringContextUtils.getBean("serviceFacade");
        List<Command> allCommand = serviceFacade.query(Command.class).getModels();
        return allCommand;
    }
    
    private List<Command> getCommandForRole(){
    	ServiceFacade serviceFacade = SpringContextUtils.getBean("serviceFacade");
    	StringBuffer moduleIds = new StringBuffer();
    	for(Module module : getModule())
    	{
    		moduleIds.append("'");
    		moduleIds.append(module.getId());
    		moduleIds.append("'");
    		moduleIds.append(",");
    	}
    	PropertyCriteria propertyCriteria = new PropertyCriteria();
        propertyCriteria.addPropertyEditor(new PropertyEditor("module_id", Operator.in, "String", moduleIds.toString()));
        return serviceFacade.query(Command.class).getModels();
    }

    public List<Module> getModule() {
    	List<String> roles = new ArrayList<>();
		if (isSuperManager()) {
			return getAllModule();
		} else if (isManager()) {
			roles.add("security");
			roles.add("teacher");
			roles.add("student");
			roles.add("navigation");
		} else if (isTeacher()) {
			roles.add("security");
			roles.add("student");
		}
		return getModuleForRole(roles);
    }
    
    private List<Module> getAllModule(){
    	ServiceFacade serviceFacade = SpringContextUtils.getBean("serviceFacade");
        List<Module> allModule = serviceFacade.query(Module.class).getModels();
        return allModule;
    }
    
    private List<Module> getModuleForRole(List<String> roles){
    	ServiceFacade serviceFacade = SpringContextUtils.getBean("serviceFacade");
    	PropertyCriteria propertyCriteria = new PropertyCriteria();
        propertyCriteria.addPropertyEditor(new PropertyEditor("english", Operator.in, PropertyType.List, roles));
    	return serviceFacade.query(Module.class, new PageCriteria(), propertyCriteria).getModels();
    }
    
    /**
     * 获取授予用户的权利
     * @return
     */
    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> grantedAuthArray=new HashSet<>();
        LOG.debug("user privilege:");
        if(isSuperManager()){
            grantedAuthArray.add(new SimpleGrantedAuthority("ROLE_SUPERMANAGER"));
            LOG.debug("ROLE_SUPERMANAGER");
        }
        else if (isManager())
        {
        	 grantedAuthArray.add(new SimpleGrantedAuthority("ROLE_MANAGER"));
             LOG.debug("ROLE_MANAGER");
        }
        else if (isTeacher())
        {
        	 grantedAuthArray.add(new SimpleGrantedAuthority("ROLE_TEACHER"));
             LOG.debug("ROLE_TEACHER");
        }
        return grantedAuthArray;
    }

    @Override
    public boolean isAccountNonExpired() {
        return !accountexpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !accountlocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !credentialsexpired;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    @Override
    @XmlAttribute
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setAccountexpired(boolean accountexpired) {
        this.accountexpired = accountexpired;
    }

    public void setAccountlocked(boolean accountlocked) {
        this.accountlocked = accountlocked;
    }

    public void setCredentialsexpired(boolean credentialsexpired) {
        this.credentialsexpired = credentialsexpired;
    }

    @Override
    @XmlAttribute
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @XmlAttribute
    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    
    @XmlAttribute
    public String getPhone() {
		return phone;
	}
    
    public void setPhone(String phone) {
		this.phone = phone;
	}

    @Override
    @XmlAttribute
    public String getUsername() {
        return username;
    }
    @Override
    public String getMetaData() {
        return "用户信息";
    }
    
    @XmlTransient
	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}
    
    public static void main(String[] args){
        User obj=new User();
        //生成Action
        ActionGenerator.generate(obj.getClass());
    }
}