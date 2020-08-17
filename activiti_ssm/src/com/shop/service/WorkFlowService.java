package com.shop.service;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;

import com.shop.pojo.Leavebill;

public interface WorkFlowService {

	// 部署流程
void saveNewDeploy(InputStream in, String processName);


// 启动流程
void saveStartLeave(Long leaveId, String name);


List<Task> findTaskListByName(String name);


Leavebill findLeaveBillByTaskId(String taskId);


List<Comment> findCommentListByTaskId(String taskId);


void submitTask(String id, String taskId, String comment, String username);



Map<String, Object> findCoordingByTask(String taskId);

InputStream findImageInputStream(String deploymentId, String imageName);

ProcessDefinition findProcessDefinitionByTaskId(String taskId);

 
 
}
