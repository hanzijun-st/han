package com.qianlima.offline;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Scanner;

@SpringBootApplication
@EnableScheduling
//@ComponentScan(basePackages = {"com.qianlima.offline.mapper","com.qianlima.offline.dao"})
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
		//aixing(12);
		System.out.println("=======================================================================================================================================================");
		System.out.println("================================================>>>>>>>>端口号为："+8999);
	}



	public static void ai() {
		for(float y = (float) 1.5;y>-1.5;y -=0.1)  {
			for(float x= (float) -1.5;x<1.5;x+= 0.05){
				float a = x*x+y*y-1;
				if((a*a*a-x*x*y*y*y)<=0.0)  {
					System.out.print("^");
				}
				else
					System.out.print(" ");
			}
			System.out.println();
		}
	}


	public static void aixing(int ai) {
		int Q = ai / 2 - 1; // 确定上面部分的行数
		int W = ai - 2; // 确定上面部分底部的星号个数
		// 循环得到上面部分
		for (int i = 1; i <= Q; i++) {
			// 得到第一个空格三角形
			for (int a = Q; a > i - 1; a--) {
				System.out.print(" ");
				System.out.print(" ");
			}
			// 得到第一个突出的三角形
			for (int b = 1; b < i + 1; b++) {
				System.out.print("*");
				System.out.print(" ");
			}
			for (int d = 1; d < i + 1; d++) {
				System.out.print("*");
				System.out.print(" ");
			}
			// 得到中间的空格三角形
			for (int r = Q; r >= i + 1; r--) {
				System.out.print(" ");
				System.out.print(" ");
			}
			for (int t = Q + 1; t >= i + 1; t--) {
				System.out.print(" ");
				System.out.print(" ");
			}

			// 得到后面的突出三角形
			for (int b = 1; b < i + 1; b++) {
				System.out.print("*");
				System.out.print(" ");
			}
			for (int d = 1; d < i + 1; d++) {
				System.out.print("*");
				System.out.print(" ");
			}
			System.out.println();
		}
		// 下面部分
		for (int w = 1; w <= ai; w++) {
			for (int e = 1; e < w; e++) {
				System.out.print(" ");
				System.out.print(" ");
			}
			for (int r = ai; r >= w; r--) {
				System.out.print("*");
				System.out.print(" ");
			}
			for (int t = ai; t > w; t--) {
				System.out.print("*");
				System.out.print(" ");
			}
			System.out.println();

		}

	}

}
