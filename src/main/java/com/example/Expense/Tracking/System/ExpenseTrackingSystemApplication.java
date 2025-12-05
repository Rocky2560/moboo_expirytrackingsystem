package com.example.Expense.Tracking.System;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ExpenseTrackingSystemApplication {

	public static void main(String[] args) {


        SpringApplication.run(ExpenseTrackingSystemApplication.class, args);
	}



}
