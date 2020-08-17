package com.shop.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.shop.mapper.EmployeeMapper;
import com.shop.pojo.Employee;
import com.shop.pojo.EmployeeExample;
import com.shop.pojo.EmployeeExample.Criteria;


@Service("employeeService")
public class EmployeeServiceImpl implements EmployeeService {

	@Autowired
	private EmployeeMapper employeeMapper;
	
	@Override
	public Employee findEmployeeByName(String name) {
		
		EmployeeExample example = new EmployeeExample();
		Criteria criteria = example.createCriteria();
		criteria.andNameEqualTo(name);
		List<Employee> list = this.employeeMapper.selectByExample(example);
		
		if(list!=null &&list.size()>0)
		{
		  return list.get(0);
		}
		
		return null;
	}

	@Override
	public Employee findEmployeeManagerByManagerId(long manageId) {
		
		return this.employeeMapper.selectByPrimaryKey(manageId);
	}

}
