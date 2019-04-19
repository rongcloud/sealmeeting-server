package cn.rongcloud;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAspectJAutoProxy
@Slf4j
@SpringBootApplication
@EnableScheduling
public class SealMeetingApplication {

	public static void main(String[] args) {
		SpringApplication.run(SealMeetingApplication.class, args);
		log.info("SealMeetingApplication started");
	}

}
