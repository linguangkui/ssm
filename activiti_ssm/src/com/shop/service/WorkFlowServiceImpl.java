package com.shop.service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.shop.mapper.LeavebillMapper;
import com.shop.pojo.Leavebill;
import com.shop.utils.Constants;

@Service
public class WorkFlowServiceImpl implements WorkFlowService {

	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RuntimeService runtimeService;
	@Autowired
	private TaskService taskService;
	@Autowired
	private FormService formService;
	@Autowired
	private HistoryService historyService;

	@Autowired
	private LeavebillMapper leaveBillMapper;

	@Override
	public void saveNewDeploy(InputStream in, String processName) {

		try {
			// 将普通的file流转换成 ZipInputStream流
			ZipInputStream zipInputStream = new ZipInputStream(in);
			this.repositoryService.createDeployment().addZipInputStream(zipInputStream).name(processName).deploy();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}



	@Override
	public List<Task> findTaskListByName(String name) {
		
		
		List<Task> list = this.taskService
		.createTaskQuery()
		.taskAssignee(name)
		.orderByTaskCreateTime()
		.desc()
		.list();
		return list;
	}



	@Override
	public void saveStartLeave(Long leaveId, String name) {
		
		
	String process_key = Constants.Leave_KEY;
		
		Map<String, Object>map = new HashMap<String,Object>();
		map.put("userId", name);
		
		// 设置Bussiness_key的规则
		String BUSSINESS_KEY = process_key+"."+leaveId;
		
		map.put("objId", BUSSINESS_KEY);
		
		this.runtimeService
		.startProcessInstanceByKey(process_key,BUSSINESS_KEY,map);
		
	}



	@Override
	public Leavebill findLeaveBillByTaskId(String taskId) {
		
		// 先根据任务id 取出task
		Task task = this.taskService
		.createTaskQuery()
		.taskId(taskId).singleResult();
		
		ProcessInstance processInstance = this.runtimeService
		.createProcessInstanceQuery()
		.processInstanceId(task.getProcessInstanceId()).singleResult();
		
		
		String businessKey = processInstance.getBusinessKey();
		
		
		String id = "";
		if(businessKey!=null&&!"".equals(businessKey))
		{
		 	id = businessKey.split("\\.")[1];
		}
		
		
		// 调用LeaveBill的mapper 去查询出  leaveBill信息
		
		Leavebill leavebill = this.leaveBillMapper.selectByPrimaryKey(Long.parseLong(id));
		return leavebill;
	}



	@Override
	public List<Comment> findCommentListByTaskId(String taskId) {
		
		Task task = this.taskService.createTaskQuery().taskId(taskId).singleResult();
		
		List<Comment> comments = this.taskService
		.getProcessInstanceComments(task.getProcessInstanceId());
		
		return comments;
	}
	@Override
	public void submitTask(String id, String taskId, String comment, String username) {
	    
		/**
		 * 1：在完成之前，添加一个批注信息，向act_hi_comment表中添加数据，用于记录对当前申请人的一些审核信息
		 */
		//使用任务ID，查询任务对象，获取流程流程实例ID
		Task task = taskService.createTaskQuery()
				.taskId(taskId)
				.singleResult();
		
		//获取流程实例ID
		String processInstanceId = task.getProcessInstanceId();
		/**
		 * 注意：添加批注的时候，由于Activiti底层代码是使用：
		 * 		String userId = Authentication.getAuthenticatedUserId();
			    CommentEntity comment = new CommentEntity();
			    comment.setUserId(userId);
			  所有需要从Session中获取当前登录人，作为该任务的办理人（审核人），对应act_hi_comment表中的User_ID的字段，不过不添加审核人，该字段为null
			 所以要求，添加配置执行使用Authentication.setAuthenticatedUserId();添加当前任务的审核人
		 * */
		//加当前任务的审核人
		Authentication.setAuthenticatedUserId(username);
		//添加批注
		taskService.addComment(taskId, processInstanceId, comment);
		
		taskService.complete(taskId);
		
		
		
		//获取流程实例
		ProcessInstance pi = runtimeService
				.createProcessInstanceQuery()//
				.processInstanceId(processInstanceId)//使用流程实例ID查询
				.singleResult();
		
		
		if(pi == null) { //流程结束
			Leavebill leave = leaveBillMapper.selectByPrimaryKey(Long.parseLong(id));
			//设置业务的状态：审批结束 (2)
			leave.setState(2);
			leaveBillMapper.updateByPrimaryKey(leave);
		}

		
	}



	@Override
	public Map<String, Object> findCoordingByTask(String taskId) {
		//存放坐标
				Map<String, Object> map = new HashMap<String,Object>();
				//使用任务ID，查询任务对象
				Task task = taskService.createTaskQuery()//
							.taskId(taskId)//使用任务ID查询
							.singleResult();
				//获取流程定义的ID
				String processDefinitionId = task.getProcessDefinitionId();
				//获取流程定义的实体对象（对应.bpmn文件中的数据）
				ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity)repositoryService.getProcessDefinition(processDefinitionId);
				//流程实例ID
				String processInstanceId = task.getProcessInstanceId();
				//使用流程实例ID，查询正在执行的执行对象表，获取当前活动对应的流程实例对象
				ProcessInstance pi = runtimeService.createProcessInstanceQuery()//创建流程实例查询
													.processInstanceId(processInstanceId)//使用流程实例ID查询
													.singleResult();
				//获取当前活动的ID
				String activityId = pi.getActivityId();
				//获取当前活动对象
				ActivityImpl activityImpl = processDefinitionEntity.findActivity(activityId);//活动ID
				//获取坐标
				map.put("x", activityImpl.getX());
				map.put("y", activityImpl.getY());
				map.put("width", activityImpl.getWidth());
				map.put("height", activityImpl.getHeight());
				return map;
	}


	/**使用部署对象ID和资源图片名称，获取图片的输入流*/
	@Override
	public InputStream findImageInputStream(String deploymentId, String imageName) {
		return repositoryService.getResourceAsStream(deploymentId, imageName);
	}



	@Override
	public ProcessDefinition findProcessDefinitionByTaskId(String taskId) {
		//使用任务ID，查询任务对象
				Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
				//获取流程定义ID
				String processDefinitionId = task.getProcessDefinitionId();
				//查询流程定义的对象
				ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()//创建流程定义查询对象，对应表act_re_procdef 
							.processDefinitionId(processDefinitionId)//使用流程定义ID查询
							.singleResult();
				return pd;		// TODO Auto-generated method stub
	}

}
