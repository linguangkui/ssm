package com.shop.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.shop.pojo.Employee;
import com.shop.service.EmployeeService;
import com.shop.utils.Constants;

@Controller
public class EmployeeController {

	@Autowired
	private EmployeeService employeeService;

	@RequestMapping(value = "/login")
	public String login(String username, String password, HttpSession session, Model model) {

		// 根据员工姓名去查询员工信息
		Employee employee = this.employeeService.findEmployeeByName(username);
		if (username != null) {
			// 再从查到的用户中取出password 和 用户传入进来的对比
			if (employee.getPassword().equals(password)) {
				// 此处说明已经完全正确，需要将查询到的信息保存到session域中
				session.setAttribute(Constants.GLOBLE_USER_SESSION, employee);

				// 跳转到index
				return "index";

			} else {
				// 账号或者密码不正确
				model.addAttribute("errorMsg", "账号或者密码不正确");
				return "login";

			}
		} else {
			// 账号或者密码不正确
			model.addAttribute("errorMsg", "账号或者密码不正确");
			return "login";
		}
	}
	
	@RequestMapping(value="/logout")
	public String logout(HttpSession session)
	{
     // 清除session(注销用户操作)
		session.invalidate();
	 // 重定向到login.jsp
		return "redirect:/login.jsp";
		
	}
}
