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

//roleId==-1或roleId<0代表为根节点，不加过滤条件
    var roleId="3";
    var namespace='security';
    var action='user';

    //本页面特殊URL
    var resetURL=contextPath+'/'+namespace+'/'+action+'/reset.action';
    //添加模型信息
    CreateModel = function() {
        return {
            getItems: function() {
                 var items=[{
                                layout: 'form',
                                items:[{
                                            xtype: 'fieldset',
                                            id:'baseInfo',
                                            title: '基本信息',
                                            collapsible: true,
                                            defaults: {
                                                allowBlank: false,
                                                anchor: '95%'
                                            },
                                            items: [
                                                {
                                                layout:'column',
                                                defaults: {width: 250},
                                                items:[{
                                                    columnWidth:.5,
                                                    layout: 'form',
                                                    defaultType: 'textfield',
                                                    defaults: {
                                                        allowBlank: false,
                                                        anchor:"90%"
                                                    },

                                                     items: [{
                                                                cls : 'attr',
                                                                name: 'model.username',
                                                                fieldLabel: '账号',
                                                                blankText : '账号不能为空'
                                                            },
                                                            {
                                                                cls : 'attr',
                                                                name: 'model.realName',
                                                                fieldLabel: '姓名',
                                                                blankText : '姓名不能为空'
                                                            },
                                                            {
                                                                xtype: 'combo',
                                                                store:userStateStore,
                                                                emptyText:'请选择',
                                                                mode:'remote',
                                                                valueField:'value',
                                                                displayField:'text',
                                                                triggerAction:'all',
                                                                forceSelection: true,
                                                                editable:       false,
                                                                cls : 'attr',
                                                                hiddenName: 'model.enabled',
                                                                fieldLabel: '状态',
                                                                allowBlank: false,
                                                                blankText : '状态不能为空'
                                                            }]
                                                },{
                                                    columnWidth:.5,
                                                    layout: 'form',
                                                    defaultType: 'textfield',
                                                    defaults: {
                                                        allowBlank: false,
                                                        anchor:"90%"
                                                    },

                                                    items: [{
                                                                cls : 'attr',
                                                                id:'password',
                                                                name: 'model.password',
                                                                fieldLabel: '密码',
                                                                blankText : '密码不能为空',
                                                                inputType : 'password',
                                                                value:'a123456'
                                                            },
                                                            {
                                                                cls : 'attr',
                                                                name: 'phone',
                                                                id: 'phone',
                                                                fieldLabel: '联系方式',
                                                                allowBlank: true,
                                                                regex: /^\d+$/,
                                                                regexText: '请输入正确的手机号'
                                                            },
                                                            {
                                                            	xtype: 'textfield',
                                                                name: 'roleId',
                                                                id:'roleId',
                                                                hidden: true,
                                                                hideLabel:true,
                                                                value:roleId
                                                            }]
                                                       }]
                                            },
                                            {
                                                xtype:'textfield',
                                                allowBlank: true,
                                                name: 'model.des',
                                                fieldLabel: '备注',
                                                anchor:"95%"
                                        }]
                                    }]
                    }];
                return items;
            },

            show: function() {
                CreateBaseModel.show('添加管理员', 'user', 800, 460, this.getItems());
            }
        };
    } ();
    //修改模型信息
    ModifyModel = function() {
        return {
            getItems: function(model) {
                 var items=[{
                                layout: 'form',
                                items:[{
                                            xtype: 'fieldset',
                                            id:'baseInfo',
                                            title: '基本信息',
                                            collapsible: true,
                                            defaults: {
                                                allowBlank: false,
                                                anchor: '95%'
                                            },
                                            items: [{
                                                    layout:'column',
                                                    defaults: {width: 250},
                                                    items:[{
                                                        columnWidth:.5,
                                                        layout: 'form',
                                                        defaultType: 'textfield',
                                                        defaults: {
                                                            allowBlank: false,
                                                            anchor:"90%"
                                                        },

                                                         items: [{
                                                                    readOnly:true,
                                                                    fieldClass:'detail_field',
                                                                    name: 'model.username',
                                                                    value: model.username,
                                                                    fieldLabel: '账号'
                                                                },{
                                                                    xtype:'textfield',
                                                                    name: 'model.realName',
                                                                    value: model.realName,
                                                                    fieldLabel: '姓名',
                                                                    allowBlank: false,
                                                                    blankText : '姓名不能为空'
                                                                }]
                                                    },{
                                                        columnWidth:.5,
                                                        layout: 'form',
                                                        defaultType: 'textfield',
                                                        defaults: {
                                                            allowBlank: false,
                                                            anchor:"90%"
                                                        }
                                                    }]
                                                },
                                                {
                                                    xtype:'textfield',
                                                    allowBlank: true,
                                                    name: 'model.des',
                                                    value: model.des,
                                                    fieldLabel: '备注',
                                                    anchor:"95%"
                                                }
                                            ]
                                        }]
                    }];
                return items;
            },

            show: function(model) {
                ModifyBaseModel.prepareSubmit=function() {
                    parent.Ext.getCmp('roles').setValue(roleSelector.getValue());
                    if("启用"==parent.Ext.getCmp('state').getValue()){
                        parent.Ext.getCmp('state').setValue("true");
                    }
                    if("停用"==parent.Ext.getCmp('state').getValue()){
                        parent.Ext.getCmp('state').setValue("false");
                    }
                }
                ModifyBaseModel.show('修改管理员', 'user', 800, 410, this.getItems(model),model);
            }
        };
    } ();
    
    //表格
    GridModel = function() {
        return {
            getFields: function(){
                var fields=[
 				{name: 'id'},
				{name: 'username'},
				{name: 'realName'},
				{name: 'phone'},
				{name: 'enabled'},
				{name: 'des'}
			];
               return fields;     
            },
            getColumns: function(){
                var columns=[
 				{header: "编号", width: 10, dataIndex: 'id', sortable: true},
				{header: "账号", width: 20, dataIndex: 'username', sortable: true},
				{header: "姓名", width: 20, dataIndex: 'realName', sortable: true,editor:new Ext.form.TextField()},
				{header: "联系方式", width: 20, dataIndex: 'phone', sortable: true,editor:new Ext.form.TextField()},
				{header: "状态", width: 20, dataIndex: 'enabled', sortable: true,editor:{
                                                                    xtype: 'combo',
                                                                    store:userStateStore,
                                                                    emptyText:'请选择',
                                                                    mode:'remote',
                                                                    valueField:'value',
                                                                    displayField:'text',
                                                                    triggerAction:'all',
                                                                    forceSelection: true,
                                                                    editable:       false
                                                                }},
				{header: "描述", width: 40, dataIndex: 'des', sortable: true,editor:new Ext.form.TextField()}
                            ];
                return columns;           
            },
            getGrid: function(){
                var pageSize=17;
                
                //添加特殊参数
                GridBaseModel.roleId=roleId;
                GridBaseModel.setStoreBaseParams=function(store){
                    store.on('beforeload',function(store){
                       store.baseParams = {queryString:GridBaseModel.queryString,search:GridBaseModel.search,roleId:GridBaseModel.roleId};
                    });
                };
                var commands=["create","delete","updatePart","export","reset"];
                var tips=['增加管理员(C)','删除管理员(R)','修改管理员(U)','导出管理员(E)',"重置密码(Z)"];
                var callbacks=[GridBaseModel.create,GridBaseModel.remove,GridBaseModel.modify,GridBaseModel.exportData,GridModel.reset];
                
                var grid=GridBaseModel.getGrid(contextPath, namespace, action, pageSize, this.getFields(), this.getColumns(), commands,tips,callbacks);   
                return grid;
            },
            reset: function(){
                var idList=GridBaseModel.getIdList();
                if(idList.length<1){
                    parent.Ext.ux.Toast.msg('操作提示：','请选择要进行操作的记录');  
                    return ;
                }
                parent.Ext.MessageBox.confirm("操作提示：","确实要对所选的用户执行密码重置操作吗？",function(button,text){
                    if(button == "yes"){
                        parent.Ext.Msg.prompt('操作提示', '请输入重置密码:', function(btn, text){
                            if (btn == 'ok'){
                                    if(text.toString()==null||text.toString().trim()==""){
                                        parent.Ext.ux.Toast.msg('操作提示：','密码不能为空'); 
                                    }else{
                                         GridModel.resetPassword(idList.join(','),text);
                                    }
                            };
                        });
                    }
                });
            },
            resetPassword: function(ids,password){
                parent.Ext.Ajax.request({
                    url : resetURL+'?time='+new Date().toString(),
                    waitTitle: '请稍等',
                    waitMsg: '正在重置密码……',
                    params : {
                        ids : ids,
                        password : password
                    },
                    method : 'POST',
                    success : function(response,opts){
                        var data=response.responseText;
                        parent.Ext.ux.Toast.msg('操作提示：','{0}',data);  
                    }
                });
            }
        }
    } ();
    UserPanel = function() {
        return {
            show: function() {
                 var frm = new Ext.Viewport({
                    layout : 'border',
                    items: [GridModel.getGrid()]
                });
            }
        };
    } ();
    
    Ext.onReady(function(){
        UserPanel.show();
    });
