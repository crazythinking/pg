package net.engining.pg.batch.sdk.test;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 
 * @author luxue
 *
 */
@SpringBootApplication
@EnableBatchProcessing
public class BatchTestApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(BatchTestApplication.class, args);
	}

}
