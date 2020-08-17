package com.shop.service;

import com.shop.pojo.Employee;

public interface EmployeeService {
 
	// 根据员工姓名去查找员工
	public Employee findEmployeeByName(String name);

	public Employee findEmployeeManagerByManagerId(long manageId);
	
}
