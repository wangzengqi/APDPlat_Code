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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apdplat.module.module.model.Command;
import org.apdplat.module.module.service.ModuleService;
import org.apdplat.platform.annotation.Database;
import org.apdplat.platform.annotation.ModelAttr;
import org.apdplat.platform.annotation.ModelAttrRef;
import org.apdplat.platform.annotation.ModelCollRef;
import org.apdplat.platform.annotation.RenderIgnore;
import org.apdplat.platform.generator.ActionGenerator;
import org.apdplat.platform.model.SimpleModel;
import org.apdplat.platform.search.annotations.Searchable;
import org.apdplat.platform.search.annotations.SearchableComponent;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


@Entity
@Scope("prototype")
@Component
@XmlRootElement
@XmlType(name = "Role")
@Searchable
@Database
public class Role extends SimpleModel {
    @Column(length=40)
    @ModelAttr("角色名")
    protected String roleName;
    @ModelAttr("备注")
    protected String des;
    @ModelAttr("超级管理员")	
    protected Boolean superManager = false;
    
    @ManyToOne
    @SearchableComponent
    @ModelAttr("上级角色")
    @ModelAttrRef("roleName")
    protected Role parent;
    
    @RenderIgnore
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "parent")
    @OrderBy("id DESC")
    @ModelAttr("下级角色")
    @ModelCollRef("roleName")
    protected List<Role> child = new ArrayList<>();
    
    /**
     * 角色拥有的命令
     */
    @ManyToMany(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
    @JoinTable(name = "role_command", joinColumns = {
    @JoinColumn(name = "roleID")}, inverseJoinColumns = {
    @JoinColumn(name = "commandID")})
    @OrderBy("id")
    @ModelAttr("角色拥有的命令列表")
    @ModelCollRef("chinese")
    protected List<Command> commands = new ArrayList<>();
 
    @XmlAttribute
    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    @XmlAttribute
    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }
    
    @XmlTransient
    public Role getParent() {
        return parent;
    }

    public void setParent(Role parent) {
        this.parent = parent;
    }

    @XmlElementWrapper(name = "subRoles")
    @XmlElement(name = "role")
    public List<Role> getChild() {
        return this.child;
    }

    public void addChild(Role child) {
        this.child.add(child);
    }

    public void removeChild(Role child) {
        this.child.remove(child);
    }

    public void clearChild() {
        this.child.clear();
    }

    @XmlAttribute
    public Boolean isSuperManager() {
        return superManager;
    }

    public void setSuperManager(Boolean superManager) {
        if(superManager == null){
            superManager = Boolean.FALSE;
        }
        this.superManager = superManager;
    }
    
    public void setCommands(List<Command> commands) {
        this.commands = commands;
    }

    @XmlTransient
    public List<Command> getCommands() {
        return Collections.unmodifiableList(commands);
    }
  
    public void addCommands(Command command) {
        this.commands.add(command);
    }
  
    public void removeCommand(Command command) {
        this.commands.remove(command);
    }
    public void clearCommand() {
        commands.clear();
    }
    
    /**
     * 获取授予角色的权利
     * @return
     */
    public List<String> getAuthorities() {
        List<String> result = new ArrayList<>();
        if (isSuperManager()) {
            result.add("ROLE_SUPERMANAGER");
            return result;
        }
        commands.forEach(command -> {
            Map<String,String> map=ModuleService.getCommandPathToRole(command);
            map.values().forEach(role -> {
                StringBuilder str = new StringBuilder();
                str.append("ROLE_MANAGER").append(role);
                result.add(str.toString());
            });
        });
        return result;
    }

    @Override
    public String getMetaData() {
        return "角色信息";
    }

    public static void main(String[] args){
        Role obj=new Role();
        //生成Action
        ActionGenerator.generate(obj.getClass());
    }
}