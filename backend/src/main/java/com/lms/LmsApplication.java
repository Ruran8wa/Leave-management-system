package com.lms;

import com.lms.auth.AuthModule;
import com.lms.leave.LeaveModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({AuthModule.class, LeaveModule.class})
public class LmsApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(LmsApplication.class, args);
        System.out.println("\n==============================================");
        System.out.println("🚀 LMS Application is running!");
        System.out.println("📍 Server: http://localhost:8080");
        System.out.println("📊 H2 Console: http://localhost:8080/h2-console");
        System.out.println("==============================================\n");
    }
}
